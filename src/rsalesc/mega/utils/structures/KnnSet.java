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


import rsalesc.baf2.waves.BreakType;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.mega.utils.Timestamped;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roberto Sales on 13/08/17.
 */
public class KnnSet<T extends Timestamped> {
    private List<Knn<T>> knns;
    private Knn.DistanceWeighter<T> weighter;

    public KnnSet() {
        knns = new ArrayList<>();
    }

    public KnnSet<T> setDistanceWeighter(Knn.DistanceWeighter<T> weighter) {
        this.weighter = weighter;
        return this;
    }

    public void mutate(Knn.ConditionMutation mutation) {
        for (Knn<T> knn : knns)
            knn.mutate(mutation);
    }

    public KnnSet<T> add(Knn<T> knn) {
        if (!knn.isBuilt())
            knn.build();
        knns.add(knn);
        return this;
    }

    public void add(TargetingLog f, T payload) {
        for (Knn<T> knn : knns)
            knn.add(f, payload);
    }

    public void add(TargetingLog f, T payload, BreakType type) {
        for (Knn<T> knn : knns) {
            if (type == BreakType.BULLET_HIT && knn.logsOnHit())
                knn.add(f, payload);
            else if (type == BreakType.BULLET_BREAK && knn.logsOnBreak())
                knn.add(f, payload);
            else if (type == BreakType.VIRTUAL_BREAK && knn.logsOnVirtual())
                knn.add(f, payload);
        }
    }

    public List<Knn.Entry<T>> query(TargetingLog f) {
        List<Knn.Entry<T>> res = new ArrayList<>();
        for (Knn<T> knn : knns) {
            res.addAll(knn.query(f));
        }

        if (weighter != null)
            res = weighter.getWeightedEntries(res);

        return res;
    }

    public List<Knn.Entry<T>> query(TargetingLog f, Object o) {
        List<Knn.Entry<T>> res = new ArrayList<>();
        for (Knn<T> knn : knns) {
            if (knn.isEnabled(o))
                res.addAll(knn.query(f));
        }

        if (weighter != null)
            res = weighter.getWeightedEntries(res);

        return res;
    }

    public List<Knn.Entry<T>> query(TargetingLog f, int K) {
        List<Knn.Entry<T>> res = new ArrayList<>();
        for (Knn<T> knn : knns) {
            if(K == 0)
                break;

            List<Knn.Entry<T>> acc = knn.query(f, Math.min(K, knn.getK()));
            res.addAll(acc);
            K -= acc.size();
        }

        if (weighter != null)
            res = weighter.getWeightedEntries(res);

        return res;
    }

    public Knn.DistanceWeighter<T> getWeighter() {
        return weighter;
    }

    public List<Knn<T>> getKnns() {
        return knns;
    }

    public int availableData() {
        int res = 0;
        for (Knn<T> knn : knns)
            res += knn.size();
        return res;
    }


    public int availableData(Object o) {
        int res = 0;
        for (Knn<T> knn : knns)
            if (knn.isEnabled(o))
                res += knn.size();
        return res;
    }

    public int queryableData() {
        int res = 0;
        for(Knn<T> knn : knns) {
            res += knn.getQueryableData();
        }

        return res;
    }

    public int queryableData(Object o) {
        int res = 0;
        for(Knn<T> knn : knns) {
            if(knn.isEnabled(o))
                res += knn.getQueryableData();
        }

        return res;
    }
}
