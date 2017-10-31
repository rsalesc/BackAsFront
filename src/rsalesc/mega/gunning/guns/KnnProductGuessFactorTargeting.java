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
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.waves.BreakType;
import rsalesc.mega.utils.IMea;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.mega.utils.stats.GaussianKernelDensity;
import rsalesc.mega.utils.stats.GuessFactorStats;
import rsalesc.mega.utils.stats.PowerKernelDensity;
import rsalesc.structures.Knn;
import rsalesc.structures.KnnProvider;
import rsalesc.structures.KnnView;

import java.util.List;

/**
 * Created by Roberto Sales on 15/09/17.
 * TODO: need some caching?
 *
 * TC1: had payload bandwidth and inverse distance weighter, besides ratio 0.1
 * TC2: had hit-angle bandwidth and gauss distance weighter, besides ratio 0.33
 */
public abstract class KnnProductGuessFactorTargeting extends StoreComponent implements GFTargeting, KnnProvider<TimestampedGFRange> {
    public List<Knn.Entry<TimestampedGFRange>> lastFound;

    public abstract KnnView<TimestampedGFRange> getNewKnnSet();
    public abstract KnnView<TimestampedGFRange> getNewAlternativeKnnSet();

    public KnnView<TimestampedGFRange> getKnnSet(String name) {
        StorageNamespace ns = getStorageNamespace().namespace(name);
        if (ns.contains("knn"))
            return (KnnView) ns.get("knn");

        KnnView<TimestampedGFRange> knn = getNewKnnSet();
        ns.put("knn", knn);
        return knn;
    }

    public KnnView<TimestampedGFRange> getAlternativeKnnSet(String name) {
        StorageNamespace ns = getStorageNamespace().namespace(name);
        if (ns.contains("knn2"))
            return (KnnView) ns.get("knn2");

        KnnView<TimestampedGFRange> knn = getNewAlternativeKnnSet();
        ns.put("knn2", knn);
        return knn;
    }

    @Override
    public boolean hasData(EnemyLog enemyLog) {
        return getKnnSet(enemyLog.getName()).availableData() > 0;
    }

    @Override
    public GeneratedAngle[] getFiringAngles(EnemyLog enemyLog, TargetingLog f, IMea mea) {
        KnnView<TimestampedGFRange> view = getKnnSet(enemyLog.getName());
        KnnView<TimestampedGFRange> view2 = getAlternativeKnnSet(enemyLog.getName());

        List<Knn.Entry<TimestampedGFRange>> found = view.query(f);
        List<Knn.Entry<TimestampedGFRange>> found2 = view2.query(f);

        lastFound = found;

        double bandwidth = Physics.hitAngle(f.distance) / 2 /
            Math.min(f.preciseMea.minAbsolute(), f.preciseMea.maxAbsolute());


        GuessFactorStats stats = new GuessFactorStats(new GaussianKernelDensity());
        GuessFactorStats stats2 = new GuessFactorStats(new PowerKernelDensity(0.4));

        for(Knn.Entry<TimestampedGFRange> entry : found) {
            stats.logGuessFactor(entry.payload.mean, entry.weight, bandwidth);
        }

        for(Knn.Entry<TimestampedGFRange> entry : found2) {
            stats2.logGuessFactor(entry.payload.mean, entry.weight, 1.0);
        }

        double[] buffer = stats.getBuffer();
        R.fuzzyDistribution(buffer);

        stats = new GuessFactorStats(buffer, null);

        double[] buffer2 = stats2.getBuffer();
        R.fuzzyDistribution(buffer2);

        stats2 = new GuessFactorStats(buffer2, null);

        double bestGf = 0;
        double bestDensity = 0;

        for(Knn.Entry<TimestampedGFRange> entry : found) {
            double gf = entry.payload.mean;
            double density = stats.getValue(gf) * (1.0 - R.sqr(stats2.getValue(gf)));

            if(density > bestDensity) {
                bestDensity = density;
                bestGf = gf;
            }
        }

        return new GeneratedAngle[]{new GeneratedAngle(1.0, mea.getAngle(bestGf), f.distance)};
    }

    @Override
    public void log(EnemyLog enemyLog, TargetingLog f, IMea mea, BreakType type) {
        double gfMean = mea.getGfFromAngle(f.preciseIntersection.getAngle(f.preciseIntersection.getCenter()));
        double gfLow = mea.getGfFromAngle(f.preciseIntersection.getStartingAngle());
        double gfHigh = mea.getGfFromAngle(f.preciseIntersection.getEndingAngle());

        if(gfLow > gfHigh) {
            double tmp = gfLow;
            gfLow = gfHigh;
            gfHigh = tmp;
        }

        getKnnSet(enemyLog.getName()).add(f, new TimestampedGFRange(f.battleTime, gfLow, gfHigh, gfMean), type);
        getAlternativeKnnSet(enemyLog.getName()).add(f, new TimestampedGFRange(f.battleTime, gfLow, gfHigh, gfMean), type);
    }
}
