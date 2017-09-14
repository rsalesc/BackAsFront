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
import rsalesc.baf2.core.listeners.RoundStartedListener;
import rsalesc.baf2.tracking.Tracker;
import rsalesc.baf2.waves.BulletManager;
import rsalesc.baf2.waves.WaveManager;
import rsalesc.mega.movement.KnightStance;
import rsalesc.mega.radar.PerfectLockRadar;
import rsalesc.mega.utils.StatTracker;
import rsalesc.mega.utils.WinDance;
import rsalesc.melee.gunning.MonkGun;
import rsalesc.melee.gunning.MovieTracker;
import rsalesc.melee.movement.MonkFeet;
import rsalesc.melee.radar.MultiModeRadar;

import java.awt.*;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public class Knight extends BackAsFrontRobot2 {
    @Override
    public void initialize() {
        add(new Colorizer());
        add(new WinDance());

        MovieTracker tracker = new MovieTracker(90, 2);
        BulletManager bulletManager = new BulletManager();
        WaveManager waveManager = new WaveManager();
        StatTracker statTracker = new StatTracker();

        KnightStance move = new KnightStance(waveManager, statTracker);
        MonkGun gun = new MonkGun();

        tracker.addListener(bulletManager);
        tracker.addListener(waveManager);
        tracker.addListener(gun);

        waveManager.addListener(statTracker);
        waveManager.addListener(move);

        add(tracker);
        add(bulletManager);
        add(waveManager);
        add(statTracker);

        add(move);
        add(gun);
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
}
