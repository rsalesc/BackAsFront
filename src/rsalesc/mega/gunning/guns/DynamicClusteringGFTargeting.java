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

package rsalesc.mega.gunning.guns;

import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.StoreComponent;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.waves.BreakType;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.mega.utils.stats.GuessFactorStats;
import rsalesc.mega.utils.stats.UncutGaussianKernelDensity;
import rsalesc.mega.utils.structures.Knn;
import rsalesc.mega.utils.structures.KnnProvider;
import rsalesc.mega.utils.structures.KnnSet;

import java.util.List;

/**
 * Created by Roberto Sales on 15/09/17.
 * TODO: need some caching?
 *
 * TC1: had payload bandwidth and inverse distance weighter, besides ratio 0.1
 * TC2: had hit-angle bandwidth and gauss distance weighter, besides ratio 0.33
 */
public abstract class DynamicClusteringGFTargeting extends StoreComponent implements GFTargeting, KnnProvider<TimestampedGFRange> {
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
    public boolean hasData(EnemyLog enemyLog) {
        return getKnnSet(enemyLog.getName()).availableData() > 0;
    }

    @Override
    public GeneratedAngle[] getFiringAngles(EnemyLog enemyLog, TargetingLog f) {
        List<Knn.Entry<TimestampedGFRange>> found = getKnnSet(enemyLog.getName()).query(f);

        GuessFactorStats stats = new GuessFactorStats(new UncutGaussianKernelDensity());

        double bandwidth = Physics.hitAngle(f.distance) / 2;

        for(Knn.Entry<TimestampedGFRange> entry : found) {
            stats.logGuessFactor(entry.payload.mean, entry.weight, entry.payload.mean);
        }

        double bestGf = 0;
        double bestDensity = 0;

        for(Knn.Entry<TimestampedGFRange> entry : found) {
            double gf = entry.payload.mean;
            double density = stats.getValue(gf);

            if(density > bestDensity) {
                bestDensity = density;
                bestGf = gf;
            }
        }

        return new GeneratedAngle[]{new GeneratedAngle(1.0, f.getAngle(bestGf), f.distance)};
    }

    @Override
    public void log(EnemyLog enemyLog, TargetingLog f, BreakType type) {
        double gfMean = f.getGfFromAngle(f.preciseIntersection.getAngle(f.preciseIntersection.getCenter()));
        double gfLow = f.getGfFromAngle(f.preciseIntersection.getStartingAngle());
        double gfHigh = f.getGfFromAngle(f.preciseIntersection.getEndingAngle());
        if(gfLow > gfHigh) {
            double tmp = gfLow;
            gfLow = gfHigh;
            gfHigh = tmp;
        }

        getKnnSet(enemyLog.getName()).add(f, new TimestampedGFRange(f.battleTime, gfLow, gfHigh, gfMean), type);
    }
}
