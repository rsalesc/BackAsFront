/*
 * Copyright (c) 2017. Roberto Sales @ rsalesc
 *
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *    1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 *
 *    2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 *
 *    3. This notice may not be removed or altered from any source
 *    distribution.
 */

package rsalesc.melee.movement.surfing;

import robocode.Bullet;
import robocode.BulletHitBulletEvent;
import robocode.HitByBulletEvent;
import robocode.Rules;
import rsalesc.baf2.BackAsFrontRobot2;
import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.benchmark.Benchmark;
import rsalesc.baf2.core.benchmark.BenchmarkNode;
import rsalesc.baf2.core.controllers.Controller;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.painting.G;
import rsalesc.baf2.painting.PaintManager;
import rsalesc.baf2.painting.Painting;
import rsalesc.baf2.predictor.FastPredictor;
import rsalesc.baf2.predictor.PrecisePredictor;
import rsalesc.baf2.predictor.PredictedPoint;
import rsalesc.baf2.tracking.*;
import rsalesc.baf2.waves.*;
import rsalesc.mega.movement.DangerPoint;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.melee.utils.stats.CircularGuessFactorStats;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

/**
 * Created by Roberto Sales on 09/10/17.
 * TODO: optimize wave querying
 * TODO: cache data
 * TODO: add shadowing
 */
public class MeleeSurfing extends Component implements CrossFireListener, EnemyWaveListener {
    // how many ticks to branchAndBound at least
    private static final boolean GOTO_STYLE = false;
    private static final double PROBABILITY_THRESHOLD = 0.1; // TODO: improve this

    private static final double WALL_STICK = 160;

    private static final int POINT_GEN = 24;
    private static final int STICK_LENGTH = 160;
    private static final int BRANCH_DEPTH = 1; // TODO: improve this
    private static final int MAX_TARGETS = 4;

    private static final double NOISE = 1.7;

    private static final String MELEE_HINT = "melee-log-hint";

    private final SurferProvider provider;
    private final WaveManager waves;
    private final TargetGuesser guesser;

    private Hashtable<String, MeleeSurfer> surfers = new Hashtable<>();
    private SurfingChoice lastChoice;

    private EnemyRobot[] latestEnemies;

    private long hits = 0;

    private Hashtable<Long, CircularGuessFactorStats> cache = new Hashtable<>();

    EnemyWaveCondition hasLog = new EnemyWaveCondition() {
        @Override
        public boolean test(EnemyWave wave) {
            return wave.getData(MELEE_HINT) != null;
        }
    };

    public MeleeSurfing(SurferProvider provider, WaveManager waves, TargetGuesser guesser) {
        this.provider = provider;
        this.waves = waves;
        this.guesser = guesser;
    }

    public MeleeSurfer getSurfer(String name) {
        MeleeSurfer surfer = surfers.get(name);
        if(surfer != null)
            return surfer;

        surfer = provider.getSurfer(name);
        surfers.put(name, surfer);

        return surfer;
    }

    @Override
    public void run() {
        MyLog myLog = MyLog.getInstance();
        MyRobot me = myLog.getLatest();
        long time = getMediator().getTime();

        EnemyWave nextWave = waves.earliestWave(me, time, hasLog);
        if(GOTO_STYLE && lastChoice != null && lastChoice.compatible(nextWave)) {
            goAhead(lastChoice.dest, lastChoice.maxVel);
            return;
        }

        latestEnemies = EnemyTracker.getInstance().getLatest(getMediator().getTime() - TargetingLog.SEEN_THRESHOLD);
        SurfingOption option = branchAndBound(PredictedPoint.from(me));

        lastChoice = new SurfingChoice(option.dest, option.maxVel, nextWave);
        goAhead(lastChoice.dest, lastChoice.maxVel);
    }

    public SurfingOption branchAndBound(PredictedPoint initialPoint) {
        return branch(initialPoint, 0, 0, Double.POSITIVE_INFINITY);
    }

