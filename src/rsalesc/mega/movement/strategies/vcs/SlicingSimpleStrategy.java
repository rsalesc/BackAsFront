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
public class SlicingSimpleStrategy extends MultipleSlicingStrategy {
    @Override
    public double[][][] getSlices() {
        return new double[][][]{
                {EMPTY, LAT_VEL_S, ADV_VEL_S, EMPTY, EMPTY, EMPTY},
                {EMPTY, LAT_VEL_S, ADV_VEL_S, ESCAPE_S, EMPTY, ACCEL_S},
                {EMPTY, LAT_VEL_P, EMPTY, EMPTY, EMPTY, ACCEL},
                {EMPTY, LAT_VEL_P, ADV_VEL_P, EMPTY, EMPTY, ACCEL_S},
                {EMPTY, LAT_VEL_P, EMPTY, EMPTY, EMPTY, ACCEL},
                {EMPTY, LAT_VEL, EMPTY, EMPTY, EMPTY, ACCEL_S},
                {EMPTY, LAT_VEL, EMPTY, EMPTY, EMPTY, ACCEL_P},
                {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, ACCEL_P},
                {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, ACCEL},
                {EMPTY, EMPTY, ADV_VEL, ESCAPE, EMPTY, ACCEL_P},
                {BFT_S, EMPTY, EMPTY, EMPTY, EMPTY, ACCEL_P},
                {BFT_S, EMPTY, EMPTY, EMPTY, EMPTY, ACCEL},
                {BFT_S, LAT_VEL_S, ADV_VEL_P, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL, EMPTY, ESCAPE_P, EMPTY, ACCEL_S},
                {BFT_S, LAT_VEL_P, EMPTY, ESCAPE, EMPTY, ACCEL_S},
                {BFT_S, LAT_VEL, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL_S, EMPTY, ESCAPE_P, EMPTY, ACCEL},
                {BFT_S, EMPTY, ADV_VEL_P, ESCAPE, EMPTY, EMPTY},
                {BFT_P, LAT_VEL_P, EMPTY, ESCAPE_P, EMPTY, EMPTY},
                {BFT_P, LAT_VEL, EMPTY, EMPTY, EMPTY, ACCEL_P},
                {BFT, LAT_VEL_S, ADV_VEL, ESCAPE_P, EMPTY, EMPTY},
                {BFT, LAT_VEL_P, ADV_VEL_S, EMPTY, EMPTY, ACCEL},
                {BFT, LAT_VEL, EMPTY, EMPTY, ESCAPE_P, EMPTY},
                {BFT, LAT_VEL, EMPTY, EMPTY, EMPTY, ACCEL_S},
                {BFT, LAT_VEL, EMPTY, EMPTY, EMPTY, ACCEL_S}
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
                f.accel
        };
    }
}
