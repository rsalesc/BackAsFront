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
 * Created by Roberto Sales on 22/08/17.
 */
public class SlicingAdaptiveStrategy extends MultipleSlicingStrategy {
    @Override
    public double[][][] getSlices() {
        return new double[][][]{
                {EMPTY, LAT_VEL_S, EMPTY, ESCAPE, EMPTY, ACCEL, EMPTY, EMPTY},
                {EMPTY, LAT_VEL_S, ADV_VEL_P, EMPTY, EMPTY, ACCEL, EMPTY, EMPTY},
                {EMPTY, LAT_VEL_S, EMPTY, ESCAPE_P, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, LAT_VEL_S, EMPTY, ESCAPE, ESCAPE_S, EMPTY, RUN_S, EMPTY},
                {EMPTY, LAT_VEL_S, ADV_VEL, EMPTY, EMPTY, ACCEL_S, EMPTY, D10_S},
                {EMPTY, LAT_VEL_S, EMPTY, ESCAPE, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, LAT_VEL_P, EMPTY, ESCAPE_S, ESCAPE, EMPTY, RUN_S, EMPTY},
                {EMPTY, LAT_VEL_P, EMPTY, ESCAPE_P, EMPTY, ACCEL, EMPTY, EMPTY},
                {EMPTY, LAT_VEL_P, EMPTY, ESCAPE, EMPTY, ACCEL, EMPTY, D10_S},
                {EMPTY, LAT_VEL_P, EMPTY, ESCAPE_P, ESCAPE, EMPTY, RUN_P, D10_S},
                {EMPTY, LAT_VEL_P, ADV_VEL, ESCAPE_S, EMPTY, ACCEL, RUN_P, EMPTY},
                {EMPTY, LAT_VEL_P, EMPTY, ESCAPE, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, LAT_VEL_P, ADV_VEL, ESCAPE_S, EMPTY, EMPTY, RUN, D10_P},
                {EMPTY, LAT_VEL_P, ADV_VEL, EMPTY, EMPTY, ACCEL, EMPTY, D10_S},
                {EMPTY, LAT_VEL, EMPTY, ESCAPE_S, EMPTY, EMPTY, EMPTY, D10_P},
                {EMPTY, LAT_VEL, EMPTY, ESCAPE_S, EMPTY, EMPTY, EMPTY, D10},
                {EMPTY, LAT_VEL, EMPTY, ESCAPE_P, ESCAPE_P, EMPTY, EMPTY, EMPTY},
                {EMPTY, LAT_VEL, EMPTY, ESCAPE, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, LAT_VEL, EMPTY, EMPTY, EMPTY, ACCEL_S, RUN_S, EMPTY},
                {EMPTY, LAT_VEL, EMPTY, EMPTY, EMPTY, ACCEL_P, EMPTY, EMPTY},
                {EMPTY, LAT_VEL, ADV_VEL_S, EMPTY, ESCAPE_P, ACCEL_S, EMPTY, D10_S},
                {EMPTY, LAT_VEL, ADV_VEL_S, EMPTY, ESCAPE, ACCEL_P, RUN_P, D10_P},
                {EMPTY, LAT_VEL, ADV_VEL, ESCAPE, EMPTY, ACCEL_S, EMPTY, D10_P},
                {EMPTY, EMPTY, EMPTY, ESCAPE, ESCAPE, ACCEL_P, EMPTY, D10},
                {BFT_S, LAT_VEL_P, ADV_VEL_S, ESCAPE_P, ESCAPE_P, ACCEL, RUN, EMPTY},
                {BFT_S, LAT_VEL, EMPTY, ESCAPE, ESCAPE_P, ACCEL_P, EMPTY, EMPTY},
                {BFT_S, LAT_VEL, ADV_VEL_P, ESCAPE, ESCAPE_P, ACCEL_P, EMPTY, D10_P},
                {BFT_S, LAT_VEL_S, EMPTY, EMPTY, ESCAPE_P, ACCEL_P, RUN_S, EMPTY},
                {BFT_S, LAT_VEL_P, EMPTY, ESCAPE_P, ESCAPE_P, EMPTY, RUN, EMPTY},
                {BFT_S, LAT_VEL, ADV_VEL_S, EMPTY, ESCAPE_S, EMPTY, EMPTY, D10_P},
                {BFT_S, LAT_VEL_P, EMPTY, ESCAPE_P, ESCAPE_S, ACCEL_S, RUN_P, EMPTY},
                {BFT_P, LAT_VEL_S, EMPTY, ESCAPE_S, ESCAPE_P, EMPTY, EMPTY, EMPTY},
                {BFT_P, LAT_VEL, EMPTY, EMPTY, ESCAPE, EMPTY, EMPTY, EMPTY},
                {BFT_P, LAT_VEL_S, EMPTY, ESCAPE_S, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_P, EMPTY, EMPTY, EMPTY, EMPTY, ACCEL_P, EMPTY, D10_S},
                {BFT_P, LAT_VEL_P, ADV_VEL_P, ESCAPE_P, ESCAPE, ACCEL_P, RUN, EMPTY},
                {BFT_P, LAT_VEL_P, ADV_VEL_P, ESCAPE_P, EMPTY, ACCEL, RUN, D10_P},
                {BFT, LAT_VEL_S, EMPTY, ESCAPE_S, ESCAPE, EMPTY, EMPTY, D10_P},
                {BFT, LAT_VEL_S, ADV_VEL, ESCAPE_P, EMPTY, ACCEL_P, EMPTY, EMPTY},
                {BFT, LAT_VEL_S, EMPTY, ESCAPE_S, EMPTY, ACCEL, EMPTY, D10_P},
                {BFT, LAT_VEL_S, ADV_VEL, EMPTY, EMPTY, ACCEL_P, RUN, D10},
                {BFT, LAT_VEL_S, EMPTY, EMPTY, EMPTY, EMPTY, RUN_P, EMPTY},
                {BFT, LAT_VEL_S, ADV_VEL, ESCAPE, EMPTY, EMPTY, RUN_P, D10_S},
                {BFT, LAT_VEL_P, EMPTY, EMPTY, ESCAPE_S, EMPTY, RUN_S, EMPTY},
                {BFT, LAT_VEL_P, EMPTY, ESCAPE_P, ESCAPE, ACCEL_P, EMPTY, D10_S},
                {BFT, LAT_VEL_P, EMPTY, ESCAPE_P, ESCAPE_P, ACCEL_S, EMPTY, D10_P},
                {BFT, LAT_VEL, EMPTY, EMPTY, EMPTY, ACCEL_P, EMPTY, D10_P},
                {BFT, EMPTY, EMPTY, ESCAPE_S, EMPTY, ACCEL_S, EMPTY, EMPTY},
                {BFT, EMPTY, EMPTY, ESCAPE, EMPTY, EMPTY, RUN_S, EMPTY},
                {BFT, EMPTY, EMPTY, EMPTY, ESCAPE_P, ACCEL_P, EMPTY, D10_P}
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
