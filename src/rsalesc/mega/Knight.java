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
import rsalesc.baf2.tracking.Tracker;
import rsalesc.baf2.waves.BulletManager;
import rsalesc.baf2.waves.ShadowManager;
import rsalesc.baf2.waves.WaveManager;
import rsalesc.mega.gunning.AntiAdaptiveGun;
import rsalesc.mega.gunning.RaikoGun;
import rsalesc.mega.gunning.SlowDecayGun;
import rsalesc.mega.gunning.guns.*;
import rsalesc.mega.gunning.power.*;
import rsalesc.mega.gunning.strategies.dc.GeneralPurposeStrategy;
import rsalesc.mega.movement.KnightStance;
import rsalesc.mega.radar.PerfectLockRadar;
import rsalesc.mega.tracking.EnemyMovie;
import rsalesc.mega.utils.StatTracker;
import rsalesc.structures.Knn;
import rsalesc.structures.KnnTree;
import rsalesc.structures.KnnView;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Roberto Sales on 11/09/17.
 * TODO: targetinglog is a bit weird with the new time-since stuff
 *
 */
public class Knight extends BackAsFrontRobot2 {
    private boolean MC2k6 = false;
    private boolean MC = false || MC2k6;
    private boolean TC = false;

    public void checkChallenges() {
        if(getName().split(" ").length > 1) {
            MC = MC || getName().split(" ")[1].startsWith("MC");
            MC2k6 = MC && getName().split(" ")[1].endsWith("2k6");
            TC = TC || getName().split(" ")[1].startsWith("TC");
        }
    }

    @Override
    public void initialize() {

        checkChallenges();

        add(new Colorizer());
        
        Tracker tracker = new Tracker();
        BulletManager bulletManager = new BulletManager();
        WaveManager waveManager = new WaveManager();
        ShadowManager shadowManager = new ShadowManager(bulletManager, waveManager);

        StatTracker statTracker = StatTracker.getInstance();
        statTracker.log();

        PowerPredictor predictor = new DuelPowerPredictor();
        PowerSelector selector = TC ? new TCPowerSelector() : new MirrorPowerSelector(predictor);

        KnightStance move = new KnightStance(waveManager);

        AutomaticGun generalPurposeGun = new SlowDecayGun(bulletManager, null);
        AutomaticGun adaptiveGun = new AntiAdaptiveGun(bulletManager, null);

        AutomaticGunArray array = new GunArray(generalPurposeGun, adaptiveGun, new RandomGun());

        if(!MC) {
            array.setPowerSelector(selector);
            array.log();
        }

        tracker.heat(predictor);
        tracker.addListener(predictor);
        tracker.addListener(bulletManager);
        tracker.addListener(waveManager);

        waveManager.addListener(statTracker);
        if(!TC) waveManager.addListener(move);

        if(!MC) {
            bulletManager.addListener(generalPurposeGun);
            bulletManager.addListener(adaptiveGun);
            bulletManager.addListener(array);
        }

        add(tracker);

        add(bulletManager);
        add(waveManager);
        add(shadowManager);

        add(statTracker);
        if(!TC) add(move);

        if(!MC) {
            add(array);
        } else if(!MC2k6) {
            add(new RaikoGun());
        }

        add(new PerfectLockRadar());
        if(selector instanceof MirrorPowerSelector)
            addListener((MirrorPowerSelector) selector);
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

    class GunArray extends AutomaticGunArray {
        GunArray(AutomaticGun generalPurpose, AutomaticGun antiSurfer, AutomaticGun random) {
            if(generalPurpose != null) addGun(generalPurpose);
            if(antiSurfer != null) addGun(antiSurfer);
            if(random != null) addGun(random);

            if(getGuns().size() == 0)
                throw new IllegalStateException("GunArray can't be empty");

            this.setPicker(new GunPicker() {
                @Override
                public AutomaticGun apply(ArrayList<GunScorePair> gunScorePairs) {
                    int round = getMediator().getRoundNum();
                    GunScorePair gpScore = AutomaticGunArray.getGunScorePair(gunScorePairs, generalPurpose);
                    GunScorePair asScore = AutomaticGunArray.getGunScorePair(gunScorePairs, antiSurfer);
                    GunScorePair randomScore = AutomaticGunArray.getGunScorePair(gunScorePairs, random);

                    double maxHits = 0;

                    if(asScore != null)
                        maxHits = Math.max(maxHits, asScore.score);
                    if(randomScore != null)
                        maxHits = Math.max(maxHits, randomScore.score);

                    if(gpScore != null && (
                            (maxHits * 0.8 <= gpScore.score && round < 7)
                            || (maxHits * 0.9 <= gpScore.score && round < 15) ||

                            maxHits <= gpScore.score
                            )) {
                        return generalPurpose;
                    } else if(asScore != null && asScore.score >= maxHits)
                        return antiSurfer;
                    else if(random != null)
                        return random;
                    else
                        return generalPurpose;
                }
            });
        }

        @Override
        public StorageNamespace getStorageNamespace() {
            return getGlobalStorage().namespace("kga");
        }
    }
}
