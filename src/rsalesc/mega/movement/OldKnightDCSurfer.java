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

import rsalesc.baf2.core.utils.geometry.Range;
import rsalesc.mega.movement.strategies.dc.*;
import rsalesc.mega.utils.NamedStatData;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.structures.Knn;
import rsalesc.structures.KnnView;
import rsalesc.structures.KnnTree;

/**
 * Created by Roberto Sales on 13/09/17.
 */
public abstract class OldKnightDCSurfer extends KnnSurfer {
    private static Knn.ParametrizedCondition ADAPTIVE_CONDITION =
            new NamedStatData.HitCondition(new Range(0.035, Double.POSITIVE_INFINITY), 0);

    private static Knn.ParametrizedCondition NORMAL_CONDITION =
            new Knn.Tautology();

    private static Knn.ParametrizedCondition FLATTENING_CONDITION =
            new Knn.AndCondition().add(ADAPTIVE_CONDITION)
                    .add(new Knn.OrCondition()
                                    .add(new NamedStatData.HitCondition(new Range(0.08, 1), 1))
//                                    .add(new NamedStatData.HitCondition(new Range(0.13, 1), 2))
//                                    .add(new NamedStatData.HitCondition(new Range(0.11, 1), 3))
//                                    .add(new NamedStatData.HitCondition(new Range(0.095, 1), 5))
//                                    .add(new NamedStatData.HitCondition(new Range(0.085, 1), 7))
                    );

    public KnnView<TimestampedGFRange> getNewKnnSet() {
        return getMonotonicKnnSet();
    }

    public KnnView<TimestampedGFRange> getMonotonicKnnSet() {
        KnnView<TimestampedGFRange> set = new KnnView<TimestampedGFRange>();

        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setK(40)
                .setRatio(0.2)
                .setScanWeight(0.5)
                .setStrategy(new OldNormalStrategy())
                .setDistanceWeighter(new Knn.GaussDistanceWeighter<TimestampedGFRange>(1.0))
                .logsHit())


        /*
        * ADAPTIVE MONOTONIC TREES
         */
                .add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setLimit(1)
                        .setK(1)
                        .setScanWeight(100)
                        .setCondition(ADAPTIVE_CONDITION)
                        .setStrategy( new OldNormalStrategy())
                        .logsHit())
                .add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setLimit(8)
                        .setK(2)
                        .setRatio(0.25)
                        .setScanWeight(100)
                        .setCondition(ADAPTIVE_CONDITION)
                        .setStrategy( new OldNormalStrategy())
                        .logsHit())
                .add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setLimit(32)
                        .setK(2)
                        .setRatio(0.25)
                        .setScanWeight(100)
                        .setCondition(ADAPTIVE_CONDITION)
                        .setStrategy( new OldNormalStrategy())
                        .logsHit())
                .add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setLimit(100)
                        .setK(3)
                        .setRatio(0.25)
                        .setScanWeight(100)
                        .setCondition(ADAPTIVE_CONDITION)
                        .setStrategy( new OldNormalStrategy())
                        .logsHit())
                .add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setLimit(1000)
                        .setK(3)
                        .setRatio(0.25)
                        .setScanWeight(100)
                        .setCondition(ADAPTIVE_CONDITION)
                        .setStrategy( new OldNormalStrategy())
                        .logsHit())
        ;

        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setLimit(12)
                .setK(2)
                .setRatio(0.15)
                .setScanWeight(40)
                .setCondition(FLATTENING_CONDITION)
                .setStrategy(new OldFlatteningStrategy())
                .logsBreak())
                .add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setLimit(32)
                        .setK(3)
                        .setRatio(0.15)
                        .setScanWeight(40)
                        .setCondition(FLATTENING_CONDITION)
                        .setStrategy(new OldFlatteningStrategy())
                        .logsBreak())
                .add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setLimit(250)
                        .setK(6)
                        .setRatio(0.15)
                        .setScanWeight(40)
                        .setCondition(FLATTENING_CONDITION)
                        .setStrategy(new OldFlatteningStrategy())
                        .setDistanceWeighter(new Knn.GaussDistanceWeighter<TimestampedGFRange>(1.0))
                        .logsBreak())
                .add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setLimit(1000)
                        .setK(8)
                        .setRatio(0.15)
                        .setScanWeight(40)
                        .setCondition(FLATTENING_CONDITION)
                        .setStrategy(new OldFlatteningStrategy())
                        .setDistanceWeighter(new Knn.GaussDistanceWeighter<TimestampedGFRange>(1.0))
                        .logsBreak())
        ;

        return set;
    }

    public KnnView<TimestampedGFRange> getDecayableKnnSet() {
        KnnView<TimestampedGFRange> set = new KnnView<TimestampedGFRange>();
        set.setDistanceWeighter(new Knn.GaussDistanceWeighter<TimestampedGFRange>(1.0));

        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setK(20)
                .setRatio(0.2)
                .setScanWeight(4)
                .setStrategy(new UnsegStrats())
                .setCondition(NORMAL_CONDITION)
                .logsHit())
        ;

        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setK(20)
                .setRatio(0.2)
                .setScanWeight(20)
                .setStrategy( new OldNormalStrategy())
                .setCondition(ADAPTIVE_CONDITION)
                .logsHit())
        ;

        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setLimit(1)
                .setK(1)
                .setScanWeight(100)
                .setCondition(ADAPTIVE_CONDITION)
                .setStrategy( new OldNormalStrategy())
                .logsHit())
                .add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setLimit(5)
                        .setK(1)
                        .setScanWeight(100)
                        .setCondition(ADAPTIVE_CONDITION)
                        .setStrategy( new OldNormalStrategy())
                        .logsHit())
                .add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setK(1)
                        .setScanWeight(100)
                        .setCondition(ADAPTIVE_CONDITION)
                        .setStrategy( new OldNormalStrategy())
                        .logsHit())
                .add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setK(8)
                        .setRatio(0.25)
                        .setScanWeight(100)
                        .setCondition(ADAPTIVE_CONDITION)
                        .setDistanceWeighter(new Knn.DecayWeighter<>(1.75))
                        .setStrategy( new OldNormalStrategy())
                        .logsHit())
                .add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setK(40)
                        .setRatio(0.35)
                        .setScanWeight(100)
                        .setCondition(ADAPTIVE_CONDITION)
                        .setDistanceWeighter(new Knn.DecayWeighter<>(1.75))
                        .setStrategy( new OldNormalStrategy())
                        .logsHit())
        ;

        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setLimit(300)
                .setK(25)
                .setRatio(0.1)
                .setScanWeight(50)
                .setCondition(FLATTENING_CONDITION)
                .setStrategy(new OldFlatteningStrategy())
                .logsBreak()
                .logsHit())
                .add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setLimit(2000)
                        .setK(50)
                        .setRatio(0.08)
                        .setScanWeight(500)
                        .setCondition(FLATTENING_CONDITION)
                        .setStrategy(new OldFlatteningStrategy())
                        .setDistanceWeighter(new Knn.DecayWeighter<>(1.5))
                        .logsBreak()
                        .logsHit())
        ;

        return set;
    }
}