    private SurfingOption branch(PredictedPoint initialPoint, int branchDepth, double accDanger, double bestDanger) {
        double closest = Double.POSITIVE_INFINITY;

        for(EnemyRobot enemy : latestEnemies) {
            closest = Math.min(closest, enemy.getDistance());
        }

        double stickLength = Math.min(STICK_LENGTH, closest * 0.75);
        long stickTime = (long) Math.ceil(stickLength / (Rules.MAX_VELOCITY / 2));

        BenchmarkNode nodeP = Benchmark.getInstance().getNode("point-generation");
        nodeP.start();
        List<PredictedPoint>[] pts = generatePoints(initialPoint, stickLength);
        nodeP.stop();

        SurfingOption bestOption = new SurfingOption(initialPoint, initialPoint, Rules.MAX_VELOCITY, Double.POSITIVE_INFINITY);

        SurfingOption[] options = new SurfingOption[pts.length];
        double[] risks = new double[pts.length];

        double maxDanger = 1e-20;
        double maxRisk = 1e-20;

        BenchmarkNode node = Benchmark.getInstance().getNode("branch-and-bound");
        node.start();
        pathTesting:
        for(int i = 0; i < pts.length; i++) {
            if(pts[i] == null)
                continue;

            final List<PredictedPoint> path = pts[i];

            List<EnemyWave> breakableWaves = waves.getWaves();
            PredictedPoint.filterBreakable(path, breakableWaves);

            double danger = 0;
            for(PredictedPoint predicted : path) {
                ArrayList<EnemyWave> toRemove = new ArrayList<>();

                for(EnemyWave comingWave : breakableWaves) {
                    // new break
                    if(comingWave.hasPassed(predicted, predicted.time)) {
                        toRemove.add(comingWave);

                        BenchmarkNode nodeD = Benchmark.getInstance().getNode("danger-eval");
                        nodeD.start();
                        danger += getDanger(comingWave, predicted);
                        nodeD.stop();
                    }
                }

                breakableWaves.removeAll(toRemove);
            }

            risks[i] = getRisk(R.getLast(path));

            PredictedPoint pt = R.getLast(path);
            options[i] = new SurfingOption(pt, pt, Rules.MAX_VELOCITY, danger);

            maxDanger = Math.max(maxDanger, danger);
            maxRisk = Math.max(maxRisk, risks[i]);
        }

        node.stop();

        for(int i = 0; i < pts.length; i++) {
            if(pts[i] == null)
                continue;

            double normDanger = options[i].danger / maxDanger;
            double normRisk = risks[i] / maxRisk;

            options[i].danger = normDanger * 3 + 1 * normRisk; // neuromancer's setup
        }

        // TODO: put sort back before increasing branch depth
//        Arrays.sort(options);

        if(branchDepth + 1 >= BRANCH_DEPTH) {
            double best = Double.POSITIVE_INFINITY;
            for(int i = 0; i < pts.length; i++) {
                if(pts[i] != null && options[i].danger < best) {
                    best = options[i].danger;
                    bestOption = options[i];
                }
            }

            return bestOption;
        }

        for(SurfingOption option : options) {
            if(bestDanger < accDanger + option.danger)
                break;

            SurfingOption branchOption = branch(option.breakPoint,branchDepth + 1,
                    accDanger + option.danger, bestDanger);

            double branchDanger = branchOption == null ? 0 : branchOption.danger;

            if(option.danger + branchDanger + accDanger < bestDanger) {
                bestDanger = option.danger + branchDanger + accDanger;
                bestOption = new SurfingOption(option.dest, option.breakPoint, option.maxVel, option.danger + branchDanger);
            }
        }

        return bestOption;
    }

    public double getRisk(PredictedPoint dest) {
        double res = 0;

        for(EnemyRobot enemy : latestEnemies) {
            double absBearing = Physics.absoluteBearing(enemy.getPoint(), dest);
            double bafHeading = dest.getBafHeading();
            double diff = R.normalRelativeAngle(bafHeading - absBearing);

            double perpendicularity = Math.abs(Math.cos(diff)) + 1;

            double myDist = dest.distance(enemy.getPoint());

            int closer = 1;
            double closest = Double.POSITIVE_INFINITY;

            for(int i = 0; i < latestEnemies.length; i++) {
                if(latestEnemies[i].getName().equals(enemy.getName()))
                    continue;

                if(latestEnemies[i].getPoint().distance(enemy.getPoint()) < myDist)
                    closer++;

                closest = Math.min(latestEnemies[i].getPoint().distance(enemy.getPoint()), closest);
            }

            double contribution = enemy.getEnergy() / closer / (myDist * myDist);

            if(closest * 1.3 > myDist)
                contribution *= perpendicularity;

            res += contribution;
        }

        return res;
    }

    public double getDanger(EnemyWave wave, PredictedPoint point) {
        double distance = wave.getSource().distance(point);
        double angle = Physics.absoluteBearing(wave.getSource(), point);

        double bandwidth = (36 / distance) * 1.1; // imprecise to make it faster
        AngularRange range = new AngularRange(angle, -bandwidth, +bandwidth);

        return getPreciseDanger(wave, range, point);
    }

