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

package rsalesc.mega.movement;

import rsalesc.baf2.core.utils.geometry.Point;

/**
 * Created by Roberto Sales on 24/07/17.
 */
public class DangerPoint extends Point implements Comparable<DangerPoint> {
    private double danger;

    public DangerPoint(double x, double y, double danger) {
        super(x, y);
        this.danger = danger;
    }

    public DangerPoint(Point point, double danger) {
        this(point.x, point.y, danger);
    }

    public double getDanger() {
        return danger;
    }

    public void setDanger(double danger) {
        this.danger = danger;
    }

    @Override
    public int compareTo(DangerPoint o) {
        return (int) Math.signum(this.danger - o.danger);
    }
}
