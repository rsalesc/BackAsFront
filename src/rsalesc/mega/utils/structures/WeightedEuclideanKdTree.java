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

package rsalesc.mega.utils.structures;

import rsalesc.baf2.core.utils.R;

/**
 * Created by Roberto Sales on 20/07/17.
 */
public class WeightedEuclideanKdTree<T> extends KdTree<T> {
    private double[] weights;

    public WeightedEuclideanKdTree(double[] weights, Integer sizeLimit) {
        super(weights.length, sizeLimit);
        this.weights = weights;
    }

    @Override
    public int minkowskiBestHyperplane(KdTree<T> node) {
        int res = 0;
        double best = (node.max[0] - node.min[0]) * weights[0];
        if (Double.isNaN(best)) best = 0;

        for (int i = 1; i < node.dim; i++) {
            double nbest = (node.max[i] - node.min[i]) * weights[i];
            if (Double.isNaN(nbest)) nbest = 0;
            if (nbest > best) {
                best = nbest;
                res = i;
            }
        }

        return res;
    }

    @Override
    public double minkowskiDistance(double[] a, double[] b) {
        double res = 0;
        for (int i = 0; i < this.dim; i++) {
            double acc = (a[i] - b[i]) * weights[i];
            if (!Double.isNaN(acc))
                res += acc * acc;
        }
        return R.sqrt(res);
    }

    @Override
    public double minkowskiToHyperrect(double[] p, double[] min, double[] max) {
        if (min == null)
            return Double.POSITIVE_INFINITY;

        double res = 0;
        for (int i = 0; i < this.dim; i++) {
            double acc = 0;
            if (p[i] > max[i])
                acc = (p[i] - max[i]) * weights[i];
            if (p[i] < min[i])
                acc = (min[i] - p[i]) * weights[i];
            if (!Double.isNaN(acc))
                res += acc * acc;
        }

        return R.sqrt(res);
    }
}
