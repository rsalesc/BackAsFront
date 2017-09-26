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

package rsalesc.mega.predictor;

import robocode.Rules;
import robocode.util.Utils;
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
public abstract class MovementPredictor {
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
            double angle = Utils.normalAbsoluteAngle(WallSmoothing.naive(shrinkedField, stick, cur,
                    pointingAngle, direction));
            cur = _tick(cur, angle, brake ? 0 : Rules.MAX_VELOCITY, Double.POSITIVE_INFINITY);
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
            cur = _tick(cur, angle, Rules.MAX_VELOCITY, distance);
            res.add(cur);
        }

        return res;
    }

    public static List<PredictedPoint> generateOnWaveImpact(AxisRectangle field, double stick, PredictedPoint initialPoint, Wave wave,
                                                            int direction, double perpendiculator, boolean hasToPass) {
        List<PredictedPoint> points =
                predictOnWaveImpact(field, stick, initialPoint, wave, direction, perpendiculator, hasToPass, false);

        points.add(0, initialPoint);
        PredictedPoint back = points.get(points.size() - 1);

        for (int i = 0; i < 3; i++) {
            double angle = Utils.normalAbsoluteAngle(WallSmoothing.naive(field, stick, back,
                    Physics.absoluteBearing(wave.getSource(), back)
                            + perpendiculator * direction, direction));

            PredictedPoint next = back.fakeTick(angle, back.getVelocity(), angle, Rules.MAX_VELOCITY);
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
            res.push(Utils.normalRelativeAngle(Physics.absoluteBearing(wave.getSource(), pos) - absBearing) * direction);
            lastEscape.add(pos);
        }

        for (PredictedPoint neg : negList) {
            lastEscape.add(neg);
            res.push(Utils.normalRelativeAngle(Physics.absoluteBearing(wave.getSource(), neg) - absBearing) * direction);
        }

        return res;
    }

    /**
     * This method, different from _tick, predicts assuming that the robot will attempt
     * to move infinitely (hit the maximum speed and never break)
     * <p>
     * This is usually quicker than _tick for wavesurfing because it does not rely
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
    private static PredictedPoint _fastTick(PredictedPoint last, double angle, double maxVelocity) {
        double offset = Utils.normalRelativeAngle(angle - last.heading);
        double turn = BackAsFrontRobot2.getQuickestTurn(offset);
        int ahead = offset == turn ? +1 : -1;

        double maxTurning = Physics.maxTurningRate(last.velocity);
        double newHeading = Utils.normalAbsoluteAngle(R.constrain(-maxTurning, turn, maxTurning) + last.heading);

        double newVelocity = getTickVelocity(last.velocity, maxVelocity, ahead, Double.POSITIVE_INFINITY);

        return last.tick(newHeading, newVelocity);
    }

    private static PredictedPoint _tick(PredictedPoint last, double angle, double maxVelocity, double remaining) {
        double offset = Utils.normalRelativeAngle(angle - last.heading);
        double turn = BackAsFrontRobot2.getQuickestTurn(offset);
        int ahead = offset == turn ? +1 : -1;

        double maxTurning = Physics.maxTurningRate(last.velocity);
        double newHeading = Utils.normalAbsoluteAngle(R.constrain(-maxTurning, turn, maxTurning) + last.heading);

        double newVelocity = new Range(-maxTurning, maxTurning).isNearlyContained(turn) || !SHARP_TURNING
                ? getTickVelocity(last.velocity, maxVelocity, ahead, remaining)
                : getTickVelocity(last.velocity, 0, ahead, remaining);

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
        return Utils.normalAbsoluteAngle(heading + R.constrain(-turnRate, turn, +turnRate));
    }

    /*
    *    DISCLAIMER: The next three functions were COPIED from the Robocode engine itself,
    *    since it's pretty much impossible to deduce what is going under the hood as the Robocode
    *    physics differ from real world physics because time in Robocode is discrete.
    *
    *    Instead of doing random guesses and trying to get a (hopefully) correct formula by interpolating
    *    these guesses, I decided to copy it from the Robocode source itself, which is open,
    *    since I see no valuable merit in the effort of trying to randomly guess things about
    *    the game which are already open in the engine itself.
    *
    *    Hope this is not seen as an issue. Moreover, it's the only
    *    copied piece of code in this whole project.
    *
    *    Roberto.
     */

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
