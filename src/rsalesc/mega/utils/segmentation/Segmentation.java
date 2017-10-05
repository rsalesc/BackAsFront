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

package rsalesc.mega.utils.segmentation;

/**
 * Created by Roberto Sales on 27/07/17.
 */
public class Segmentation<T> {
    private int                 pieces;
    private int                 depth;
    private double[][]          slices;
    private SegmentTrie<T>      stats;

    public Segmentation(double[][] slices) {
        this.slices = slices;
        depth = slices.length;
        int[] sizes = new int[depth];
        for(int i = 0; i < depth; i++) {
            sizes[i] = slices[i].length + 1;
            pieces += sizes[i];
        }
        stats = new SegmentTrie<>(sizes);
    }

    public void addFromSegments(int[] segs, T payload) {
        if(segs.length != depth)
            throw new IllegalArgumentException();

        stats.add(segs, payload);
    }

    public void add(double[] values, T payload) {
        if(values.length != depth)
            throw new IllegalArgumentException();

        addFromSegments(valuesToSegments(values), payload);
    }

    public T getFromSegments(int[] segs) {
        if(segs.length != depth)
            throw new IllegalArgumentException();

        return stats.get(segs);
    }

    public T get(double[] values) {
        if(values.length != depth)
            throw new IllegalArgumentException();

        return getFromSegments(valuesToSegments(values));
    }

    private int[] valuesToSegments(double[] values) {
        int[] query = new int[depth];
        for(int i = 0; i < depth; i++) {
            int l = -1, r = slices[i].length - 1;
            while(l < r) {
                int mid = (l + r + 1) / 2;
                if(slices[i][mid] <= values[i])
                    l = mid;
                else r = mid - 1;
            }

            query[i] = l + 1;
        }

        return query;
    }

    public int getSliceCount() {
        return pieces;
    }
}
