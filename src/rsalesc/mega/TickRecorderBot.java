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
import rsalesc.baf2.waves.WaveManager;
import rsalesc.mega.gunning.AntiRandomGun;
import rsalesc.mega.gunning.RaikoGun;
import rsalesc.mega.gunning.guns.AutomaticGunArray;
import rsalesc.mega.gunning.power.*;
import rsalesc.mega.learning.recording.GunRecorder;
import rsalesc.mega.movement.KnightStance;
import rsalesc.mega.radar.PerfectLockRadar;
import rsalesc.mega.utils.StatTracker;

import java.awt.*;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public class TickRecorderBot extends BackAsFrontRobot2 {
    private boolean MC = false;
    private boolean TC = true;

    public void checkChallenges() {
        MC = MC || getName().endsWith("mc");
    }

    @Override
    public void initialize() {
        checkChallenges();

        add(new Colorizer());

        Tracker tracker = new Tracker();
        BulletManager bulletManager = new BulletManager();
        WaveManager waveManager = new WaveManager();

        StatTracker statTracker = StatTracker.getInstance();
        statTracker.log();

        PowerPredictor predictor = new DuelPowerPredictor();
        PowerSelector selector = TC ? new TCPowerSelector() : new MirrorPowerSelector(predictor);

        KnightStance move = new KnightStance(waveManager);

        AntiRandomGun randomGun = new AntiRandomGun(bulletManager, null);
//        AntiAdaptiveGun adaptiveGun = new AntiAdaptiveGun(bulletManager, null);
        AutomaticGunArray array = new AutomaticGunArray() {
            @Override
            public StorageNamespace getStorageNamespace() {
                return getGlobalStorage().namespace("knight-gun-array");
            }
        };

        array.setPowerSelector(selector);

        array.addGun(randomGun);
//        array.addGun(adaptiveGun);
        array.log();


        tracker.addListener(predictor);
        tracker.addListener(bulletManager);
        tracker.addListener(waveManager);

        waveManager.addListener(statTracker);
        if(!TC) waveManager.addListener(move);

        bulletManager.addListener(randomGun);
//        bulletManager.addListener(adaptiveGun);
        bulletManager.addListener(array);

        add(tracker);

        add(bulletManager);
        add(waveManager);

        add(statTracker);
        if(!TC) add(move);

        if(!MC) {
            add(array);

            GunRecorder recorder = new GunRecorder();
            recorder.logTicks();

            add(recorder);
            bulletManager.addListener(recorder);

            add(new PerfectLockRadar());
        } else {
            add(new RaikoGun());
        }
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
}
