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
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.waves.BreakType;
import rsalesc.mega.utils.NamedStatData;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.mega.utils.stats.GuessFactorStats;
import rsalesc.mega.utils.stats.UncutGaussianKernelDensity;
import rsalesc.mega.utils.structures.Knn;
import rsalesc.mega.utils.structures.KnnProvider;
import rsalesc.mega.utils.structures.KnnView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Roberto Sales on 12/09/17.
 */
public abstract class DynamicClusteringSurfer extends StoreComponent implements Surfer, KnnProvider<TimestampedGFRange> {
    private HashMap<Long, List<Knn.Entry<TimestampedGFRange>>> cache = new HashMap<>();
    private HashMap<Long, GuessFactorStats> statsCache = new HashMap<>();

    public abstract KnnView<TimestampedGFRange> getNewKnnSet();

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

    public static TimestampedGFRange getGfRange(TargetingLog log) {
        AngularRange intersection = log.preciseIntersection;

        if (intersection == null)
            throw new NullPointerException();

        double gfMean = log.getGfFromAngle(intersection.getAngle(intersection.getCenter()));
        double gfLow = log.getGfFromAngle(intersection.getStartingAngle());
        double gfHigh = log.getGfFromAngle(intersection.getEndingAngle());
        if (gfLow > gfHigh) {
            double tmp = gfLow;
            gfLow = gfHigh;
            gfHigh = tmp;
        }

        return new TimestampedGFRange(log.battleTime, gfLow, gfHigh, gfMean);
    }

    @Override
    public void log(EnemyLog enemyLog, TargetingLog log, BreakType type) {
        getKnnSet(enemyLog.getName()).add(log, getGfRange(log), type);
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
    public GuessFactorStats getStats(EnemyLog enemyLog, TargetingLog f, long cacheIndex, NamedStatData o) {
        if (f == null)
            throw new IllegalStateException();

        if(statsCache.containsKey(cacheIndex))
            return statsCache.get(cacheIndex);

        KnnView<TimestampedGFRange> set = getKnnSet(enemyLog.getName());

        if (set.availableData(o) == 0) {
            return BaseSurfing.getFallbackStats(f.distance, Rules.getBulletSpeed(f.velocity));
        }

        List<Knn.Entry<TimestampedGFRange>> found = getMatches(enemyLog, f, cacheIndex, o);

        GuessFactorStats stats = new GuessFactorStats(new UncutGaussianKernelDensity()); // TODO: rethink
        double totalWeight = Knn.getTotalWeight(found);

        double width = Physics.hitAngle(f.distance) / 2;
        double bandwidth = width / Math.max(f.preciseMea.maxAbsolute(),
                                                        f.preciseMea.minAbsolute());

        for (Knn.Entry<TimestampedGFRange> entry : found) {
            double gf = entry.payload.mean;

            stats.logGuessFactor(gf, entry.weight / totalWeight, entry.payload.getLongestDeviation());
        }

        statsCache.put(cacheIndex, stats);

        return stats;
    }

    @Override
    public double getDanger(EnemyLog enemyLog, TargetingLog f, long cacheIndex, NamedStatData o,
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
