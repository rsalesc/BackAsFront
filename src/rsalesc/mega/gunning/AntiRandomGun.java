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
import rsalesc.mega.gunning.guns.DynamicClusteringGFTargeting;
import rsalesc.mega.gunning.guns.GuessFactorGun;
import rsalesc.mega.gunning.power.PowerSelector;
import rsalesc.mega.gunning.strategies.dc.GeneralPurposeStrategy;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.mega.utils.structures.Knn;
import rsalesc.mega.utils.structures.KnnSet;
import rsalesc.mega.utils.structures.KnnTree;

import java.sql.Time;

/**
 * Created by Roberto Sales on 15/09/17.
 */
public class AntiRandomGun extends GuessFactorGun {
    public AntiRandomGun(BulletManager manager, PowerSelector selector) {
        super(new DynamicClusteringGFTargeting() {
            @Override
            public KnnSet<TimestampedGFRange> getNewKnnSet() {
                KnnSet<TimestampedGFRange> set = new KnnSet<>();
                set.setDistanceWeighter(new Knn.InverseDistanceWeighter<TimestampedGFRange>(1.0))
                .add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setK(225)
                        .setRatio(0.1)
                        .setStrategy(new GeneralPurposeStrategy())
                        .logsEverything());

                return set;
            }

            @Override
            public StorageNamespace getStorageNamespace() {
                return this.getGlobalStorage().namespace("anti-random-targeting");
            }
        }, manager);

        setPowerSelector(selector);
    }

    @Override
    public String getGunName() {
        return "Anti-Random Gun";
    }

    @Override
    public StorageNamespace getStorageNamespace() {
        return getGlobalStorage().namespace("anti-random-gun");
    }
}
