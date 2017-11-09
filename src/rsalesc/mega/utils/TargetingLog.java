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

package rsalesc.mega.utils;

import robocode.Rules;
import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.utils.BattleTime;
import rsalesc.baf2.core.utils.Pair;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.core.utils.geometry.Range;
import rsalesc.baf2.predictor.PrecisePredictor;
import rsalesc.baf2.predictor.PredictedPoint;
import rsalesc.baf2.tracking.*;
import rsalesc.baf2.waves.Wave;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Roberto Sales on 30/07/17.
 */
public class TargetingLog implements Serializable, IMea {
    private static final long serialVersionUID = 4242424242L;

    private static double[] dx = new double[]{0, 1, 0, -1};
    private static double[] dy = new double[]{1, 0, -1, 0};
    private static Point[] dir = new Point[dx.length + 1];

    static {
        dir[0] = new Point(0, 0);
        for(int i = 0; i < dx.length; i++)
            dir[i + 1] = new Point(dx[i], dy[i]);
    }

    public static final int SEEN_THRESHOLD = 10;
    public static final int BACK_IN_TIME = 80;
    private static final int MEA_STICK = 105; // default 105
    private static final int ENEMY_MEA_STICK = 105; // default 105
    private static final int MELEE_STICK = 140;
    private static final String TIME_SINCE_HINT = "ts";

    public Point source;
    public AxisRectangle field;
    public double absBearing;
    public double velocity;
    public double distance;
    public double lateralVelocity;
    public double advancingVelocity;
    public double bulletPower;
    public double relativeHeading;
    public double accel;
    public long bulletsFired;
    public int direction;
    public int accelDirection;
    public int escapeDirection;
    public double positiveEscape;
    public double negativeEscape;
    public double bafHeading;

    public long time;
    public long timeAccel;
    public long timeDecel;
    public long timeRevert;
    public int others;
    public double displaceLast10;
    public double displaceLast20;
    public double displaceLast40;
    public double displaceLast80;

    public double distanceToWall;
    public double distanceToCorner;
    public double distanceToCenter;
    public double advancingVelocityToWall;

    public double lastGf;
    public long revertLast20;
    public double gunHeat;

    public long lastRun;
    public long run;

    public boolean aiming = false;

    // new param
    public double accelSignal;

    // melee
    public double closestDistance;
    public double closestLateralVelocity;
    public double closestAdvancingVelocity;
    public double closestEnergyRatio;
    public double hitChance;

    // risk
    public double[] distanceAvg;
    public double[] distanceEnergyAvg;

    // for miss
    public double hitAngle;
    public double hitDistance;

    public int hits;

    public BattleTime battleTime;

    public Range preciseMea;
    public AngularRange preciseIntersection;

    /****** COMPUTING ***/

    public static TargetingLog getCrossLog(MyRobot pastMe, InterpolatedSnapshot receiver,
                                           Point source, RobotMediator mediator, double power, String reference) {

        TargetingLog f = new TargetingLog();
        f.source = source;
        f.bulletPower = power;
        f.field = mediator.getBattleField();
        f.others = pastMe.getOthers();
        f.gunHeat = 0;
        f.aiming = true;

        computeMeleeLog(f, f.imprecise(), EnemyTracker.getInstance().getLog(receiver), receiver, reference);
        f.escapeDirection = f.direction;


        return f;
    }

    public static TargetingLog getLog(EnemyRobot enemy, RobotMediator mediator, double power, boolean aiming) {
        TargetingLog f = new TargetingLog();
        f.source = mediator.getNextPosition();
        f.bulletPower = power;
        f.field = mediator.getBattleField();
        f.others = mediator.getOthers();
        f.gunHeat = mediator.getGunHeat();
        f.aiming = aiming;
        f.bulletsFired = mediator.getBulletsFired();

        computeEnemyDuelLog(f, f, EnemyTracker.getInstance().getLog(enemy), enemy, mediator.getName());
        f.escapeDirection = f.direction;

        return f;
    }

    public static TargetingLog getTickLog(EnemyRobot enemy, RobotMediator mediator, double power) {
        TargetingLog f = new TargetingLog();

        EnemyLog enemyLog = EnemyTracker.getInstance().getLog(enemy);

        RobotSnapshot interpolated = enemyLog.interpolate(mediator.getTime() - 1);

        if(interpolated == null)
            interpolated = enemy;

        f.source = mediator.getPoint();
        f.bulletPower = power;
        f.field = mediator.getBattleField();
        f.others = interpolated.getOthers();
        f.gunHeat = mediator.getGunHeat();
        f.aiming = false;
        f.bulletsFired = mediator.getBulletsFired();

        computeDuelLog(f, f, enemyLog, interpolated, mediator.getName()); // TODO: duel or enemyduel?
        f.escapeDirection = f.direction;

        return f;
    }

