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

package rsalesc.mega.movement.strategies.dc;

import rsalesc.baf2.core.utils.R;
import rsalesc.mega.utils.Strategy;
import rsalesc.mega.utils.TargetingLog;

public class KnightBaseStrategy extends Strategy {
    @Override
    public double[] getQuery(TargetingLog f) {
        return new double[]{
                Math.abs(f.lateralVelocity) / 8,
                (f.advancingVelocity + 8) / 16,
                Math.min(f.bft() / 81, 1),
                (f.accel + 1) / 2,
                R.constrain(0, f.getPreciseMea().max / f.getTraditionalMea(), 1),
                R.constrain(0, -f.getPreciseMea().min / f.getTraditionalMea(), 1),
                1.0 / (1.0 + 2*f.timeDecel),
                1.0 / (1.0 + 2*f.timeRevert),
                Math.min(f.displaceLast10 / 80, 1)
        };
    }

    @Override
    public double[] getWeights() {
        return new double[]{7.5, 3.75, 3, 2.5, 4.5, 1.75, 2, 2, 2};
    }
}
