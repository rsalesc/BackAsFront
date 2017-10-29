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

/**
 * Created by rsalesc on 20/07/17.
 */

import rsalesc.baf2.core.utils.R;

import java.util.*;

/**
 * Implementation of K-d Tree inspired by
 * Robério e Seus Teclados' ICPC library
 * based on "Optimizing Search Strategies in k-d Trees"
 * https://graphics.stanford.edu/~tpurcell/pubs/search.pdf
 * and Rednaxela's 2nd gen
 * <p>
 * This version additionally supports
 * approximate kNN, which is usually fine
 * for some applications and is WAY faster,
 * so you can search for much more points.
 * <p>
 * For a given alpha 0 < alpha < 1, it reduces the
 * search ball's radius by (1-alpha)*radius, meaning
 * that points which are in the original ball but are
 * further than alpha*radius unities will be skipped.
 * Though, this ensures that the algorithm will not
 * miss neighbors closer than alpha*[distanceToEdges to the
 * k-th furthest node]
 * <p>
 * It plays well with Play-It Forward strategies :)
 * <p>
 * TODO: do not split node if its largest bound is already epsilon
 *
 * @param <T> The type of the payload which will be
 *            stored along with the points
 */
abstract public class KdTree<T> {
    private static int BUCKET_SIZE = 50;

    // initial data
    protected int dim;
    protected double[][] points;
    // shrinked clipping window
    protected double[] min;
    protected double[] max;
    private Object[] data;
    private int length;
    private Integer maxLength;
    private int bucketSize;
    // splitting>
    private int hyperplane;
    private double cutPosition;

    // neighbors
    private KdTree<T> left;
    private KdTree<T> right;
    private KdTree<T> parent;

    // old points
    private Queue<double[]> pointQueue;

    public KdTree(int dimensions, Integer sizeLimit, int bucketLimit) {
        this.dim = dimensions;
        this.bucketSize = bucketLimit;

        this.points = new double[bucketLimit][];
        this.data = new Object[bucketLimit];
        this.length = 0;
        this.maxLength = sizeLimit;

        this.pointQueue = new ArrayDeque<>();
        this.parent = null;

//        this.min = new double[this.dim];
//        Arrays.fill(this.min, Double.POSITIVE_INFINITY);
//        this.max = new double[this.dim];
//        Arrays.fill(this.max, Double.NEGATIVE_INFINITY);
    }

    public KdTree(int dimensions, Integer sizeLimit) {
        this(dimensions, sizeLimit, BUCKET_SIZE);
    }

    private KdTree(KdTree<T> parent) {
        this.dim = parent.dim;
        this.bucketSize = parent.bucketSize;

        this.points = new double[Math.max(this.bucketSize, parent.data.length / 2)][];
        this.data = new Object[Math.max(this.bucketSize, parent.data.length / 2)];
        this.length = 0;
        this.maxLength = parent.maxLength;

        this.parent = parent;
    }

    public int size() {
        return this.length;
    }

    /**
     * Add a point to the knn, along with a payload
     * associated to it. Note that in this operation,
     * points may be removed from the knn if the sizeLimit
     * parameter was specified in the construction and the
     * resulting knn have more than sizeLimit points added to
     * it. In such case, the oldest point will be removed (FIFO)
     *
     * @param point   the point to be added
     * @param payload the payload to be associated to
     *                the point
     */
    public void add(double point[], T payload) {
        if (point.length != dim)
            throw new IllegalArgumentException();

        for (int i = 0; i < this.dim; i++) {
            if (Double.isNaN(point[i]))
                throw new IllegalStateException("NaN on query point");
        }

        KdTree<T> current = this;

        while (!current.isLeaf() || current.isHeavy()) {
            // in this case we have to split the node
            if (current.isLeaf()) {
                current.hyperplane = this.minkowskiBestHyperplane(current);
                current.cutPosition = (current.max[current.hyperplane] + current.min[current.hyperplane]) / 2;

                if (current.cutPosition == Double.POSITIVE_INFINITY)
                    current.cutPosition = Double.MAX_VALUE;
                else if (current.cutPosition == Double.NEGATIVE_INFINITY)
                    current.cutPosition = -Double.MAX_VALUE;
                else if (Double.isNaN(current.cutPosition))
                    current.cutPosition = 0;
//                else
//                    current.cutPosition = R.constrain(current.min[current.hyperplane], current.cutPosition,
//                                                        current.max[current.hyperplane]);

                if (R.isNear(current.max[current.hyperplane], current.min[current.hyperplane])) {
                    current.stretch();
                    break;
                }

                if (current.cutPosition == current.max[current.hyperplane])
                    current.cutPosition = current.min[current.hyperplane];

                KdTree<T> left = new KdNode(current);
                KdTree<T> right = new KdNode(current);

                double max = 0;
                for (int i = 0; i < current.length; i++) {
                    max = Math.max(max, current.points[i][current.hyperplane]);
                    KdTree<T> addOn = current.points[i][current.hyperplane] <= current.cutPosition ? left : right;
                    if(addOn.isHeavy())
                        addOn.stretch();
                    addOn.extendNode(current.points[i], current.data[i]);
                }

//                if (left.isHeavy()) {
//                    System.out.println(current.min[current.hyperplane] + " " + current.max[current.hyperplane]
//                            + " " + current.cutPosition + " " + bucketSize + " " + current.length + " " + maxLength);
//                    throw new IllegalStateException("left child is heavy");
//                }
//
//                if (right.isHeavy()) {
//                    System.out.println(current.min[current.hyperplane] + " " + current.max[current.hyperplane]
//                            + " " + current.cutPosition + " " + bucketSize + " " + current.length + " " + maxLength);
//                    throw new IllegalStateException("right child is heavy");
//                }

                current.left = left;
                current.right = right;
                current.points = null;
                current.data = null;
            }

            current.updateClippingWindow(point);
            current.length++;

            if (point[current.hyperplane] <= current.cutPosition)
                current = current.left;
            else
                current = current.right;
        }

        // found our node
        current.extendNode(point, payload);

        // check if knn has exceeded sizeLimit?
        this.pointQueue.add(point);
        if (this.maxLength != null && this.maxLength < this.length) {
            this.remove(this.pointQueue.poll());
        }
    }