    public static TargetingLog getEnemyLog(MyRobot me, Point source, RobotMediator mediator, double power, String reference) {
        TargetingLog f = new TargetingLog();
        f.source = source;
        f.bulletPower = power;
        f.field = mediator.getBattleField();
        f.others = me.getOthers();
        f.gunHeat = 0;
        computeDuelLog(f, f, MyLog.getInstance(), me, reference);
        f.escapeDirection = f.direction;

        return f;
    }

    public static TargetingLog getEnemyMeleeLog(RobotSnapshot me, Point source, RobotMediator mediator, double power, String reference) {
        TargetingLog f = new TargetingLog();
        f.source = source;
        f.bulletPower = power;
        f.field = mediator.getBattleField();
        f.others = me.getOthers();
        f.gunHeat = 0;
        computeMeleeLog(f, f.imprecise(), MyLog.getInstance(), me, reference);
        f.escapeDirection = f.direction;

        return f;
    }

    private static Pair<TargetingLog, RobotSnapshot> getTimeSinceCache(RobotSnapshot robot, RobotLog log, String reference) {
        Pair<TargetingLog, RobotSnapshot> pair = (Pair) log.getData(TIME_SINCE_HINT + "-" + reference);
        if(pair == null)
            return null;

        if(pair.second.getBattleTime().getRound() != robot.getBattleTime().getRound())
            return null;

        return pair;
    }

    private static void saveTimeSinceCache(RobotSnapshot robot, RobotLog log, TargetingLog f, String reference) {
        Pair<TargetingLog, RobotSnapshot> pair = new Pair<>(f, robot);

        log.setData(TIME_SINCE_HINT + "-" + reference, pair);
    }

    private static void computeLog(TargetingLog f, IMea mea, RobotLog log, RobotSnapshot robot, String reference) {
        RobotSnapshot pastRobot = log.before(robot); // TODO: maybe interpolate here

        f.accel = 1;
        if (pastRobot != null)
            f.accel = (robot.getVelocity() - pastRobot.getVelocity())
                    * Math.signum(robot.getVelocity() + 1e-8);

        f.accelSignal = Math.signum(f.accel);

        f.bafHeading = robot.getBafHeading();

        f.relativeHeading = Math.abs(R.normalRelativeAngle(f.bafHeading -
                Physics.absoluteBearing(f.source, robot.getPoint())));

        f.positiveEscape = R.getWallEscape(f.field, robot.getPoint(), f.bafHeading);
        f.negativeEscape = R.getWallEscape(f.field, robot.getPoint(),
                R.normalAbsoluteAngle(f.bafHeading + R.PI));

        f.distanceToCenter = f.field.getCenter().distance(robot.getPoint());
        f.distanceToWall = f.field.distanceToEdges(robot.getPoint());
        f.distanceToCorner = Double.POSITIVE_INFINITY;
        for(Point corner : f.field.getCorners())
            f.distanceToCorner = Math.min(f.distanceToCorner, corner.distance(robot.getPoint()));

        double velX = f.velocity * R.sin(robot.getHeading());
        double velY = f.velocity * R.cos(robot.getHeading());

        Point point = robot.getPoint();
        double best = Double.POSITIVE_INFINITY;

        f.advancingVelocityToWall = 0;

        if(f.field.getWidth() - point.x < best) {
            best = f.field.getWidth() - point.x;
            f.advancingVelocityToWall = velX;
        }

        if(point.x < best) {
            best = point.x;
            f.advancingVelocityToWall = -velX;
        }

        if(f.field.getHeight() - point.y < best) {
            best = f.field.getHeight() - point.y;
            f.advancingVelocityToWall = velY;
        }

        if(point.y < best) {
            f.advancingVelocityToWall = -velY;
        }

        if (f.accel < 0)
            f.accelDirection = -f.direction;
        else
            f.accelDirection = f.direction;

        Pair<TargetingLog, RobotSnapshot> tsCache = getTimeSinceCache(robot, log, reference);

        long backInTime = BACK_IN_TIME;

        if(tsCache != null) {
            backInTime = Math.min(backInTime, f.time - tsCache.second.getTime());
        }

        f.timeAccel = 0;
        f.timeDecel = 0;
        f.timeRevert = 0;
        f.run = 0;

        for (int i = 0; i < 80; i++) {
            // biggest interpolate change
            RobotSnapshot curRobot = log.interpolate(f.time - i);
            RobotSnapshot lastRobot = log.interpolate(f.time - i - 1);
            if(curRobot == null || lastRobot == null)
                break;

            double prevAccel = (curRobot.getVelocity() - lastRobot.getVelocity())
                    * Math.signum(curRobot.getVelocity() - 1e-8);
            if (f.timeAccel == i && prevAccel <= 0)
                f.timeAccel++;
            if (f.timeDecel == i && prevAccel >= 0)
                f.timeDecel++;
            if (f.timeRevert == i && curRobot.getDirection(f.source) * lastRobot.getDirection(f.source) >= 0)
                f.timeRevert++;
            if (f.run == i && R.isNear(curRobot.getVelocity(), lastRobot.getVelocity()))
                f.run++;
        }

//        if(tsCache != null) {
//            if(f.timeAccel == backInTime)
//                f.timeAccel += tsCache.first.timeAccel;
//            if(f.timeDecel == backInTime)
//                f.timeDecel += tsCache.first.timeDecel;
//            if(f.timeRevert == backInTime)
//                f.timeRevert += tsCache.first.timeRevert;
//            if(f.run == backInTime)
//                f.run += tsCache.first.run;
//        }

        saveTimeSinceCache(robot, log, f, reference);

        try {
            f.displaceLast10 = log.interpolate(f.time - 10).getPoint()
                    .distance(robot.getPoint());
        } catch(Exception ex) {}

        try {
            f.displaceLast20 = log.interpolate(f.time - 20).getPoint()
                    .distance(robot.getPoint());
        } catch(Exception ex){}

        try {
            f.displaceLast40 = log.interpolate(f.time - 40).getPoint()
                    .distance(robot.getPoint());
        } catch(Exception ex){}

        try {
            f.displaceLast80 = log.interpolate(f.time - 80).getPoint()
                    .distance(robot.getPoint());
        } catch(Exception ex){}
    }

