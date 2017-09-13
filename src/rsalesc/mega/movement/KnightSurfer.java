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

import rsalesc.mega.movement.strategies.UnsegStrats;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.mega.utils.structures.Knn;
import rsalesc.mega.utils.structures.KnnSet;
import rsalesc.mega.utils.structures.KnnTree;

/**
 * Created by Roberto Sales on 13/09/17.
 */
public abstract class KnightSurfer extends DynamicClusteringSurfer {
    @Override
    public KnnSet<TimestampedGFRange> getNewKnnSet() {
        KnnSet<TimestampedGFRange> set = new KnnSet<TimestampedGFRange>();
        set.setDistanceWeighter(new Knn.GaussDistanceWeighter<>(1.0));

        set.add(new KnnTree<TimestampedGFRange>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setK(2)
                .setRatio(0.2)
                .setScanWeight(1)
                .setStrategy(new UnsegStrats())
                .logsHit())
        ;

        return set;
    }
}