    private void stretch() {
        double[][] newPoints = new double[this.length * 2][];
        System.arraycopy(this.points, 0, newPoints, 0, this.length);
        this.points = newPoints;

        Object[] newData = new Object[this.length * 2];
        System.arraycopy(this.data, 0, newData, 0, this.length);
        this.data = newData;
    }

    private void extendNode(double[] point, Object payload) {
        this.points[this.length] = point;
        this.data[this.length] = payload;
        this.length++;
        this.updateClippingWindow(point);
    }

    private void updateClippingWindow(double[] point) {
        if (this.length == 0) return;
        if (this.min == null || this.max == null) {
            this.min = new double[this.dim];
            System.arraycopy(point, 0, this.min, 0, this.dim);
            this.max = new double[this.dim];
            System.arraycopy(point, 0, this.max, 0, this.dim);
        } else {
            for (int i = 0; i < this.dim; i++) {
                if (Double.isNaN(point[i])) {
                    this.min[i] = 0;
                    this.max[i] = 0;
                } else {
                    this.min[i] = Math.min(this.min[i], point[i]);
                    this.max[i] = Math.max(this.max[i], point[i]);
                }


            }
        }
    }

    private void recomputeClippingWindow() {
//        if (!isLeaf())
//            throw new IllegalArgumentException("only leaf can have clipping window recomputed");

        min = null;
        max = null;
        for (int i = 0; i < length; i++) {
            updateClippingWindow(points[i]);
        }
    }

    /**
     * Return the K neighbors closest to query in the knn by a
     * factor of approximation of alpha (default is 1), which is
     * better explained at the top, in the class documentation.
     * <p>
     * Note that the returned list may contain less than K points
     * if the knn itself have less than K points.
     *
     * @param query the query point, which may not be in the knn
     * @param K     how many neighbors should be returned
     * @param alpha the factor of approximation explained in the class
     *              documentation
     * @return a list of the K (possibly less) closest neighbors
     * in the knn of the query point, as Entry<T>
     */
    public List<Entry<T>> kNN(double[] query, int K, double alpha) {
        if (query.length != dim)
            throw new IllegalArgumentException();

        for (int i = 0; i < this.dim; i++) {
            if (Double.isNaN(query[i]))
                throw new IllegalStateException("NaN on kNN point");
        }

        if (this.size() == 0)
            return new ArrayList<Entry<T>>();

        FloatingHeap.Min<KdTree<T>> queue = new FloatingHeap.Min<>();
        FloatingHeap.Max<T> found = new FloatingHeap.Max<>();

        int actualK = Math.min(K, size());
        queue.push(0, this);

        while (queue.size() > 0 &&
                (found.size() < actualK || queue.top().key < found.top().key)) {
            searchTick(query, actualK, alpha, queue, found);
        }

        ArrayList<Entry<T>> res = new ArrayList<>();

        while (found.size() > 0) {
            res.add(new Entry<>(found.top().key / alpha, found.top().payload));
            found.pop();
        }

        return res;
    }

    public List<Entry<T>> kNN(double[] query, int K) {
        return kNN(query, K, 1.0);
    }

    private void searchTick(double[] query, int K, double alpha, FloatingHeap<KdTree<T>> queue, FloatingHeap<T> found) {
        KdTree<T> current = queue.top().payload;
        queue.pop();

        while (!current.isLeaf()) {
            // try to find out what is the most promising subtree
            KdTree<T> next;
            KdTree<T> other;

            double leftCost = this.minkowskiToHyperrect(query, current.left.min, current.left.max);
            double rightCost = this.minkowskiToHyperrect(query, current.right.min, current.right.max);
            double distance;

            if (leftCost < rightCost) {
                next = current.left;
                other = current.right;
                distance = rightCost;
            } else {
                next = current.right;
                other = current.left;
                distance = leftCost;
            }

            if (other.length > 0 && (found.size() < K || distance < found.top().key)) {
                queue.push(distance, other); // distanceToEdges actually
            }

            if (next.length == 0)
                return;
            else
                current = next;
        }

        for (int i = 0; i < current.length; i++) {
            double distance = this.minkowskiDistance(query, current.points[i]) * alpha;
            if (found.size() < K) {
                found.push(distance, (T) current.data[i]);
            } else if (found.top().key > distance) {
                found.pop();
                found.push(distance, (T) current.data[i]);
            }
        }
    }

