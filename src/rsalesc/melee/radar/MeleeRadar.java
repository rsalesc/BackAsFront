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

package rsalesc.melee.radar;

import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.controllers.Controller;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.EnemyTracker;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Roberto Sales on 12/09/17.
 */
public class MeleeRadar extends Component {
    private EnemyRobot lastEnemy;
    private double lastSignal;

    @Override
    public void run() {
        EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest(getMediator().getTime() - 8);
        Arrays.sort(enemies, new Comparator<EnemyRobot>() {
            @Override
            public int compare(EnemyRobot o1, EnemyRobot o2) {
                return (int) (o1.getTime() - o2.getTime());
            }
        });

        Controller controller = getMediator().getRadarControllerOrDummy();

        lastEnemy = null;

        if (enemies.length < getMediator().getOthers() || enemies.length == 0) {
            controller.setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
        } else {
            double absBearing = enemies[0].getAbsoluteBearing();

            double signal = enemies[0] != lastEnemy ? Math.signum(R.normalRelativeAngle(
                    absBearing - getMediator().getRadarHeadingRadians()
            )) : lastSignal;

            controller.setTurnRadarRightRadians(Double.POSITIVE_INFINITY * signal);

            lastSignal = signal;
            lastEnemy = enemies[0];
        }

        controller.release();
    }
}
