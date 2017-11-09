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

import robocode.Rules;
import rsalesc.baf2.core.utils.Pair;
import rsalesc.baf2.predictor.FastPredictor;
import rsalesc.baf2.predictor.PrecisePredictor;
import rsalesc.baf2.predictor.PredictedPoint;

import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public abstract class RobotLog {
    private Hashtable<Pair<RobotSnapshot, RobotSnapshot>, PredictedPoint[]> cache = new Hashtable<>();
    private Map<String, Object> data = new TreeMap<>();

    public abstract RobotSnapshot exactlyAt(long time);

    public abstract RobotSnapshot atLeastAt(long time);

    public abstract RobotSnapshot atMostAt(long time);

    public abstract RobotSnapshot getLatest();

    public abstract RobotSnapshot getKthLatest(int k);

    public abstract RobotSnapshot getAtLeastKthLatest(int k);

    public abstract RobotSnapshot before(RobotSnapshot robot);

    public abstract RobotSnapshot after(RobotSnapshot robot);

    public abstract int size();

    public Object getData(String key) {
        return data.get(key);
    }

    public void setData(String key, Object obj) {
        data.put(key, obj);
    }

    // TODO: optimize this function in general
    // TODO: return null if interpolation is too harsh
    public InterpolatedSnapshot interpolate(long time) {
        RobotSnapshot atMost = atMostAt(time);

        if(atMost == null)
            return null;

        if(atMost.getTime() == time)
            return new InterpolatedSnapshot(atMost, PredictedPoint.from(atMost));

        InterpolatedSnapshot res;

        PredictedPoint cur = PredictedPoint.from(atMost);

        RobotSnapshot after = after(atMost);

        if(after == null) {
            RobotSnapshot pastAtMost = before(atMost);
            int accel = 1;
            if(pastAtMost != null) {
                accel = (int) ((atMost.getVelocity() - pastAtMost.getVelocity())
                        * Math.signum(atMost.getVelocity() + 1e-8));
            }

            double maxVel = accel == 0 ? atMost.getVelocity() : (accel < 0 ? 0 : Rules.MAX_VELOCITY);

            // TODO: cache the whole process
            while(cur.time < time)
                cur = PrecisePredictor.tick(cur, atMost.getHeading(), maxVel, Double.POSITIVE_INFINITY);

            res = new PredictedSnapshot(atMost, cur);
        } else {
            Pair<RobotSnapshot, RobotSnapshot> pair = new Pair<>(atMost, after);
            int steps = (int) (time - cur.time);

            PredictedPoint[] cached = cache.get(pair);
            if(cached == null) {
                cache.put(pair, cached = FastPredictor.interpolate(cur, PredictedPoint.from(after)));
            }

            res = new InterpolatedSnapshot(atMost, cached[steps]);
        }

        return res;
    }
}
