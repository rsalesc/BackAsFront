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

package rsalesc.mega.movement;

import robocode.Rules;
import rsalesc.baf2.core.controllers.Controller;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.painting.G;
import rsalesc.baf2.painting.PaintManager;
import rsalesc.baf2.painting.Painting;
import rsalesc.baf2.predictor.PrecisePredictor;
import rsalesc.baf2.predictor.PredictedPoint;
import rsalesc.baf2.predictor.WallSmoothing;
import rsalesc.baf2.tracking.*;
import rsalesc.baf2.waves.EnemyWave;
import rsalesc.baf2.waves.EnemyWaveCondition;
import rsalesc.baf2.waves.Wave;
import rsalesc.baf2.waves.WaveManager;
import rsalesc.mega.movement.distancing.DefaultSurfingDistancer;
import rsalesc.mega.movement.distancing.SurfingDistancer;
import rsalesc.mega.utils.IMea;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.mega.utils.stats.GuessFactorStats;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roberto Sales on 12/09/17.
 */
public class TrueSurfing extends BaseSurfing {
    SurfingDistancer distancer = new DefaultSurfingDistancer();
    private EnemyWave nextWave;
    private EnemyWave secondWave;

    private ArrayList<Point> breakOptions = new ArrayList<>();

    public TrueSurfing(Surfer surfer, WaveManager manager) {
        super(surfer, manager);
    }

    @Override
    public void setupPaintings(PaintManager manager) {
        super.setupPaintings(manager);

        Painting painting = new Painting() {
            @Override
            public void paint(G g) {
                for(Point point : breakOptions) {
                    g.drawPoint(point, 36, Color.LIGHT_GRAY);
                }
            }
        };

        manager.add(KeyEvent.VK_S, "surfing", painting, true);
    }

    public void run() {
        final MyRobot me = MyLog.getInstance().getLatest();
        final EnemyRobot enemy = getEnemy();

        breakOptions.clear();

        if (enemy == null)
            return;

        EnemyWaveCondition hasLogCondition = new EnemyWaveCondition() {
            @Override
            public boolean test(EnemyWave wave) {
                return wave.getData(LOG_HINT) != null && !wave.hasAnyHit() /*&& wave.getTouchTime(me) > getMediator().getTime()*/;
            }
        };

        nextWave = getManager().earliestWave(me, enemy, hasLogCondition);
        secondWave = null;

        Controller controller = getMediator().getBodyControllerOrDummy();

        if (nextWave == null || !distancer.shouldSurf(nextWave.getSource().distance(me.getPoint()))) {
            nextWave = null;
            EnemyLog targetLog = getEnemyLog();
            if (targetLog != null && targetLog.getLatest() != null)
                fallback(controller, enemy);
        } else {
            double stopDanger = 0;
            double clockwiseDanger = 0;
            double counterDanger = 0;

            EnemyWaveCondition hasLogCondition2 = new EnemyWaveCondition() {
                @Override
                public boolean test(EnemyWave wave) {
                    return wave.getData(LOG_HINT) != null && !wave.hasAnyHit() && wave.getTouchTime(me) > nextWave.getTouchTime(me) + 1;
                }
            };

            AxisRectangle shrinkedField = getMediator().getBattleField().shrinkX(18).shrinkY(18);
            secondWave = getManager().earliestWave(me, enemy, (long) (nextWave.getBreakTime(me.getPoint()) + 1), hasLogCondition);
            SurfingCandidate[] firstCandidates = getSurfingCandidates(PredictedPoint.from(me), nextWave, 0);

            for (int i = 0; i < 3; i++) {
                double currentDanger = firstCandidates[i].danger;
                SurfingCandidate[] secondCandidates = getSurfingCandidates(firstCandidates[i].passPoint, secondWave, 1);
                if (secondCandidates != null) {
                    double bestCompoundDanger = Double.POSITIVE_INFINITY;
                    for (int j = 0; j < 3; j++) {
                        bestCompoundDanger = Math.min(bestCompoundDanger, currentDanger + secondCandidates[j].danger);
                    }
                    currentDanger = bestCompoundDanger;
                }

                if (i == 0) clockwiseDanger = currentDanger;
                else if (i == 1) stopDanger = currentDanger;
                else counterDanger = currentDanger;
            }

            for(int i = 0; i < firstCandidates.length; i++) {
                breakOptions.add(firstCandidates[i].passPoint);
            }

            double distance = nextWave.getSource().distance(me.getPoint());
            double perp = distancer.getPerpendiculator(distance);
            if (stopDanger < counterDanger && stopDanger < clockwiseDanger) {
                int stopDirection = me.getDirection(nextWave.getSource());
                if (stopDirection == 0) stopDirection = 1;

                controller.setMaxVelocity(0);
                double angle = R.normalAbsoluteAngle(WallSmoothing.naive(shrinkedField, WALL_STICK, me.getPoint(),
                        Physics.absoluteBearing(nextWave.getSource(), me.getPoint())
                                + perp * stopDirection, stopDirection));
                controller.setBackAsFront(angle);
            } else if (clockwiseDanger < counterDanger) {
                double angle = R.normalAbsoluteAngle(WallSmoothing.naive(shrinkedField, WALL_STICK, me.getPoint(),
                        Physics.absoluteBearing(nextWave.getSource(), me.getPoint())
                                + perp, +1));
                controller.setMaxVelocity(Rules.MAX_VELOCITY);
                controller.setBackAsFront(angle);
            } else {
                double angle = R.normalAbsoluteAngle(WallSmoothing.naive(shrinkedField, WALL_STICK, me.getPoint(),
                        Physics.absoluteBearing(nextWave.getSource(), me.getPoint())
                                - perp, -1));
                controller.setMaxVelocity(Rules.MAX_VELOCITY);
                controller.setBackAsFront(angle);
            }
        }

        controller.release();
    }

