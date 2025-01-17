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

package rsalesc.mega.movement.strategies.vcs;

/**
 * Created by Roberto Sales on 23/08/17.
 */
public class Slices {
    public static double[] EMPTY = {Double.POSITIVE_INFINITY};

    public static double[] BFT_S = new double[]{20, 40, 60};
    public static double[] BFT = new double[]{10, 30, 50, 70};
    public static double[] BFT_P = new double[]{8, 16, 24, 32, 40, 48, 56, 64, 72};

    public static double[] LAT_VEL_S = new double[]{2, 4, 6};
    public static double[] LAT_VEL = new double[]{1, 3, 5, 7};
    public static double[] LAT_VEL_P = new double[]{0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5};

    public static double[] ADV_VEL_S = new double[]{-4, 0, +4};
    public static double[] ADV_VEL = new double[]{-6, -3, 0, +3, +6};
    public static double[] ADV_VEL_P = new double[]{-7, -5.5, -4, -2, 0, +2, +4, +5.5, +7};

    public static double[] ACCEL_S = new double[]{0.0};
    public static double[] ACCEL = new double[]{-0.5, +0.5};
    public static double[] ACCEL_P = new double[]{-2.1, -1.9, 0.9, 1.1};

    public static double[] ESCAPE_S = new double[]{0.4, 1.0};
    public static double[] ESCAPE = new double[]{0.3, 0.66, 0.99};
    public static double[] ESCAPE_P = new double[]{0.2, 0.4, 0.6, 0.8, 1.0, 1.15};

    public static double[] RUN_S = new double[]{0.33, 0.66};
    public static double[] RUN = new double[]{0.2, 0.4, 0.6, 0.8};
    public static double[] RUN_P = new double[]{0.05, 0.2, 0.35, 0.5, 0.65, 0.8, 0.95};

    public static double[] DECEL_S = new double[]{0.33, 0.66, 1.33};
    public static double[] DECEL = new double[]{0.2, 0.4, 0.6, 0.8, 1.2, 1.6};
    public static double[] DECEL_P = new double[]{0.05, 0.2, 0.35, 0.5, 0.8, 1.0, 1.2, 1.4, 1.8};

    public static double[] REVERT_S = new double[]{0.33, 0.66, 1.33};
    public static double[] REVERT = new double[]{0.2, 0.4, 0.6, 0.8, 1.2, 1.6};
    public static double[] REVERT_P = new double[]{0.05, 0.2, 0.35, 0.5, 0.8, 1.0, 1.2, 1.4, 1.8};

    public static double[] D10_S = new double[]{20, 40, 60};
    public static double[] D10 = new double[]{10, 20, 30, 40, 50, 60, 70};
    public static double[] D10_P = new double[]{5, 15, 25, 35, 45, 55, 65, 75};
}
