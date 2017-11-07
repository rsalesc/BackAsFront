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
import rsalesc.mega.gunning.guns.KnnGuessFactorTargeting;
import rsalesc.mega.gunning.power.PowerSelector;
import rsalesc.mega.gunning.strategies.dc.FastDecayStrategy;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.structures.Knn;
import rsalesc.structures.KnnTree;
import rsalesc.structures.KnnView;

import java.util.function.Function;

/**
 * Created by Roberto Sales on 15/09/17.
 */
public class FastDecayGun extends GuessFactorGun {
    public FastDecayGun(BulletManager manager, PowerSelector selector) {
        super(new KnnGuessFactorTargeting() {
            @Override
            public KnnView<TimestampedGFRange> getNewKnnSet() {
                KnnView<TimestampedGFRange> set = new KnnView<>();
                set.setDistanceWeighter(new Knn.GaussDistanceWeighter<>(1.0, 10));
                set.add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setK(100)
                        .setRatio(new Function<Integer, Double>() {
                            @Override
                            public Double apply(Integer integer) {
                                return Math.sqrt(integer);
                            }
                        })
                        .setStrategy(new FastDecayStrategy())
                        .logsHit()
                        .logsBreak());

                return set;
            }

            @Override
            public StorageNamespace getStorageNamespace() {
                return this.getGlobalStorage().namespace("fdt");
            }
        }, manager);

        setPowerSelector(selector);
    }

    @Override
    public String getGunName() {
        return "Fast Decay Gun";
    }

    @Override
    public StorageNamespace getStorageNamespace() {
        return getGlobalStorage().namespace("fdg");
    }
}
