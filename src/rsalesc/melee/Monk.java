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
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.waves.BulletManager;
import rsalesc.baf2.waves.WaveManager;
import rsalesc.mega.gunning.guns.*;
import rsalesc.mega.gunning.power.MeleePowerSelector;
import rsalesc.mega.gunning.power.MonkPowerSelector;
import rsalesc.mega.gunning.power.PowerSelector;
import rsalesc.mega.tracking.EnemyMovie;
import rsalesc.mega.utils.StatTracker;
import rsalesc.mega.utils.Strategy;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.mega.utils.structures.Knn;
import rsalesc.mega.utils.structures.KnnView;
import rsalesc.mega.utils.structures.KnnTree;
import rsalesc.melee.gunning.AutomaticMeleeGunArray;
import rsalesc.melee.gunning.MonkGun;
import rsalesc.mega.tracking.MovieTracker;
import rsalesc.melee.gunning.SwarmGun;
import rsalesc.melee.movement.MonkFeet;
import rsalesc.melee.radar.MultiModeRadar;

import java.awt.*;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public class Monk extends BackAsFrontRobot2 {
    private static final boolean TC = false;

    @Override
    public void initialize() {
        R.pushFastMath(true);

        add(new Colorizer());

        MovieTracker tracker = new MovieTracker(105, 8);
        BulletManager bulletManager = new BulletManager();
        WaveManager waveManager = new WaveManager();
        StatTracker statTracker = StatTracker.getInstance();

        MonkFeet move = new MonkFeet(waveManager, statTracker);

        tracker.addListener(bulletManager);
        tracker.addListener(waveManager);

        PlayItForwardGun pifGun = new PifGun(null);

        MeleeGunArray meleeArray = new MeleeGunArray();
        meleeArray.addGun(pifGun);

        SwarmGun swarm = new SwarmGun(meleeArray, 100);
        swarm.setPowerSelector(new MonkPowerSelector());

        tracker.addListener(pifGun);

        waveManager.addListener(statTracker);
        if(!TC) waveManager.addListener(move);

        add(tracker);
        add(bulletManager);
        add(waveManager);
        add(statTracker);

        bulletManager.addListener(meleeArray);

        if(!TC) add(move);

        addListener(meleeArray);
        add(swarm);
        add(new MultiModeRadar());
    }

    class Colorizer extends Component implements RoundStartedListener {
        @Override
        public void onRoundStarted(int round) {
            RobotMediator mediator = getMediator();
            mediator.setBodyColor(new Color(255, 182, 135));
            mediator.setGunColor(new Color(165, 24, 6));
            mediator.setRadarColor(new Color(165, 24, 6));
            mediator.setScanColor(new Color(255, 0, 0));
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
            super(new DynamicClusteringPlayer() {
                @Override
                public KnnView<EnemyMovie> getNewKnnSet() {
                    return new KnnView<EnemyMovie>()
                            .setDistanceWeighter(new Knn.GaussDistanceWeighter<EnemyMovie>(1.0))
                            .add(new KnnTree<EnemyMovie>()
                                    .setMode(KnnTree.Mode.MANHATTAN)
                                    .setRatio(0.5)
                                    .setK(24)
                                    .setStrategy(new AntiRandomStrategy())
                                    .logsEverything());
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
                        1.0 / (1.0 + 2 * f.timeDecel),
                        1.0 / (1.0 + 2 * f.timeRevert),
                        Math.min(f.others - 1, 1),
                        Math.min(f.closestDistance / 400, 1),
                        (f.accel + 1) * .5,
                        f.heat() / 16,
                        Math.abs(f.velocity) / 8.,
                };
            }

            @Override
            public double[] getWeights() {
                return new double[]{2, 1, 2, 6, 2, 2, 4};
            }
        }
    }
}
