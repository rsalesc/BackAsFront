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

package rsalesc.mega.movement.distancing;


import rsalesc.baf2.core.utils.R;

/**
 * Created by Roberto Sales on 25/08/17.
 */
public class DefaultSurfingDistancer extends SurfingDistancer {
    @Override
    public double getPerpendiculator(double distance) {
        return R.HALF_PI +
                R.constrain(-1.5,
                        (distance - DESIRED_DISTANCE) / DESIRED_DISTANCE * 0.6,
                        +1.5);
    }

    @Override
    public boolean shouldSurf(double distance) {
        return true;
    }
}
