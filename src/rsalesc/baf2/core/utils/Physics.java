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

package rsalesc.baf2.core.utils;

import robocode.Rules;
import robocode.util.Utils;
import rsalesc.baf2.core.utils.geometry.Point;

/**
 * Created by Roberto Sales on 21/07/17.
 */
public abstract class Physics {
    public static final double MAX_VELOCITY = Rules.MAX_VELOCITY;
    public static final double MAX_TURN_RATE = Rules.MAX_TURN_RATE;
    public static final double MAX_POWER = 3.0;
    public static final double MIN_POWER = 0.1;
    public static final double BOT_WIDTH = 18;

    public static double absoluteBearing(Point source, Point dest) {
        return new Point(source, dest).absoluteBearing();
    }

    /**
     * Returns lateral velocity relative to robot, but
     * assuming it as a stationary bot.
     * Positive means clockwise/to the right.
     *
     * @param absBearing
     * @param velocity
     * @param heading
     * @return
     */
    public static double getLateralVelocityFromStationary(double absBearing, double velocity, double heading) {
        return R.sin(heading - absBearing) * velocity;
    }

    /**
     * Return angular velocity, assuming that the enemy
     * is doing a circular movement around you and you are stationary.
     * Positive means clockwise.
     *
     * @param distance
     * @param velocity
     * @return
     */
    public static double getAngularVelocityFromStationary(double absBearing, double distance,
                                                          double velocity, double heading) {
        return (velocity / distance) * Math.signum(getLateralVelocityFromStationary(absBearing, velocity, heading));
    }

    public static double getApproachingVelocityFromStationary(double absBearing,
                                                              double velocity, double heading) {
        return -R.cos(heading - absBearing) * velocity;
    }

    public static double maxEscapeAngle(double velocity) {
        return R.asin(MAX_VELOCITY / Math.abs(velocity));
    }

    public static double maxTurningRate(double velocity) {
        return Math.toRadians(MAX_TURN_RATE - 0.75 * Math.abs(velocity));
    }

    public static double bulletVelocity(double power) {
        return 20. - power * 3;
    }

    public static double bulletPower(double velocity) {
        return (20.0 - Math.abs(velocity)) / 3.0;
    }

    public static double hitAngle(double distance) {
        return 36.0 / distance;
    }

    public static double getNewHeading(double heading, double turn, double velocity) {
        double turnRate = Rules.getTurnRateRadians(velocity);
        return Utils.normalAbsoluteAngle(heading + R.constrain(-turnRate, turn, +turnRate));
    }

    public static double getNewVelocity(double velocity, double maxVelocity, double distance) {
        if (distance < 0) {
            // If the distance is negative, then change it to be positive
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
