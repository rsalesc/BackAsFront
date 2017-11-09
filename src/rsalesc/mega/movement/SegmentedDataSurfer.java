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

package rsalesc.mega.movement;

import robocode.Rules;
import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.StoreComponent;
import rsalesc.baf2.core.utils.PredictedHashMap;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.waves.BreakType;
import rsalesc.mega.utils.IMea;
import rsalesc.mega.utils.NamedStatData;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.mega.utils.segmentation.SegmentationView;
import rsalesc.mega.utils.segmentation.WeightedEntry;
import rsalesc.mega.utils.segmentation.WeightedSegmentedData;
import rsalesc.mega.utils.stats.GuessFactorStats;
import rsalesc.mega.utils.stats.PowerKernelDensity;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Roberto Sales on 01/10/17.
 */
public abstract class SegmentedDataSurfer extends StoreComponent implements Surfer {
    private HashMap<Long, GuessFactorStats> statsCache = new PredictedHashMap<>(3000);

    public abstract SegmentationView<TimestampedGFRange> getNewSegmentationView();

    public SegmentationView<TimestampedGFRange> getSegmentationView(String name) {
        StorageNamespace ns = getStorageNamespace().namespace(name);
        if(ns.contains("segview"))
            return (SegmentationView) ns.get("segview");

        SegmentationView<TimestampedGFRange> view = getNewSegmentationView();
        ns.put("segview", view);
        return view;
    }

    @Override
    public boolean hasData(EnemyLog enemyLog, NamedStatData o) {
        return getSegmentationView(enemyLog.getName()).availableData(o) > 0;
    }

    @Override
    public void log(EnemyLog enemyLog, TargetingLog log, IMea mea, BreakType type) {
        getSegmentationView(enemyLog.getName()).add(log, KnnSurfer.getGfRange(log, mea), type);
    }

    @Override
    public GuessFactorStats getStats(EnemyLog enemyLog, TargetingLog f, IMea mea, long cacheIndex, NamedStatData o) {
        if(f == null)
            throw new IllegalStateException();

        if(statsCache.containsKey(cacheIndex))
            return statsCache.get(cacheIndex);

        SegmentationView<TimestampedGFRange> view = getSegmentationView(enemyLog.getName());

        if(view.availableData(o) == 0) {
            return BaseSurfing.getFallbackStats(f.distance, Rules.getBulletSpeed(f.velocity));
        }

        List<WeightedSegmentedData<TimestampedGFRange>> data = view.query(f, o);

        GuessFactorStats stats = new GuessFactorStats(new PowerKernelDensity(0.15));

        for(WeightedSegmentedData<TimestampedGFRange> entry : data) {
            for(WeightedEntry<TimestampedGFRange> range : entry.getWeightedData()) {
                stats.add(stats.getBucket(range.payload.mean), range.weight, 1);
            }
        }

        stats.normalize();

        statsCache.put(cacheIndex, stats);
        return stats;
    }
}
