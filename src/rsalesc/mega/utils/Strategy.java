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

package rsalesc.mega.utils;

import rsalesc.baf2.core.utils.R;

import java.util.Arrays;

/**
 * Created by Roberto Sales on 13/08/17.
 */
public abstract class Strategy {
    private double[] forcedParams;

    public static double[] unitaryWeight(int size) {
        double[] res = new double[size];
        Arrays.fill(res, 1);
        return res;
    }

    public abstract double[] getQuery(TargetingLog f);

    public abstract double[] getWeights();

    public void forceParams(double[] params) {
        forcedParams = params;
    }

    public double[] getParams() {
        return new double[0];
    }

    public double[] getForcedParams() {
        if(forcedParams != null)
            return forcedParams;
        return getParams();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("new double[]{");
        double[] weights = getWeights();

        for(int i = 0; i < weights.length; i++) {
            if(i > 0)
                builder.append(", ");
            builder.append(R.formattedDouble(weights[i]));
        }

        builder.append("};\n");

        builder.append("new double[]{");
        double[] params = getForcedParams();

        for(int i = 0; i < params.length; i++) {
            if(i > 0)
                builder.append(", ");
            builder.append(R.formattedDouble(params[i]));
        }

        builder.append("};\n");

        return builder.toString();
    }
}
