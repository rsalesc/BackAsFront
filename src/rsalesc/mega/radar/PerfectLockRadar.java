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

package rsalesc.mega.radar;

import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.controllers.Controller;
import rsalesc.baf2.core.listeners.ScannedRobotListener;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.EnemyTracker;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Roberto Sales on 12/09/17.
 */
public class PerfectLockRadar extends Component implements ScannedRobotListener {
    private boolean lostScan = true;
    private int direction = 1;

    @Override
    public void run() {
        Controller controller = getMediator().getRadarControllerOrDummy();
        if (lostScan) {
            controller.setTurnRadarRightRadians(Double.POSITIVE_INFINITY * -direction);
        } else {
            EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();
            if (enemies.length > 0) {
                Arrays.sort(enemies, new Comparator<EnemyRobot>() {
                    @Override
                    public int compare(EnemyRobot o1, EnemyRobot o2) {
                        return (int) Math.signum(o1.getDistance() - o2.getDistance());
                    }
                });

                EnemyRobot enemy = enemies[0];

                double turn = Utils.normalRelativeAngle(enemy.getAbsoluteBearing() - getMediator().getRadarHeadingRadians());
                controller.setTurnRadarRightRadians(turn * 2);
            }
        }

        if(getMediator().getRadarTurnRemainingRadians() == 0)
            controller.setTurnGunRightRadians(Math.toRadians(1));

        controller.release();
    }

    @Override
    public void afterRun() {
        lostScan = true;
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        lostScan = false;
        direction *= -1;
    }
}
