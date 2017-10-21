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

import rsalesc.baf2.core.annotations.Modified;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.tracking.EnemyTracker;
import rsalesc.baf2.tracking.InterpolatedSnapshot;
import rsalesc.baf2.tracking.MyLog;
import rsalesc.baf2.tracking.RobotLog;
import rsalesc.mega.utils.IMea;
import rsalesc.mega.utils.TargetingLog;

public class SimpleTargetGuesser implements TargetGuesser {
    @Override
    public void evaluateShot(@Modified MeleeSituation[] sits, long time) {
        double[] p = new double[sits.length];

        for(int i = 0; i < sits.length; i++) {
            p[i] = 1.0 / R.sqr(sits[i].log.distance);
        }

        // R.probabilityDistribution(p);

        for(int i = 0; i < sits.length; i++) {
            sits[i].weight = p[i];
        }
    }

    @Override
    public void evaluateHit(@Modified MeleeSituation[] sits, double hitAngle, double hitDistance, long time) {
        double closest = Double.POSITIVE_INFINITY;

        for(MeleeSituation sit : sits) {
            closest = Math.min(closest, sit.log.distance);
        }

        double[] p = new double[sits.length];

        for(int i = 0; i < sits.length; i++) {
            MeleeSituation sit = sits[i];

            TargetingLog f = sit.log;
            double d = f.distance / closest;

            if(d > 1.25) {
                continue;
            }

            RobotLog enemyLog = EnemyTracker.getInstance().getLog(sit.name);
            if(enemyLog == null) {
                enemyLog = MyLog.getInstance();
            }

            InterpolatedSnapshot snap = enemyLog.interpolate(f.time);

            if(snap == null)
                continue;

            double absBearing = Physics.absoluteBearing(f.source, snap.getPoint());
            double offset = R.normalRelativeAngle(absBearing - hitAngle);

            IMea mea = f.imprecise();
            double gf = mea.getUnconstrainedGf(offset);

            if(gf < -1 || gf > 1) {
                continue;
            }

            p[i] = /* R.gaussKernel(gf) */ R.gaussKernel(d); // TODO: rethink
        }

        R.probabilityDistribution(p);

        for(int i = 0; i < p.length; i++)
            sits[i].weight = p[i];
    }
}
