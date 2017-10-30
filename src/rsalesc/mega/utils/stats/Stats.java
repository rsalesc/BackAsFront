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

import java.util.Arrays;

/**
 * Created by Roberto Sales on 12/09/17.
 */
public class Stats {
    protected final double[] buffer;
    protected KernelDensity kernel;
    protected BinKernelDensity binKernel;

    public Stats(int size) {
        buffer = new double[size];
        kernel = null;
    }

    public Stats(int size, KernelDensity kernel) {
        buffer = new double[size];
        this.kernel = kernel;
    }

    public Stats(double[] buffer, KernelDensity kernel) {
        this.buffer = buffer;
        this.kernel = kernel;
    }

    public Stats(int size, BinKernelDensity kernel) {
        buffer = new double[size];
        this.kernel = kernel.getKernelDensity();
        this.binKernel = kernel;
    }

    public Stats(double[] buffer, BinKernelDensity kernel) {
        this.buffer = buffer;
        this.kernel = kernel.getKernelDensity();
        this.binKernel = kernel;
    }

    public KernelDensity getKernel() {
        return kernel;
    }

    public void setKernel(KernelDensity kernel) {
        this.kernel = kernel;
    }

    public void setBinKernel(KernelDensity kernel, double bandwidth) {
        this.kernel = kernel;
        this.binKernel = new BinKernelDensity(kernel, bandwidth);
    }

    public void setBinKernel(BinKernelDensity binKernel) {
        this.kernel = binKernel.getKernelDensity();
        this.binKernel = binKernel;
    }

    public double[] getBuffer() {
        return buffer;
    }

    public int size() {
        return buffer.length;
    }

    public double get(int i) {
        return buffer[i];
    }

    public void add(int i, double x) {
        if(binKernel == null)
            buffer[i] += x;
        else {
            double density;
            int j = i - 1;
            while (j > 0 && (density = binKernel.getBinDensity(i - j)) > 0) {
                buffer[j--] += density * x;
            }

            j = i;
            while (j < buffer.length && (density = binKernel.getBinDensity(i - j)) > 0) {
                buffer[j++] += density * x;
            }
        }
    }

    public void add(int i, double x, int bandwidth) {
        if (kernel == null)
            buffer[i] += x;
        else {
            double density;
            int j = i - 1;
            while (j > 0 && (density = kernel.getDensity(i - j, bandwidth)) > 0) {
                buffer[j--] += density * x;
            }

            j = i;
            while (j < buffer.length && (density = kernel.getDensity(i - j, bandwidth)) > 0) {
                buffer[j++] += density * x;
            }
        }
    }

    public void normalize() {
        double max = 0;
        for (int i = 0; i < buffer.length; i++)
            max = Math.max(max, Math.abs(buffer[i]));

        if (!R.isNear(max, 0)) {
            for (int i = 0; i < buffer.length; i++) {
                buffer[i] /= max;
            }
        }
    }

    public void clear() {
        Arrays.fill(buffer, 0);
    }
}
