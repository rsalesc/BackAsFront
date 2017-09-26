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

/**
 * CREDITS: www.java-gaming.com, user Icecore
 */
public class FastMath {
    private static final int Size_Ac = 1000;
    private static final int Size_Ar = Size_Ac + 1;
    private static final float Pi = (float) Math.PI;
    private static final float Pi_H = Pi / 2;

    private static final float[] Atan2 = new float[Size_Ar];
    private static final float[] Atan2_PM = new float[Size_Ar];
    private static final float[] Atan2_MP = new float[Size_Ar];
    private static final float[] Atan2_MM = new float[Size_Ar];

    private static final float[] Atan2_R = new float[Size_Ar];
    private static final float[] Atan2_RPM = new float[Size_Ar];
    private static final float[] Atan2_RMP = new float[Size_Ar];
    private static final float[] Atan2_RMM = new float[Size_Ar];
    private static final int SIN_BITS, SIN_MASK, SIN_COUNT;
    private static final float radFull, radToIndex;
    private static final float degFull, degToIndex;
    private static final float[] sin, cos;

    static {
        for (int i = 0; i <= Size_Ac; i++) {
            double d = (double) i / Size_Ac;
            double x = 1;
            double y = x * d;
            float v = (float) Math.atan2(y, x);
            Atan2[i] = v;
            Atan2_PM[i] = Pi - v;
            Atan2_MP[i] = -v;
            Atan2_MM[i] = -Pi + v;

            Atan2_R[i] = Pi_H - v;
            Atan2_RPM[i] = Pi_H + v;
            Atan2_RMP[i] = -Pi_H + v;
            Atan2_RMM[i] = -Pi_H - v;
        }
    }

    static {
        SIN_BITS = 12;
        SIN_MASK = ~(-1 << SIN_BITS);
        SIN_COUNT = SIN_MASK + 1;

        radFull = (float) (Math.PI * 2.0);
        degFull = (float) (360.0);
        radToIndex = SIN_COUNT / radFull;
        degToIndex = SIN_COUNT / degFull;

        sin = new float[SIN_COUNT];
        cos = new float[SIN_COUNT];

        for (int i = 0; i < SIN_COUNT; i++) {
            sin[i] = (float) Math.sin((i + 0.5f) / SIN_COUNT * radFull);
            cos[i] = (float) Math.cos((i + 0.5f) / SIN_COUNT * radFull);
        }

        // Four cardinal directions (credits: Nate)
        for (int i = 0; i < 360; i += 90) {
            sin[(int) (i * degToIndex) & SIN_MASK] = (float) Math.sin(i * Math.PI / 180.0);
            cos[(int) (i * degToIndex) & SIN_MASK] = (float) Math.cos(i * Math.PI / 180.0);
        }
    }

    public static final float sinClosedFormula(float radians) {
        float offset = (float) (radians - R.DOUBLE_PI * Math.floor((radians + R.PI) / R.DOUBLE_PI));
        if (Math.abs(offset) > R.HALF_PI)
            return -xsin((float) ((offset - Math.signum(offset) * R.PI) / R.HALF_PI));
        else
            return xsin((float) (offset / R.HALF_PI));
    }

    public static final float atan2(float y, float x) {
        if (y < 0) {
            if (x < 0) {
                //(y < x) because == (-y > -x)
                if (y < x) {
                    return Atan2_RMM[(int) (x / y * Size_Ac)];
                } else {
                    return Atan2_MM[(int) (y / x * Size_Ac)];
                }
            } else {
                y = -y;
                if (y > x) {
                    return Atan2_RMP[(int) (x / y * Size_Ac)];
                } else {
                    return Atan2_MP[(int) (y / x * Size_Ac)];
                }
            }
        } else {
            if (x < 0) {
                x = -x;
                if (y > x) {
                    return Atan2_RPM[(int) (x / y * Size_Ac)];
                } else {
                    return Atan2_PM[(int) (y / x * Size_Ac)];
                }
            } else {
                if (y > x) {
                    return Atan2_R[(int) (x / y * Size_Ac)];
                } else {
                    return Atan2[(int) (y / x * Size_Ac)];
                }
            }
        }
    }

    private static float xsin(float x) {
        float x2 = x * x;
        return ((((.00015148419f * x2
                - .00467376557f) * x2
                + .07968967928f) * x2
                - .64596371106f) * x2
                + 1.57079631847f) * x;
    }

    private static double fastAtan2(double y, double x) {
        if (x == 0.0f) {
            if (y > 0.0f) {
                return R.HALF_PI;
            }
            if (y == 0.0f) {
                return 0.0f;
            }
            return -R.HALF_PI;
        }

        final double atan;
        final double z = y / x;
        if (Math.abs(z) < 1.0f) {
            atan = z / (1.0f + 0.28f * z * z);
            if (x < 0.0f) {
                return (y < 0.0f) ? atan - R.PI : atan + R.PI;
            }
            return atan;
        } else {
            atan = R.HALF_PI - z / (z * z + 0.28f);
            return (y < 0.0f) ? atan - R.PI : atan;
        }
    }

    public static final float sin(float rad) {
        return sin[(int) (rad * radToIndex) & SIN_MASK];
    }

    public static final float cos(float rad) {
        return cos[(int) (rad * radToIndex) & SIN_MASK];
    }
}
