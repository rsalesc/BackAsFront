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

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import rsalesc.baf2.BackAsFrontRobot2;
import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.StoreComponent;
import rsalesc.baf2.core.controllers.RadarController;
import rsalesc.baf2.core.listeners.HitListener;
import rsalesc.baf2.core.listeners.ScannedRobotListener;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.tracking.*;
import rsalesc.baf2.waves.BulletManager;
import rsalesc.baf2.waves.WaveManager;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public class Baf extends BackAsFrontRobot2 {
    @Override
    public void initialize() {
        Tracker tracker = new Tracker();
        Gun gun = new Gun();
        Movement movement = new Movement();
        WaveManager waveManager = new WaveManager();
        BulletManager bulletManager = new BulletManager();

        tracker.addListener(movement);
        tracker.addListener(waveManager);
        tracker.addListener(bulletManager);
        bulletManager.addListener(gun);

        add(tracker);
        add(waveManager);
        add(bulletManager);
        add(movement);
        add(gun);
        add(new Radar());
    }

    class Radar extends Component implements ScannedRobotListener {
        private boolean lostScan = true;

        @Override
        public void run() {
            RadarController controller = getMediator().getRadarController();

            if (lostScan)
                controller.setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            else {
                EnemyRobot[] latest = EnemyTracker.getInstance().getLatest();
                if (latest.length > 0) {
                    controller.setTurnRadarRightRadians(R.normalRelativeAngle(latest[0].getAbsoluteBearing()
                            - getMediator().getRadarHeadingRadians()) * 1.99999999);
                }
            }

            controller.release();
        }

        @Override
        public void afterRun() {
            lostScan = true;
        }

        @Override
        public void onScannedRobot(ScannedRobotEvent e) {
            lostScan = false;
        }
    }

    class Movement extends Component implements HitListener, EnemyFireListener {
        private double perp = R.HALF_PI;

        @Override
        public void run() {
            setBackAsFront(perp);
        }

        @Override
        public void onHitByBullet(HitByBulletEvent e) {

        }

        @Override
        public void onHitRobot(HitRobotEvent e) {

        }

        @Override
        public void onHitWall(HitWallEvent e) {
            perp = -perp;
            setBackAsFront(perp);
        }

        @Override
        public void onEnemyFire(EnemyFireEvent e) {
            perp = -perp;
            setBackAsFront(perp);
        }
    }

    class Gun extends StoreComponent {
        @Override
        public void beforeRun() {
            setFire(selectPower());
        }

        public double selectPower() {
            return 3.0;
        }

        @Override
        public void run() {

        }

        @Override
        public StorageNamespace getStorageNamespace() {
            return getGlobalStorage().namespace("pif-gun");
        }
    }
}
