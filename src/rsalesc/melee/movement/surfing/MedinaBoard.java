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

package rsalesc.melee.movement.surfing;

import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.waves.WaveManager;
import rsalesc.mega.utils.Strategy;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.mega.utils.structures.Knn;
import rsalesc.mega.utils.structures.KnnTree;
import rsalesc.mega.utils.structures.KnnView;

/**
 * Created by Roberto Sales on 11/10/17.
 */
public class MedinaBoard extends MeleeSurfing {
    public MedinaBoard(WaveManager waves) {
        super(new SurferProvider() {
            @Override
            public MeleeSurfer getSurfer(String name) {
                return new KnnMeleeSurfer() {
                    @Override
                    public StorageNamespace getStorageNamespace() {
                        return this.getGlobalStorage().namespace("medina-surfer").namespace(name);
                    }

                    @Override
                    public KnnView<TimestampedGFRange> getNewKnnSet() {
                        return new KnnView<TimestampedGFRange>()
                                .setDistanceWeighter(new Knn.InverseDistanceWeighter<>(1.0))
                                .add(new KnnTree<TimestampedGFRange>()
                                    .setMode(KnnTree.Mode.MANHATTAN)
                                    .setRatio(0.5)
                                    .setK(3)
                                    .setStrategy(new MedinaSurfingStrategy())
                                    .logsHit());
                    }
                };
            }
        }, waves);
    }

    private static class MedinaSurfingStrategy extends Strategy {
        @Override
        public double[] getQuery(TargetingLog f) {
            return new double[]{
                    f.distance / 800,
                    Math.abs(f.lateralVelocity) / 8,
                    (f.accel + 1) / 2
            };
        }

        @Override
        public double[] getWeights() {
            return new double[]{1, 1, 1};
        }
    }
}