    public double getPreciseDanger(EnemyWave wave, AngularRange intersection, PredictedPoint point) {
        CircularGuessFactorStats stats;
        try {
            stats = getCombinedStats(wave);
        } catch(IllegalStateException ex) {
            return 0.0;
        }

        double distance = wave.getSource().distance(point);

        double distanceToWave = (distance - wave.getDistanceTraveled(point.time));

        double res = getPreciseDanger(stats, intersection);

        res /= Math.max(distanceToWave / wave.getVelocity(), 1);
        res *= wave.getDamage();
;
        return res;
    }

    public double getPreciseDanger(CircularGuessFactorStats stats, AngularRange intersection) {
        double res = 0;

        double startingAngle = intersection.getStartingAngle();
        double endingAngle = intersection.getEndingAngle();

        int iBucket = stats.getBucket(startingAngle);
        int jBucket = stats.getBucket(endingAngle);

        int nd = jBucket < iBucket ? CircularGuessFactorStats.BUCKET_COUNT - 1 : jBucket;
        int length = (jBucket + CircularGuessFactorStats.BUCKET_COUNT - iBucket) % CircularGuessFactorStats.BUCKET_COUNT + 1;

        for(int i = iBucket; i <= nd; i++) {
            res += stats.getValueFromBucket(i);
        }

        if(jBucket < iBucket) {
            for (int i = 0; i <= jBucket; i++) {
                res += stats.getValueFromBucket(i);
            }
        }

        res /= length;
        res *= intersection.getLength();

        // TODO: add shadowing (possibly bot shadow as well)

        return res;
    }

    public CircularGuessFactorStats getCombinedStats(EnemyWave wave) {
        long cacheIndex = getCacheIndex(wave);
        if(cacheIndex != -1 && cache.containsKey(cacheIndex))
            return cache.get(cacheIndex);

        MeleeSituation[] logs = (MeleeSituation[]) wave.getData(MELEE_HINT);

        if(logs == null)
            throw new IllegalStateException();

        EnemyLog shooterLog = EnemyTracker.getInstance().getLog(wave.getEnemy());

        CircularGuessFactorStats[] stats = new CircularGuessFactorStats[logs.length];
        double[] weights = new double[logs.length];

        double myAngle = wave.getAngle(MyLog.getInstance().interpolate(wave.getTime()).getPoint());
        double classicMea = Physics.maxEscapeAngle(wave.getVelocity());

        for(int i = 0; i < logs.length; i++) {
            TargetingLog f = logs[i].log;
            double diff = R.normalRelativeAngle(f.absBearing - myAngle);

            if(Math.abs(diff) > 2.01 * classicMea)
                continue;

            double ods = f.hitChance;
            f.hitChance = 1.0;

            stats[i] = getSurfer(logs[i].name).getStats(shooterLog, f, f.imprecise(), getCacheIndex(wave));
            weights[i] = logs[i].weight;

            f.hitChance = ods;
        }

        CircularGuessFactorStats res =
                CircularGuessFactorStats.mergeSum(stats, weights);

        if(cacheIndex != -1)
            cache.put(cacheIndex, res);

        return res;
    }

    public List<PredictedPoint>[] generatePoints(PredictedPoint source, double stick) {
        // optimize this
        AxisRectangle shrinked = getMediator().getBattleField().shrink(18, 18);

        List<PredictedPoint>[] pts = new List[POINT_GEN];

        boolean has = false;

        for(int i = 0; i < POINT_GEN; i++) {
            double angle = R.DOUBLE_PI / POINT_GEN * i;
            Point pt = source.project(angle, stick);
            List<PredictedPoint> path = FastPredictor.tracePath(source, pt);
            if(!PrecisePredictor.smartCollides(shrinked, path)) {
                pts[i] = path;
                has = true;
            }
        }

        if(!has)
            BackAsFrontRobot2.warn("Point generation function could not generate any points!");

        return pts;
    }

    public List<List<PredictedPoint>> eightPoints(PredictedPoint source, double stick)  {
        AxisRectangle shrinked = getMediator().getBattleField().shrink(18, 18);

        ArrayList<List<PredictedPoint>> pts = new ArrayList<>();
        for(int i = 0; i < 8; i++) {
            double offset = R.PI - R.DOUBLE_PI / 8 * i;
            double angle = R.normalAbsoluteAngle(source.getBafHeading() + offset);
            Point pt = source.project(angle, stick);
            List<PredictedPoint> path = FastPredictor.tracePath(source, pt);
            if(!PrecisePredictor.smartCollides(shrinked, path))
                pts.add(path);
        }

        return pts;
    }

