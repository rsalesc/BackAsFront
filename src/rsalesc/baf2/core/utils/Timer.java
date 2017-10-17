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

import java.text.DecimalFormat;

/**
 * Created by Roberto Sales on 02/10/17.
 */
public class Timer {
    private long acc = 0;
    private Long lastStart = null;

    public void start() {
        lastStart = System.nanoTime();
    }

    public long pause() {
        long newAcc = spent();
        long res = newAcc - acc;
        acc = newAcc;
        lastStart = null;
        return res;
    }

    public long spent() {
        if(lastStart != null)
            return acc + System.nanoTime() - lastStart;
        return acc;
    }

    public long stop() {
        long res = spent();
        lastStart = null;
        acc = 0;

        return res;
    }

    public static long getInMilliseconds(long delta) {
        return delta / (long) 1e6;
    }

    public static long getInSeconds(long delta) {
        return delta / (long) 1e9;
    }

    public static long getMinutes(long delta) {
        return delta / ((long) 1e9 * 60);
    }

    public static long getSeconds(long delta) {
        return delta / (long) 1e9 % 60;
    }

    public static String getFormattedMinutes(long delta) {
        return String.format("%d:%02d", getMinutes(delta), getSeconds(delta));
    }

    public static long getEta(long spent, int experiments, int remaining) {
        return (long) ((double) spent / experiments * remaining);
    }
}
