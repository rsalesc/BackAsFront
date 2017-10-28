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

package rsalesc.baf2.predictor;

import robocode.Rules;
import rsalesc.baf2.BackAsFrontRobot2;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.core.utils.geometry.Range;
import rsalesc.baf2.waves.Wave;

import java.util.ArrayList;
import java.util.List;

/**
 * This class has methods to support precise movement prediction.
 * Note that all methods assume that you are using a BackAsFrontRobot-like robot.
 */
public abstract class PrecisePredictor {
    public static List<PredictedPoint> lastEscape = null;
    private static boolean SHARP_TURNING = false;

    public static List<PredictedPoint> predictOnWaveImpact(AxisRectangle field, double stick, PredictedPoint initialPoint, Wave wave,
                                                           int direction, double perpendiculator, boolean hasToPass, boolean brake) {

        if (direction == 0 && !brake)
            throw new IllegalStateException();

        if (direction == 0)
            direction = initialPoint.getDirection(wave.getSource());

        if (direction == 0)
            direction = +1;

        AxisRectangle shrinkedField = field.shrink(18, 18);
        List<PredictedPoint> res = new ArrayList<PredictedPoint>();
        res.add(initialPoint);

        PredictedPoint cur = initialPoint;
        while (hasToPass && !wave.hasPassedRobot(cur, cur.time) || !wave.hasPassed(cur, cur.time)) {
            double pointingAngle = Physics.absoluteBearing(wave.getSource(), cur) + perpendiculator * direction;
            double angle = R.normalAbsoluteAngle(WallSmoothing.naive(shrinkedField, stick, cur,
                    pointingAngle, direction));
            cur = tick(cur, angle, brake ? 0 : Rules.MAX_VELOCITY, Double.POSITIVE_INFINITY);
            res.add(cur);
        }

        return res;
    }

    public static List<PredictedPoint> predictOnWaveImpact(PredictedPoint initialPoint, Wave wave,
                                                           Point dest, boolean hasToPass) {
        List<PredictedPoint> res = new ArrayList<PredictedPoint>();

        PredictedPoint cur = initialPoint;
        res.add(initialPoint);

        while (hasToPass && !wave.hasPassedRobot(cur, cur.time) || !wave.hasPassed(cur, cur.time)) {
            double distance = cur.distance(dest);
            double angle = R.isNear(distance, 0) ? cur.heading : Physics.absoluteBearing(cur, dest);
            cur = tick(cur, angle, Rules.MAX_VELOCITY, distance);
            res.add(cur);
        }

        return res;
    }

    public static List<PredictedPoint> generateOnWaveImpact(AxisRectangle field, double stick, PredictedPoint initialPoint, Wave wave,
                                                            int direction, double perpendiculator, boolean hasToPass) {
        List<PredictedPoint> points =
                predictOnWaveImpact(field, stick, initialPoint, wave, direction, perpendiculator, hasToPass, false);

        PredictedPoint back = points.get(points.size() - 1);

        for (int i = 0; i < 3; i++) {
            double angle = R.normalAbsoluteAngle(WallSmoothing.naive(field, stick, back,
                    Physics.absoluteBearing(wave.getSource(), back)
                            + perpendiculator * direction, direction));

            PredictedPoint next = back.fakeTick(angle, back.velocity, angle, Rules.MAX_VELOCITY);
            points.add(next);
            back = next;
        }

        return points;
    }

    public static Range getBetterMaximumEscapeAngle(AxisRectangle field, double stick, PredictedPoint initialPoint, Wave wave,
                                                    int direction) {
        if (direction == 0)
            direction = 1;

        List<PredictedPoint> posList = predictOnWaveImpact(field, stick, initialPoint, wave, direction, R.HALF_PI, true, false);
        List<PredictedPoint> negList = predictOnWaveImpact(field, stick, initialPoint, wave, -direction, R.HALF_PI, true, false);

        double absBearing = Physics.absoluteBearing(wave.getSource(), initialPoint);
        Range res = new Range(-1e-8, +1e-8);

        lastEscape = new ArrayList<>();
        for (PredictedPoint pos : posList) {
            res.push(R.normalRelativeAngle(Physics.absoluteBearing(wave.getSource(), pos) - absBearing) * direction);
            lastEscape.add(pos);
        }

        for (PredictedPoint neg : negList) {
            lastEscape.add(neg);
            res.push(R.normalRelativeAngle(Physics.absoluteBearing(wave.getSource(), neg) - absBearing) * direction);
        }

        return res;
    }

    public static PredictedPoint predictStop(PredictedPoint initialPoint, double angle) {
        PredictedPoint cur = initialPoint;
        int iterations = 0;
        while(cur.getSpeed() > 0 && iterations++ < 10) {
            cur = tick(cur, angle, 0, Double.POSITIVE_INFINITY);
        }

        return cur;
    }

    public static List<PredictedPoint> tracePath(PredictedPoint initialPoint, Point dest, double maxVel, long deltaTime) {
        PredictedPoint cur = initialPoint;
        ArrayList<PredictedPoint> path = new ArrayList<>();
        path.add(cur);

        long end = initialPoint.time + deltaTime;

        double distance;
        while((distance = cur.distance(dest)) > 0.1 || cur.getSpeed() > 0.1) {
            double angle = distance < 1 ? cur.getBafHeading() : Physics.absoluteBearing(cur, dest);
            cur = tick(cur, angle, maxVel, distance);
            path.add(cur);
        }

        return path;
    }

