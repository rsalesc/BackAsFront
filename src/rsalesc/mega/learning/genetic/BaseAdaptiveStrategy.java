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

package rsalesc.mega.learning.genetic;

import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.Range;
import rsalesc.mega.utils.Strategy;
import rsalesc.mega.utils.TargetingLog;

/**
 * Created by Roberto Sales on 03/10/17.
 */
public class BaseAdaptiveStrategy extends GeneticStrategy {
    @Override
    public double[] getQuery(TargetingLog f) {
        double[] params = getForcedParams();

        return new double[]{
                Math.min(Math.abs(f.lateralVelocity) / 8, 1),
                R.constrain(0, (f.advancingVelocity / 8 + 1) / 2, 1),
                Math.min(f.bft() / 80, 1),
                R.constrain(0, (f.accel + 1) / 2, 1),
                R.constrain(0, f.getPreciseMea().max / f.getMea(), 1.25) / 1.25,
                R.constrain(0, -f.getPreciseMea().min / f.getMea(), 1.25) / 1.25,
                1.0 / (1.0 + params[0] * f.timeRevert / f.bft()),
                1.0 / (1.0 + params[1] * f.timeDecel / f.bft()),
        };
    }

    @Override
    public Range[] getWeightsScheme() {
        Range[] range = super.getWeightsScheme();
        range[0].min = range[1].min = range[2].min = range[3].min = 0.75;
        range[4].min = 0.4;

        return range;
    }

    @Override
    public Range[] getParamsScheme() {
        return new Range[]{
                new Range(1.5, 3.0),
                new Range(1.5, 3.0)
        };
    }

    @Override
    public double[] getWeights() {
        return new double[8];
    }
}
