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
public class SlicingFlattenerStrategy extends MultipleSlicingStrategy {
    @Override
    public double[][][] getSlices() {
        return new double[][][]{
                // flattener
                {EMPTY, LAT_VEL_S, ADV_VEL, ESCAPE_P, ESCAPE, ACCEL_S, EMPTY, EMPTY, EMPTY},
                {EMPTY, LAT_VEL_S, EMPTY, EMPTY, ESCAPE, ACCEL, DECEL, REVERT_P, EMPTY},
                {EMPTY, LAT_VEL_S, EMPTY, ESCAPE_S, ESCAPE_P, ACCEL_S, EMPTY, EMPTY, EMPTY},
                {EMPTY, LAT_VEL_S, ADV_VEL, ESCAPE_S, ESCAPE_P, ACCEL_S, EMPTY, REVERT_P, EMPTY},
                {EMPTY, LAT_VEL_S, EMPTY, ESCAPE_P, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, LAT_VEL_S, ADV_VEL, ESCAPE, ESCAPE_P, EMPTY, DECEL_P, REVERT, D10},
                {EMPTY, LAT_VEL_P, EMPTY, ESCAPE_S, EMPTY, EMPTY, EMPTY, REVERT_S, D10},
                {EMPTY, LAT_VEL, EMPTY, ESCAPE_P, EMPTY, ACCEL_S, DECEL, EMPTY, EMPTY},
                {EMPTY, LAT_VEL, EMPTY, EMPTY, EMPTY, ACCEL_S, DECEL_S, EMPTY, EMPTY},
                {EMPTY, LAT_VEL, ADV_VEL_S, ESCAPE, EMPTY, ACCEL_P, DECEL_P, EMPTY, EMPTY},
                {EMPTY, LAT_VEL, ADV_VEL, ESCAPE, ESCAPE_P, ACCEL_P, EMPTY, REVERT_P, EMPTY},
                {EMPTY, EMPTY, ADV_VEL, ESCAPE, EMPTY, ACCEL_S, EMPTY, REVERT, EMPTY},
                {BFT_S, LAT_VEL_S, ADV_VEL_S, ESCAPE_S, ESCAPE_S, ACCEL_S, DECEL_S, REVERT_S, D10_S},
                {BFT_S, LAT_VEL, ADV_VEL_P, ESCAPE, EMPTY, ACCEL_P, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL, EMPTY, ESCAPE_P, EMPTY, ACCEL_S, EMPTY, EMPTY, D10_S},
                {BFT_S, LAT_VEL, EMPTY, EMPTY, ESCAPE_S, EMPTY, EMPTY, REVERT, EMPTY},
                {BFT_S, LAT_VEL_P, ADV_VEL, ESCAPE_P, EMPTY, ACCEL_S, EMPTY, REVERT_P, D10_P},
                {BFT_S, LAT_VEL_S, EMPTY, EMPTY, EMPTY, ACCEL, DECEL_P, EMPTY, D10_S},
                {BFT_S, LAT_VEL_S, EMPTY, ESCAPE_S, EMPTY, EMPTY, EMPTY, REVERT_S, EMPTY},
                {BFT_S, LAT_VEL_P, ADV_VEL_P, EMPTY, ESCAPE_P, ACCEL, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL_P, ADV_VEL_S, ESCAPE_S, ESCAPE_P, ACCEL, EMPTY, EMPTY, D10},
                {BFT_S, LAT_VEL, EMPTY, EMPTY, EMPTY, ACCEL_S, EMPTY, REVERT_S, EMPTY},
                {BFT_S, LAT_VEL_P, ADV_VEL_P, ESCAPE_S, ESCAPE, ACCEL, DECEL, EMPTY, EMPTY},
                {BFT_S, EMPTY, ADV_VEL_P, EMPTY, ESCAPE, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL_S, EMPTY, ESCAPE_S, EMPTY, ACCEL_S, DECEL_P, EMPTY, D10_P},
                {BFT_S, LAT_VEL, EMPTY, ESCAPE_S, ESCAPE, ACCEL_P, EMPTY, EMPTY, D10_P},
                {BFT_S, LAT_VEL_P, EMPTY, ESCAPE_S, ESCAPE, EMPTY, EMPTY, EMPTY, D10_S},
                {BFT_S, LAT_VEL_S, EMPTY, EMPTY, ESCAPE_P, ACCEL_P, EMPTY, EMPTY, EMPTY},
                {BFT_P, EMPTY, ADV_VEL, ESCAPE_P, EMPTY, ACCEL, EMPTY, REVERT_S, D10},
                {BFT_P, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, REVERT, D10_S},
                {BFT_P, LAT_VEL, EMPTY, EMPTY, EMPTY, EMPTY, DECEL, REVERT, EMPTY},
                {BFT_P, LAT_VEL_S, EMPTY, EMPTY, EMPTY, ACCEL, DECEL_P, REVERT_P, D10_S},
                {BFT_P, LAT_VEL_S, ADV_VEL, EMPTY, ESCAPE, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_P, LAT_VEL, EMPTY, EMPTY, ESCAPE_S, ACCEL, DECEL_S, REVERT_S, EMPTY},
                {BFT_P, LAT_VEL, EMPTY, ESCAPE_P, EMPTY, ACCEL, DECEL_S, REVERT_S, EMPTY},
                {BFT_P, LAT_VEL_S, ADV_VEL_P, ESCAPE_S, ESCAPE_P, ACCEL_P, EMPTY, EMPTY, D10_S},
                {BFT, LAT_VEL_S, ADV_VEL, EMPTY, EMPTY, ACCEL_S, DECEL_P, EMPTY, D10},
                {BFT, LAT_VEL_S, ADV_VEL_S, EMPTY, ESCAPE_P, ACCEL_P, EMPTY, EMPTY, D10_S},
                {BFT, LAT_VEL_S, EMPTY, EMPTY, ESCAPE, ACCEL_S, EMPTY, EMPTY, EMPTY},
                {BFT, LAT_VEL_S, ADV_VEL_P, EMPTY, ESCAPE_S, EMPTY, DECEL_P, EMPTY, EMPTY},
                {BFT, LAT_VEL_S, ADV_VEL_P, EMPTY, ESCAPE, ACCEL_S, EMPTY, REVERT_S, EMPTY},
                {BFT, LAT_VEL_S, ADV_VEL, ESCAPE_P, EMPTY, ACCEL, EMPTY, EMPTY, D10},
                {BFT, LAT_VEL_P, EMPTY, ESCAPE, EMPTY, ACCEL, DECEL_S, EMPTY, EMPTY},
                {BFT, LAT_VEL_P, EMPTY, ESCAPE, EMPTY, EMPTY, EMPTY, EMPTY, D10},
                {BFT, LAT_VEL_P, ADV_VEL, ESCAPE_P, EMPTY, EMPTY, DECEL_S, EMPTY, EMPTY},
                {BFT, LAT_VEL_P, ADV_VEL_S, EMPTY, EMPTY, ACCEL_P, DECEL_S, REVERT, EMPTY},
                {BFT, LAT_VEL, EMPTY, ESCAPE_S, EMPTY, EMPTY, EMPTY, REVERT, D10_P},
                {BFT, LAT_VEL, EMPTY, ESCAPE, ESCAPE, EMPTY, DECEL, REVERT, EMPTY},
                {BFT, LAT_VEL, ADV_VEL, EMPTY, EMPTY, ACCEL_S, DECEL_S, REVERT, EMPTY},
                {BFT, EMPTY, ADV_VEL, EMPTY, ESCAPE_S, ACCEL, DECEL, REVERT_P, D10}
        };

    }

    @Override
    public double[] getQuery(TargetingLog f) {
        return new double[]{
                f.bft(),
                Math.abs(f.lateralVelocity),
                f.advancingVelocity,
                f.getPreciseMea().max / f.getTraditionalMea(),
                -f.getPreciseMea().min / f.getTraditionalMea(),
                f.accel,
                f.timeDecel / f.bft(),
                f.timeRevert / f.bft(),
                f.displaceLast10
        };
    }
}
