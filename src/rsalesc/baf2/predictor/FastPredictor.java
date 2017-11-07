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

import rsalesc.baf2.BackAsFrontRobot2;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roberto Sales on 12/10/17.
 */
public class FastPredictor {
    public static List<PredictedPoint> tracePath(PredictedPoint initialPoint, Point dest, int deltaTime) {
        PredictedPoint cur = initialPoint;
        ArrayList<PredictedPoint> path = new ArrayList<>(10);
        path.add(cur);

        double remaining = cur.distance(dest);
        double cachedCos = 0;
        double cachedSin = 0;
        boolean straight = false;

        do {
            double velocity = cur.velocity;
            double heading = cur.heading;
            int ahead = cur.ahead;

            if(!straight && (remaining > 1 || cur.getSpeed() > 0.1)) {
                double angle = Physics.absoluteBearing(cur, dest);
                double dumbOffset = R.normalRelativeAngle(angle - heading);
                double offset = BackAsFrontRobot2.getQuickestTurn(dumbOffset);

                ahead = offset == dumbOffset ? +1 : -1;

                double maxTurn = R.PI / 18 - R.PI / 240 * Math.abs(velocity);
                offset = R.constrain(-maxTurn, offset, +maxTurn);
                heading = R.normalAbsoluteAngle(heading + offset);

                cachedCos = R.cos(heading);
                cachedSin = R.sin(heading);
                if(R.isNear(offset, 0, 1e-4))
                    straight = true;
            }

            // turn usual velocity into baf velocity
            velocity *= ahead;

            if(velocity >= 0 & remaining >= decelDistance(velocity))
                velocity = Math.min(velocity + 1, 8);
            else {
                // TODO: review that, weird calc from Neuromancer, but it is pretty damn fast
                velocity = R.constrain(-1.9999999999, Math.abs(velocity) - Math.min(Math.max(Math.abs(velocity), remaining), 2), 6)
                        * (velocity < 0 ? -1 : 1);

//                velocity = PrecisePredictor.getNewVelocity(velocity, 8, remaining);
            }

            if(velocity > remaining)
                straight = false;

            // turn baf velocity into usual velocity
            velocity *= ahead;

            cur = new PredictedPoint(cur.x + cachedSin * velocity, cur.y + cachedCos * velocity, heading,
                    velocity, cur.time + 1, ahead);

            path.add(cur);

            if(straight)
                remaining = Math.abs(remaining - velocity * ahead);
            else
                remaining = cur.distance(dest);

        } while(--deltaTime != 0  && (Math.abs(remaining) > 0.1 || Math.abs(cur.velocity) > 0.1));

        return path;
    }

    public static List<PredictedPoint> tracePath(PredictedPoint initialPoint, Point dest) {
        return tracePath(initialPoint, dest, 91);
    }

    public static PredictedPoint[] interpolate(PredictedPoint source, PredictedPoint dest) {
        int steps = (int) (dest.time - source.time);
        if(steps == 0)
            return new PredictedPoint[]{dest};

        double dh = BackAsFrontRobot2.getQuickestTurn(R.normalRelativeAngle(dest.heading - source.heading)) / steps;
        double dx = (dest.x - source.x) / steps;
        double dy = (dest.y - source.y) / steps;
        double dv = (dest.velocity - source.velocity) / steps;

        PredictedPoint[] res = new PredictedPoint[steps + 1];
        res[0] = source;
        res[steps] = dest;

        // TODO: better think about ahead?
        for(int i = 1; i < steps; i++) {
            res[i] = new PredictedPoint(source.x + dx * i, source.y + dy * i, source.heading + dh * i,
                            source.velocity + dv * i, res[i-1].time + 1, dest.ahead);
        }

        return res;
    }

    public static PredictedPoint interpolate(PredictedPoint source, PredictedPoint dest, long tick) {
        if(!R.isBetween(source.time, tick, dest.time))
            throw new IllegalStateException();

        int steps = (int) (dest.time - source.time);
        int after = (int) (tick - source.time);

        if(steps == 0)
            return dest;

        double dh = BackAsFrontRobot2.getQuickestTurn(R.normalRelativeAngle(dest.heading - source.heading)) / steps;
        double dx = (dest.x - source.x) / steps;
        double dy = (dest.y - source.y) / steps;
        double dv = (dest.velocity - source.velocity) / steps;

        return new PredictedPoint(source.x + dx * after, source.y + dy * after,
                R.normalAbsoluteAngle(source.heading + dh * after),
                    source.velocity + dv * after, tick, dest.ahead);
    }

    private static double decelDistance(double vel){

        int intVel = (int)Math.ceil(vel);
        switch(intVel){
            case 8:
                return 6 + 4 + 2;
            case 7:
                return 5 + 3 + 1;
            case 6:
                return 4 + 2;
            case 5:
                return 3 + 1;
            case 4:
                return 2;
            case 3:
                return 1;
            case 2:
                // return 2;
            case 1:
                // return 1;
            case 0:
                return 0;

        }
        return 6 + 4 + 2;
    }
}
