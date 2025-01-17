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

package rsalesc.melee;

import rsalesc.baf2.BackAsFrontRobot2;
import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.listeners.RoundStartedListener;
import rsalesc.baf2.tracking.MeleeTracker;
import rsalesc.baf2.waves.BulletManager;
import rsalesc.baf2.waves.WaveManager;
import rsalesc.mega.gunning.guns.AutomaticGunArray;
import rsalesc.mega.gunning.guns.KnnPlayer;
import rsalesc.mega.gunning.guns.PlayItForwardGun;
import rsalesc.mega.gunning.power.MonkPowerSelector;
import rsalesc.mega.gunning.power.PowerSelector;
import rsalesc.mega.tracking.EnemyMovie;
import rsalesc.mega.tracking.MovieTracker;
import rsalesc.mega.utils.StatTracker;
import rsalesc.mega.utils.Strategy;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.melee.gunning.AutomaticMeleeGunArray;
import rsalesc.melee.gunning.SwarmGun;
import rsalesc.melee.movement.surfing.MedinaBoard;
import rsalesc.melee.radar.MultiModeRadar;
import rsalesc.structures.Knn;
import rsalesc.structures.KnnTree;
import rsalesc.structures.KnnView;

import java.awt.*;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public class Medina extends BackAsFrontRobot2 {
    private static final boolean TC = false;

    @Override
    public void initialize() {
        add(new Colorizer());

        BulletManager bulletManager = new BulletManager();
        WaveManager waveManager = new WaveManager();

        MeleeTracker tracker = new MeleeTracker(waveManager);
        MovieTracker movieTracker = new MovieTracker(105, 20, 8);

//        ShadowManager shadowManager = new ShadowManager(bulletManager, waveManager);

        StatTracker statTracker = StatTracker.getInstance();

        MedinaBoard move = new MedinaBoard(waveManager);

        tracker.addListener(bulletManager);
        tracker.addListener(waveManager);
        if(!TC) tracker.addListener(move);

        PlayItForwardGun pifGun = new PifGun(null);

        SwarmGun swarm = new SwarmGun(pifGun, 100, 32);
        swarm.setPowerSelector(new MonkPowerSelector());

        movieTracker.addListener(pifGun);

        waveManager.addListener(statTracker);
        if(!TC) waveManager.addListener(move);

        add(tracker);
        add(movieTracker);
        add(bulletManager);
        add(waveManager);
//        add(shadowManager);

        add(statTracker);

        if(!TC) add(move);

        addListener(pifGun);
        add(swarm);
        add(new MultiModeRadar());
    }

    class Colorizer extends Component implements RoundStartedListener {
        @Override
        public void onRoundStarted(int round) {
            RobotMediator mediator = getMediator();
            mediator.setBodyColor(new Color(34, 164, 23));
            mediator.setGunColor(new Color(255, 247, 34));
            mediator.setRadarColor(new Color(28, 149, 27));
            mediator.setScanColor(new Color(57, 225, 255));
        }
    }

    private static class GunArray extends AutomaticGunArray {
        @Override
        public StorageNamespace getStorageNamespace() {
            return getGlobalStorage().namespace("roborito-array");
        }
    }

    private static class MeleeGunArray extends AutomaticMeleeGunArray {

        @Override
        public StorageNamespace getStorageNamespace() {
            return getGlobalStorage().namespace("monk-array");
        }
    }

    private static class PifGun extends PlayItForwardGun {
        public PifGun(PowerSelector selector) {
            super(new KnnPlayer() {
                @Override
                public KnnView<EnemyMovie> getNewKnnSet() {
                    return new KnnView<EnemyMovie>()
//                            .setDistanceWeighter(new Knn.InverseDistanceWeighter<>(0.5))
                            .add(new KnnTree<EnemyMovie>()
                                    .setMode(KnnTree.Mode.MANHATTAN)
                                    .setRatio(0.5)
                                    .setK(24)
                                    .setStrategy(new ExperimentalMeleeStrategy())
                                    .logsEverything());
                }

                @Override
                public Knn.DistanceWeighter<EnemyMovie> getLazyWeighter() {
                    return new Knn.GaussDistanceWeighter<>(1.0);
                }

                @Override
                public StorageNamespace getStorageNamespace() {
                    return this.getGlobalStorage().namespace("monkzito-player");
                }
            });

            setPowerSelector(selector);
        }

        @Override
        public StorageNamespace getStorageNamespace() {
            return getGlobalStorage().namespace("monkzito-gun");
        }

        private static class AntiRandomStrategy extends Strategy {
            @Override
            public double[] getQuery(TargetingLog f) {
                return new double[]{
                        1.0 / (1.0 + 2 * f.timeDecel), // TODO: bft?
                        1.0 / (1.0 + 2 * f.timeRevert),
                        Math.min(f.others - 1, 1),
                        Math.min(f.closestDistance / 400, 1),
                        (f.accel + 1) * .5,
                        f.heat(),
                        Math.abs(f.velocity) / 8.,
                };
            }

            @Override
            public double[] getWeights() {
                return new double[]{2, 1, 2, 6, 2, 2, 4};
            }
        }

        private static class ExperimentalMeleeStrategy extends Strategy {
            @Override
            public double[] getQuery(TargetingLog f) {
                return new double[]{
                        1.0 / (1.0 + 2 * f.timeDecel / f.bft()),
                        1.0 / (1.0 + 2 * f.timeRevert / f.bft()),
                        Math.min(f.displaceLast10 / 80, 1),
                        2.0 / (1.0 + f.others),
                        Math.min(f.closestDistance / 1500, 1),
                        (Math.abs(f.closestLateralVelocity)) / 8,
                        (f.closestAdvancingVelocity + 8) / 16,
                        Math.min(f.distanceToWall / 500, 1),
                        (f.advancingVelocityToWall + 8) / 16,
                        (f.accel + 1) * .5,
                        Math.min(f.virtuality(), 1)
                };
            }

            @Override
            public double[] getWeights() {
                return new double[]{2, 2.5, 2, 6, 5, 6, 3, 5.25, 4, 1.5, 3};
            }
        }
    }
}
