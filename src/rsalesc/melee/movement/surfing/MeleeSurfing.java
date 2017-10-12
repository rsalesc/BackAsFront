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
import robocode.util.Utils;
import rsalesc.baf2.BackAsFrontRobot2;
import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.controllers.Controller;
import rsalesc.baf2.core.utils.Pair;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.PredictedHashMap;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.painting.G;
import rsalesc.baf2.painting.PaintManager;
import rsalesc.baf2.painting.Painting;
import rsalesc.baf2.tracking.*;
import rsalesc.baf2.waves.*;
import rsalesc.mega.movement.DangerPoint;
import rsalesc.mega.predictor.MovementPredictor;
import rsalesc.mega.predictor.PredictedPoint;
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

    private static final double WALL_STICK = 160;

    private static final int WAVE_GEN = 5;
    private static final int CHORD_GEN = 2;

    private static final int POINT_GEN = 32;
    private static final int STICK_LENGTH = 180;
    private static final int BRANCH_DEPTH = 1; // TODO: improve this
    private static final int BRANCH_THRESHOLD = 24;

    private static final int SEEN_THRESHOLD = 10;
    private static final int MAX_TARGETS = 5;

    private static final double NOISE = 1.7;

    private static final String MELEE_HINT = "melee-log-hint";

    private final SurferProvider provider;
    private final WaveManager waves;

    private HashMap<String, MeleeSurfer> surfers = new HashMap<String, MeleeSurfer>();
    private SurfingChoice lastChoice;

    private long hits = 0;

    private HashMap<Long, CircularGuessFactorStats> cache = new PredictedHashMap<>(2000);

    EnemyWaveCondition hasLog = new EnemyWaveCondition() {
        @Override
        public boolean test(EnemyWave wave) {
            return wave.getData(MELEE_HINT) != null;
        }
    };

    public MeleeSurfing(SurferProvider provider, WaveManager waves) {
        this.provider = provider;
        this.waves = waves;
    }

    public MeleeSurfer getSurfer(String name) {
        if(surfers.containsKey(name))
            return surfers.get(name);

        MeleeSurfer surfer = provider.getSurfer(name);
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


        SurfingOption option = branchAndBound(PredictedPoint.from(me));
        lastChoice = new SurfingChoice(option.dest, option.maxVel, nextWave);
        goAhead(lastChoice.dest, lastChoice.maxVel);
    }

    public SurfingOption branchAndBound(PredictedPoint initialPoint) {
        return branch(initialPoint, 0, 0, Double.POSITIVE_INFINITY);
    }

    private SurfingOption branch(PredictedPoint initialPoint, int branchDepth, double accDanger, double bestDanger) {
        double closest = Double.POSITIVE_INFINITY;
        EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest(getMediator().getTime() - SEEN_THRESHOLD);

        for(EnemyRobot enemy : enemies) {
            closest = Math.min(closest, enemy.getDistance());
        }

        double stickLength = Math.min(STICK_LENGTH, closest * 0.75);
        long stickTime = (long) Math.ceil(stickLength / (Rules.MAX_VELOCITY / 2));
        List<Point> pts = branchDepth == 0 ? generatePoints(initialPoint, stickLength) : eightPoints(initialPoint, stickLength);

        ArrayList<SurfingOption> options = new ArrayList<>();
        ArrayList<Double> risks = new ArrayList<>();

        double maxDanger = 1e-20;
        double maxRisk = 1e-20;

        pathTesting:
        for(Point pt : pts) {
            // TODO: tracePath or predictOnImpact?
            List<PredictedPoint> path = MovementPredictor.tracePath(initialPoint, pt, Rules.MAX_VELOCITY, stickTime);

            List<EnemyWave> breakableWaves = waves.getWaves();
            PredictedPoint.filterBreakable(path, breakableWaves);

            double danger = 0;
            for(PredictedPoint predicted : path) {
                ArrayList<EnemyWave> toRemove = new ArrayList<>();

                for(EnemyWave comingWave : breakableWaves) {
                    // new break
                    if(comingWave.hasPassed(predicted, predicted.getTime())) {
                        toRemove.add(comingWave);

                        danger += getDanger(comingWave, predicted);
                    }
                }

                breakableWaves.removeAll(toRemove);
            }

            double risk = getRisk(R.getLast(path));
            risks.add(risk);
            options.add(new SurfingOption(pt, R.getLast(path), Rules.MAX_VELOCITY, danger));

            maxDanger = Math.max(maxDanger, danger);
            maxRisk = Math.max(maxRisk, risk);
        }

        for(int i = 0; i < options.size(); i++) {
            double normDanger = options.get(i).danger / maxDanger;
            double normRisk = risks.get(i) / maxRisk;

            options.get(i).danger = normDanger * 3 + 1 * normRisk; // neuromancer's setup
        }

        Collections.sort(options);

        SurfingOption bestOption = new SurfingOption(initialPoint, initialPoint, Rules.MAX_VELOCITY, Double.POSITIVE_INFINITY);

        if(branchDepth + 1 >= BRANCH_DEPTH)
            return options.isEmpty()
                    ? bestOption
                    : options.get(0);

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
        EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest(getMediator().getTime() - SEEN_THRESHOLD);

        double res = 0;

        for(EnemyRobot enemy : enemies) {
            double absBearing = Physics.absoluteBearing(enemy.getPoint(), dest);
            double bafHeading = dest.getBafHeading();
            double diff = Utils.normalRelativeAngle(bafHeading - absBearing);

            double perpendicularity = Math.abs(Math.cos(diff)) + 1;

            double myDist = dest.distance(enemy.getPoint());

            int closer = 1;
            double closest = Double.POSITIVE_INFINITY;

            for(int i = 0; i < enemies.length; i++) {
                if(enemies[i].getName().equals(enemy.getName()))
                    continue;

                if(enemies[i].getPoint().distance(enemy.getPoint()) < myDist)
                    closer++;

                closest = Math.min(enemies[i].getPoint().distance(enemy.getPoint()), closest);
            }

            double contribution = enemy.getEnergy() / Math.pow(R.constrain(1, closer, 3), 0.8) / (myDist * myDist);

            if(closest * 1.3 > myDist)
                contribution *= perpendicularity;

            res += contribution;
        }

        return res;
    }

    public double getDanger(EnemyWave wave, PredictedPoint point) {
        double distance = wave.getSource().distance(point);
        double angle = Physics.absoluteBearing(wave.getSource(), point);

        double bandwidth = Physics.hitAngle(distance) / 2;
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

        double distanceToWave = (distance - wave.getDistanceTraveled(point.getTime()));

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

        if(jBucket < iBucket) jBucket += CircularGuessFactorStats.BUCKET_COUNT;

        for(int i = iBucket; i <= jBucket; i++) {
            res += stats.getValueFromBucket(i % CircularGuessFactorStats.BUCKET_COUNT);
        }

        res /= jBucket - iBucket + 1;
        res *= intersection.getLength();

        // TODO: add shadowing (possibly bot shadow as well)

        return res;
    }

    // TODO: cache this
    public CircularGuessFactorStats getCombinedStats(EnemyWave wave) {
        long cacheIndex = getCacheIndex(wave);
        if(cacheIndex != -1 && cache.containsKey(cacheIndex))
            return cache.get(cacheIndex);

        Pair<String, TargetingLog>[] logs = (Pair[]) wave.getData(MELEE_HINT);

        if(logs == null)
            throw new IllegalStateException();

        EnemyLog shooterLog = EnemyTracker.getInstance().getLog(wave.getEnemy());

        ArrayList<CircularGuessFactorStats> stats = new ArrayList<>();
        ArrayList<Double> weights = new ArrayList<>();

        double myAngle = wave.getAngle(MyLog.getInstance().interpolate(wave.getTime()).getPoint());
        double classicMea = Physics.maxEscapeAngle(wave.getVelocity());

        for(Pair<String, TargetingLog> pair : logs) {
            TargetingLog f = pair.second;
            double diff = Utils.normalRelativeAngle(f.absBearing - myAngle);

            if(Math.abs(diff) > 2.4 * classicMea)
                continue;

            stats.add(getSurfer(pair.first).getStats(shooterLog, f, f.imprecise(), getCacheIndex(wave)));
            weights.add(1.0 / (f.distance * f.distance));
        }

        double[] primitiveWeights = new double[weights.size()];
        for(int i = 0; i < weights.size(); i++) primitiveWeights[i] = weights.get(i);

        CircularGuessFactorStats res =
                CircularGuessFactorStats.mergeSum(stats.toArray(new CircularGuessFactorStats[0]), primitiveWeights);

        if(cacheIndex != -1)
            cache.put(cacheIndex, res);

        return res;
    }

    public List<Point> generatePoints(PredictedPoint source, double stick) {
        // optimize this
        AxisRectangle shrinked = getMediator().getBattleField().shrink(18, 18);

        ArrayList<Point> pts = new ArrayList<>();

        for(int i = 0; i < POINT_GEN; i++) {
            double angle = R.DOUBLE_PI / POINT_GEN * i;
            Point pt = source.project(angle, stick);
            if(!MovementPredictor.collides(shrinked, source, pt, Rules.MAX_VELOCITY))
                pts.add(pt);
        }

        if(pts.size() == 0)
            BackAsFrontRobot2.warn("Point generation function could not generate any points!");

        return pts;
    }

    public List<Point> eightPoints(PredictedPoint source, double stick)  {
        ArrayList<Point> pts = new ArrayList<>();
        for(int i = 0; i < 8; i++) {
            double offset = R.PI - R.DOUBLE_PI / 8 * i;
            double angle = Utils.normalAbsoluteAngle(source.getBafHeading() + offset);
            pts.add(source.project(angle, stick));
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

    // TODO: somehow interpolate hit position as well
    // TODO: log whenever a hit happens, not only when it hits a specific enemy
    public void logHit(EnemyWave wave, RobotSnapshot hitRobot, Bullet hint) {
        Pair<String, TargetingLog>[] pairs = (Pair[]) wave.getData(MELEE_HINT);
        if(pairs == null)
            return;

        TargetingLog f = null;
        for(Pair<String, TargetingLog> entry : pairs) {
            if(entry.first.equals(hitRobot.getName()))
                f = entry.second;
        }

        if(f == null)
            return;

        hits++;

        EnemyLog enemyLog = EnemyTracker.getInstance().getLog(wave.getEnemy());
        f.hitDistance = wave.getSource().distance(hitRobot.getPoint());
        f.hitAngle = hint != null ? hint.getHeadingRadians() : wave.getAngle(hitRobot.getPoint());

        double bandwidth = Physics.hitAngle(f.hitDistance) / 2 * NOISE;
        f.preciseIntersection = new AngularRange(f.hitAngle, -bandwidth, +bandwidth);

        getSurfer(hitRobot.getName()).log(enemyLog, f, f.imprecise(), BreakType.BULLET_HIT);
    }

    @Override
    public void onCrossHit(EnemyWave wave, EnemyRobot hitEnemy) {
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

        EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest(getMediator().getTime() - SEEN_THRESHOLD);

        ArrayList<InterpolatedSnapshot> snaps = new ArrayList<>();

        InterpolatedSnapshot mySnap = MyLog.getInstance().interpolate(wave.getTime() - 1);
        if(mySnap != null)
            snaps.add(mySnap);

        for(EnemyRobot enemy : enemies) {
            if(enemy.getName().equals(enemyLog.getName()))
                continue;

            EnemyLog otherLog = EnemyTracker.getInstance().getLog(enemy);
            InterpolatedSnapshot interpolatedSnapshot = otherLog.interpolate(wave.getTime() - 1);

            if(interpolatedSnapshot == null)
                continue;

            snaps.add(interpolatedSnapshot);
        }

        ArrayList<Pair<String, TargetingLog>> data = new ArrayList<>();

        int cnt = 0;
        for(InterpolatedSnapshot snap : snaps) {
            if(cnt >= MAX_TARGETS)
                continue;

            TargetingLog f = snap.isMe()
                    ? TargetingLog.getEnemyMeleeLog(snap, wave.getSource(), getMediator(), wave.getPower())
                    : TargetingLog.getCrossLog(pastMe, snap, interpolatedFirer.getPoint(), getMediator(), wave.getPower());

            data.add(new Pair<>(snap.getName(), f));
            cnt++;
        }

        // avoid surfing a wave with no data
        if(data.size() > 0)
            wave.setData(MELEE_HINT, data.toArray(new Pair[0]));
    }

    @Override
    public void onEnemyWaveBreak(EnemyWave wave, MyRobot me) {

    }

    @Override
    public void onEnemyWaveHitMe(EnemyWave wave, HitByBulletEvent e) {
        logHit(wave, MyLog.getInstance().getLatest(), e.getBullet());
    }

    // TODO: handle bullet-hit-bullet
    @Override
    public void onEnemyWaveHitBullet(EnemyWave wave, BulletHitBulletEvent e) {

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

                        double diff = Utils.normalRelativeAngle(myAngle - angle);
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
