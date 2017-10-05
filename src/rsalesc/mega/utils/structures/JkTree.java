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

import jk.tree.KDTree;
import rsalesc.mega.utils.Timestamped;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roberto Sales on 04/10/17.
 */
public class JkTree<T extends Timestamped> extends Knn<T> {
    private Integer limit = null;
    private KDTree<T> tree;
    private KnnTree.Mode mode;

    public JkTree<T> setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public JkTree<T> setMode(KnnTree.Mode mode) {
        this.mode = mode;
        return this;
    }

    @Override
    protected void buildStructure() {
        KDTree.WeightedManhattan<T> manhattan = new KDTree.WeightedManhattan<>(getStrategy().getWeights().length);
        manhattan.setWeights(getStrategy().getWeights());
        tree = manhattan;
    }

    @Override
    public int size() {
        return tree.size();
    }

    @Override
    public void add(double[] point, T payload) {
        tree.addPoint(point, payload);
    }

    @Override
    public List<Entry<T>> query(double[] point, int K, double alpha) {
        if(alpha != 1.0)
            throw new IllegalArgumentException();

        List<KDTree.SearchResult<T>> results = tree.nearestNeighbours(point, K);

        List<Entry<T>> res = new ArrayList<>();

        for(KDTree.SearchResult<T> result : results) {
            res.add(makeEntry(result.distance, result.payload));
        }

        return res;
    }
}
