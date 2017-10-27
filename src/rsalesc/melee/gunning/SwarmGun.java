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
import rsalesc.baf2.BackAsFrontRobot2;
import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.controllers.Controller;
import rsalesc.baf2.core.listeners.FireEvent;
import rsalesc.baf2.core.listeners.FireListener;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.painting.G;
import rsalesc.baf2.painting.PaintManager;
import rsalesc.baf2.painting.Painting;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.EnemyTracker;
import rsalesc.mega.gunning.guns.AutomaticGun;
import rsalesc.mega.gunning.guns.GeneratedAngle;
import rsalesc.mega.utils.StatData;
import rsalesc.mega.utils.StatTracker;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Roberto Sales on 20/09/17.
 */
public class SwarmGun extends AutomaticGun implements FireListener {
    private static final int THRESHOLD = 10;

    private AutomaticGun gun;
    private boolean normalize = false;
    private int maxSumK;
    private int maxK;

    private GeneratedAngle[] lastGenerated;
    private GeneratedAngle[] lastFired;

    private GeneratedAngle lastPicked;
    private GeneratedAngle lastFirePicked;

    private Point lastFireSource;

    public SwarmGun(AutomaticGun gun, int maxSumK, int maxK) {
        if(gun != null && !(gun instanceof MeleeGun))
            throw new IllegalStateException();

        this.gun = gun;
        this.maxSumK = maxSumK;
        this.maxK = maxK;
    }

    public EnemyRobot[] getLatestSeen() {
        return EnemyTracker.getInstance().getLatest(getMediator().getTime() - THRESHOLD);
    }

    public int getCommonK() {
        EnemyRobot[] enemies = getLatestSeen();
        int res = maxSumK / (enemies.length == 0 ? 10 : enemies.length);

        for(EnemyRobot enemy : enemies) {
            res = Math.min(res, ((MeleeGun) getGun()).queryableData(EnemyTracker.getInstance().getLog(enemy)));
        }

        return Math.max(Math.min(res, maxK), 1);
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
                        EnemyRobot[] enemies = getLatestSeen();
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
    public void setupPaintings(PaintManager manager) {
        manager.add(KeyEvent.VK_P, "swarm", new Painting() {
            @Override
            public void paint(G g) {
                if(!(getGun() instanceof MeleeGun))
                    return;

                if(lastFirePicked != null) {
                    g.drawPoint(lastFireSource.project(lastFirePicked.angle, lastFirePicked.distance),
                            Physics.BOT_WIDTH * 2, new Color(118, 119, 119, 200));
                }
            }
        }, true);
    }

    @Override
    public GeneratedAngle[] generateFiringAngles(EnemyLog enemyLog, double power) {
        if(enemyLog != null)
            throw new IllegalStateException();

        EnemyRobot[] enemies = getLatestSeen();

        ArrayList<GeneratedAngle> everyAngle = new ArrayList<>();

        AutomaticGun gun = getGun();

        if(gun instanceof MeleeGun)
            ((MeleeGun) gun).setK(getCommonK());

        for(EnemyRobot enemy : enemies) {
            enemyLog = EnemyTracker.getInstance().getLog(enemy);
            GeneratedAngle[] angles  = gun.generateFiringAngles(enemyLog, power);

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

        return lastGenerated = everyAngle.toArray(new GeneratedAngle[0]);
    }

    @Override
    public double pickBestAngle(EnemyLog enemyLog, GeneratedAngle[] angles, double power) {
        int remaining = getMediator().getTicksToCool();

        // TODO: conditionally thrash if its better to thrash
        // TODO: focus enemies that i have high hit rate against (somehow virtualize shots to account that?)
        // TODO: maybe i'll have to develop a whole separate logic for PifGun

        double delta = Math.min(R.PI, Rules.GUN_TURN_RATE_RADIANS * Math.max(remaining * 1.1, 1));
        AngularRange range = new AngularRange(getMediator().getGunHeadingRadians(), -delta, +delta);

        double bestDensity = Double.NEGATIVE_INFINITY;
        GeneratedAngle bestAngle = null;

        for(int i = 0; i < 5; i++) {
            for (GeneratedAngle shootAngle : angles) {
                if (i < 4 && !range.isAngleNearlyContained(shootAngle.angle))
                    continue;

                double density = 0;
                for (GeneratedAngle candidate : angles) {
                    double distance = candidate.distance;
                    double angle = candidate.angle;
                    double off = R.normalRelativeAngle(shootAngle.angle - angle);

                    double x = off / (40 / distance);
                    if (Math.abs(x) < 1) {
                        density += R.cubicKernel(x) * candidate.weight / R.sqrt(distance);
                    }
                }

                if (density > bestDensity) {
                    bestDensity = density;
                    bestAngle = shootAngle;
                }
            }

            if(bestAngle != null) {
                lastPicked = bestAngle;
                return bestAngle.angle;
            }

            if(i == 0)
                range = new AngularRange(getMediator().getGunHeadingRadians(), -R.PI/4, +R.PI/4);
            else {
                range.min *= 2;
                range.max *= 2;
            }
        }

        BackAsFrontRobot2.warn("No angle was picked at SwarmGun.pickBestAngle, picking one arbitrarily.");
        return angles[(int) (angles.length * Math.random())].angle;
    }

    @Override
    public void onFire(FireEvent e) {
        lastFireSource = e.getSource();
        lastFired = lastGenerated;
        lastFirePicked = lastPicked;
    }
}
