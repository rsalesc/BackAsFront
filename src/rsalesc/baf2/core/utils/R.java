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

import jk.math.FastTrig;
import robocode.util.Utils;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.core.utils.geometry.Point;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
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
    public static final boolean FAST_MATH = true;
    public static final LinkedList<Boolean> fm = new LinkedList<>();

    public static double sin(double radians) {
        if (!FAST_MATH)
            return Math.sin(radians);
//        return FastMath.sin((float) radians);
        return FastTrig.sin(radians);
    }

    public static double cos(double radians) {
        if (!FAST_MATH)
            return Math.cos(radians);
//        return FastMath.cos((float) radians);
        return FastTrig.cos(radians);
    }

    public static double asin(double x) {
        if(!FAST_MATH)
            return Math.asin(x);
        return FastTrig.asin(x);
    }

    public static double acos(double x) {
        if(!FAST_MATH)
            return Math.acos(x);
        return FastTrig.acos(x);
    }

    public static double atan(double x) {
        if(!FAST_MATH)
            return Math.atan(x);
        return FastTrig.atan(x);
    }

    public static double atan2(double y, double x) {
        if (!FAST_MATH)
            return Math.atan2(y, x);
//        return FastMath.atan2((float) y, (float) x);
        return FastTrig.atan2(y, x);
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
        return R.pow(2, -0.5 * 1.44269504089 * x * x);
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

    public static double marginOfError(double p, int samples) {
        return R.sqrt(p*(1-p)/samples);
    }

    public static boolean nearOrBetween(double min, double x, double max, double error) {
        return min - error < x && x < max + error;
    }

    public static boolean isNear(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    public static boolean isNear(double a, double b, double error) {
        return Math.abs(a - b) < error;
    }

    public static double transposeAngle(double angle) {
        return R.normalAbsoluteAngle(-R.normalRelativeAngle(angle) + R.HALF_PI);
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
        return formattedDouble(v * 100) + " %";
    }

    public static String formattedDouble(double v) {
        return PERCENTAGE_FORMATTER.format(v).replace(",", ".");
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

    public static <T> T[] concat(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static <T> ArrayList<ArrayList<T>> subsequences(List<T> list) {
        ArrayList<T> base = new ArrayList<>();
        ArrayList<ArrayList<T>> res = new ArrayList<>();
        base.addAll(list);

        int n = base.size();

        for(long i = 1; i < (1L << n); i++) {
            ArrayList<T> cur = new ArrayList<>();
            for(int j = 0; j < n; j++) {
                if(((i>>j)&1) > 0) {
                    cur.add(base.get(j));
                }
            }

            res.add(cur);
        }

        return res;
    }

    public static double sqr(double x) {
        return x * x;
    }

    public static long combinatorialSqrt(long x) {
        long res = 1;
        while(res * (res - 1) / 2 <= x)
            res++;

        return res - 1;
    }

    public static boolean isNearAngle(double heading, double heading1) {
        return R.isNear(R.normalRelativeAngle(heading - heading1), 0);
    }

    public static double normalRelativeAngle(double x) {
        if(!FAST_MATH)
            return Utils.normalRelativeAngle(x);
        return FastTrig.normalRelativeAngle(x);
    }

    public static double normalAbsoluteAngle(double x) {
        if(!FAST_MATH)
            return Utils.normalAbsoluteAngle(x);
        return FastTrig.normalAbsoluteAngle(x);
    }
}