    public void goTo(Point dest, double maxVel) {
        Controller controller = getMediator().getBodyControllerOrDummy();
        double distance = getMediator().getPoint().distance(dest);
        controller.setMaxVelocity(maxVel);

        if(distance < 0.1)
            controller.setBackAsFront(getMediator().getHeadingRadians(), distance);
        else
            controller.setBackAsFront(Physics.absoluteBearing(getMediator().getPoint(), dest), distance);

        controller.release();
    }

    public void goAhead(Point dest, double maxVel) {
        Controller controller = getMediator().getBodyControllerOrDummy();
        controller.setMaxVelocity(maxVel);

        double distance = getMediator().getPoint().distance(dest);

        if(distance < 0.1)
            controller.setBackAsFront(getMediator().getHeadingRadians(), distance);
        else
            controller.setBackAsFront(Physics.absoluteBearing(getMediator().getPoint(), dest), Double.POSITIVE_INFINITY);

        controller.release();
    }

    public long getCacheIndex(EnemyWave wave) {
        return Objects.hash(wave, wave.getTime()) * (long) 1e9 + hits;
    }

    public void logHit(EnemyWave wave, RobotSnapshot hitRobot, Bullet hint) {
        if(hitRobot == null && hint == null)
            return;

        MeleeSituation[] sits = (MeleeSituation[]) wave.getData(MELEE_HINT);
        if(sits == null)
            return;

        EnemyLog enemyLog = EnemyTracker.getInstance().getLog(wave.getEnemy());

        hits++;

        long hitTime = hint != null ? getMediator().getTime() : hitRobot.getTime();
        double hitDistance = wave.getSource().distance(hint != null ? new Point(hint.getX(), hint.getY()) : hitRobot.getPoint());
        double hitAngle = hint != null ? hint.getHeadingRadians() : wave.getAngle(hitRobot.getPoint());

        guesser.evaluateHit(sits, hitAngle, hitDistance, hitTime, hitRobot);

        // TODO: preciseIntersection is probably not a good thing right?
        double bandwidth = Physics.hitAngle(hitDistance) / 2;
        AngularRange hitRange = new AngularRange(hitAngle, -bandwidth, +bandwidth);

        for(MeleeSituation sit : sits) {
            if(R.isNear(sit.weight, 0)) {
                continue;
            }

            TargetingLog f = sit.log;
            if(f == null)
                continue;

            f.hitDistance = hitDistance;
            f.hitAngle = hitAngle;
            f.preciseIntersection = hitRange;
            f.hitChance = sit.weight;

            getSurfer(sit.name).log(enemyLog, f, f.imprecise(), BreakType.BULLET_HIT, 1.0);
        }
    }

    @Override
    public void onCrossHit(EnemyWave wave, RobotSnapshot hitEnemy) {
        logHit(wave, hitEnemy, null);
    }

    @Override
    public void onEnemyWaveFired(EnemyWave wave) {
        EnemyLog enemyLog = EnemyTracker.getInstance().getLog(wave.getEnemy());
        InterpolatedSnapshot interpolatedFirer = enemyLog.interpolate(wave.getTime());

        MyRobot pastMe = MyLog.getInstance().atMostAt(wave.getTime() - 1);

        if(interpolatedFirer == null) {
            BackAsFrontRobot2.warn("Couldn't interpolate shooter's position!");
            return;
        }

        EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest(getMediator().getTime() - TargetingLog.SEEN_THRESHOLD);
        MeleeSituation[] data = new MeleeSituation[enemies.length + 1];

        InterpolatedSnapshot mySnap = MyLog.getInstance().interpolate(wave.getTime() - 1);
        int cnt = 0;

        if(mySnap != null) {
            TargetingLog f = TargetingLog.getEnemyMeleeLog(mySnap, wave.getSource(), getMediator(), wave.getPower());
            data[cnt++] = new MeleeSituation(mySnap.getName(), f);
        }

        for(EnemyRobot enemy : enemies) {
            if(enemy.getName().equals(enemyLog.getName()))
                continue;

            EnemyLog otherLog = EnemyTracker.getInstance().getLog(enemy);
            InterpolatedSnapshot interpolatedSnapshot = otherLog.interpolate(wave.getTime() - 1);

            if(interpolatedSnapshot == null)
                continue;

            TargetingLog f =
                    TargetingLog.getCrossLog(pastMe, interpolatedSnapshot, interpolatedFirer.getPoint(), getMediator(), wave.getPower());
            data[cnt++] = new MeleeSituation(interpolatedSnapshot.getName(), f);
        }

        if(cnt > 0) {
            MeleeSituation[] actualData = new MeleeSituation[cnt];

            for (int i = 0, j = 0; i < data.length; i++) {
                if (data[i] != null)
                    actualData[j++] = data[i];
            }

            guesser.evaluateShot(actualData, wave.getTime());

            // avoid surfing a wave with no data
            wave.setData(MELEE_HINT, actualData);

            getCombinedStats(wave); // Make sure the stats were computed before
        }
    }

