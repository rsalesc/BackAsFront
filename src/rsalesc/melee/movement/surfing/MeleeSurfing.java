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

import robocode.Rules;
import robocode.util.Utils;
import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.controllers.Controller;
import rsalesc.baf2.core.utils.Pair;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.tracking.*;
import rsalesc.baf2.waves.EnemyWave;
import rsalesc.baf2.waves.EnemyWaveCondition;
import rsalesc.baf2.waves.WaveManager;
import rsalesc.mega.predictor.MovementPredictor;
import rsalesc.mega.predictor.PredictedPoint;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.melee.utils.stats.CircularGuessFactorStats;

import java.util.*;

/**
 * Created by Roberto Sales on 09/10/17.
 * TODO: optimize wave querying
 * TODO: cache data
 * TODO: add shadowing
 */
public class MeleeSurfing extends Component {
    // how many ticks to branch at least
    private static final int POINT_GEN = 48;
    private static final int STICK_LENGTH = 200;
    private static final int BRANCH_DEPTH = 3;
    private static final int BRANCH_THRESHOLD = 24;

    private static final int SEEN_THRESHOLD = 10;
    private static final int MAX_TARGETS = 3;

    private static final String MELEE_HINT = "melee-log-hint";

    private final SurferProvider provider;
    private final WaveManager waves;

    private HashMap<String, MeleeSurfer> surfers = new HashMap<String, MeleeSurfer>();
    private SurfingChoice lastChoice;

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
        if(lastChoice != null && lastChoice.compatible(nextWave)) {
            goTo(lastChoice.dest, lastChoice.maxVel);
            return;
        }

        SurfingOption option = branch(nextWave, PredictedPoint.from(me), 0);
        if(option != null) {
            lastChoice = new SurfingChoice(option.dest, option.maxVel, nextWave);
            goTo(lastChoice.dest, lastChoice.maxVel);
        } else {
            lastChoice = null;
            // fallback movement
        }
    }

    public SurfingOption branch(EnemyWave nextWave, PredictedPoint initialPoint, int waveIndex) {
        return branch(nextWave, initialPoint, waveIndex, 0, Double.POSITIVE_INFINITY);
    }

    private SurfingOption branch(EnemyWave nextWave, PredictedPoint initialPoint, int waveIndex, double accDanger, double bestDanger) {
        if(nextWave == null)
            return null;

        double stickLength = Math.max(STICK_LENGTH, Rules.MAX_VELOCITY * (nextWave.getBreakTime(initialPoint) - initialPoint.getTime()));
        List<Point> pts = waveIndex == 0 ? generatePoints(initialPoint, stickLength) : eightPoints(initialPoint, stickLength);

        ArrayList<SurfingOption> options = new ArrayList<>();

        for(Point pt : pts) {
            List<PredictedPoint> path = MovementPredictor.predictOnWaveImpact(initialPoint, nextWave, pt, false);

            List<EnemyWave> breakableWaves = waves.getWaves();
            PredictedPoint.filterBreakable(path, breakableWaves);

            double dangerSum = 0;

            for(PredictedPoint predicted : path) {
                ArrayList<EnemyWave> toRemove = new ArrayList<>();

                for(EnemyWave comingWave : breakableWaves) {
                    // new break
                    if(comingWave.hasPassed(predicted, predicted.getTime())) {
                        toRemove.add(comingWave);

                        dangerSum += getDanger(comingWave, predicted);
                    }
                }

                breakableWaves.removeAll(toRemove);
            }

            options.add(new SurfingOption(pt, R.getLast(path), Rules.MAX_VELOCITY, dangerSum));
        }

        Collections.sort(options);

        if(waveIndex + 1 == BRANCH_DEPTH)
            return options.isEmpty() ? null : options.get(0);

        SurfingOption bestOption = null;

        for(SurfingOption option : options) {
            if(bestDanger < accDanger + option.danger)
                break;

            EnemyWave otherWave = waves.earliestWave(initialPoint, initialPoint.getTime() + BRANCH_THRESHOLD, hasLog);
            SurfingOption branchOption = branch(otherWave, option.breakPoint,waveIndex + 1,
                    accDanger + option.danger, bestDanger);

            double branchDanger = branchOption == null ? 0 : branchOption.danger;

            if(option.danger + branchDanger + accDanger < bestDanger) {
                bestDanger = option.danger + branchDanger + accDanger;
                bestOption = new SurfingOption(option.dest, option.breakPoint, option.maxVel, option.danger + branchDanger);
            }
        }

        return bestOption;
    }

    public double getDanger(EnemyWave wave, Point point) {
        double distance = wave.getSource().distance(point);
        double angle = Physics.absoluteBearing(wave.getSource(), point);

        double bandwidth = Physics.hitAngle(distance) / 2;
        AngularRange range = new AngularRange(angle, -bandwidth, +bandwidth);

        return getPreciseDanger(wave, range);
    }

    public double getPreciseDanger(EnemyWave wave, AngularRange intersection) {
        Pair<String, TargetingLog>[] logs = (Pair[]) wave.getData(MELEE_HINT);
        double res = 0;

        for(Pair<String, TargetingLog> pair : logs) {
            EnemyLog enemyLog = EnemyTracker.getInstance().getLog(pair.first);
            if(enemyLog == null)
                continue;
            res += getPreciseDanger(enemyLog, pair.second, intersection);
        }

        res *= wave.getDamage();

        return res;
    }

    public double getPreciseDanger(EnemyLog enemyLog, TargetingLog f, AngularRange intersection) {
        CircularGuessFactorStats stats = getSurfer(enemyLog.getName()).getStats(enemyLog, f, f.imprecise(), getCacheIndex());
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

        return res;
    }

    public List<Point> generatePoints(PredictedPoint source, double stick) {
        // optimize this

        ArrayList<Point> pts = new ArrayList<>();
        for(int i = 0; i < POINT_GEN; i++) {
            pts.add(source.project((Math.random() - 0.5) * R.DOUBLE_PI, Math.random() * stick));
        }

        return pts;
    }

    public List<Point> eightPoints(PredictedPoint source, double stick)  {
        ArrayList<Point> pts = new ArrayList<>();
        for(int i = 0; i < 8; i++) {
            double offset = R.PI - R.DOUBLE_PI / i;
            double angle = Utils.normalAbsoluteAngle(source.getBafHeading() + offset);
            double dist = stick * (1 - Math.random() / 2);
            pts.add(source.project(angle, dist));
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

    public int getCacheIndex() {
        return 0;
    }

    private static class SurfingOption implements Comparable<SurfingOption> {
        private final Point dest;
        private final PredictedPoint breakPoint;
        private final double maxVel;
        private final double danger;

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
            return waves.size() > 0 && waves.get(0).getBattleTime().equals(wave.getBattleTime());
        }
    }
}
