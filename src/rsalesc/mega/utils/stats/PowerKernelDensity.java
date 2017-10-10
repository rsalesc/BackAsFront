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

package rsalesc.mega.utils.stats;

/**
 * Created by Roberto Sales on 08/10/17.
 */
public class PowerKernelDensity extends KernelDensity {
    private final double ratio;

    public PowerKernelDensity(double ratio) {
        this.ratio = ratio;
    }

    @Override
    protected double _getDensity(double x) {
        return 1.0 / (ratio * x * x + 1);
    }

    @Override
    public double cutoff() {
        return Double.POSITIVE_INFINITY;
    }
}
