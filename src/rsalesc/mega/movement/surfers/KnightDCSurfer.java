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

package rsalesc.mega.movement.surfers;

import rsalesc.baf2.core.utils.geometry.Range;
import rsalesc.mega.movement.KnnSurfer;
import rsalesc.mega.movement.strategies.dc.KnightBaseStrategy;
import rsalesc.mega.utils.NamedStatData;
import rsalesc.mega.utils.Strategy;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.structures.Knn;
import rsalesc.structures.KnnTree;
import rsalesc.structures.KnnView;

import java.util.function.Function;

/**
 * Created by Roberto Sales on 13/09/17.
 */
public abstract class KnightDCSurfer extends KnnSurfer {
    public static final Knn.ParametrizedCondition FLAT_CONDITION =
            new Knn.OrCondition()
                    .add(new NamedStatData.HitCondition(new Range(0.09, 1), 2))
                    .add(new NamedStatData.HitCondition(new Range(0.085, 1), 3))
                    .add(new NamedStatData.HitCondition(new Range(0.075, 1), 5))
                    .add(new NamedStatData.HitCondition(new Range(0.065, 1), 7));

    private static final Strategy BASE_STRATEGY = new KnightBaseStrategy();
    private static final Strategy FLAT_STRATEGY = new KnightBaseStrategy();

    public KnnView<TimestampedGFRange> getNewKnnSet() {
        return getSimpleKnnSet();
    }

    public KnnView<TimestampedGFRange> getSimpleKnnSet() {
        KnnView<TimestampedGFRange> set = new KnnView<TimestampedGFRange>();

        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setK(100)
                .setRatio(new Function<Integer, Double>() {
                    @Override
                    public Double apply(Integer integer) {
                        return Math.sqrt(integer);
                    }
                })
                .setDistanceWeighter(new Knn.DecayedGaussWeighter<>(5.5))
                .setStrategy(BASE_STRATEGY)
                .logsHit())
        ;

        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setK(100)
                .setRatio(new Function<Integer, Double>() {
                    @Override
                    public Double apply(Integer integer) {
                        return Math.sqrt(integer);
                    }
                })
                .setScanWeight(0.75)
                .setStrategy(FLAT_STRATEGY)
                .setDistanceWeighter(new Knn.DecayedGaussWeighter<>(1.5))
                .setCondition(FLAT_CONDITION)
                .logsHit()
                .logsBreak())
        ;

        return set;
    }

}
