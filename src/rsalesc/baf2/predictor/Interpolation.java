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

package rsalesc.baf2.predictor;

import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;
import rsalesc.baf2.core.utils.R;

/**
 * Created by Roberto Sales on 13/10/17.
 */
public class Interpolation {
    private static final double fat[] = new double[11];

    static {
        fat[0] = 1;
        for(int i = 1; i < 11; i++) fat[i] = fat[i-1] * i;
    }

    public static void main(String[] args) {
        System.out.println(evalPolynomialCoefficientDerivative(new double[]{1, 1, 1}, 1, 2, 2));

        double[] p =
                cubicInterpolation(new double[]{1.0}, 2,
                                    new double[]{5.0}, 8,
                        100, 1, 0.67);

        for(int i = 0; i < p.length; i++)
            System.out.print(p[i] + " ");

        System.out.println();

        System.out.println(evalPolynomial(p, 2) + " " + evalPolynomial(p, 8));

        XYChart chart = new XYChart(500, 400);

        double[] x = new double[100];
        double[] y = new double[100];

        for(int i = 0; i < 100; i++) {
            x[i] = i * 0.1;
            y[i] = evalPolynomial(p, x[i]);
        }

        XYSeries series = chart.addSeries("poly3", x, y);
        series.setMarker(SeriesMarkers.NONE);

//        new SwingWrapper<>(chart).displayChart();
    }

    public static double[] cubicInterpolation(double[] f1, double x1, double[] f2, double x2, int iterations, double initialRate, double decay) {
        // TODO: get a better initial solution
        double[] p = new double[Math.max(f1.length, f2.length) + 1];
        for(int i = 0; i < p.length; i++)
            p[i] = (Math.random() - 0.5) / 2;

        double rate = initialRate;

        do {
            double[] g = new double[p.length];
            double mse = 0;

            for(int i = 0; i < f1.length; i++)
                mse += R.sqr(evalPolynomialDerivative(p, i, x1) - f1[i]);

            for(int i = 0; i < f2.length; i++)
                mse += R.sqr(evalPolynomialDerivative(p, i, x2) - f2[i]);

            for(int i = 0; i < p.length; i++) {
                for(int j = 0; j < f1.length; j++) {
                    g[i] += 2 * (evalPolynomialDerivative(p, j, x1) - f1[j])
                            * evalPolynomialCoefficientDerivative(p, j, x1, i);
                }
                for(int j = 0; j < f2.length; j++) {
                    g[i] += 2 * (evalPolynomialDerivative(p, j, x2) - f2[j])
                            * evalPolynomialCoefficientDerivative(p, j, x2, i);
                }
            }

            for(int i = 0; i < p.length; i++) {
                p[i] -= rate * g[i];
            }

            System.out.println(mse);
        } while(--iterations != 0 && (rate *= decay) > 1e-6);

        return p;
    }

    public static double evalPolynomial(double[] p, double x) {
        double res = 0;
        for(int i = p.length - 1; i >= 0; i--) {
            res = res * x + p[i];
        }

        return res;
    }

    public static double evalPolynomialDerivative(double[] p, int d, double x) {
        if(d > p.length - 1)
            return 0;

        double res = 0;
        for(int i = p.length - 1; i >= d; i--) {
            res = res * x + p[i] * fat[i] / fat[i-d];
        }

        return res;
    }

    public static double evalPolynomialCoefficientDerivative(double[] p, int d, double x, int coef) {
        if(d > coef)
            return 0;

        return Math.pow(x, coef - d) * fat[coef] / fat[coef - d];
    }

    public static double mse(double[] x1, double[] x2) {
        double res = 0;
        for(int i = 0; i < x1.length; i++) {
            res += R.sqr(x1[i] - x2[i]);
        }

        return res;
    }
}
