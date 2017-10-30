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

import rsalesc.baf2.BackAsFrontRobot2;
import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.listeners.RoundStartedListener;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.tracking.Tracker;
import rsalesc.baf2.waves.BulletManager;
import rsalesc.baf2.waves.ShadowManager;
import rsalesc.baf2.waves.WaveManager;
import rsalesc.mega.gunning.AntiAdaptiveGun;
import rsalesc.mega.gunning.AntiRandomGun;
import rsalesc.mega.gunning.RaikoGun;
import rsalesc.mega.gunning.guns.AutomaticGunArray;
import rsalesc.mega.gunning.guns.KnnPlayer;
import rsalesc.mega.gunning.guns.PlayItForwardGun;
import rsalesc.mega.gunning.power.MirrorPowerSelector;
import rsalesc.mega.gunning.power.PowerSelector;
import rsalesc.mega.gunning.power.TCPowerSelector;
import rsalesc.mega.gunning.strategies.dc.GeneralPurposeStrategy;
import rsalesc.mega.movement.KnightStance;
import rsalesc.mega.radar.PerfectLockRadar;
import rsalesc.mega.tracking.EnemyMovie;
import rsalesc.mega.utils.StatTracker;
import rsalesc.structures.Knn;
import rsalesc.structures.KnnTree;
import rsalesc.structures.KnnView;

import java.awt.*;
import java.util.Comparator;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public class Knight extends BackAsFrontRobot2 {
    private boolean MC2k6 = false;
    private boolean MC = false || MC2k6;
    private boolean TC = true;

    public void checkChallenges() {
        MC = MC || getName().endsWith("mc");
    }

    @Override
    public void initialize() {

        checkChallenges();

        add(new Colorizer());

//	    MovieTracker movieTracker = new MovieTracker(105, 20, 8);
        
        Tracker tracker = new Tracker();
        BulletManager bulletManager = new BulletManager();
        WaveManager waveManager = new WaveManager();
        ShadowManager shadowManager = new ShadowManager(bulletManager, waveManager);

        StatTracker statTracker = StatTracker.getInstance();
        statTracker.log();

        PowerSelector selector = TC ? new TCPowerSelector() : new MirrorPowerSelector();

        KnightStance move = new KnightStance(waveManager);

//        ExperimentalPifRandomGun randomGun = new ExperimentalPifRandomGun();
        AntiRandomGun randomGun = new AntiRandomGun(bulletManager, null);
        AntiAdaptiveGun adaptiveGun = new AntiAdaptiveGun(bulletManager, null);
        AutomaticGunArray array = new AutomaticGunArray() {
            @Override
            public StorageNamespace getStorageNamespace() {
                return getGlobalStorage().namespace("knight-gun-array");
            }
        };

        if(!MC) {
            array.setPowerSelector(selector);

            array.addGun(randomGun);
//            array.addGun(adaptiveGun);
            array.log();
        }

        if(selector instanceof MirrorPowerSelector)
            tracker.addListener(selector);

        tracker.addListener(bulletManager);
        tracker.addListener(waveManager);

        waveManager.addListener(statTracker);
        if(!TC) waveManager.addListener(move);

        if(!MC) {
            bulletManager.addListener(randomGun);
//            bulletManager.addListener(adaptiveGun);
            bulletManager.addListener(array);
        }

        add(tracker);
//        add(movieTracker);

        if(selector instanceof MirrorPowerSelector)
            addListener((MirrorPowerSelector) selector);

        add(bulletManager);
        add(waveManager);
        add(shadowManager);
        
//        movieTracker.addListener(randomGun);

        add(statTracker);
        if(!TC) add(move);

//        addListener(randomGun);

        if(!MC) {
            array.setComparator(new Comparator<AutomaticGunArray.GunScorePair>() {
                @Override
                public int compare(AutomaticGunArray.GunScorePair o1, AutomaticGunArray.GunScorePair o2) {
                    boolean invert = o1.gun == randomGun;

                    AutomaticGunArray.GunScorePair ro = !invert ? o1 : o2;
                    AutomaticGunArray.GunScorePair ao = !invert ? o2 : o1;

                    int round = array.getMediator().getRoundNum();
                    int res = 1;

                    if(round < 2 || ro.score > ao.score
                            || (round <= 8 && ro.score > 4 * ao.score)
                            || (round <= 15 && ro.score > 8 * ao.score)
                            || (R.isNear(ao.score, 0)))
                        res = -1;

                    return res * (invert ? -1 : 1);
                }
            });

            add(array);
        } else if(!MC2k6) {
            add(new RaikoGun());
        }

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
