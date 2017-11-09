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
                {EMPTY, LAT_VEL_S, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, LAT_VEL_S, EMPTY, ESCAPE_S, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, LAT_VEL_S, EMPTY, EMPTY, ESCAPE_S, ACCEL_P, EMPTY, EMPTY, EMPTY},
                {EMPTY, LAT_VEL_S, EMPTY, ESCAPE_P, ESCAPE_S, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, LAT_VEL_S, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, LAT_VEL_P, ADV_VEL_P, EMPTY, ESCAPE, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, LAT_VEL_P, ADV_VEL_S, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, LAT_VEL_P, ADV_VEL_S, ESCAPE, ESCAPE_S, ACCEL_P, EMPTY, EMPTY, EMPTY},
                {EMPTY, LAT_VEL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, EMPTY, EMPTY, ESCAPE_P, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, EMPTY, EMPTY, EMPTY, ESCAPE, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, EMPTY, ADV_VEL_S, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, EMPTY, ADV_VEL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_S, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_S, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL, EMPTY, ESCAPE_S, ESCAPE, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL_P, EMPTY, ESCAPE_P, ESCAPE_P, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL_S, ADV_VEL_S, ESCAPE_P, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL, EMPTY, EMPTY, ESCAPE_P, ACCEL, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL, ADV_VEL_P, ESCAPE_P, EMPTY, ACCEL_P, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL_P, ADV_VEL_P, ESCAPE_P, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL_S, ADV_VEL_P, ESCAPE, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL_P, ADV_VEL, ESCAPE_S, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL_P, EMPTY, ESCAPE_S, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL, EMPTY, ESCAPE_S, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL_P, ADV_VEL, ESCAPE_S, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL_S, EMPTY, ESCAPE, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL, ADV_VEL_P, ESCAPE_S, ESCAPE, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL_S, ADV_VEL_S, ESCAPE_P, ESCAPE_S, ACCEL, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL_P, EMPTY, EMPTY, ESCAPE_P, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL, EMPTY, ESCAPE, ESCAPE_S, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_S, LAT_VEL_S, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_P, LAT_VEL_P, EMPTY, ESCAPE, ESCAPE_P, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_P, LAT_VEL_S, ADV_VEL, ESCAPE_P, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_P, LAT_VEL_S, ADV_VEL, ESCAPE_S, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_P, LAT_VEL_S, EMPTY, ESCAPE_S, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_P, LAT_VEL_S, ADV_VEL, ESCAPE, ESCAPE_S, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_P, LAT_VEL_S, EMPTY, ESCAPE, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_P, LAT_VEL_S, EMPTY, ESCAPE_P, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT_P, LAT_VEL_P, EMPTY, ESCAPE, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT, LAT_VEL_S, EMPTY, ESCAPE, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT, LAT_VEL_S, ADV_VEL_P, ESCAPE_P, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT, LAT_VEL_S, EMPTY, ESCAPE_S, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT, LAT_VEL_S, ADV_VEL, EMPTY, ESCAPE, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT, LAT_VEL_S, ADV_VEL, ESCAPE, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT, LAT_VEL_P, EMPTY, ESCAPE_S, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT, LAT_VEL_P, EMPTY, ESCAPE_S, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT, LAT_VEL_P, EMPTY, EMPTY, EMPTY, ACCEL_P, EMPTY, EMPTY, EMPTY},
                {BFT, LAT_VEL_P, ADV_VEL_S, EMPTY, ESCAPE_P, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT, LAT_VEL_P, EMPTY, ESCAPE_P, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT, LAT_VEL_P, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT, LAT_VEL_P, ADV_VEL_S, ESCAPE_P, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT, LAT_VEL, ADV_VEL_S, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT, EMPTY, EMPTY, ESCAPE_S, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT, EMPTY, EMPTY, ESCAPE_P, ESCAPE_S, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT, EMPTY, EMPTY, EMPTY, ESCAPE, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {BFT, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY}
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
