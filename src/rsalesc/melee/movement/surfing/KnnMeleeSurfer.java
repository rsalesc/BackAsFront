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

package rsalesc.melee.movement.surfing;

import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.StoreComponent;
import rsalesc.baf2.core.utils.Pair;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.waves.BreakType;
import rsalesc.mega.utils.IMea;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.mega.utils.WeightedGF;
import rsalesc.mega.utils.structures.Knn;
import rsalesc.mega.utils.structures.KnnProvider;
import rsalesc.mega.utils.structures.KnnView;
import rsalesc.melee.utils.stats.CircularGuessFactorStats;

import java.util.Hashtable;
import java.util.List;

/**
 * Created by Roberto Sales on 11/10/17.
 */
public abstract class KnnMeleeSurfer extends StoreComponent implements MeleeSurfer, KnnProvider<WeightedGF> {
    private Hashtable<Pair<String, Long>, CircularGuessFactorStats> cache = new Hashtable<>();

    public KnnView<WeightedGF> getKnnSet(String name) {
        StorageNamespace ns = getStorageNamespace().namespace(name);

        Object res = ns.get("knn");
        if(res != null)
            return (KnnView) res;

        KnnView<WeightedGF> knn = getNewKnnSet();
        ns.put("knn", knn);
        return knn;
    }

    @Override
    public boolean hasData(EnemyLog enemyLog) {
        return getKnnSet(enemyLog.getName()).availableData() > 0;
    }

    public static WeightedGF getGfRange(TargetingLog log, IMea mea, double weight) {
        AngularRange intersection = log.preciseIntersection;

        if (intersection == null)
            throw new NullPointerException();

        double gfMean = mea.getGfFromAngle(intersection.getAngle(intersection.getCenter()));

        return new WeightedGF(log.battleTime, gfMean, weight);
    }

    @Override
    public void log(EnemyLog enemyLog, TargetingLog log, IMea mea, BreakType type, double weight) {
        getKnnSet(enemyLog.getName()).add(log, getGfRange(log, mea, weight), type);
    }

    @Override
    public CircularGuessFactorStats getStats(EnemyLog enemyLog, TargetingLog f, IMea mea, long cacheIndex) {
        if(f == null)
            throw new IllegalStateException();

        Pair<String, Long> cacheKey = new Pair<>(enemyLog.getName(), cacheIndex);

        if(cacheIndex != -1) {
            CircularGuessFactorStats res = cache.get(cacheKey);
            if(res != null)
                return res;
        }

        KnnView<WeightedGF> set = getKnnSet(enemyLog.getName());

        // TODO: add fallback stats
//        if (set.availableData() == 0) {
//            return BaseSurfing.getFallbackStats(f.distance, Rules.getBulletSpeed(f.velocity));
//        }

        List<Knn.Entry<WeightedGF>> found = set.query(f);

        double width = mea.getMea().getRadius() / R.DOUBLE_PI * CircularGuessFactorStats.BUCKET_COUNT;

        CircularGuessFactorStats stats = new CircularGuessFactorStats(new MeleePowerDensity(0.35, width));

        double totalWeight = 0;
        for (Knn.Entry<WeightedGF> entry : found) {
            totalWeight += entry.payload.weight * entry.weight;
        }

        // TODO: think better about the weight of scans
        for (Knn.Entry<WeightedGF> entry : found) {
            double gf = entry.payload.gf;
            double angle = mea.getAngle(gf);

            stats.add(stats.getBucket(angle), entry.payload.weight * entry.weight / totalWeight, 1);
        }

        if(cacheIndex != -1)
            cache.put(cacheKey, stats);

        return stats;
    }
}