    public static boolean collides(AxisRectangle shrinkedField, PredictedPoint initialPoint, Point dest, double maxVel) {
        return smartCollides(shrinkedField, tracePath(initialPoint, dest, maxVel, 100));
    }

    public static boolean collides(AxisRectangle shrinkedField, List<PredictedPoint> path) {
        for(PredictedPoint point : path)
            if(!shrinkedField.contains(point))
                return true;

        return false;
    }

    public static boolean smartCollides(AxisRectangle shrinkedField, List<PredictedPoint> path) {
        PredictedPoint lastPoint = null;
        for(PredictedPoint point : path) {
            if(lastPoint != null && R.isNear(lastPoint.velocity, point.velocity) &&
                    R.isNearAngle(lastPoint.heading, point.heading))
                return !shrinkedField.contains(R.getLast(path));

            if (!shrinkedField.contains(point))
                return true;

            lastPoint = point;
        }

        return false;
    }

    /**
     * This method, different from tick, predicts assuming that the robot will attempt
     * to move infinitely (hit the maximum speed and never break)
     * <p>
     * This is usually quicker than tick for wavesurfing because it does not rely
     * on getNewVelocity() to get the velocity the bot must hit to still be able to break
     * and stop at the destination. There is no real destination here. You can tick, for
     * example, while the enemy's wave does not hit you.
     *
     * @param last        point predicted in the last tick (or initial point)
     * @param angle       the absolute angle the bot is trying to move, normalized
     * @param maxVelocity the maximum velocity the bot should move
     *                    usually: Physics.MAX_VELOCITY
     * @return the current predicted point
     */
    public static PredictedPoint fastTick(PredictedPoint last, double angle, double maxVelocity) {
        double offset = R.normalRelativeAngle(angle - last.heading);
        double turn = BackAsFrontRobot2.getQuickestTurn(offset);
        int ahead = offset == turn ? +1 : -1;

        double newHeading = getNewHeading(last.heading, turn, last.velocity);

        double newVelocity = getTickVelocity(last.velocity, maxVelocity, ahead, Double.POSITIVE_INFINITY);

        return last.tick(newHeading, newVelocity);
    }

    public  static PredictedPoint tick(PredictedPoint last, double angle, double maxVelocity, double remaining) {
        double offset = R.normalRelativeAngle(angle - last.heading);
        double turn = BackAsFrontRobot2.getQuickestTurn(offset);
        int ahead = offset == turn ? +1 : -1;

        double newHeading = getNewHeading(last.heading, turn, last.velocity);

        double newVelocity = getTickVelocity(last.velocity, maxVelocity, ahead, remaining);

        return last.tick(newHeading, newVelocity);
    }

    public static double getTickVelocity(double velocity, double maxVelocity, int ahead, double remaining) {
        if (ahead < 0) {
            return -getTickVelocity(-velocity, maxVelocity, -ahead, remaining);
        }

        return getNewVelocity(velocity, maxVelocity, remaining);
    }

    public static double getNewHeading(double heading, double turn, double velocity) {
        double turnRate = Rules.getTurnRateRadians(velocity);
        return R.normalAbsoluteAngle(heading + R.constrain(-turnRate, turn, +turnRate));
    }

    // getNewVelocity(velocity * ahead, maxVelocity, distance) * ahead

    public static double getNewVelocity(double velocity, double maxVelocity, double distance) {
        if (distance < 0) {
            // If the distanceToEdges is negative, then change it to be positive
            // and change the sign of the input velocity and the result
            return -getNewVelocity(-velocity, maxVelocity, -distance);
        }

        final double goalVel;

        if (distance == Double.POSITIVE_INFINITY) {
            goalVel = maxVelocity;
        } else {
            goalVel = Math.min(getMaxVelocity(distance), maxVelocity);
        }

        if (velocity >= 0) {
            return Math.max(velocity - Rules.DECELERATION, Math.min(goalVel, velocity + Rules.ACCELERATION));
        }
        // else
        return Math.max(velocity - Rules.ACCELERATION, Math.min(goalVel, velocity + maxDecel(-velocity)));
    }

    private final static double getMaxVelocity(double distance) {
        final double decelTime = Math.max(1, Math.ceil(
                (Math.sqrt((4 * 2 / Rules.DECELERATION) * distance + 1) - 1) / 2));

        if (decelTime == Double.POSITIVE_INFINITY) {
            return Rules.MAX_VELOCITY;
        }

        final double decelDist = (decelTime / 2.0) * (decelTime - 1)
                * Rules.DECELERATION;

        return ((decelTime - 1) * Rules.DECELERATION) + ((distance - decelDist) / decelTime);
    }

    private static double maxDecel(double speed) {
        double decelTime = speed / Rules.DECELERATION;
        double accelTime = (1 - decelTime);

        return Math.min(1, decelTime) * Rules.DECELERATION + Math.max(0, accelTime) * Rules.ACCELERATION;
    }
}
