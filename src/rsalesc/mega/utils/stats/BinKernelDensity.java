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

/**
 * Created by Roberto Sales on 01/10/17.
 */
public class BinKernelDensity {
    private final KernelDensity density;
    private double[] profile = new double[]{0};
    private int profileLength = 1;

    private final double bandwidth;

    public BinKernelDensity(KernelDensity density, double bandwidth) {
        this.density = density;
        this.bandwidth = bandwidth;
    }

    private void ensureProfile(int length) {
        if(length > profileLength) {
            double[] newProfile = new double[2*length - 1];
            System.arraycopy(profile, 0, newProfile,
                    length - profileLength, 2*profileLength - 1);

            for(int i = 0; i < length - profileLength; i++)
                newProfile[i] = density.getDensity(i - length, bandwidth);
            for(int i = length - 1 + profileLength; i < 2*length-1; i++)
                newProfile[i] = density.getDensity(i - length, bandwidth);

            profile = newProfile;
            profileLength = length;
        }
    }

    public double getBinDensity(int diff) {
        ensureProfile(Math.abs(diff) + 1);
        return profile[diff + profileLength - 1];
    }

    public KernelDensity getKernelDensity() {
        return density;
    }
}
