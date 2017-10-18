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
import rsalesc.baf2.core.benchmark.Benchmark;
import rsalesc.baf2.core.benchmark.BenchmarkNode;
import rsalesc.baf2.core.utils.BattleTime;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.core.utils.geometry.Range;
import rsalesc.baf2.tracking.*;
import rsalesc.baf2.waves.Wave;
import rsalesc.baf2.predictor.PrecisePredictor;
import rsalesc.baf2.predictor.PredictedPoint;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Roberto Sales on 30/07/17.
 */
public class TargetingLog implements Serializable, IMea {
    private static final long serialVersionUID = 4242424242L;

    private static final int SEEN_THRESHOLD = 10;
    public static final int BACK_IN_TIME = 80;
    private static final int MEA_STICK = 105;

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

    public long revertLast20;
    public double coveredLast20;
    public double gunHeat;

    public long lastRun;
    public long run;

    public boolean aiming = false;

    // melee
    public double closestDistance;
    public double distanceSum;

    // for miss
    public double hitAngle;
    public double hitDistance;

    public int hits;

    public BattleTime battleTime;

    public Range preciseMea;
    public AngularRange preciseIntersection;

//    public double wallDistance;

    /****** COMPUTING ***/

    public static TargetingLog getCrossLog(MyRobot pastMe, InterpolatedSnapshot receiver,
                                           Point source, RobotMediator mediator, double power) {

        BenchmarkNode node = Benchmark.getInstance().getNode("cross-log");
        node.start();

        TargetingLog f = new TargetingLog();
        f.source = source;
        f.bulletPower = power;
        f.field = mediator.getBattleField();
        f.others = pastMe.getOthers();
        f.gunHeat = 0;
        f.aiming = true;

        computeMeleeLog(f, f.imprecise(), EnemyTracker.getInstance().getLog(receiver), receiver);
        f.escapeDirection = f.direction;

        node.stop();

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

        computeDuelLog(f, f, EnemyTracker.getInstance().getLog(enemy), enemy);
        f.escapeDirection = f.direction;

        return f;
    }

    public static TargetingLog getEnemyLog(MyRobot me, Point source, RobotMediator mediator, double power) {
        TargetingLog f = new TargetingLog();
        f.source = source;
        f.bulletPower = power;
        f.field = mediator.getBattleField();
        f.others = me.getOthers();
        f.gunHeat = 0;
        computeDuelLog(f, f, MyLog.getInstance(), me);
        f.escapeDirection = f.direction;

        return f;
    }

    public static TargetingLog getEnemyMeleeLog(RobotSnapshot me, Point source, RobotMediator mediator, double power) {
        TargetingLog f = new TargetingLog();
        f.source = source;
        f.bulletPower = power;
        f.field = mediator.getBattleField();
        f.others = me.getOthers();
        f.gunHeat = 0;
        computeMeleeLog(f, f.imprecise(), MyLog.getInstance(), me);
        f.escapeDirection = f.direction;

        return f;
    }

