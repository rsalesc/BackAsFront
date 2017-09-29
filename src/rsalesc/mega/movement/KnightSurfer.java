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
import rsalesc.mega.movement.strategies.dc.FlatteningStrategy;
import rsalesc.mega.movement.strategies.dc.NormalStrategy;
import rsalesc.mega.movement.strategies.dc.UnsegStrats;
import rsalesc.mega.utils.NamedStatData;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.mega.utils.structures.Knn;
import rsalesc.mega.utils.structures.KnnSet;
import rsalesc.mega.utils.structures.KnnTree;

import java.sql.Time;

/**
 * Created by Roberto Sales on 13/09/17.
 */
public abstract class KnightSurfer extends DynamicClusteringSurfer {
    private static Knn.ParametrizedCondition ADAPTIVE_CONDITION =
            new NamedStatData.HitCondition(new Range(0.035, 1), 0);

    private static Knn.ParametrizedCondition NORMAL_CONDITION =
            new Knn.Tautology();

    private static Knn.ParametrizedCondition FLATTENING_CONDITION =
            new Knn.AndCondition().add(ADAPTIVE_CONDITION)
                .add(new Knn.OrCondition().add(new NamedStatData.HitCondition(new Range(0.065, 1), 1)))
            ;

    public KnnSet<TimestampedGFRange> getNewKnnSet() {
        return getDecayableKnnSet();
    }

    public KnnSet<TimestampedGFRange> getMonotonicKnnSet() {
        KnnSet<TimestampedGFRange> set = new KnnSet<TimestampedGFRange>();

        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setK(40)
                .setRatio(0.2)
                .setScanWeight(0.5)
                .setStrategy(new NormalStrategy())
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
                        .setStrategy(new NormalStrategy())
                        .logsHit())
                .add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setLimit(8)
                        .setK(2)
                        .setRatio(0.25)
                        .setScanWeight(100)
                        .setCondition(ADAPTIVE_CONDITION)
                        .setStrategy(new NormalStrategy())
                        .logsHit())
                .add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setLimit(32)
                        .setK(2)
                        .setRatio(0.25)
                        .setScanWeight(100)
                        .setCondition(ADAPTIVE_CONDITION)
                        .setStrategy(new NormalStrategy())
                        .logsHit())
                .add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setLimit(100)
                        .setK(3)
                        .setRatio(0.25)
                        .setScanWeight(100)
                        .setCondition(ADAPTIVE_CONDITION)
                        .setStrategy(new NormalStrategy())
                        .logsHit())
                .add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setLimit(1000)
                        .setK(3)
                        .setRatio(0.25)
                        .setScanWeight(100)
                        .setCondition(ADAPTIVE_CONDITION)
                        .setStrategy(new NormalStrategy())
                        .logsHit())
        ;

        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setLimit(12)
                .setK(2)
                .setRatio(0.15)
                .setScanWeight(40)
                .setCondition(FLATTENING_CONDITION)
                .setStrategy(new FlatteningStrategy())
                .logsBreak())
        .add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setLimit(32)
                .setK(3)
                .setRatio(0.15)
                .setScanWeight(40)
                .setCondition(FLATTENING_CONDITION)
                .setStrategy(new FlatteningStrategy())
                .logsBreak())
        .add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setLimit(250)
                .setK(6)
                .setRatio(0.15)
                .setScanWeight(40)
                .setCondition(FLATTENING_CONDITION)
                .setStrategy(new FlatteningStrategy())
                .setDistanceWeighter(new Knn.GaussDistanceWeighter<TimestampedGFRange>(1.0))
                .logsBreak())
        .add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setLimit(1000)
                .setK(8)
                .setRatio(0.15)
                .setScanWeight(40)
                .setCondition(FLATTENING_CONDITION)
                .setStrategy(new FlatteningStrategy())
                .setDistanceWeighter(new Knn.GaussDistanceWeighter<TimestampedGFRange>(1.0))
                .logsBreak())
;

        return set;
    }

    public KnnSet<TimestampedGFRange> getDecayableKnnSet() {
        KnnSet<TimestampedGFRange> set = new KnnSet<TimestampedGFRange>();
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
            .setStrategy(new NormalStrategy())
            .setCondition(ADAPTIVE_CONDITION)
            .logsHit())
        ;

        set.add(new KnnTree<TimestampedGFRange>()
            .setMode(KnnTree.Mode.MANHATTAN)
            .setLimit(1)
            .setK(1)
            .setScanWeight(100)
            .setCondition(ADAPTIVE_CONDITION)
            .setStrategy(new NormalStrategy())
            .logsHit())
        .add(new KnnTree<TimestampedGFRange>()
            .setMode(KnnTree.Mode.MANHATTAN)
            .setLimit(5)
            .setK(1)
            .setScanWeight(100)
            .setCondition(ADAPTIVE_CONDITION)
            .setStrategy(new NormalStrategy())
            .logsHit())
        .add(new KnnTree<TimestampedGFRange>()
            .setMode(KnnTree.Mode.MANHATTAN)
            .setK(1)
            .setScanWeight(100)
            .setCondition(ADAPTIVE_CONDITION)
            .setStrategy(new NormalStrategy())
            .logsHit())
        .add(new KnnTree<TimestampedGFRange>()
            .setMode(KnnTree.Mode.MANHATTAN)
            .setK(8)
            .setRatio(0.25)
            .setScanWeight(100)
            .setCondition(ADAPTIVE_CONDITION)
            .setDistanceWeighter(new Knn.DecayWeighter<>(1.75))
            .setStrategy(new NormalStrategy())
            .logsHit())
        .add(new KnnTree<TimestampedGFRange>()
            .setMode(KnnTree.Mode.MANHATTAN)
            .setK(40)
            .setRatio(0.35)
            .setScanWeight(100)
            .setCondition(ADAPTIVE_CONDITION)
            .setDistanceWeighter(new Knn.DecayWeighter<>(1.75))
            .setStrategy(new NormalStrategy())
            .logsHit())
        ;

        set.add(new KnnTree<TimestampedGFRange>()
            .setMode(KnnTree.Mode.MANHATTAN)
            .setLimit(300)
            .setK(25)
            .setRatio(0.1)
            .setScanWeight(50)
            .setCondition(FLATTENING_CONDITION)
            .setStrategy(new FlatteningStrategy())
            .logsBreak()
            .logsHit())
        .add(new KnnTree<TimestampedGFRange>()
            .setMode(KnnTree.Mode.MANHATTAN)
            .setLimit(2000)
            .setK(50)
            .setRatio(0.08)
            .setScanWeight(500)
            .setCondition(FLATTENING_CONDITION)
            .setStrategy(new FlatteningStrategy())
            .setDistanceWeighter(new Knn.DecayWeighter<>(1.5))
            .logsBreak()
            .logsHit())
        ;

        return set;
    }
}
