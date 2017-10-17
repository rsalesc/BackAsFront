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

package rsalesc.mega.learning.benchmark;

import rsalesc.mega.learning.genetic.BaseAdaptiveStrategy;
import rsalesc.mega.learning.genetic.GeneticStrategy;

/**
 * Created by Roberto Sales on 04/10/17.
 */
public class HandAdaptiveStrategy extends BaseAdaptiveStrategy {
    @Override
    public double[] getParams() {
        return new double[]{2.0, 2.0};
    }

    @Override
    public double[] getWeights() {
        return new double[]{2.0, 0.3, 2.0, 1.5, 1.8, 0.5, 1.0, 1.0};
    }
}
