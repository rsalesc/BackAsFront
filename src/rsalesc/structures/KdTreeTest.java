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

package rsalesc.structures;

import jk.tree.KDTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Roberto Sales on 20/07/17.
 */
public class KdTreeTest {
    private static int SIZE_LIMIT = 10000;
    private double[][] points;
    private double[][] queries;
    private int dimension;
    private int pointsCount;
    private int queriesCount;
    private int K;
    private boolean hasDump;

    private Random random;

    void dump(double[] point) {
        if (!hasDump) return;
        for (int i = 0; i < point.length; i++)
            System.out.print(point[i] + " ");
        System.out.println("");
    }

    double randomWithRange(double min, double max) {
        double range = (max - min);
        return (random.nextDouble() * range) + min;
    }

    double random() {
        return randomWithRange(-100, 100);
    }

    double[] generatePoint(int dimension) {
        double[] res = new double[dimension];
        for (int i = 0; i < dimension; i++)
            res[i] = random();
        return res;
    }

    @BeforeEach
    public void setup() {
        hasDump = false;
        dimension = 8;
        K = 3;
        pointsCount = 30000;
        queriesCount = 10000;

        random = new Random(42L);

        points = new double[pointsCount][];
        for (int i = 0; i < pointsCount; i++) {
            points[i] = generatePoint(dimension);
        }

        queries = new double[queriesCount][];
        for (int i = 0; i < queriesCount; i++) {
            queries[i] = generatePoint(dimension);
        }
    }

    double[][] withIterator() {
        System.out.println("Running with knn-iterator");
        EuclideanKdTree<Integer> tree = new EuclideanKdTree<>(dimension, SIZE_LIMIT);
        for (int i = 0; i < pointsCount; i++) {
            tree.add(points[i], null);
        }

        double[][] res = new double[queriesCount][];
        for (int i = 0; i < queriesCount; i++) {
            List<KdTree.Entry<Integer>> entries = new ArrayList<>();

            Iterator<KdTree.Entry<Integer>> it = tree.iterator(queries[i]);

            while(it.hasNext() && entries.size() < this.K) {
                entries.add(it.next());
            }

            int actualK = Math.min(this.K, entries.size());

            res[i] = new double[actualK];

            int j = 0;
            for (KdTree.Entry<Integer> entry : entries) {
                res[i][j++] = entry.distance;
            }

            dump(res[i]);
        }

        return res;
    }

    double[][] withJkTree() {
        System.out.println("Running with jk-knn");
        KDTree.Euclidean<Integer> tree = new KDTree.Euclidean<>(dimension);
        for (int i = 0; i < pointsCount; i++) {
            tree.addPoint(points[i], null);
        }

        double[][] res = new double[queriesCount][];
        for (int i = 0; i < queriesCount; i++) {
            ArrayList<KDTree.SearchResult<Integer>> entries = tree.nearestNeighbours(queries[i], this.K);
            int actualK = Math.min(this.K, entries.size());

            res[i] = new double[actualK];

            int j = actualK;
            for (KDTree.SearchResult<Integer> entry : entries) {
                res[i][--j] = entry.distance;
            }

            dump(res[i]);
        }

        return res;
    }

    double[][] withTree() {
        System.out.println("Running with knn");
        EuclideanKdTree<Integer> tree = new EuclideanKdTree<>(dimension, SIZE_LIMIT);
        for (int i = 0; i < pointsCount; i++) {
            tree.add(points[i], null);
        }

        double[][] res = new double[queriesCount][];
        for (int i = 0; i < queriesCount; i++) {
            List<KdTree.Entry<Integer>> entries = tree.kNN(queries[i], this.K);
            int actualK = Math.min(this.K, entries.size());

            res[i] = new double[actualK];

            int j = actualK;
            for (KdTree.Entry<Integer> entry : entries) {
                res[i][--j] = entry.distance;
            }

            dump(res[i]);
        }

        return res;
    }

    double dist(double[] p1, double[] p2) {
        double res = 0;
        for (int i = 0; i < dimension; i++) {
            res += (p1[i] - p2[i]) * (p1[i] - p2[i]);
        }
        return res;
    }

    double[][] withoutTree() {
        System.out.println("Running without knn");
        double[][] res = new double[queriesCount][];
        for (int i = 0; i < queriesCount; i++) {
            PriorityQueue<Double> pq = new PriorityQueue<Double>();
            for (int j = Math.max(pointsCount - SIZE_LIMIT, 0); j < pointsCount; j++) {
                pq.add(dist(points[j], queries[i]));
            }

            int actualK = Math.min(this.K, pq.size());
            res[i] = new double[actualK];
            for (int j = 0; j < actualK; j++) {
                res[i][j] = pq.poll();
            }

            dump(res[i]);
        }

        return res;
    }

    @Test
    public void add() {
        double[][] p1 = withTree();
        double[][] p2 = withoutTree();

        int differed = 0;
        double error = 0;
        for (int i = 0; i < queriesCount; i++) {
            if (p1[i].length != p2[i].length) {
                differed++;
            } else {
                for (int j = 0; j < p1[i].length; j++) {
                    error += Math.abs(p1[i][j] - p2[i][j]);
                }
            }
        }

        assertTrue(error < 1e-9);
    }

    @Test
    public void iterator() {
        double[][] p1 = withIterator();
        double[][] p2 = withoutTree();

        int differed = 0;
        double error = 0;
        for (int i = 0; i < queriesCount; i++) {
            if (p1[i].length != p2[i].length) {
                differed++;
            } else {
                for (int j = 0; j < p1[i].length; j++) {
//                    System.out.println("A: " + p1[i][j] + ", B: " + p2[i][j]);

                    error += Math.abs(p1[i][j] - p2[i][j]);
                }

//                System.out.println();
            }
        }

//        System.out.println(error + " " + differed);
        assertTrue(error < 1e-9);
    }

    public void jk() {
        double[][] p1 = withTree();
        double[][] p2 = withJkTree();

        int differed = 0;
        double error = 0;
        for (int i = 0; i < queriesCount; i++) {
            if (p1[i].length != p2[i].length) {
                differed++;
            } else {
                for (int j = 0; j < p1[i].length; j++) {
//                    System.out.println("A: " + p1[i][j] + ", B: " + p2[i][j]);

                    error += Math.abs(p1[i][j] - p2[i][j]);
                }

//                System.out.println();
            }
        }

//        System.out.println(error + " " + differed);
        assertTrue(error < 1e-9);
    }

}