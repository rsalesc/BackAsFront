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

package rsalesc.mega.gunning;

import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.waves.BulletManager;
import rsalesc.mega.gunning.guns.GuessFactorGun;
import rsalesc.mega.gunning.guns.KnnProductGuessFactorTargeting;
import rsalesc.mega.gunning.power.PowerSelector;
import rsalesc.mega.gunning.strategies.dc.AntiSurferStrategy;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.structures.Knn;
import rsalesc.structures.KnnTree;
import rsalesc.structures.KnnView;

/**
 * Created by Roberto Sales on 15/09/17.
 */
public class ExperimentalAntiAdaptiveGun extends GuessFactorGun {
    public ExperimentalAntiAdaptiveGun(BulletManager manager, PowerSelector selector) {
        super(new KnnProductGuessFactorTargeting() {
            @Override
            public KnnView<TimestampedGFRange> getNewKnnSet() {
                return new KnnView<TimestampedGFRange>()
                        .setDistanceWeighter(new Knn.GaussDistanceWeighter<>(1.0))
                        .add(new KnnTree<TimestampedGFRange>()
                                .setLimit(7500)
                                .setMode(KnnTree.Mode.MANHATTAN)
                                .setK(4)
                                .setRatio(0.15)
                                .setStrategy(new AntiSurferStrategy())
                                .logsBreak()
                                .logsHit())
                        .add(new KnnTree<TimestampedGFRange>()
                                .setLimit(2000)
                                .setMode(KnnTree.Mode.MANHATTAN)
                                .setK(4)
                                .setRatio(0.15)
                                .setStrategy(new AntiSurferStrategy())
                                .logsBreak()
                                .logsHit())
                        .add(new KnnTree<TimestampedGFRange>()
                                .setLimit(350)
                                .setMode(KnnTree.Mode.MANHATTAN)
                                .setK(4)
                                .setRatio(0.15)
                                .setStrategy(new AntiSurferStrategy())
                                .logsBreak()
                                .logsHit())
                        .add(new KnnTree<TimestampedGFRange>()
                                .setLimit(125)
                                .setMode(KnnTree.Mode.MANHATTAN)
                                .setK(4)
                                .setRatio(0.15)
                                .setStrategy(new AntiSurferStrategy())
                                .logsBreak()
                                .logsHit())
                        .add(new KnnTree<TimestampedGFRange>()
                                .setLimit(32)
                                .setMode(KnnTree.Mode.MANHATTAN)
                                .setK(2)
                                .setRatio(0.15)
                                .setStrategy(new AntiSurferStrategy())
                                .logsBreak()
                                .logsHit());
            }

            @Override
            public KnnView<TimestampedGFRange> getNewAlternativeKnnSet() {
                return new KnnView<TimestampedGFRange>()
                        .add(new KnnTree<TimestampedGFRange>()
                            .setLimit(4)
                            .setMode(KnnTree.Mode.MANHATTAN)
                            .setK(1)
                            .setRatio(0.15)
                            .setStrategy(new AntiSurferStrategy())
                            .logsHit())
//                        .add(new KnnTree<TimestampedGFRange>()
//                            .setLimit(12)
//                            .setMode(KnnTree.Mode.MANHATTAN)
//                            .setK(2)
//                            .setRatio(0.15)
//                            .setStrategy(new AntiSurferStrategy())
//                            .logsHit())
                    ;
            }

            @Override
            public StorageNamespace getStorageNamespace() {
                return this.getGlobalStorage().namespace("exp-adaptive-targeting");
            }
        }, manager);

        setPowerSelector(selector);
    }

    @Override
    public String getGunName() {
        return "Exp. Anti-Adaptive Gun";
    }

    @Override
    public StorageNamespace getStorageNamespace() {
        return getGlobalStorage().namespace("exp-adaptive-gun");
    }
}
