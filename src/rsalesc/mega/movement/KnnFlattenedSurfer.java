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

import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.StoreComponent;
import rsalesc.baf2.core.benchmark.Benchmark;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.waves.BreakType;
import rsalesc.mega.utils.IMea;
import rsalesc.mega.utils.NamedStatData;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.mega.utils.stats.GuessFactorStats;
import rsalesc.mega.utils.stats.PowerKernelDensity;
import rsalesc.structures.Knn;
import rsalesc.structures.KnnProvider;
import rsalesc.structures.KnnView;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by Roberto Sales on 12/09/17.
 */
public abstract class KnnFlattenedSurfer extends StoreComponent implements Surfer, KnnProvider<TimestampedGFRange> {
    private TreeMap<Long, List<Knn.Entry<TimestampedGFRange>>> cache = new TreeMap<>();
    private TreeMap<Long, List<Knn.Entry<TimestampedGFRange>>> flatCache = new TreeMap<>();
    private TreeMap<Long, GuessFactorStats> statsCache = new TreeMap<>();

    public abstract KnnView<TimestampedGFRange> getNewFlattenerKnnSet();
    public abstract boolean flattenerEnabled(NamedStatData o);

    public KnnView<TimestampedGFRange> getKnnSet(String name) {
        StorageNamespace ns = getStorageNamespace().namespace(name);
        if (ns.contains("knn"))
            return (KnnView) ns.get("knn");

        KnnView<TimestampedGFRange> knn = getNewKnnSet();
        ns.put("knn", knn);
        return knn;
    }

    public KnnView<TimestampedGFRange> getFlattenerKnnSet(String name) {
        StorageNamespace ns = getStorageNamespace().namespace(name);
        if (ns.contains("knn-f"))
            return (KnnView) ns.get("knn-f");

        KnnView<TimestampedGFRange> knn = getNewFlattenerKnnSet();
        ns.put("knn-f", knn);
        return knn;
    }

    @Override
    public boolean hasData(EnemyLog enemyLog, NamedStatData o) {
        return getKnnSet(enemyLog.getName()).availableData(o) > 0;
    }

    public static TimestampedGFRange getGfRange(TargetingLog log, IMea mea) {
        AngularRange intersection = log.preciseIntersection;

        if (intersection == null)
            throw new NullPointerException();

        double gfMean = mea.getGfFromAngle(intersection.getAngle(intersection.getCenter()));
        double gfLow = mea.getGfFromAngle(intersection.getStartingAngle());
        double gfHigh = mea.getGfFromAngle(intersection.getEndingAngle());
        if (gfLow > gfHigh) {
            double tmp = gfLow;
            gfLow = gfHigh;
            gfHigh = tmp;
        }

        return new TimestampedGFRange(log.battleTime, gfLow, gfHigh, gfMean);
    }

    @Override
    public void log(EnemyLog enemyLog, TargetingLog log, IMea mea, BreakType type) {
        TimestampedGFRange range = getGfRange(log, mea);

        getKnnSet(enemyLog.getName()).add(log, range, type);
        getFlattenerKnnSet(enemyLog.getName()).add(log, range, type);
    }

    public List<Knn.Entry<TimestampedGFRange>> getMatches(EnemyLog enemyLog, TargetingLog f, long cacheIndex, NamedStatData o) {
        List<Knn.Entry<TimestampedGFRange>> res = cacheIndex == -1 ? null : cache.get(cacheIndex);
        if(res == null) {
            KnnView<TimestampedGFRange> view = getKnnSet(enemyLog.getName());

            if(view.availableData(o) == 0) {
                return BaseSurfing.getFallbackScans();
            }

            res = view.query(f, o);
            cache.put(cacheIndex, res);
        }
        return res;
    }

    public List<Knn.Entry<TimestampedGFRange>> getFlattenerMatches(EnemyLog enemyLog, TargetingLog f, long cacheIndex, NamedStatData o) {
        List<Knn.Entry<TimestampedGFRange>> res = cacheIndex == -1 ? null : flatCache.get(cacheIndex);
        if(res == null) {
            KnnView<TimestampedGFRange> view = getFlattenerKnnSet(enemyLog.getName());

            if(view.availableData(o) == 0) {
                return new ArrayList<>();
            }

            res = view.query(f, o);
            flatCache.put(cacheIndex, res);
        }
        return res;
    }


    @Override
    public GuessFactorStats getStats(EnemyLog enemyLog, TargetingLog f, IMea mea, long cacheIndex, NamedStatData o) {
        if (f == null)
            throw new IllegalStateException();

        if(statsCache.containsKey(cacheIndex))
            return statsCache.get(cacheIndex);

        Benchmark.getInstance().start("KnnFlattenedSurfer.getStats() non-cached");

        List<Knn.Entry<TimestampedGFRange>> found = getMatches(enemyLog, f, cacheIndex, o);

//        double sum = 1e-21;
//        for(Knn.Entry<TimestampedGFRange> entry : found) {
//            sum += entry.distance;
//        }
//
//        double invMean = found.size() / sum;

        GuessFactorStats stats = new GuessFactorStats(new PowerKernelDensity(0.1)); // TODO: rethink

        for (Knn.Entry<TimestampedGFRange> entry : found) {
            double gf = entry.payload.mean;

            stats.add(stats.getBucket(gf), entry.weight, 1);
        }

        found = getFlattenerMatches(enemyLog, f, cacheIndex, o);

//        sum = 1e-21;
//        for(Knn.Entry<TimestampedGFRange> entry : found) {
//            sum += entry.distance;
//        }
//
//        invMean = found.size() / sum;

        GuessFactorStats flatStats = new GuessFactorStats(new PowerKernelDensity(0.1)); // TODO: rethink

        for (Knn.Entry<TimestampedGFRange> entry : found) {
            double gf = entry.payload.mean;

            flatStats.add(flatStats.getBucket(gf), entry.weight, 1);
        }

//        double flatWeight = flattenerEnabled(o) ? 0.4 : 0.25;
        double flatWeight = 0.35;
        GuessFactorStats finalStats = GuessFactorStats.merge(new GuessFactorStats[]{stats, flatStats},
                                                            new double[]{0.5, flatWeight});

        statsCache.put(cacheIndex, finalStats);

        Benchmark.getInstance().stop();
        return finalStats;
    }

}
