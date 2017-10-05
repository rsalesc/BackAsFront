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

import java.util.*;

/**
 * Created by Roberto Sales on 30/09/17.
 */
public class SegmentedData<T> {
    private final int maxSize;
    private final LinkedList<T> list;

    public SegmentedData(int maxSize) {
        this.maxSize = maxSize;
        list = new LinkedList<T>();
    }

    public void add(T data) {
        while(list.size() >= maxSize)
            list.pop();

        list.add(data);
    }

    public T[] getData() {
        LinkedList<T> rev = (LinkedList<T>) list.clone();
        Collections.reverse(rev);
        return (T[]) rev.toArray();
    }

    public WeightedSegmentedData<T> weight(double w) {
        WeightedSegmentedData<T> data = new WeightedSegmentedData<>(maxSize, w);
        for(T entry : list)
            data.add(entry);

        return data;
    }

    public static <T> double getTotalWeight(List<WeightedSegmentedData<T>> list) {
        double res = 0;
        for(WeightedSegmentedData<T> data : list)
            res += data.getWeight() * data.getData().length;

        return res;
    }
}