    private static void computeMeleeLog(TargetingLog f, IMea mea, RobotLog log, RobotSnapshot robot, String referece) {
        final int backInTime = 40;

        computeBasics(f, log, robot);
        computeLog(f, mea, log, robot, referece);

        // TODO: does it make sense to interpolate here?
        f.distanceAvg = new double[dir.length];
        f.distanceEnergyAvg = new double[dir.length];

        f.closestDistance = Double.POSITIVE_INFINITY;
        RobotSnapshot[] others = getOthersAlive(robot, f.time);
        RobotSnapshot closest = null;
        for(RobotSnapshot other : others) {
            double dist = other.getPoint().distance(robot.getPoint());
            if (dist < f.closestDistance) {
                f.closestDistance = dist;
                closest = other;
            }
        }

        double stickLength = Math.min(MELEE_STICK, f.closestDistance * 0.8);

        for(int i = 0; i < dir.length; i++) {
            Point dest = robot.getPoint().add(dir[i].rotate(robot.getBafHeading()).resized(stickLength));

            double energyRatioSum = 1e-20;
            for (RobotSnapshot other : others) {
                double energyRatio = other.getEnergy() / (robot.getEnergy() + 1e-12);
                double dist = other.getPoint().distance(dest);

                energyRatioSum += energyRatio;

                f.distanceAvg[i] += dist;
                f.distanceEnergyAvg[i] += energyRatio * dist;
            }

            f.distanceAvg[i] /= others.length;
            f.distanceEnergyAvg[i] /= energyRatioSum;
        }

        if(closest != null) {
            f.closestLateralVelocity = robot.getLateralVelocity(closest.getPoint());
            f.closestAdvancingVelocity = robot.getAdvancingVelocity(closest.getPoint());
            f.closestEnergyRatio = closest.getEnergy() / (robot.getEnergy() + 1e-8);
        }
    }

    private static void computeDuelLog(TargetingLog f, IMea mea, RobotLog log, RobotSnapshot robot, String reference) {
        computeBasics(f, log, robot);

        BattleTime shotBattleTime = new BattleTime(f.time + 1, f.battleTime.getRound());

        f.preciseMea = PrecisePredictor.getPreciseMEA(f.field, MEA_STICK, // TODO: change that MEA stick for me
                PredictedPoint.from(robot), new Wave(f.source, shotBattleTime, Rules.getBulletSpeed(f.bulletPower)),
                f.direction);

        double halfWidth = Physics.hitAngle(f.source.distance(robot.getPoint())) / 2; // TODO: change that for me
        f.preciseMea.push(f.preciseMea.max + halfWidth);
        f.preciseMea.push(f.preciseMea.min - halfWidth);

        computeLog(f, mea, log, robot, reference);
    }

    private static void computeEnemyDuelLog(TargetingLog f, IMea mea, RobotLog log, RobotSnapshot robot, String reference) {
        computeBasics(f, log, robot);

        BattleTime shotBattleTime = new BattleTime(f.time + 1, f.battleTime.getRound());

        f.preciseMea = PrecisePredictor.getBetterPreciseMEA(f.field, ENEMY_MEA_STICK, PredictedPoint.from(robot),
                new Wave(f.source, shotBattleTime, Rules.getBulletSpeed(f.bulletPower)), f.direction);

        double halfWidth = Physics.hitAngle(f.source.distance(robot.getPoint())) / 2; // TODO: change that for me
        f.preciseMea.push(f.preciseMea.max + halfWidth);
        f.preciseMea.push(f.preciseMea.min - halfWidth);

        computeLog(f, mea, log, robot, reference);
    }

