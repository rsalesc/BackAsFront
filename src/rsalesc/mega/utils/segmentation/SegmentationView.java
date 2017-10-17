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

import rsalesc.baf2.waves.BreakType;
import rsalesc.mega.utils.TargetingLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roberto Sales on 30/09/17.
 */
public class SegmentationView<T> {
    private ArrayList<SegmentationSet<T>> sets = new ArrayList<>();

    public int availableData() {
        int res = 0;
        for(SegmentationSet<T> set : sets)
            res += set.size();
        return res;
    }

    public int availableData(Object o) {
        int res = 0;
        for(SegmentationSet<T> set : sets)
            if(set.isEnabled(o))
                res += set.size();

        return res;
    }

    public SegmentationView<T> add(SegmentationSet<T> set) {
        if(!set.isBuilt())
            set.build();
        sets.add(set);
        return this;
    }

    public void add(TargetingLog f, T payload) {
        for(SegmentationSet<T> set : sets)
            set.log(f, payload);
    }

    public void add(TargetingLog f, T payload, BreakType type) {
        for(SegmentationSet<T> set : sets) {
            if(type == BreakType.VIRTUAL_BREAK && set.logsOnVirtual())
                set.log(f, payload);
            else if(type == BreakType.BULLET_BREAK && set.logsOnBreak())
                set.log(f, payload);
            else if(type == BreakType.BULLET_HIT && set.logsOnHit())
                set.log(f, payload);
        }
    }

    public List<WeightedSegmentedData<T>> query(TargetingLog f, Object o) {
        ArrayList<WeightedSegmentedData<T>> res = new ArrayList<>();

        for(SegmentationSet<T> set : sets) {
            if(set.isEnabled(o))
                res.addAll(set.getData(f));
        }

        return res;
    }

    public List<WeightedSegmentedData<T>> query(TargetingLog f) {
        ArrayList<WeightedSegmentedData<T>> res = new ArrayList<>();

        for(SegmentationSet<T> set : sets) {
            res.addAll(set.getData(f));
        }

        return res;
    }
}
