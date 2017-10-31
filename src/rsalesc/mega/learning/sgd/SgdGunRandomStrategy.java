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

package rsalesc.mega.learning.sgd;

import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.Range;
import rsalesc.mega.learning.genetic.GeneticStrategy;
import rsalesc.mega.utils.TargetingLog;

public class SgdGunRandomStrategy extends GeneticStrategy {
    private static final Range IMPORTANT_RANGE = new Range(0.4, 1.0);
    private static final Range DISCARDABLE_RANGE = new Range(0.0, 3.5);
    private static final Range LESS_IMPORTANT_RANGE = new Range(0.4, 2.0);

    @Override
    public double[] getQuery(TargetingLog f) {
        double[] params = getForcedParams();

        return new double[]{
                10  * Math.min(Math.abs(f.lateralVelocity) / 8, 1),
                2   * R.constrain(0, (f.advancingVelocity / 8 + 1) / 2, 1),
                6   * Math.min(f.bft() / 81, 1),
                2   * R.constrain(0, (f.accel + 1) / 2, 1),
                4.5 * R.constrain(0, f.getPreciseMea().max / f.getTraditionalMea(), 1), // was 1.3
                2   * R.constrain(0, -f.getPreciseMea().min / f.getTraditionalMea(), 1),
                2   * 1.0 / (1.0 + 2. * f.timeRevert / f.bft()),
                2   * 1.0 / (1.0 + 2. * f.timeDecel / f.bft()),
                2   * R.constrain(0, f.displaceLast10 / 80, 1),
//                1   * Math.pow(f.bulletsFired * params[0], params[1]),
                3   * (R.constrain(-1, f.lastGf, +1) + 1) / 2
        };
    }

    @Override
    public Range[] getWeightsScheme() {
        return new Range[]{
                IMPORTANT_RANGE,
                LESS_IMPORTANT_RANGE,
                IMPORTANT_RANGE,
                LESS_IMPORTANT_RANGE,
                IMPORTANT_RANGE,
                LESS_IMPORTANT_RANGE,
                DISCARDABLE_RANGE,
                LESS_IMPORTANT_RANGE,
                LESS_IMPORTANT_RANGE,
//                new Range(0.0, 1.0),
                DISCARDABLE_RANGE
        };
    }

    @Override
    public Range[] getParamsScheme() {
        return new Range[0];
    }

    @Override
    public double[] getWeights() {
        return new double[10];
    }

    @Override
    public double[] getParams() {
        return new double[0];
    }
}
