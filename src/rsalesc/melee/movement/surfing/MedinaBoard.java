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
import rsalesc.mega.movement.BaseSurfing;
import rsalesc.mega.movement.TrueSurfing;
import rsalesc.mega.movement.surfers.KnightDCSurfer;
import rsalesc.mega.utils.Strategy;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.mega.utils.WeightedGF;
import rsalesc.structures.Knn;
import rsalesc.structures.KnnTree;
import rsalesc.structures.KnnView;

/**
 * Created by Roberto Sales on 11/10/17.
 */
public class MedinaBoard extends MultiModeSurfing {
    public MedinaBoard(WaveManager waves) {
        super(getMelee(waves), getDuel(waves));
    }

    private static BaseSurfing getDuel(WaveManager waves) {
        return new TrueSurfing(new KnightDCSurfer() {
            @Override
            public StorageNamespace getStorageNamespace() {
                return getGlobalStorage().namespace("medina-knight");
            }
        }, waves);
    }

    private static MeleeSurfing getMelee(WaveManager waves) {
        SurferProvider provider = new SurferProvider() {
            @Override
            public MeleeSurfer getSurfer(String name) {
                return new KnnMeleeSurfer() {
                    @Override
                    public StorageNamespace getStorageNamespace() {
                        return this.getGlobalStorage().namespace("medina-surfer").namespace(name);
                    }

                    @Override
                    public KnnView<WeightedGF> getNewKnnSet() {
                        return new KnnView<WeightedGF>()
                                .setDistanceWeighter(new Knn.InverseDistanceWeighter<>(1.0))
                                .add(new KnnTree<WeightedGF>()
                                        .setMode(KnnTree.Mode.MANHATTAN)
                                        .setRatio(0.5)
                                        .setK(5)
                                        .setStrategy(new OldSurfingStrategy())
                                        .logsHit());
                    }
                };
            }
        };

        return new MeleeSurfing(provider, waves, new SimpleTargetGuesser());
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

    private static class FutureSurfingStrategy extends Strategy {
        @Override
        public double[] getQuery(TargetingLog f) {
            return new double[]{
                    f.bft() / 80,
                    Math.abs(f.lateralVelocity) / 8,
                    (f.advancingVelocity + 8) / 16,
                    Math.min(f.distanceToWall / 400, 1),
                    (f.accel + 1) / 2,
                    (1.0 / (2.0 + f.timeDecel / f.bft())),
                    f.displaceLast10 / 80,
            };
        }

        @Override
        public double[] getWeights() {
            return new double[]{3, 2, 2, 2, 4, 2, 1.5};
        }
    }

    private static class OldSurfingStrategy extends Strategy {
        @Override
        public double[] getQuery(TargetingLog f) {
            return new double[]{
                    f.distance / 800,
                    Math.abs(f.lateralVelocity) / 8,
                    (f.accel + 1) / 2,
                    f.hitChance
            };
        }

        @Override
        public double[] getWeights() {
            return new double[]{1, 1, 1, 3};
        }
    }

}
