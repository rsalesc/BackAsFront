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
import rsalesc.mega.movement.KnnFlattenedSurfer;
import rsalesc.mega.movement.strategies.dc.KnightAdaptiveStrategy;
import rsalesc.mega.movement.strategies.dc.KnightBaseStrategy;
import rsalesc.mega.movement.strategies.dc.UnsegStrats;
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
public abstract class KnightDCSurfer extends KnnFlattenedSurfer {
    public static final Knn.ParametrizedCondition ADAPTIVE_CONDITION = new NamedStatData.HitCondition(new Range(0.02, 1), 0);

    public static final Knn.ParametrizedCondition FLAT_CONDITION =
            new Knn.OrCondition()
                    .add(new NamedStatData.WeightedHitCondition(0.09, 2, 17))
                    .add(new NamedStatData.WeightedHitCondition(0.08, 6, 17))
            ;

    public static final Knn.ParametrizedCondition MFLAT_CONDITION =
            new NamedStatData.WeightedHitCondition(0.075, 2, 1000, 1.25);

    public static final Knn.ParametrizedCondition LIGHT_CONDITION =
                    new Knn.OrCondition()
                            .add(new NamedStatData.HitCondition(new Range(0.075, 1), 2))
                            .add(new NamedStatData.HitCondition(new Range(0.06, 1), 4))
                            .add(new NamedStatData.HitCondition(new Range(0.05, 1), 6))
            ;


//    public static final Knn.ParametrizedCondition FLAT_CONDITION =
//            new Knn.OrCondition()
//                    .add(new NamedStatData.RelativelyWeightedHitCondition(0.8, 2, 10000))
//                    .add(new NamedStatData.RelativelyWeightedHitCondition(0.73, 3, 10000))
//                    .add(new NamedStatData.RelativelyWeightedHitCondition(0.65, 5, 10000))
//                    .add(new NamedStatData.RelativelyWeightedHitCondition(0.62, 7, 10000))
//            ;

    private static final Strategy BASE_STRATEGY = new KnightBaseStrategy();
    private static final Strategy ADAPTIVE_STRATEGY = new KnightAdaptiveStrategy();

    private static final Knn.DecayWeighter<TimestampedGFRange> DECAY = new Knn.DecayWeighter<>(1.5);

    private static final Function<Integer, Double> SQRT_FN = new Function<Integer, Double>() {
        @Override
        public Double apply(Integer integer) {
            return Math.sqrt(integer);
        }
    };

    public KnnView<TimestampedGFRange> getNewKnnSet() {
        return getMonotonicKnnSet();
    }

    public KnnView<TimestampedGFRange> getMonotonicKnnSet() {
        KnnView<TimestampedGFRange> set = new KnnView<TimestampedGFRange>();
        set.setDistanceWeighter(new Knn.GaussDistanceWeighter<>(1.0));

        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setK(25)
                .setRatio(0.3)
                .setStrategy(new UnsegStrats())
                .setDistanceWeighter(new Knn.NormalizeManhattanWeighter<>(new UnsegStrats(), BASE_STRATEGY))
                .setScanWeight(0.03)
                .logsHit())
        ;

        // general
        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setK(20)
                .setRatio(0.2)
                .setCondition(ADAPTIVE_CONDITION)
                .setStrategy(BASE_STRATEGY)
                .setScanWeight(0.5)
                .logsHit())
        ;

        // adaptive
        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setK(100)
                .setRatio(0.5)
                .setDistanceWeighter(DECAY)
                .setCondition(ADAPTIVE_CONDITION)
                .setStrategy(BASE_STRATEGY)
                .logsHit())
        ;

        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setK(32)
                .setRatio(0.3)
                .setDistanceWeighter(DECAY)
                .setCondition(ADAPTIVE_CONDITION)
                .setStrategy(BASE_STRATEGY)
                .logsHit())
        ;

        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setK(8)
                .setRatio(0.25)
                .setDistanceWeighter(DECAY)
                .setCondition(ADAPTIVE_CONDITION)
                .setStrategy(BASE_STRATEGY)
                .logsHit())
        ;

        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setK(1)
                .setDistanceWeighter(DECAY)
                .setCondition(ADAPTIVE_CONDITION)
                .setStrategy(BASE_STRATEGY)
                .logsHit())
        ;

        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setLimit(6)
                .setK(1)
                .setCondition(ADAPTIVE_CONDITION)
                .setStrategy(BASE_STRATEGY)
                .logsHit())
        ;

        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setLimit(1)
                .setK(1)
                .setCondition(ADAPTIVE_CONDITION)
                .setStrategy(BASE_STRATEGY)
                .logsHit())
        ;

        return set;
    }

    @Override
    public KnnView<TimestampedGFRange> getNewFlattenerKnnSet() {
        KnnView<TimestampedGFRange> set = new KnnView<TimestampedGFRange>();
        set.setDistanceWeighter(new Knn.GaussDistanceWeighter<>(1.0));

//        set.add(new KnnTree<TimestampedGFRange>()
//                .setMode(KnnTree.Mode.MANHATTAN)
//                .setK(10)
//                .setRatio(0.2)
//                .setScanWeight(0.15)
//                .setCondition(LIGHT_CONDITION)
//                .setStrategy(BASE_STRATEGY)
//                .setDistanceWeighter(new Knn.NormalizeManhattanWeighter<>(BASE_STRATEGY, ADAPTIVE_STRATEGY))
//                .logsHit()
//                .logsBreak())
//        ;

        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setLimit(300)
                .setK(20)
                .setRatio(0.08)
                .setScanWeight(0.5)
                .setCondition(FLAT_CONDITION)
                .setStrategy(ADAPTIVE_STRATEGY)
                .logsHit()
                .logsBreak())
        ;

        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setLimit(2000)
                .setK(50)
                .setRatio(0.08)
                .setScanWeight(1)
                .setCondition(FLAT_CONDITION)
                .setStrategy(ADAPTIVE_STRATEGY)
                .setDistanceWeighter(DECAY)
                .logsHit()
                .logsBreak())
        ;

        return set;
    }

    @Override
    public boolean flattenerEnabled(NamedStatData o) {
        return FLAT_CONDITION.test(o);
    }
}