    public Iterator iterator(double[] query) {
        return new Iterator(query, 1.0);
    }

    public Iterator iterator(double[] query, double alpha) {
        return new Iterator(query, alpha);
    }

    /**
     * Remove a point from the Kd Tree. Note, though,
     * that the point is removed according to its object
     * signature (address) instead of its actual content,
     * so if you pass another point with the same data of
     * one of the points in the knn, do not expect it to
     * be removed.
     *
     * @param point the point to be removed
     */
    public void remove(double[] point) {
        KdTree<T> current = this;

        while (!current.isLeaf()) {
            if (point[current.hyperplane] <= current.cutPosition)
                current = current.left;
            else
                current = current.right;
        }

        for (int i = 0; i < current.length; i++) {
            // Note that the array addresses are being compared, not the arrays themselves
            if (point == current.points[i]) {
                System.arraycopy(current.points, i + 1, current.points, i, current.length - (i + 1));
                System.arraycopy(current.data, i + 1, current.data, i, current.length - (i + 1));
                // GC
                current.points[current.length - 1] = null;
                current.data[current.length - 1] = null;
                KdTree<T> leaf = current;
                while (current != null) {
                    current.length--;
                    current = current.parent;
                }

                leaf.recomputeClippingWindow();
                return;
            }
        }

        throw new IllegalStateException("point was not found");
    }

    private boolean isLeaf() {
        return this.data != null;
    }

    private boolean isHeavy() {
        return this.length >= this.data.length;
    }

    public abstract int minkowskiBestHyperplane(KdTree<T> node);

    public abstract double minkowskiDistance(double[] a, double[] b);

    public abstract double minkowskiToHyperrect(double[] p, double[] min, double[] max);

    public static class Entry<T> {
        public final double distance;
        public final T payload;

        public Entry(double distance, T payload) {
            this.distance = distance;
            this.payload = payload;
        }
    }

    private class KdNode extends KdTree<T> {
        private KdNode(KdTree<T> parent) {
            super(parent);
        }

        /**
         * Node is the node being partitioned. The hyperrectangles
         * may contain NaN values
         *
         * @param node the node being partitioned
         * @return the best hyperplane to partition this node
         */
        @Override
        public int minkowskiBestHyperplane(KdTree<T> node) {
            throw new UnsupportedOperationException();
        }

        /**
         * Should return the distanceToEdges between two points in some
         * Minkowski distanceToEdges (L1, L2, ... Loo)
         *
         * @param a the first point
         * @param b the second point
         * @return minkowski distanceToEdges Lx between those points for some x >= 1
         */
        @Override
        public double minkowskiDistance(double[] a, double[] b) {
            throw new UnsupportedOperationException();
        }

        /**
         * Distance from the point p to the hyperrectangle
         * with boundaries at min and max
         * <p>
         * Notice that min/max may be null, since we may be
         * checking a node that doesn't have points
         *
         * @param p   query point
         * @param min min boundaries of the hyperrectangle
         * @param max max boundaries of the hyperrectangle
         * @return
         */
        @Override
        public double minkowskiToHyperrect(double[] p, double[] min, double[] max) {
            throw new UnsupportedOperationException();
        }
    }

    private class Iterator implements java.util.Iterator<Entry<T>> {
        private final double[] query;
        private final double alpha;

        private FloatingHeap.Min<KdTree<T>> queue = new FloatingHeap.Min<>();
        private FloatingHeap.Min<T> found = new FloatingHeap.Min<>();

        private ArrayList<FloatingHeap.Entry<T>> reallyFound = new ArrayList<>();

        private int K = 0;

        private Iterator(double[] query, double alpha) {
            this.query = query;
            this.alpha = alpha;

            this.queue.push(0, KdTree.this);
        }

        @Override
        public boolean hasNext() {
            return queue.size() > 0 && K < pointQueue.size();
        }

        @Override
        public Entry<T> next() {
            ++K;

            while (queue.size() > 0 && reallyFound.size() < K) {
                searchTick(query, Integer.MAX_VALUE, alpha, queue, found);

                while(found.size() > 0 && (queue.size() == 0 || queue.top().key >= found.top().key)) {
                    reallyFound.add(found.top());
                    found.pop();
                }
            }

            if(reallyFound.size() < K)
                throw new IllegalStateException();

            FloatingHeap.Entry<T> res = reallyFound.get(K-1);

            return new Entry<>(res.key / alpha, res.payload);
        }
    }
}