    private static void computeBasics(TargetingLog f, RobotLog log, RobotSnapshot robot) {
        f.time = robot.getTime();
        f.battleTime = robot.getBattleTime();
        f.absBearing = Physics.absoluteBearing(f.source, robot.getPoint());
        f.velocity = robot.getVelocity();
        f.direction = robot.getDirection(f.source);
        if (f.direction == 0)
            f.direction = 1;

        f.distance = robot.getPoint().distance(f.source);
        f.lateralVelocity = robot.getLateralVelocity(f.source);
        f.advancingVelocity = robot.getAdvancingVelocity(f.source);
    }

    private static RobotSnapshot[] getOthersAlive(RobotSnapshot robot, long time) {
        EnemyRobot[] others = EnemyTracker.getInstance().getLatest(time - SEEN_THRESHOLD);

        ArrayList<RobotSnapshot> res = new ArrayList<>();

        for (int i = 0; i < others.length; i++) {
            if (others[i].getName().equals(robot.getName())) {
                InterpolatedSnapshot me = MyLog.getInstance().interpolate(time);
                if (me != null) res.add(me);
            } else {
                InterpolatedSnapshot enemy = EnemyTracker.getInstance().getLog(others[i]).interpolate(time);
                if (enemy != null) res.add(enemy);
            }
        }

        return res.toArray(new RobotSnapshot[0]);
    }

    // helpers
    public Range getPreciseMea() {
        return preciseMea;
    }

    public double getTraditionalMea() {
        return Physics.maxEscapeAngle(Rules.getBulletSpeed(bulletPower));
    }

    public double bft() {
        return distance / (Rules.getBulletSpeed(bulletPower) + 1e-8);
    }

    public double closestBft() {
        return closestDistance / (Rules.getBulletSpeed(bulletPower) + 1e-8);
    }

    public double getUnconstrainedGfFromAngle(double angle) {
        return getUnconstrainedGf(R.normalRelativeAngle(angle - absBearing));
    }

    public double getGfFromAngle(double angle) {
        return R.constrain(-1, getUnconstrainedGfFromAngle(angle), +1);
    }

    @Override
    public Range getMea() {
        return getPreciseMea();
    }

    public double getGf(double offset) {
        return R.constrain(-1, getUnconstrainedGf(offset), +1);
    }

    public double getUnconstrainedGf(double offset) {
        return R.zeroNan(this.escapeDirection * offset /
                (this.escapeDirection * offset > 0 ? preciseMea.max : -preciseMea.min));
    }

    public double getAngle(double gf) {
        return R.normalAbsoluteAngle(getZeroGf() + getOffset(gf));
    }

    public double getOffset(double gf) {
        return R.zeroNan(escapeDirection * gf * (gf > 0 ? preciseMea.max : -preciseMea.min));
    }

    public double heat() {
        double heatGenerated = 1.0 + bulletPower / 5;
        return Math.min(gunHeat / 0.1, Math.max(heatGenerated - gunHeat, 0) / 0.1) / 16;
    }

    public double virtuality() {
        return aiming ? 0 : heat();
    }

    public double getZeroGf() {
        return absBearing;
    }

    public Imprecise imprecise() {
        return new Imprecise();
    }

    public Linear linear() { return new Linear(); }

    public class Imprecise implements IMea {
        public double getZeroGf() {
            return absBearing;
        }

        public double getOffset(double gf) {
            return R.zeroNan(escapeDirection * gf * getMea().getRadius());
        }

        public double getAngle(double gf) {
            return R.normalAbsoluteAngle(getZeroGf() + getOffset(gf));
        }

        public double getUnconstrainedGf(double offset) {
            return R.zeroNan(escapeDirection * offset / getMea().getRadius());
        }

        public double getUnconstrainedGfFromAngle(double angle) {
            return this.getUnconstrainedGf(R.normalRelativeAngle(angle - absBearing));
        }

        public double getGfFromAngle(double angle) {
            return R.constrain(-1, this.getUnconstrainedGfFromAngle(angle), +1);
        }

        @Override
        public double getGf(double offset) {
            return R.constrain(-1, this.getUnconstrainedGf(offset), +1);
        }

        public Range getMea() {
            double radius = TargetingLog.this.getTraditionalMea();
            return new Range(-radius, +radius);
        }
    }

    public class Linear extends Imprecise {
        @Override
        public Range getMea() {
            double radius = TargetingLog.this.getTraditionalMea() * Math.abs(lateralVelocity) / Rules.MAX_VELOCITY;
            return new Range(-radius, +radius);
        }
    }
}
