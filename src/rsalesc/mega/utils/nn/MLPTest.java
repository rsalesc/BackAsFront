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

import org.junit.jupiter.api.Test;

/**
 * Created by Roberto Sales on 17/08/17.
 */
class MLPTest {
    @Test
    public void test() {
        final int iterations = 10000;
        final int tests = 64;
        final int bitLength = 3;
        final int batchSize = 8;

        MLP net = new MLP(2 * bitLength, new int[]{2*bitLength}, bitLength)
                .setStrategy(new LogisticStrategy())
                .buildRandomly();

        int[][] input = new int[tests][2 * bitLength];
        int[][] output = new int[tests][bitLength];

        double[][] inputFloat = new double[tests][2 * bitLength];
        double[][] outputFloat = new double[tests][bitLength];

        for(int i = 0; i < tests; i++) {
            for (int j = 0; j < 2 * bitLength; j++) {
                input[i][j] = (int) (Math.random() * 1.99999999);
                output[i][j / 2] ^= input[i][j];
            }

            for(int j = 0; j < 2 * bitLength; j++)
                inputFloat[i][j] = input[i][j];

            for(int j = 0; j < bitLength; j++)
                outputFloat[i][j] = output[i][j];
        }

        for(int i = 0; i < iterations; i++) {
            double loss = 0;

            for(int j = 0; j < tests; j += batchSize) {
                int len = Math.min(batchSize, tests - j);
                double[][] inputBatch = new double[len][2 * bitLength];
                double[][] outputBatch = new double[len][bitLength];

                for(int k = 0; k < len; k++) {
                    for(int l = 0; l < 2 * bitLength; l++) {
                        inputBatch[k][l] = inputFloat[j + k][l];
                    }
                }

                for(int k = 0; k < len; k++) {
                    for(int l = 0; l < bitLength; l++) {
                        outputBatch[k][l] = outputFloat[j + k][l];
                    }
                }

                net.train(inputBatch, outputBatch, 0.25);
                double old = net.getCost(outputBatch);

                loss += old;
            }

            loss /= (tests + batchSize - 1) / batchSize;
            System.out.println("loss: " + loss);
        }

        for(int i = 0; i < tests; i++) {
            double[] out = net.feed(inputFloat[i]);

            System.out.print("Found: ");
            for(int j = 0; j < out.length; j++) {
                System.out.print((int)(out[j] * 1.9999999999) + " ");
            }

            System.out.print("Expected: ");
            for(int j = 0; j < out.length; j++) {
                System.out.print(output[i][j] + " ");
            }

            System.out.println();
        }
    }

    @Test
    void mirror() {
        final int iterations = 10000;
        final int tests = 64;
        final int batchSize = 8;

        MLP net = new MLP(1, new int[]{6}, 1)
                .setStrategy(new LogisticStrategy())
                .setOutputStrategy(new RawStrategy())
                .buildRandomly();

        double[] input = new double[tests];
        double[] output = new double[tests];

        for(int i = 0; i < tests; i++) {
            input[i] = Math.random();
            output[i] = -input[i];
        }

        for(int i = 0; i < iterations; i++) {
            double loss = 0;
            for(int j = 0; j < tests; j += batchSize) {
                int len = Math.min(batchSize, tests - j);

                double[][] inputBatch = new double[len][1];
                double[][] outputBatch = new double[len][1];

                for(int k = 0; k < len; k++) {
                    inputBatch[k][0] = input[k+j];
                    outputBatch[k][0] = output[k+j];
                }

                net.train(inputBatch, outputBatch, 0.15);
                double old = net.getCost(outputBatch);

                loss += old;
            }

            loss /= (tests + batchSize - 1) / batchSize;
            System.out.println("loss: " + loss);
        }
    }
}