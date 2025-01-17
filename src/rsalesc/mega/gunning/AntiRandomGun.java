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
import rsalesc.mega.gunning.strategies.dc.MainStrategy;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.structures.Knn;
import rsalesc.structures.KnnTree;
import rsalesc.structures.KnnView;

/**
 * Created by Roberto Sales on 15/09/17.
 * MANHATTAN + /=distance gave good results
 */
public class AntiRandomGun extends GuessFactorGun {
    public AntiRandomGun(BulletManager manager, PowerSelector selector) {
        super(new KnnGuessFactorTargeting() {
            @Override
            public KnnView<TimestampedGFRange> getNewKnnSet() {
                KnnView<TimestampedGFRange> set = new KnnView<>();
                set.setDistanceWeighter(new Knn.InverseDistanceWeighter<>(1.0));
                set.add(new KnnTree<TimestampedGFRange>()
                        .setMode(KnnTree.Mode.MANHATTAN)
                        .setK(200)
                        .setRatio(0.11)
                        .setStrategy(new MainStrategy())
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
