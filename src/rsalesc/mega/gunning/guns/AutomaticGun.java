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

package rsalesc.mega.gunning.guns;

import rsalesc.baf2.core.StoreComponent;
import rsalesc.baf2.core.controllers.Controller;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.EnemyTracker;
import rsalesc.mega.gunning.power.PowerSelector;
import rsalesc.mega.utils.StatTracker;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Roberto Sales on 15/09/17.
 */
public abstract class AutomaticGun extends StoreComponent {
    private PowerSelector powerSelector;
    protected double lastPower = 0;

    public String getGunName() {
        return "(unnamed gun)";
    }

    @Override
    public void beforeRun() {
        if(getMediator().getGunHeat() == 0 && getMediator().getGunTurnRemainingRadians() == 0 && lastPower > 0.09) {
            Controller controller = getMediator().getGunControllerOrDummy();
            controller.setFire(lastPower);
            controller.release();
        }
    }

    @Override
    public void run() {
        if(powerSelector != null) {
            EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();

            if(enemies.length > 0) {
                Controller controller = getMediator().getAimControllerOrDummy();

                // TODO: reactivate
                if(getMediator().getTicksToCool() <= 2 || getMediator().getTicksToCool() % 3 == 0) {
                    EnemyLog enemyLog = EnemyTracker.getInstance().getLog(enemies[0]);
                    GeneratedAngle[] angles = generateFiringAngles(enemyLog,
                            lastPower = powerSelector.selectPower(getMediator(), StatTracker.getInstance().getCurrentStatData()));

                    if (angles != null && angles.length > 0) {
                        controller.setGunTo(pickBestAngle(enemyLog, angles, lastPower));
                    } else {
                        controller.setGunTo(enemies[0].getAbsoluteBearing());
                        lastPower = 0;
                    }
                } else {
//                    controller.setGunTo(enemies[0].getAbsoluteBearing());
                    lastPower = 0;
                }

                controller.release();
            }
        } else {
            lastPower = 0;
        }
    }

    public double pickBestAngle(EnemyLog enemyLog, GeneratedAngle[] angles, double power) {
        Arrays.sort(angles, new Comparator<GeneratedAngle>() {
            @Override
            public int compare(GeneratedAngle o1, GeneratedAngle o2) {
                return o2.compareTo(o1);
            }
        });

        return angles[0].angle;
    }

    public abstract GeneratedAngle[] generateFiringAngles(EnemyLog enemyLog, double power);

    public void setPowerSelector(PowerSelector powerSelector) {
        this.powerSelector = powerSelector;
    }

    public PowerSelector getPowerSelector() {
        return powerSelector;
    }
}
