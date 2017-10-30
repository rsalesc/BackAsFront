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

package rsalesc.mega.utils.stats;

import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.Range;

import java.util.Arrays;

/**
 * Created by Roberto Sales on 12/09/17.
 */
public class GuessFactorStats extends Stats {
    public static final int BUCKET_COUNT = 101;
    public static final int BUCKET_MID = (BUCKET_COUNT - 1) / 2;

    public GuessFactorStats(KernelDensity kernel) {
        super(BUCKET_COUNT, kernel);
    }

    public GuessFactorStats(BinKernelDensity binKernel) {
        super(BUCKET_COUNT, binKernel);
    }

    public GuessFactorStats(double[] buffer, KernelDensity kernel) {
        super(buffer, kernel);
        if (buffer.length != BUCKET_COUNT)
            throw new IllegalStateException();
    }

    public static GuessFactorStats merge(GuessFactorStats[] sts, double[] weights) {
        int size = BUCKET_COUNT;
        double[] buffer = new double[size];
        for (int i = 0; i < sts.length; i++) {
            if (sts[i] == null)
                continue;
            GuessFactorStats normalized = (GuessFactorStats) (sts[i].clone());
            normalized.normalize();
            for (int j = 0; j < size; j++) {
                buffer[j] += normalized.get(j) * weights[i];
            }
        }

        return new GuessFactorStats(buffer, null);
    }

    @Override
    public Object clone() {
        double[] newBuffer = Arrays.copyOf(buffer, buffer.length);
        GuessFactorStats res = new GuessFactorStats(newBuffer, kernel);
        return res;
    }

    public int getBucket(double alpha) {
        int index = (int) (BUCKET_MID + alpha * BUCKET_MID);
        return R.constrain(0, index, BUCKET_COUNT - 1);
    }

    public double getGuessFactor(int index) {
        return R.constrain(-1, (double) (index - BUCKET_MID) / BUCKET_MID, +1);
    }

    public int getBestBucket(Range range) {
        double acc = get(0);
        int best = 0;
        for (int i = 1; i < BUCKET_COUNT; i++) {
            if (range.isNearlyContained(getGuessFactor(i)) && get(i) > acc) {
                acc = get(i);
                best = i;
            }
        }

        if (R.isNear(get(BUCKET_MID), acc, 1e-12))
            return BUCKET_MID;

        return best;
    }

    public int getBestBucket() {
        return getBestBucket(new Range(-1.0, 1.0));
    }

    public double getBestGuessFactor() {
        return getGuessFactor(getBestBucket());
    }

    public double getBestGuessFactor(Range range) {
        return getGuessFactor(getBestBucket(range));
    }

    public void logGuessFactor(double gf, double weight, double bandwidth) {
        add(getBucket(gf), weight, (int) Math.round(bandwidth * BUCKET_MID));
    }

    public void logGuessFactor(double gf, double weight) {
        add(getBucket(gf), weight);
    }

    public void logGuessFactor(double gf) {
        logGuessFactor(gf, 1.0);
    }

    public double getValue(double gf) {
        return getValueFromBucket(getBucket(gf));
    }

    public double getValueFromBucket(int index) {
        return get(index);
    }

    public double sqr(double x) {
        return x * x;
    }
}
