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
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.waves.BreakType;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.mega.utils.stats.CubicKernelDensity;
import rsalesc.mega.utils.stats.GuessFactorStats;
import rsalesc.mega.utils.structures.Knn;
import rsalesc.mega.utils.structures.KnnSet;

import java.util.List;

/**
 * Created by Roberto Sales on 12/09/17.
 */
public abstract class DynamicClusteringSurfer extends StoreComponent implements Surfer {
    public abstract KnnSet<TimestampedGFRange> getNewKnnSet();

    public KnnSet<TimestampedGFRange> getKnnSet(String name) {
        StorageNamespace ns = getStorageNamespace().namespace(name);
        if (ns.contains("knn"))
            return (KnnSet) ns.get("knn");

        KnnSet<TimestampedGFRange> knn = getNewKnnSet();
        ns.put("knn", knn);
        return knn;
    }

    @Override
    public boolean hasData(EnemyLog enemyLog, Knn.ParametrizedCondition o) {
        return getKnnSet(enemyLog.getName()).availableData() > 0;
    }

    @Override
    public void log(EnemyLog enemyLog, TargetingLog log, BreakType type) {
        AngularRange intersection = log.preciseIntersection;

        if (intersection == null)
            throw new NullPointerException();

        double gfLow = log.getGfFromAngle(intersection.getStartingAngle());
        double gfHigh = log.getGfFromAngle(intersection.getEndingAngle());
        if (gfLow > gfHigh) {
            double tmp = gfLow;
            gfLow = gfHigh;
            gfHigh = tmp;
        }

        getKnnSet(enemyLog.getName()).add(log, new TimestampedGFRange(log.battleTime, gfLow, gfHigh), type);
    }

    // TODO: cache that
    @Override
    public GuessFactorStats getStats(EnemyLog enemyLog, TargetingLog f, int cacheIndex, Knn.ParametrizedCondition o) {
        if (f == null)
            throw new IllegalStateException();

        KnnSet<TimestampedGFRange> set = getKnnSet(enemyLog.getName());

        if (set.availableData(o) == 0) {
            return BaseSurfing.getFallbackStats(f.distance, Rules.getBulletSpeed(f.velocity));
        }

        List<Knn.Entry<TimestampedGFRange>> found = set.query(f);

        GuessFactorStats stats = new GuessFactorStats(new CubicKernelDensity()); // TODO: rethink
        double totalWeight = Knn.getTotalWeight(found);

        for (Knn.Entry<TimestampedGFRange> entry : found) {
            double gf = entry.payload.getCenter();

            stats.logGuessFactor(gf, entry.weight / totalWeight, entry.payload.getRadius());
        }

        return stats;
    }
}
