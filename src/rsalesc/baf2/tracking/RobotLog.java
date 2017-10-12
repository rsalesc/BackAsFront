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

package rsalesc.baf2.tracking;

import robocode.Robot;
import robocode.Rules;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.mega.predictor.MovementPredictor;
import rsalesc.mega.predictor.PredictedPoint;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public interface RobotLog {
    RobotSnapshot exactlyAt(long time);

    RobotSnapshot atLeastAt(long time);

    RobotSnapshot atMostAt(long time);

    RobotSnapshot getLatest();

    RobotSnapshot getKthLatest(int k);

    RobotSnapshot getAtLeastKthLatest(int k);

    RobotSnapshot before(RobotSnapshot robot);

    RobotSnapshot after(RobotSnapshot robot);

    int size();

    default InterpolatedSnapshot interpolate(long time) {
        RobotSnapshot atMost = atMostAt(time);

        if(atMost == null)
            return null;

        PredictedPoint cur = PredictedPoint.from(atMost);

        if(atMost.getTime() == time)
            return new InterpolatedSnapshot(atMost, PredictedPoint.from(atMost));

        RobotSnapshot after = after(atMost);

        if(after == null) {
            RobotSnapshot pastAtMost = before(atMost);
            int accel = 1;
            if(pastAtMost != null) {
                accel = (int) ((atMost.getVelocity() - pastAtMost.getVelocity())
                        * Math.signum(atMost.getVelocity() + 1e-8));
            }

            double maxVel = accel == 0 ? atMost.getVelocity() : (accel < 0 ? 0 : Rules.MAX_VELOCITY);

            while(cur.getTime() < time)
                cur = MovementPredictor.tick(cur, atMost.getHeading(), maxVel, Double.POSITIVE_INFINITY);
        } else {
            PredictedPoint afterCur = PredictedPoint.from(after);

            while(afterCur.getSpeed() > R.EPSILON) {
                afterCur = MovementPredictor.tick(afterCur, after.getHeading(), 0, Double.POSITIVE_INFINITY);
            }

            while(cur.getTime() < time) {
                double angle = Physics.absoluteBearing(cur, afterCur);
                cur = MovementPredictor.tick(cur, angle, Rules.MAX_VELOCITY, cur.distance(afterCur));
            }
        }

        return new InterpolatedSnapshot(atMost, cur);
    }
}
