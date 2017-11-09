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

package rsalesc.mega.utils.nn;

/**
 * Created by Roberto Sales on 18/08/17.
 */
public class L2Regularization extends MLPRegularization {
    private double lambda;

    public L2Regularization(double lambda) {
        this.lambda = lambda;
    }

    @Override
    public double getValue(double[][][] weights) {
        double res = 0;
        for(int i = 0; i < weights.length; i++) {
            for(int j = 0; j < weights[i].length; j++) {
                for(int k = 0; k < weights[i][j].length; k++) {
                    res += sqr(weights[i][j][k]);
                }
            }
        }

        return res * lambda / 2;
    }

    @Override
    public double[][] getDerivative(double[][] w) {
        double[][] res = new double[w.length][w[0].length];

        for(int i = 0; i < w.length; i++) {
            for(int j = 0; j < w[0].length; j++) {
                res[i][j] = w[i][j] * lambda / outputSize;
            }
        }

        return res;
    }

    public double sqr(double x) {
        return x*x;
    }
}
