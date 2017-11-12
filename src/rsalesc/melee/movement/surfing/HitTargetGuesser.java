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
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.tracking.RobotSnapshot;

public class HitTargetGuesser implements TargetGuesser {
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
    public void evaluateHit(@Modified MeleeSituation[] sits, double hitAngle, double hitDistance, long time, RobotSnapshot hitRobot) {
        double[] p = new double[sits.length];

        if(hitRobot != null) {
            for (int i = 0; i < sits.length; i++) {
                if(sits[i].name.equals(hitRobot.getName())) {
                    p[i] = 1.0;
                }
            }
        }

        for(int i = 0; i < p.length; i++)
            sits[i].weight = p[i];
    }
}
