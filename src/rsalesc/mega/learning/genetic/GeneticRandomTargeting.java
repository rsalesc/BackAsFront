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
public class GeneticRandomTargeting extends GeneticGunTargeting {
    public GeneticRandomTargeting(int threadNum, Strategy strategy) {
        super(threadNum, strategy);
    }

    @Override
    public KnnView<TimestampedGFRange> getNewKnnSet() {
        KnnView<TimestampedGFRange> set = new KnnView<>();
        set.setDistanceWeighter(new Knn.InverseDistanceWeighter<TimestampedGFRange>(1.0))
                .add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setK(100)
                        .setRatio(0.1)
                        .setStrategy(getGeneticStrategy())
                        .logsEverything());

        return set;
    }
}
