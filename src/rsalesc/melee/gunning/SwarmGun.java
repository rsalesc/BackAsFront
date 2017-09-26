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

package rsalesc.melee.gunning;

import robocode.Rules;
import robocode.util.Utils;
import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.controllers.Controller;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.EnemyTracker;
import rsalesc.mega.gunning.guns.AutomaticGun;
import rsalesc.mega.gunning.guns.GeneratedAngle;
import rsalesc.mega.utils.StatData;
import rsalesc.mega.utils.StatTracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Roberto Sales on 20/09/17.
 */
public class SwarmGun extends AutomaticGun {
    private AutomaticGun gun;
    private boolean normalize = false;

    public SwarmGun(AutomaticGun gun) {
        this.gun = gun;
    }

    @Override
    public void init(RobotMediator mediator) {
        super.init(mediator);
        if(gun != null) {
            if(getPowerSelector() != null)
                gun.setPowerSelector(getPowerSelector());
            gun.init(mediator);
        }
    }

    public void normalize() { normalize = true; }

    public AutomaticGun getGun() {
        return gun;
    }

    @Override
    public StorageNamespace getStorageNamespace() {
        return getGlobalStorage().namespace("le-swarm-gun");
    }

    @Override
    public String getGunName() {
        return "Swarm Gun";
    }

    @Override
    public void run() {
        if(getPowerSelector() != null) {
            if(EnemyTracker.getInstance().sizeAlive() > 0) {
                Controller controller = getMediator().getAimControllerOrDummy();
                int toCool = getMediator().getTicksToCool();
                if(toCool <= 2 || toCool % 2 == 0) {
                    StatData data = StatTracker.getInstance().getCurrentStatData();
                    GeneratedAngle[] angles =
                            generateFiringAngles(null, lastPower = getPowerSelector().selectPower(getMediator(), data));

                    if (angles != null && angles.length > 0) {
                        controller.setGunTo(pickBestAngle(null, angles, lastPower));
                    } else {
                        // head-on backup
                        EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();
                        if (enemies.length > 0) {
                            Arrays.sort(enemies, new Comparator<EnemyRobot>() {
                                @Override
                                public int compare(EnemyRobot o1, EnemyRobot o2) {
                                    return (int) Math.signum(o1.getDistance() - o2.getDistance());
                                }
                            });

                            controller.setGunTo(Physics.absoluteBearing(getMediator().getNextPosition(), enemies[0].getPoint()));
                        }
                    }
                }
                controller.release();
            } else {
                lastPower = 0;
            }
        } else {
            lastPower = 0;
        }
    }

    @Override
    public GeneratedAngle[] generateFiringAngles(EnemyLog enemyLog, double power) {
        if(enemyLog != null)
            throw new IllegalStateException();

        EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();

        ArrayList<GeneratedAngle> everyAngle = new ArrayList<>();

        for(EnemyRobot enemy : enemies) {
            enemyLog = EnemyTracker.getInstance().getLog(enemy);
            GeneratedAngle[] angles  = getGun().generateFiringAngles(enemyLog, power);

            double sum = 1e-12;
            for(GeneratedAngle angle : angles) {
                sum += angle.weight;
            }

            for(GeneratedAngle angle : angles) {
                if(normalize)
                    angle.weight /= sum;
                everyAngle.add(angle);
            }
        }

        return everyAngle.toArray(new GeneratedAngle[0]);
    }

    @Override
    public double pickBestAngle(EnemyLog enemyLog, GeneratedAngle[] angles, double power) {
        int remaining = getMediator().getTicksToCool();

        double delta = Math.max(R.PI, Rules.GUN_TURN_RATE_RADIANS * Math.max(remaining * 1.1, 1));
        AngularRange range = new AngularRange(getMediator().getGunHeadingRadians(), -delta, +delta);

        double bestDensity = Double.NEGATIVE_INFINITY;
        GeneratedAngle bestAngle = null;

        for (GeneratedAngle shootAngle : angles) {
            if(!range.isAngleNearlyContained(shootAngle.angle))
                continue;

            double density = 0;
            for (GeneratedAngle candidate : angles) {
                double distance = candidate.distance;
                double angle = candidate.angle;
                double off = Utils.normalRelativeAngle(shootAngle.angle - angle);

                double x = off / (Physics.hitAngle(distance) * 0.9);
                if (Math.abs(x) < 1) {
                    density += R.cubicKernel(x) * candidate.weight / R.sqrt(distance);
                }
            }

            if (density > bestDensity) {
                bestDensity = density;
                bestAngle = shootAngle;
            }
        }

        return bestAngle.angle;
    }
}
