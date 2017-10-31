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

package rsalesc.mega;

import jk.mega.gun.DrussGunDC;
import rsalesc.baf2.BackAsFrontRobot2;
import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.listeners.RoundStartedListener;
import rsalesc.baf2.tracking.Tracker;
import rsalesc.mega.gunning.guns.KnnPlayer;
import rsalesc.mega.gunning.guns.PlayItForwardGun;
import rsalesc.mega.gunning.strategies.dc.GeneralPurposeStrategy;
import rsalesc.mega.radar.PerfectLockRadar;
import rsalesc.mega.tracking.EnemyMovie;
import rsalesc.structures.Knn;
import rsalesc.structures.KnnTree;
import rsalesc.structures.KnnView;

import java.awt.*;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public class DrussTest extends BackAsFrontRobot2 {
    @Override
    public void initialize() {
        add(new Colorizer());
        
        Tracker tracker = new Tracker();
//        BulletManager bulletManager = new BulletManager();
//        WaveManager waveManager = new WaveManager();
//        ShadowManager shadowManager = new ShadowManager(bulletManager, waveManager);

//        StatTracker statTracker = StatTracker.getInstance();
//        statTracker.log();

//        waveManager.addListener(statTracker);

        add(tracker);

//        add(bulletManager);
//        add(waveManager);
//        add(shadowManager);

//        add(statTracker);

        add(new DrussGunDC());

        add(new PerfectLockRadar());
    }

    class Colorizer extends Component implements RoundStartedListener {
        @Override
        public void onRoundStarted(int round) {
            RobotMediator mediator = getMediator();
            mediator.setBodyColor(new Color(16, 16, 16));
            mediator.setGunColor(new Color(0, 0, 0));
            mediator.setRadarColor(new Color(46, 9, 2));
            mediator.setScanColor(new Color(98, 99, 99));
        }
    }

    class ExperimentalPifRandomGun extends PlayItForwardGun {

        public ExperimentalPifRandomGun() {
            super(new KnnPlayer() {
                @Override
                public KnnView<EnemyMovie> getNewKnnSet() {
                    return new KnnView<EnemyMovie>()
                            .add(new KnnTree<EnemyMovie>()
                                .setMode(KnnTree.Mode.MANHATTAN)
                                .setK(48)
                                .setStrategy(new GeneralPurposeStrategy())
                                .setRatio(0.1)
                                .logsEverything());
                }

                @Override
                public Knn.DistanceWeighter<EnemyMovie> getLazyWeighter() {
                    return new Knn.InverseDistanceWeighter<>(1.0);
                }

                @Override
                public StorageNamespace getStorageNamespace() {
                    return this.getGlobalStorage().namespace("knn-rz");
                }
            });
        }

        @Override
        public StorageNamespace getStorageNamespace() {
            return this.getGlobalStorage().namespace("knn-rr");
        }
    }
}
