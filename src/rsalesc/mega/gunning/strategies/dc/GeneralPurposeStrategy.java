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

package rsalesc.mega.gunning.strategies.dc;

import rsalesc.baf2.core.utils.R;
import rsalesc.mega.utils.Strategy;
import rsalesc.mega.utils.TargetingLog;

/**
 * Created by Roberto Sales on 15/09/17.
 */
public class GeneralPurposeStrategy extends Strategy {
    @Override
    public double[] getQuery(TargetingLog f) {
        return new double[]{
                Math.min(f.bft() / 80, 1),
                R.constrain(0, f.bulletPower / 3, 1),
                Math.abs(R.sin(f.relativeHeading)), // was 4
                (R.cos(f.relativeHeading) + 1) / 2.,
                Math.abs(f.velocity) / 8.,
                R.constrain(0, (f.accel + 1) / 2, 1),
                R.constrain(0, f.getPreciseMea().max / f.getMea(), 1),
                R.constrain(0, -f.getPreciseMea().min / f.getMea(), 1),
                1.0 / (1.0 + 2. * f.timeDecel),
                f.virtuality()
        };
    }

    @Override
    public double[] getWeights() {
//        return new double[]{5, 0.5, 3.5, 3, 2, 4, 4, 2, 1.75, 1};
        return new double[]{5, 4, 4, 7, 1, 3, 4, 2, 4, 3};
    }
}
