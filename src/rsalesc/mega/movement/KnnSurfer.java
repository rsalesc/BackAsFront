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
import rsalesc.baf2.core.utils.R;
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

import java.util.List;
import java.util.TreeMap;

/**
 * Created by Roberto Sales on 12/09/17.
 */
public abstract class KnnSurfer extends StoreComponent implements Surfer, KnnProvider<TimestampedGFRange> {
    private TreeMap<Long, List<Knn.Entry<TimestampedGFRange>>> cache = new TreeMap<>();
    private TreeMap<Long, GuessFactorStats> statsCache = new TreeMap<>();

    public KnnView<TimestampedGFRange> getKnnSet(String name) {
        StorageNamespace ns = getStorageNamespace().namespace(name);
        if (ns.contains("knn"))
            return (KnnView) ns.get("knn");

        KnnView<TimestampedGFRange> knn = getNewKnnSet();
        ns.put("knn", knn);
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
        getKnnSet(enemyLog.getName()).add(log, getGfRange(log, mea), type);
    }

    private List<Knn.Entry<TimestampedGFRange>> getMatches(EnemyLog enemyLog, TargetingLog f, long cacheIndex, NamedStatData o) {
        List<Knn.Entry<TimestampedGFRange>> res = cacheIndex == -1 ? null : cache.get(cacheIndex);
        if(res == null) {
            res = getKnnSet(enemyLog.getName()).query(f, o);
            cache.put(cacheIndex, res);
        }
        return res;
    }

    @Override
    public GuessFactorStats getStats(EnemyLog enemyLog, TargetingLog f, IMea mea, long cacheIndex, NamedStatData o) {
        if (f == null)
            throw new IllegalStateException();

        if(statsCache.containsKey(cacheIndex))
            return statsCache.get(cacheIndex);

        KnnView<TimestampedGFRange> set = getKnnSet(enemyLog.getName());

        if (set.availableData(o) == 0) {
            return BaseSurfing.getFallbackStats(f.distance, Rules.getBulletSpeed(f.velocity));
        }

        List<Knn.Entry<TimestampedGFRange>> found = getMatches(enemyLog, f, cacheIndex, o);

//        BinKernelDensity density = new BinKernelDensity(new PowerKernelDensity(0.1), 1.0);
        GuessFactorStats stats = new GuessFactorStats(new PowerKernelDensity(0.1)); // TODO: rethink
        double totalWeight = Knn.getTotalWeight(found);

        for (Knn.Entry<TimestampedGFRange> entry : found) {
            double gf = entry.payload.mean;

            stats.add(stats.getBucket(gf), entry.weight / totalWeight, 1);
        }

        statsCache.put(cacheIndex, stats);

        return stats;
    }

    public double getDanger(EnemyLog enemyLog, TargetingLog f, IMea mea, long cacheIndex, NamedStatData o,
                            AngularRange intersection) {
        if (f == null)
            throw new IllegalStateException();

        KnnView<TimestampedGFRange> set = getKnnSet(enemyLog.getName());

        double gf = f.getGfFromAngle(intersection.getAngle(intersection.getCenter()));
        double bandwidth = intersection.getRadius() / Math.max(f.preciseMea.maxAbsolute(),
                                                                f.preciseMea.minAbsolute());

        if (set.availableData(o) == 0) {
            return BaseSurfing.getFallbackStats(f.distance, Rules.getBulletSpeed(f.velocity))
                    .getValue(gf);
        }

        List<Knn.Entry<TimestampedGFRange>> found = getMatches(enemyLog, f, cacheIndex, o);
        double totalWeight = Knn.getTotalWeight(found);

        double res = 0;

        for(Knn.Entry<TimestampedGFRange> entry : found) {
            double pl = entry.payload.mean;

            res += R.gaussKernel((gf - pl) / bandwidth) * entry.weight;
        }

        res /= totalWeight;

        return res;
    }
}
