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

package rsalesc.baf2.waves;

import robocode.*;
import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.listeners.*;
import rsalesc.baf2.core.utils.BattleTime;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.painting.G;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.EnemyTracker;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public class BulletManager extends Component implements FireListener, PaintListener, ScannedRobotListener,
        BulletListener, TickListener {
    private ArrayList<BulletWave> waves = new ArrayList<>();
    private ArrayList<TickWave> tickWaves = new ArrayList<>();
    private boolean checked = false;
    private double lastPower = 0;

    @Override
    public void onFire(FireEvent e) {
        BulletWave wave = new BulletWave(e.getSource(), new BattleTime(e.getTime(), getMediator().getRoundNum()),
                e.getSpeed(), e.getHeadingRadians());
        waves.add(wave);

        for (Object obj : getListeners()) {
            if (obj instanceof BulletWaveListener) {
                BulletWaveListener listener = (BulletWaveListener) obj;
                listener.onBulletWaveFired(wave);
            }
        }

        lastPower = e.getPower();

        tickWaves.removeIf(new Predicate<TickWave>() {
            @Override
            public boolean test(TickWave tickWave) {
                return tickWave.getTime() == e.getTime();
            }
        });
    }

    public ArrayList<BulletWave> getWaves() {
        ArrayList<BulletWave> res = new ArrayList<>();
        res.addAll(waves);
        return res;
    }

    @Override
    public void beforeRun() {
        if (!checked)
            check();
    }

    @Override
    public void afterRun() {
        checked = false;
    }

    @Override
    public void onPaint(Graphics2D gr) {
        G g = new G(gr);
        long time = getMediator().getTime();

        for (BulletWave wave : waves) {
            g.drawCircle(wave.getSource(), wave.getDistanceTraveled(time), Color.DARK_GRAY);
            g.drawRadial(wave.getSource(), wave.getAngle(), 0, wave.getDistanceTraveled(time), Color.DARK_GRAY);
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        if (!checked)
            check();
    }

    public boolean hasPreciseListener() {
        for (Object obj : getListeners()) {
            if (obj instanceof BulletWavePreciseListener) {
                return true;
            }
        }

        return false;
    }

    public void check() {
        checked = true;
        long time = getMediator().getTime();
        double longestSide = Math.max(
                getMediator().getBattleField().getWidth(),
                getMediator().getBattleField().getHeight()
        );

        ArrayList<RobotWave> allWaves = new ArrayList<>();
        allWaves.addAll(waves);
        allWaves.addAll(tickWaves);

        Iterator<RobotWave> it = allWaves.iterator();
        while (it.hasNext()) {
            RobotWave wave = it.next();

            EnemyTracker tracker = EnemyTracker.getInstance();
            EnemyRobot[] enemies = tracker.getLatest();

            for (EnemyRobot enemy : enemies) {
                AxisRectangle hitbox = enemy.getHitBox();
                EnemyRobot pastEnemy = tracker.getLog(enemy).getKthLatest(2);

                if (wave.getCircle(time).isInside(enemy.getPoint()) && (pastEnemy == null ||
                        !wave.getCircle(time - 1).isInside(pastEnemy.getPoint()))) {

                    if(wave instanceof BulletWave) {
                        for (Object obj : getListeners()) {
                            if (obj instanceof BulletWaveListener) {
                                BulletWaveListener listener = (BulletWaveListener) obj;
                                listener.onBulletWaveBreak((BulletWave) wave, enemy);
                            }
                        }
                    } else if(wave instanceof TickWave) {
                        for (Object obj : getListeners()) {
                            if (obj instanceof TickBulletListener) {
                                TickBulletListener listener = (TickBulletListener) obj;
                                listener.onTickWaveBreak((TickWave) wave, enemy);
                            }
                        }
                    }
                }

                if (wave.getCircle(time).isInside(hitbox) && (pastEnemy == null ||
                        !wave.getCircle(time - 1).isInside(pastEnemy.getHitBox()))) {

                    if(wave instanceof BulletWave) {
                        for (Object obj : getListeners()) {
                            if (obj instanceof BulletWaveListener) {
                                BulletWaveListener listener = (BulletWaveListener) obj;
                                listener.onBulletWavePass((BulletWave) wave, enemy);
                            }
                        }
                    }

                    if(hasPreciseListener()) {
                        AngularRange intersection =
                                Wave.preciseIntersection(wave, EnemyTracker.getInstance().getLog(enemy), getMediator().getTime());
                        for (Object obj : getListeners()) {
                            if (obj instanceof BulletWavePreciseListener) {
                                BulletWavePreciseListener listener = (BulletWavePreciseListener) obj;
                                if(wave instanceof BulletWave) {
                                    listener.onBulletWavePreciselyIntersects((BulletWave) wave, enemy, intersection);
                                } else if(wave instanceof TickWave) {
                                    listener.onTickWavePreciselyIntersects((TickWave) wave, enemy, intersection);
                                }
                            }
                        }
                    }
                }
            }

            if (wave.getDistanceTraveled(time) > 2 * longestSide) {
                if(wave instanceof TickWave)
                    tickWaves.remove(wave);
                else if(wave instanceof BulletWave)
                    waves.remove(wave);
                it.remove();
            }
        }
    }

    @Override
    public void onBulletHitBullet(BulletHitBulletEvent e) {
        for (BulletWave wave : waves) {
            if (wave.wasFiredBy(e.getBullet(), e.getTime())) {
                wave.setBulletHit(e.getHitBullet());
                wave.setHitTime(getMediator().getTime());

                for (Object obj : getListeners()) {
                    if (obj instanceof BulletWaveListener) {
                        BulletWaveListener listener = (BulletWaveListener) obj;
                        listener.onBulletWaveHitBullet(wave, e);
                    }
                }

                return;
            }
        }

        System.out.println("Didnt found wave for bullet hit bullet!");
    }

    @Override
    public void onBulletHit(BulletHitEvent e) {
        for (BulletWave wave : waves) {
            if (wave.wasFiredBy(e.getBullet(), e.getTime())) {
                wave.setHit(e.getBullet());
                wave.setHitTime(getMediator().getTime());

                for (Object obj : getListeners()) {
                    if (obj instanceof BulletWaveListener) {
                        BulletWaveListener listener = (BulletWaveListener) obj;
                        listener.onBulletWaveHitEnemy(wave, e);
                    }
                }

                return;
            }
        }

        System.out.println("Didnt found wave for bullet hit enemy!");
    }

    @Override
    public void onBulletMissed(BulletMissedEvent e) {
        Iterator<BulletWave> it = waves.iterator();
        while (it.hasNext()) {
            BulletWave wave = it.next();

            if (!wave.hasAnyHit() && wave.wasFiredBy(e.getBullet(), e.getTime())) {
                wave.setMissed(true);
                return;
            }
        }

        System.out.println("Didnt found wave for missed bullet!");
    }

    @Override
    public void onTick(long time) {
        if(lastPower > 0) {
            TickWave wave = new TickWave(getMediator().getPoint(), getMediator().getBattleTime(), Rules.getBulletSpeed(lastPower));
            tickWaves.add(wave);
            for (Object obj : getListeners()) {
                if (obj instanceof TickBulletListener) {
                    TickBulletListener listener = (TickBulletListener) obj;
                    listener.onTickWaveFired(wave);
                }
            }
        }
    }
}
