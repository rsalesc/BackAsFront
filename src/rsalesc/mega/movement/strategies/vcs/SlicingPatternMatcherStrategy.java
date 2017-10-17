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

package rsalesc.mega.movement.strategies.vcs;

import rsalesc.mega.utils.MultipleSlicingStrategy;
import rsalesc.mega.utils.TargetingLog;

import static rsalesc.mega.movement.strategies.vcs.Slices.*;

/**
 * Created by Roberto Sales on 28/08/17.
 */
public class SlicingPatternMatcherStrategy extends MultipleSlicingStrategy {
    @Override
    public double[][][] getSlices() {
        return new double[][][]{
                {EMPTY, LAT_VEL_S, ADV_VEL_S, EMPTY, EMPTY, EMPTY, RUN, EMPTY},
                {EMPTY, LAT_VEL_S, ADV_VEL_P, EMPTY, EMPTY, EMPTY, RUN_S, D10},
                {EMPTY, LAT_VEL_S, ADV_VEL, EMPTY, EMPTY, EMPTY, RUN_P, D10},
                {EMPTY, LAT_VEL_S, ADV_VEL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, LAT_VEL_S, EMPTY, EMPTY, EMPTY, EMPTY, RUN_S, D10_S},
                {EMPTY, LAT_VEL_S, ADV_VEL_S, EMPTY, EMPTY, EMPTY, RUN_P, EMPTY},
                {EMPTY, LAT_VEL_S, ADV_VEL_S, EMPTY, EMPTY, ACCEL, EMPTY, D10_P},
                {EMPTY, LAT_VEL_S, ADV_VEL_P, EMPTY, EMPTY, EMPTY, RUN, D10},
                {EMPTY, LAT_VEL_S, ADV_VEL, EMPTY, EMPTY, EMPTY, RUN_S, EMPTY},
                {EMPTY, LAT_VEL_S, EMPTY, EMPTY, EMPTY, ACCEL_P, EMPTY, D10_P},
                {EMPTY, LAT_VEL_S, ADV_VEL, EMPTY, EMPTY, EMPTY, EMPTY, D10_P},
                {EMPTY, LAT_VEL_S, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, D10},
                {EMPTY, LAT_VEL_S, ADV_VEL_S, EMPTY, EMPTY, EMPTY, RUN_S, D10_P},
                {EMPTY, LAT_VEL_S, ADV_VEL, EMPTY, EMPTY, EMPTY, RUN, D10_P},
                {EMPTY, LAT_VEL_P, ADV_VEL_S, EMPTY, EMPTY, EMPTY, RUN_S, D10_P},
                {EMPTY, LAT_VEL_P, ADV_VEL, EMPTY, EMPTY, EMPTY, RUN, D10_P},
                {EMPTY, LAT_VEL_P, ADV_VEL, EMPTY, EMPTY, EMPTY, EMPTY, D10_P},
                {EMPTY, LAT_VEL_P, ADV_VEL, EMPTY, EMPTY, ACCEL_P, EMPTY, D10_S},
                {EMPTY, LAT_VEL_P, ADV_VEL_P, EMPTY, EMPTY, ACCEL_S, RUN_S, EMPTY},
                {EMPTY, LAT_VEL_P, ADV_VEL_S, EMPTY, EMPTY, EMPTY, RUN_S, D10},
                {EMPTY, LAT_VEL_P, EMPTY, EMPTY, EMPTY, EMPTY, RUN, D10},
                {EMPTY, LAT_VEL_P, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, LAT_VEL_P, ADV_VEL_P, EMPTY, EMPTY, ACCEL_S, RUN_S, D10},
                {EMPTY, LAT_VEL_P, ADV_VEL_S, EMPTY, EMPTY, EMPTY, RUN_P, D10},
                {EMPTY, LAT_VEL, EMPTY, EMPTY, EMPTY, EMPTY, RUN_S, D10},
                {EMPTY, LAT_VEL, EMPTY, EMPTY, EMPTY, ACCEL_S, EMPTY, EMPTY},
                {EMPTY, LAT_VEL, ADV_VEL_S, EMPTY, EMPTY, EMPTY, EMPTY, D10},
                {EMPTY, LAT_VEL, ADV_VEL_S, EMPTY, EMPTY, EMPTY, RUN_S, D10_P},
                {EMPTY, LAT_VEL, ADV_VEL_S, EMPTY, EMPTY, EMPTY, RUN_S, D10_P},
                {EMPTY, LAT_VEL, ADV_VEL_S, EMPTY, EMPTY, EMPTY, EMPTY, D10_S},
                {EMPTY, LAT_VEL, ADV_VEL_P, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, LAT_VEL, ADV_VEL_P, EMPTY, EMPTY, EMPTY, RUN_P, EMPTY},
                {EMPTY, LAT_VEL, ADV_VEL_P, EMPTY, EMPTY, EMPTY, EMPTY, D10_P},
                {EMPTY, LAT_VEL, ADV_VEL_P, EMPTY, EMPTY, EMPTY, RUN_S, D10_P},
                {EMPTY, LAT_VEL, ADV_VEL_P, EMPTY, EMPTY, EMPTY, RUN_P, D10},
                {EMPTY, LAT_VEL, ADV_VEL_P, EMPTY, EMPTY, EMPTY, RUN, D10},
                {EMPTY, LAT_VEL, ADV_VEL, EMPTY, EMPTY, EMPTY, RUN_S, D10},
                {EMPTY, LAT_VEL, ADV_VEL, EMPTY, EMPTY, EMPTY, EMPTY, D10_P},
                {EMPTY, LAT_VEL, ADV_VEL, EMPTY, EMPTY, ACCEL_S, RUN_P, D10_S},
                {EMPTY, LAT_VEL, ADV_VEL, EMPTY, EMPTY, ACCEL_S, RUN_P, D10}
        };

    }

    @Override
    public double[] getQuery(TargetingLog f) {
        return new double[]{
                f.bft(),
                f.lateralVelocity,
                f.advancingVelocity,
                f.positiveEscape,
                f.negativeEscape,
                f.accel,
                Math.max(f.run / f.bft(), 1),
                f.displaceLast10
        };
    }
}