    @Override
    public void onEnemyWaveBreak(EnemyWave wave, MyRobot me) {

    }

    @Override
    public void onEnemyWaveHitMe(EnemyWave wave, HitByBulletEvent e) {
        logHit(wave, MyLog.getInstance().getLatest(), e.getBullet());
    }

    @Override
    public void onEnemyWaveHitBullet(EnemyWave wave, BulletHitBulletEvent e) {
        logHit(wave, null, e.getHitBullet());
    }

    @Override
    public void onEnemyWavePass(EnemyWave wave, MyRobot me) {

    }

    @Override
    public void setupPaintings(PaintManager manager) {
        manager.add(KeyEvent.VK_M, "melee", new Painting() {
            @Override
            public void paint(G g) {
                long time = getMediator().getTime();
                final int WAVE_DIVISIONS = CircularGuessFactorStats.BUCKET_COUNT;

                if(lastChoice != null) {
                    List<PredictedPoint> path = FastPredictor.tracePath(PredictedPoint.from(MyLog.getInstance().getLatest()), lastChoice.dest);

                    for(Point pt : path) {
                        g.drawPoint(pt, 3, Color.WHITE);
                    }
                }

                for (EnemyWave wave : waves.getWaves()) {
                    if(wave.everyoneInside(getMediator()))
                        continue;

                    Object log = wave.getData(MELEE_HINT);
                    if (log == null)
                        continue;

                    double myAngle = wave.getAngle(MyLog.getInstance().interpolate(wave.getTime()).getPoint());
                    double classicMea = Physics.maxEscapeAngle(wave.getVelocity());

                    CircularGuessFactorStats stats = getCombinedStats(wave);

                    double angle = 0;
                    double ratio = R.DOUBLE_PI / WAVE_DIVISIONS;
                    double maxDanger = 1e-11;

                    ArrayList<DangerPoint> dangerPoints = new ArrayList<>();

                    for (int i = 0; i < WAVE_DIVISIONS; i++) {
                        angle += ratio;
                        Point hitPoint = wave.getSource().project(angle, wave.getDistanceTraveled(time));

                        double diff = R.normalRelativeAngle(myAngle - angle);
                        if(Math.abs(diff) > 1.3 * classicMea)
                            continue;

                        double value = stats.getValueFromBucket(i);
                        dangerPoints.add(new DangerPoint(hitPoint, value));
                        maxDanger = Math.max(maxDanger, value);
                    }

                    for (DangerPoint dangerPoint : dangerPoints) {
                        if(dangerPoint.getDanger() / maxDanger < 0.1)
                            continue;

                        Color dangerColor = G.getWaveDangerColor(dangerPoint.getDanger() / maxDanger);

                        Point base = wave.getSource().project(wave.getAngle(dangerPoint), wave.getDistanceTraveled(time) - 10);
                        g.fillCircle(base, 2, dangerColor);
                    }
                }
            }
        }, true);
    }

    private static class SurfingOption implements Comparable<SurfingOption> {
        private final Point dest;
        private final PredictedPoint breakPoint;
        private final double maxVel;
        private double danger;

        private SurfingOption(Point dest, PredictedPoint breakPoint, double maxVel, double danger) {
            this.dest = dest;
            this.breakPoint = breakPoint;
            this.maxVel = maxVel;
            this.danger = danger;
        }

        @Override
        public int compareTo(SurfingOption o) {
            return (int) Math.signum(danger - o.danger);
        }
    }

    private static class SurfingChoice {
        private final Point dest;
        private final double maxVel;
        private ArrayList<EnemyWave> waves = new ArrayList<>();

        public SurfingChoice(Point dest, double maxVel, EnemyWave ...waves) {
            this.dest = dest;
            this.maxVel = maxVel;
            this.waves.addAll(Arrays.asList(waves));
        }

        public void addWave(EnemyWave wave) {
            waves.add(wave);
        }

        public boolean compatible(EnemyWave wave) {
            if(wave == null)
                return false;
            return waves.size() > 0 && waves.get(0).getBattleTime().equals(wave.getBattleTime());
        }
    }
}