    private static void computeLog(TargetingLog f, IMea mea, RobotLog log, RobotSnapshot robot, int backInTime) {
        RobotSnapshot pastRobot = log.before(robot); // not using interpolate here, too much behavior change

        f.accel = 1;
        if (pastRobot != null)
            f.accel = (robot.getVelocity() - pastRobot.getVelocity())
                    * Math.signum(robot.getVelocity() + 1e-8);

        f.bafHeading = robot.getHeading();

        if (robot.getAhead() < 0) {
            f.bafHeading = R.normalAbsoluteAngle(f.bafHeading + R.PI);
        }

        f.relativeHeading = Math.abs(R.normalRelativeAngle(f.bafHeading -
                Physics.absoluteBearing(f.source, robot.getPoint())));

        f.positiveEscape = R.getWallEscape(f.field, robot.getPoint(), f.bafHeading);
        f.negativeEscape = R.getWallEscape(f.field, robot.getPoint(),
                R.normalAbsoluteAngle(f.bafHeading + R.PI));

        f.distanceToWall = f.field.distanceToEdges(robot.getPoint());

        if (f.accel < 0)
            f.accelDirection = -f.direction;
        else
            f.accelDirection = f.direction;

        f.timeAccel = f.accel > 0 ? 0 : 1;
        f.timeDecel = f.accel < 0 ? 0 : 1;
        f.timeRevert = pastRobot != null && robot.getDirection(f.source) * pastRobot.getDirection(f.source) < 0 ? 0 : 1;
        f.revertLast20 = f.timeRevert ^ 1;
        f.run = pastRobot != null && robot.getVelocity() != pastRobot.getVelocity() ? 0 : backInTime;
        f.lastRun = backInTime;

        Range coveredLast20 = new Range();

        for (int i = 1; i < backInTime; i++) {
            // biggest interpolate change
            RobotSnapshot curRobot = log.interpolate(f.time - i);
            RobotSnapshot lastRobot = log.interpolate(f.time - i - 1);
            if(curRobot == null || lastRobot == null)
                break;

            double prevAccel = (curRobot.getVelocity() - lastRobot.getVelocity())
                    * Math.signum(curRobot.getVelocity() + 1e-8);
            if (f.timeAccel == i && prevAccel <= 0)
                f.timeAccel++;
            if (f.timeDecel == i && prevAccel >= 0)
                f.timeDecel++;
            if (curRobot.getDirection(f.source) * lastRobot.getDirection(f.source) >= 0 && f.timeRevert == i)
                f.timeRevert++;
            if (f.run == backInTime && curRobot.getVelocity() != lastRobot.getVelocity())
                f.run = i;
            if (f.run != backInTime && curRobot.getVelocity() != lastRobot.getVelocity())
                f.lastRun = i - f.run;

            if (i <= 20) {
                double curBearing = Physics.absoluteBearing(f.source, curRobot.getPoint());
                double curOffset = curBearing - f.absBearing;
                coveredLast20.push(mea.getGf(curOffset));

                if (curRobot.getDirection(f.source) * lastRobot.getDirection(f.source) < 0)
                    f.revertLast20++;
            }
        }

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

        f.coveredLast20 = coveredLast20.maxAbsolute();

        f.distanceSum = 0;
        f.closestDistance = 1e9;
        RobotSnapshot[] others = getOthersAlive(robot, f.time);
        for (RobotSnapshot other : others) {
            f.closestDistance = Math.min(f.closestDistance, other.getPoint().distance(robot.getPoint()));
            f.distanceSum += other.getPoint().distance(robot.getPoint());
        }
    }

    private static void computeMeleeLog(TargetingLog f, IMea mea, RobotLog log, RobotSnapshot robot) {
        computeBasics(f, log, robot);
        computeLog(f, mea, log, robot, 5);
    }

    private static void computeDuelLog(TargetingLog f, IMea mea, RobotLog log, RobotSnapshot robot) {
        computeBasics(f, log, robot);

        BattleTime shotBattleTime = new BattleTime(f.time + 1, f.battleTime.getRound());

        f.preciseMea = PrecisePredictor.getBetterMaximumEscapeAngle(f.field, MEA_STICK,
                PredictedPoint.from(robot), new Wave(f.source, shotBattleTime, Rules.getBulletSpeed(f.bulletPower)),
                f.direction);

        double halfWidth = Physics.hitAngle(f.source.distance(robot.getPoint())) / 2;
        f.preciseMea.push(f.preciseMea.max + halfWidth);
        f.preciseMea.push(f.preciseMea.min - halfWidth);

        computeLog(f, mea, log, robot, BACK_IN_TIME);
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

    private class Imprecise implements IMea {
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

    private class Linear extends Imprecise {
        @Override
        public Range getMea() {
            double radius = TargetingLog.this.getTraditionalMea() * Math.abs(lateralVelocity) / Rules.MAX_VELOCITY;
            return new Range(-radius, +radius);
        }
    }
}
