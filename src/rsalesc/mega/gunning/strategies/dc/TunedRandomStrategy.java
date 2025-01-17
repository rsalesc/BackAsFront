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

package rsalesc.mega.gunning.strategies.dc;

import rsalesc.mega.learning.sgd.SgdGunRandomStrategy;

/**
 * Created by Roberto Sales on 15/09/17.
 */
public class TunedRandomStrategy extends SgdGunRandomStrategy {
    @Override
    public double[] getWeights() {
        return new double[]{2.11, 6.61, 6.61, 10, 7.68, 9.62, 3.05, 3.68, 7.22, 0, 0};
    }

    @Override
    public double[] getParams() {
        return new double[]{0, 0};
    }
}