    private SurfingCandidate[] getSurfingCandidates(PredictedPoint initialPoint, EnemyWave nextWave, int wavePosition) {
        if (nextWave == null)
            return null;

        TargetingLog f = (TargetingLog) nextWave.getData(LOG_HINT);
        if (f == null)
            throw new IllegalStateException();

        IMea mea = getMea(f);

        EnemyLog enemyLog = EnemyTracker.getInstance().getLog(nextWave.getEnemy());
        GuessFactorStats stats = getSurfer().getStats(enemyLog, f, mea, getCacheIndex(nextWave), getViewCondition(enemyLog.getName()));

        AxisRectangle field = getMediator().getBattleField();
        double distance = nextWave.getSource().distance(initialPoint);
        double perp = distancer.getPerpendiculator(distance);

        int stopDirection = initialPoint.getDirection(nextWave.getSource());
        if (stopDirection == 0) stopDirection = 1;

        List<PredictedPoint> clockwisePoints = PrecisePredictor
                .predictOnWaveImpact(field, WALL_STICK, initialPoint, nextWave, +1, perp, true, false);

        List<PredictedPoint> counterPoints = PrecisePredictor
                .predictOnWaveImpact(field, WALL_STICK, initialPoint, nextWave, -1, perp, true, false);

        List<PredictedPoint> stopPoints = PrecisePredictor
                .predictOnWaveImpact(field, WALL_STICK, initialPoint, nextWave, stopDirection, perp, true, true);

        PredictedPoint clockwisePass = R.getLast(clockwisePoints);
        PredictedPoint counterPass = R.getLast(counterPoints);
        PredictedPoint stopPass = R.getLast(stopPoints);

        AngularRange clockwiseIntersection =
                Wave.preciseIntersection(nextWave, clockwisePoints);
        AngularRange counterIntersection =
                Wave.preciseIntersection(nextWave, counterPoints);
        AngularRange stopIntersection =
                Wave.preciseIntersection(nextWave, stopPoints);

//        double clockwiseDanger = getDanger(nextWave, enemyLog, clockwiseIntersection, clockwisePass);
//        double counterDanger = getDanger(nextWave, enemyLog, counterIntersection, counterPass);
//        double stopDanger = getDanger(nextWave, enemyLog, stopIntersection, stopPass);
        double clockwiseDanger = getDanger(nextWave, stats, clockwiseIntersection, clockwisePass, PRECISE);
        double counterDanger = getDanger(nextWave, stats, counterIntersection, counterPass, PRECISE);
        double stopDanger = getDanger(nextWave, stats, stopIntersection, stopPass, PRECISE);

        SurfingCandidate[] res = new SurfingCandidate[]{
                new SurfingCandidate(clockwiseDanger, clockwisePass),
                new SurfingCandidate(stopDanger, stopPass),
                new SurfingCandidate(counterDanger, counterPass)
        };

        double distanceToSource = initialPoint.distance(nextWave.getSource());
        double impactTime = Math.max((distanceToSource - nextWave.getDistanceTraveled(initialPoint.time))
                / nextWave.getVelocity(), 1);

        for (int i = 0; i < 3; i++) {
//            if (distanceToSource < 100) {
//                res[i].danger *= Math.max((Rules.MAX_VELOCITY - Math.abs(res[i].passPoint.velocity)) / 4, 1);
//            }

            res[i].danger *= Rules.getBulletDamage(Physics.bulletPower(nextWave.getVelocity()));
            res[i].danger /= impactTime / Math.pow(1.0, wavePosition);
            res[i].danger *=
                    Math.pow(2.5, distanceToSource / res[i].passPoint.distance(nextWave.getSource()) - 1);
        }

        return res;
    }

    @Override
    public void onEnemyWavePass(EnemyWave wave, MyRobot me) {

    }

    private class SurfingCandidate {
        public final PredictedPoint passPoint;
        public double danger;

        public SurfingCandidate(double danger, PredictedPoint passPoint) {
            this.danger = danger;
            this.passPoint = passPoint;
        }
    }
}
