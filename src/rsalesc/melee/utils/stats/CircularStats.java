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

import rsalesc.baf2.core.utils.R;
import rsalesc.mega.utils.stats.BinKernelDensity;
import rsalesc.mega.utils.stats.KernelDensity;

import java.util.Arrays;

/**
 * Created by Roberto Sales on 12/09/17.
 */
public class CircularStats {
    protected final double[] buffer;
    protected KernelDensity kernel;
    protected BinKernelDensity binKernel;

    public CircularStats(int size) {
        buffer = new double[size];
        kernel = null;
    }

    public CircularStats(int size, KernelDensity kernel) {
        buffer = new double[size];
        this.kernel = kernel;
    }

    public CircularStats(double[] buffer, KernelDensity kernel) {
        this.buffer = buffer;
        this.kernel = kernel;
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
            int j = (i - 1 + buffer.length) % buffer.length;
            int ptr = i - 1;

            while (j != i && (density = binKernel.getBinDensity(i - ptr)) > 0) {
                buffer[j--] += density * x;
                ptr--;

                if(j < 0) j += buffer.length;
            }

            buffer[i] += binKernel.getBinDensity(0) * x;

            j = (i + 1) % buffer.length;
            ptr = i + 1;
            while (j != i && (density = binKernel.getBinDensity(i - ptr)) > 0) {
                buffer[j++] += density * x;
                ptr++;

                if(j >= buffer.length) j -= buffer.length;
            }
        }
    }

    public void add(int i, double x, int bandwidth) {
        if (kernel == null)
            buffer[i] += x;
        else {
            double density;
            int j = (i - 1 + buffer.length) % buffer.length;
            int ptr = i - 1;
            while (j != i && (density = kernel.getDensity(i - ptr, bandwidth)) > 0) {
                buffer[j--] += density * x;
                ptr--;

                if(j < 0) j += buffer.length;
            }

            buffer[i] += kernel.getDensity(0) * x;

            j = (i + 1) % buffer.length;
            ptr = i + 1;
            while (j != i && (density = kernel.getDensity(i - ptr, bandwidth)) > 0) {
                buffer[j++] += density * x;
                ptr++;

                if(j >= buffer.length) j -= buffer.length;
            }
        }
    }

    public void normalize() {
        double max = 1e-20;
        for (int i = 0; i < buffer.length; i++)
            max = Math.max(max, Math.abs(buffer[i]));

        for (int i = 0; i < buffer.length; i++) {
            buffer[i] /= max;
        }
    }

    public void normalizeSum() {
        double sum = 1e-20;
        for(int i = 0; i < buffer.length; i++)
            sum += buffer[i];

        for(int i = 0; i < buffer.length; i++)
            buffer[i] /= sum;
    }

    public void scale(double x) {
        for(int i = 0; i < buffer.length; i++)
            buffer[i] *= x;
    }

    public void clear() {
        Arrays.fill(buffer, 0);
    }
}
