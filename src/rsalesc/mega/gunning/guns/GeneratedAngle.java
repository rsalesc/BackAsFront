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

package rsalesc.mega.gunning.guns;

import rsalesc.mega.utils.TargetingLog;

/**
 * Created by Roberto Sales on 19/09/17.
 */
public class GeneratedAngle implements Comparable<GeneratedAngle> {
    public double weight;
    public final double angle;
    public final double distance;

    public GeneratedAngle(double weight, double angle, double distance) {
        this.weight = weight;
        this.angle = angle;
        this.distance = distance;
    }

    @Override
    public int compareTo(GeneratedAngle o) {
        return (int) Math.signum(weight - o.weight);
    }
}
