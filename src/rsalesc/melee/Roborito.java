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

import robocode.Rules;
import rsalesc.baf2.BackAsFrontRobot2;
import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.listeners.RoundStartedListener;
import rsalesc.baf2.waves.BulletManager;
import rsalesc.baf2.waves.WaveManager;
import rsalesc.mega.gunning.guns.AutomaticGunArray;
import rsalesc.mega.gunning.guns.KnnPlayer;
import rsalesc.mega.gunning.guns.HeadOnGun;
import rsalesc.mega.gunning.guns.PlayItForwardGun;
import rsalesc.mega.gunning.power.MirrorSwarmSelector;
import rsalesc.mega.gunning.power.PowerSelector;
import rsalesc.mega.tracking.EnemyMovie;
import rsalesc.mega.utils.StatTracker;
import rsalesc.mega.utils.Strategy;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.mega.utils.structures.Knn;
import rsalesc.mega.utils.structures.KnnView;
import rsalesc.mega.utils.structures.KnnTree;
import rsalesc.mega.tracking.MovieTracker;
import rsalesc.melee.movement.risk.MonkFeet;
import rsalesc.melee.radar.MultiModeRadar;

import java.awt.*;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public class Roborito extends BackAsFrontRobot2 {
    private static final boolean TC = false;

    @Override
    public void initialize() {
        add(new Colorizer());

        MovieTracker tracker = new MovieTracker(105, 8);
        BulletManager bulletManager = new BulletManager();
        WaveManager waveManager = new WaveManager();
        StatTracker statTracker = StatTracker.getInstance();

        AutomaticGunArray meleeArray = new GunArray();
        MonkFeet move = new MonkFeet(waveManager);

        MirrorSwarmSelector swarmSelector = new MirrorSwarmSelector();

        PifGun pifGun = new PifGun(null);

        meleeArray.addGun(pifGun);
        meleeArray.addGun(new HeadOnGun());
        meleeArray.log();

//        SegmentedSwarmGun swarm = new SegmentedSwarmGun();
//        swarm.setPowerSelector(swarmSelector);

//        swarm.addGun(meleeArray, 4);
//        swarm.addGun(pifGun, 0);

        tracker.addListener(bulletManager);
        tracker.addListener(waveManager);
        tracker.addListener(swarmSelector);
        tracker.addListener(pifGun);

        waveManager.addListener(statTracker);
        if(!TC) waveManager.addListener(move);

        bulletManager.addListener(meleeArray);

        add(tracker);
        add(bulletManager);
        add(waveManager);
        add(statTracker);

        addListener(swarmSelector);

        if(!TC) add(move);
//        add(swarm);
        add(new MultiModeRadar());
    }

    private static class Colorizer extends Component implements RoundStartedListener {
        @Override
        public void onRoundStarted(int round) {
            RobotMediator mediator = getMediator();
            mediator.setBodyColor(new Color(199, 9, 21));
            mediator.setGunColor(new Color(255, 196, 79));
            mediator.setRadarColor(new Color(255, 248, 179));
            mediator.setScanColor(new Color(255, 174, 5));
        }
    }

    private static class GunArray extends AutomaticGunArray {
        @Override
        public StorageNamespace getStorageNamespace() {
            return getGlobalStorage().namespace("roborito-array");
        }
    }

    private static class PifGun extends PlayItForwardGun {
        public PifGun(PowerSelector selector) {
            super(new KnnPlayer() {
                @Override
                public KnnView<EnemyMovie> getNewKnnSet() {
                    return new KnnView<EnemyMovie>()
                            .setDistanceWeighter(new Knn.GaussDistanceWeighter<EnemyMovie>(1.0))
                            .add(new KnnTree<EnemyMovie>()
                                .setMode(KnnTree.Mode.MANHATTAN)
                                .setRatio(0.4)
                                .setK(20)
                                .setStrategy(new AntiRandomStrategy()));
                }

                @Override
                public StorageNamespace getStorageNamespace() {
                    return this.getGlobalStorage().namespace("roborito-player");
                }
            });

            setPowerSelector(selector);
        }

        @Override
        public StorageNamespace getStorageNamespace() {
            return getGlobalStorage().namespace("roborito-gun");
        }

        private static class AntiRandomStrategy extends Strategy {

            @Override
            public double[] getQuery(TargetingLog f) {
                return new double[]{
                        1.0 / (1.0 + 2 * f.timeDecel),
                        Math.min(f.others - 1, 1),
                        Math.min(f.closestDistance / Rules.getBulletSpeed(f.bulletPower) / 80, 1),
                        Math.min(f.displaceLast10 / 80, 1),
                        (f.accel + 1) * .5,
                        f.heat() / 16,
                        Math.abs(f.velocity) / 8.,
                        Math.min(f.distanceToWall / 400, 1)
                };
            }

            @Override
            public double[] getWeights() {
                return new double[]{2, 3, 5, 2.5, 3, 1, 4, 2.5};
            }
        }
    }
}
