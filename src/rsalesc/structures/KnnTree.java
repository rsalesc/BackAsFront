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

import rsalesc.mega.utils.Timestamped;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roberto Sales on 13/08/17.
 */
public class KnnTree<T extends Timestamped> extends Knn<T> {
    private Mode mode;
    private Integer limit = null;
    private KdTree<T> tree;

    public KnnTree<T> setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public KnnTree<T> setMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public void buildStructure() {
        if (mode == Mode.MANHATTAN)
            tree = new WeightedManhattanKdTree<T>(this.getStrategy().getWeights(), limit);
        else if (mode == Mode.EUCLIDEAN)
            tree = new WeightedEuclideanKdTree<T>(this.getStrategy().getWeights(), limit);
        else
            throw new IllegalStateException("building with no valid mode specified");
    }

    @Override
    public int size() {
        return tree.size();
    }

    @Override
    public void add(double[] point, T payload) {
        tree.add(point, payload);
    }

    @Override
    public List<Entry<T>> query(double[] point, int K, double alpha) {
        List<Entry<T>> res = new ArrayList<>();
        for (KdTree.Entry<T> entry : tree.kNN(point, K, alpha)) {
            res.add(makeEntry(entry.distance, entry.payload));
        }

        return res;
    }

    @Override
    public Knn<T>.Iterator iterator(double[] query, double alpha) {
        return new Knn<T>.Iterator(new Iterator(query, alpha));
    }

    private class Iterator implements java.util.Iterator<Knn.Entry<T>> {
        private final java.util.Iterator<KdTree.Entry<T>> iterator;

        private Iterator(double[] query, double alpha) {
            this.iterator = tree.iterator(query, alpha);
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Entry<T> next() {
            KdTree.Entry<T> entry = iterator.next();
            return makeEntry(entry.distance, entry.payload);
        }
    }

    public enum Mode {
        MANHATTAN,
        EUCLIDEAN
    }
}
