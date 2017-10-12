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

package rsalesc.melee.utils.stats;

import robocode.util.Utils;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.core.utils.geometry.Range;
import rsalesc.mega.utils.stats.BinKernelDensity;
import rsalesc.mega.utils.stats.KernelDensity;
import rsalesc.mega.utils.stats.Stats;

import java.util.Arrays;

/**
 * Created by Roberto Sales on 10/10/17.
 */
public class CircularGuessFactorStats extends CircularStats {
    public static final int BUCKET_COUNT = 240;

    public CircularGuessFactorStats(KernelDensity kernel) {
        super(BUCKET_COUNT, kernel);
    }

    public CircularGuessFactorStats(BinKernelDensity binKernel) {
        super(BUCKET_COUNT, binKernel.getKernelDensity());
        setBinKernel(binKernel);
    }

    public CircularGuessFactorStats(double[] buffer, KernelDensity kernel) {
        super(buffer, kernel);
        if (buffer.length != BUCKET_COUNT)
            throw new IllegalStateException();
    }

    public static CircularGuessFactorStats merge(CircularGuessFactorStats[] sts, double[] weights) {
        int size = BUCKET_COUNT;
        double[] buffer = new double[size];
        for (int i = 0; i < sts.length; i++) {
            if (sts[i] == null)
                continue;
            CircularGuessFactorStats normalized = (CircularGuessFactorStats) (sts[i].clone());
            normalized.normalize();
            for (int j = 0; j < size; j++) {
                buffer[j] += normalized.get(j);
            }
        }

        return new CircularGuessFactorStats(buffer, null);
    }

    public static CircularGuessFactorStats mergeSum(CircularGuessFactorStats[] sts, double[] weights) {
        int size = BUCKET_COUNT;
        double[] buffer = new double[size];
        for (int i = 0; i < sts.length; i++) {
            if (sts[i] == null)
                continue;
            CircularGuessFactorStats normalized = (CircularGuessFactorStats) (sts[i].clone());
            normalized.normalizeSum();
            for (int j = 0; j < size; j++) {
                buffer[j] += normalized.get(j);
            }
        }

        return new CircularGuessFactorStats(buffer, null);
    }

    @Override
    public Object clone() {
        double[] newBuffer = Arrays.copyOf(buffer, buffer.length);
        CircularGuessFactorStats res = new CircularGuessFactorStats(newBuffer, kernel);
        return res;
    }

    public int getBucket(double angle) {
        int index = (int) (angle / R.DOUBLE_PI * BUCKET_COUNT);
        return R.constrain(0, index, BUCKET_COUNT - 1);
    }

    public double getAngle(int index) {
        return Utils.normalAbsoluteAngle((double) index / BUCKET_COUNT * R.DOUBLE_PI);
    }

    public int getBestBucket(AngularRange range) {
        double acc = Double.NEGATIVE_INFINITY;
        int best = 0;
        for (int i = 0; i < BUCKET_COUNT; i++) {
            if (range.isAngleNearlyContained(getAngle(i)) && get(i) > acc) {
                acc = get(i);
                best = i;
            }
        }

        return best;
    }

    public int getBestBucket() {
        return getBestBucket(new AngularRange(0, -R.PI, +R.PI));
    }

    public double getBestAngle() {
        return getAngle(getBestBucket());
    }

    public double getBestAngle(AngularRange range) {
        return getAngle(getBestBucket(range));
    }

    public void logAngle(double angle, double weight, double bandwidth) {
        add(getBucket(angle), weight, (int) Math.round(bandwidth * BUCKET_COUNT / R.DOUBLE_PI));
    }

    public void logAngle(double angle, double weight) {
        add(getBucket(angle), weight);
    }

    public void logAngle(double angle) {
        logAngle(angle, 1.0);
    }

    public double getValue(double angle) {
        return getValueFromBucket(getBucket(angle));
    }

    public double getValueFromBucket(int index) {
        return get(index);
    }

    public double sqr(double x) {
        return x * x;
    }
}
