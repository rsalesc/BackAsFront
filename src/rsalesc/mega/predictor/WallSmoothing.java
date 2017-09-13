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

package rsalesc.mega.predictor;

import robocode.util.Utils;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.core.utils.geometry.Point;

/**
 * Created by Roberto Sales on 25/07/17.
 */
public abstract class WallSmoothing {
    private static final double[] NORMAL_ANGLES = new double[]{
            0,
            R.HALF_PI,
            R.PI,
            R.PI + R.HALF_PI
    };

    public static double naive(AxisRectangle shrinkedField, double stick, Point source, double angle, int direction) {
//        if(field.contains(source.project(angle, WALL_STICK)))
//            return angle;
//
//        double l = 0, r = R.PI;
//        while(l+0.06 < r) {
//            double mid = (l+r)/2;
//            if(field.contains(source.project(angle + mid * direction, WALL_STICK)))
//                r = mid;
//            else l = mid;
//        }
//
//        if(field.contains(source.project(angle + l * direction, WALL_STICK)))
//            return angle + l * direction;

//        while(!shrinkedField.strictlyContains(source.project(angle, stick)))
//            angle += 0.05*direction;
//
//        return angle;

        return pythagorean(shrinkedField, stick, source, angle, direction);
    }

    public static double pythagorean(AxisRectangle shrinkedField, double stick, Point source, double angle, int direction) {
        if (direction == 0)
            throw new IllegalStateException();

        Point projected = source.project(angle, stick);

        int bestIndex;
        if (projected.x > shrinkedField.maxx)
            bestIndex = 1;
        else if (projected.y < shrinkedField.miny)
            bestIndex = 2;
        else if (projected.x < shrinkedField.minx)
            bestIndex = 3;
        else if (projected.y > shrinkedField.maxy)
            bestIndex = 0;
        else
            return angle;

        for (int j = 0; j < 2; j++) {
            int i = bestIndex + j * direction;
            if (i < 0) i += 4;
            else if (i >= 4) i -= 4;

            if (Math.abs(Utils.normalRelativeAngle(NORMAL_ANGLES[i] - angle)) > R.HALF_PI)
                continue;

            Point base = new Point(0, 0);
            double a;
            int sig;

            if (i == 0) {
                a = shrinkedField.maxy - source.y;
                base.x = source.x;
                base.y = shrinkedField.maxy;
                sig = 1;
            } else if (i == 1) {
                a = shrinkedField.maxx - source.x;
                base.x = shrinkedField.maxx;
                base.y = source.y;
                sig = -1;
            } else if (i == 2) {
                a = source.y - shrinkedField.miny;
                base.x = source.x;
                base.y = shrinkedField.miny;
                sig = -1;
            } else {
                a = source.x - shrinkedField.minx;
                base.x = shrinkedField.minx;
                base.y = source.y;
                sig = 1;
            }

            a = Math.max(a, 0);

            double bSqr = stick * stick - a * a;
            if (bSqr < 0)
                continue;

            if ((i & 1) == 0) {
                base.x += sig * direction * R.sqrt(bSqr);
            } else {
                base.y += sig * direction * R.sqrt(bSqr);
            }

            projected = base;
        }

        return Physics.absoluteBearing(source, projected);
    }
}
