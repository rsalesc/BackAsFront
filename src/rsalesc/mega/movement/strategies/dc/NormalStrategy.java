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

/**
 * Created by Roberto Sales on 21/08/17.
 */
public class NormalStrategy extends Strategy {
    @Override
    public double[] getQuery(TargetingLog f) {
        return new double[]{
                Math.max(f.bft() / 80, 1),
                Math.max(Math.abs(f.lateralVelocity) / 8, 1),
                Math.max((f.advancingVelocity + 8) / 16.0, 1),
                (f.accel + 1) * 0.5,
                R.constrain(0, f.getPreciseMea().max / f.getTraditionalMea(), 1),
                R.constrain(0, -f.getPreciseMea().min / f.getTraditionalMea(), 1),
                1.0 / (1.0 + 2*f.timeDecel),
                1.0 / (1.0 + 2*f.timeRevert),
        };
    }

    @Override
    public double[] getWeights() {
        return new double[]{6, 5, 4, 2, 4, 1, 1, 1};
    }
}
