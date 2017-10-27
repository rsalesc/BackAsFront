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

package rsalesc.mega.learning.genetic;

import rsalesc.mega.utils.Strategy;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.structures.Knn;
import rsalesc.structures.KnnTree;
import rsalesc.structures.KnnView;

/**
 * Created by Roberto Sales on 03/10/17.
 */
public class GeneticAdaptiveTargeting extends GeneticGunTargeting {
    public GeneticAdaptiveTargeting(int threadNum, Strategy strategy) {
        super(threadNum, strategy);
    }

    @Override
    public KnnView<TimestampedGFRange> getNewKnnSet() {
        return new KnnView<TimestampedGFRange>()
                .setDistanceWeighter(new Knn.GaussDistanceWeighter<>(1.0))
                .add(new KnnTree<TimestampedGFRange>()
                        .setLimit(1000)
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setK(3)
                        .setRatio(0.15)
                        .setStrategy(getGeneticStrategy())
                        .logsBreak()
                        .logsHit())
                .add(new KnnTree<TimestampedGFRange>()
                        .setLimit(125)
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setK(2)
                        .setRatio(0.15)
                        .setStrategy(getGeneticStrategy())
                        .logsBreak()
                        .logsHit())
                .add(new KnnTree<TimestampedGFRange>()
                        .setLimit(32)
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setK(1)
                        .setRatio(0.35)
                        .setStrategy(getGeneticStrategy())
                        .logsBreak()
                        .logsHit())
                .add(new KnnTree<TimestampedGFRange>()
                        .setLimit(8)
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setK(1)
                        .setRatio(0.5)
                        .setStrategy(getGeneticStrategy())
                        .logsBreak()
                        .logsHit())
                ;
    }
}
