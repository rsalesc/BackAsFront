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

package rsalesc.baf2.core.utils;

import robocode.util.Utils;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.core.utils.geometry.Point;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Roberto Sales on 22/07/17.
 */
public class R {
    public static final double PI = Math.acos(-1);
    public static final double HALF_PI = PI / 2;
    public static final double DOUBLE_PI = PI * 2;
    public static final double EPSILON = 1e-9;
    private static final DecimalFormat PERCENTAGE_FORMATTER = new DecimalFormat("#.##");
    public static boolean FAST_MATH = false;

    public static double sin(double radians) {
        if (!FAST_MATH)
            return Math.sin(radians);
        return FastMath.sin((float) radians);
    }

    public static double cos(double radians) {
        if (!FAST_MATH)
            return Math.cos(radians);
        return FastMath.cos((float) radians);
    }

    public static double asin(double x) {
        return Math.asin(x);
    }

    public static double acos(double x) {
        return Math.acos(x);
    }

    public static double atan(double x) {
        return Math.atan(x);
    }

    public static double atan2(double y, double x) {
        if (!FAST_MATH)
            return Math.atan2(y, x);
        return FastMath.atan2((float) y, (float) x);
    }

    public static double tan(double radians) {
        return Math.atan(radians);
    }

    public static double abs(double x) {
        return Math.abs(x);
    }

    public static double sqrt(double x) {
        return Math.sqrt(x);
    }

    public static double exp(double val) {
        return Math.exp(val);
    }

    public static double pow(final double a, final double b) {
        return Math.pow(a, b);
    }

    public static double gaussKernel(double x) {
        return R.pow(1.65, -0.5 * x);
    }

    public static double cubicKernel(double x) {
        return Math.max(0, 1.0 - Math.abs(x * x * x));
    }

    public static double logisticFunction(double x, double x0, double k) {
        return 1.0 / (1.0 + R.exp(-k * (x - x0)));
    }

    public static double logisticFunction(double x) {
        return logisticFunction(x, 0.0, 1.0);
    }

    public static double constrain(double min, double x, double max) {
        return Math.max(min, Math.min(max, x));
    }

    public static int constrain(int min, int x, int max) {
        return Math.max(min, Math.min(max, x));
    }

    public static boolean isBetween(int min, int x, int max) {
        return min <= x && x <= max;
    }

    public static boolean isBetween(long min, long x, long max) {
        return min <= x && x <= max;
    }

    public static boolean nearOrBetween(double min, double x, double max) {
        return min - EPSILON < x && x < max + EPSILON;
    }

    public static boolean isNear(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    public static boolean isNear(double a, double b, double error) {
        return Math.abs(a - b) < error;
    }

    public static double transposeAngle(double angle) {
        return Utils.normalAbsoluteAngle(-Utils.normalRelativeAngle(angle) + R.HALF_PI);
    }

    public static double getWallEscape(AxisRectangle field, Point point, double heading) {
        return Math.min(getVerticalEscapeAngle(field, point, heading),
                getVerticalEscapeAngle(field.transposed(), point.transposed(), R.transposeAngle(heading)));
    }

    public static double getVerticalEscapeAngle(AxisRectangle field, Point point, double heading) {
        if (R.isNear(heading, R.HALF_PI) || R.isNear(heading, R.HALF_PI * 3))
            return Double.POSITIVE_INFINITY;
        else if (heading < R.HALF_PI || heading > 3 * R.HALF_PI)
            return (field.getHeight() - point.y) / R.cos(heading);
        else
            return -point.y / R.cos(heading);
    }

    public static String formattedPercentage(double v) {
        return PERCENTAGE_FORMATTER.format(v * 100).replace(",", ".") + " %";
    }

    public static double zeroNan(double v) {
        if (Double.isNaN(v))
            return 0.0;
        return v;
    }

    public static <T> T getLast(List<T> col) {
        if (col.size() == 0)
            return null;
        return col.get(col.size() - 1);
    }

    public static <T> T getFirst(List<T> col) {
        if (col.size() == 0)
            return null;
        return col.get(0);
    }

    public static boolean strictlyBetween(double min, double x, double max) {
        return R.nearOrBetween(min, x, max) && !R.isNear(x, min) && !R.isNear(x, max);
    }

    public static double basicSurferRounding(double x) {
        return Math.max(Math.min(x, 0.15), Math.floor(x / 0.05) * 0.05 - (x < 0.05 ? 0.05 : 0));
    }
}
