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
import rsalesc.baf2.core.listeners.BulletListener;
import rsalesc.baf2.core.listeners.HitListener;
import rsalesc.baf2.core.listeners.ScannedRobotListener;
import rsalesc.baf2.core.utils.BattleTime;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.painting.G;
import rsalesc.baf2.painting.PaintManager;
import rsalesc.baf2.painting.Painting;
import rsalesc.baf2.predictor.PredictedPoint;
import rsalesc.baf2.tracking.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public class WaveManager extends Component implements EnemyFireListener, EnemyWaveListener,
        ScannedRobotListener, BulletListener, HitListener, EnemyWavePreciseListener, EnemyCooledListener, EnemyPreCooledListener {
    private boolean checked = false;
    private ArrayList<EnemyWave> waves = new ArrayList<>();

    @Override
    public void onEnemyFire(EnemyFireEvent e) {
        BattleTime battleTime = new BattleTime(e.getTime(), getMediator().getRoundNum());

        EnemyWave wave = isHeat(e) ? new HeatWave(e.getEnemy(), e.getSource(), battleTime, e.getSpeed()) :
                new EnemyWave(e.getEnemy(), e.getSource(), battleTime, e.getSpeed());

        waves.removeIf(new Predicate<EnemyWave>() {
            @Override
            public boolean test(EnemyWave enemyWave) {
                return enemyWave.getEnemy().getName().equals(e.getEnemy().getName()) && enemyWave.isHeat();
            }
        });

        waves.add(wave);

        onEnemyWaveFired(wave);
    }

    private boolean isHeat(EnemyFireEvent e) {
        return e instanceof EnemyCooledEvent;
    }

    public boolean hasPreciseListener() {
        for (Object obj : getListeners()) {
            if (obj instanceof EnemyWavePreciseListener) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void beforeRun() {
        check();
    }

    @Override
    public void afterRun() {
        checked = false;
    }

    public ArrayList<EnemyWave> getWaves() {
        ArrayList<EnemyWave> res = new ArrayList<>();
        res.addAll(waves);

        return res;
    }

    public void check() {
        if (checked)
            return;

        checked = true;
        long time = getMediator().getTime();
        double longestSide = Math.max(
                getMediator().getBattleField().getWidth(),
                getMediator().getBattleField().getHeight()
        );

        MyLog log = MyLog.getInstance();
        MyRobot me = log.getLatest();

        MyRobot pastMe = log.getKthLatest(2);

        AxisRectangle hitbox = me.getHitBox();

        Iterator<EnemyWave> it = waves.iterator();
        while (it.hasNext()) {
            EnemyWave wave = it.next();

            if (wave.getCircle(time).isInside(me.getPoint()) && (pastMe == null ||
                    !wave.getCircle(time - 1).isInside(pastMe.getPoint()))) {

                onEnemyWaveBreak(wave, me);
            }

            if (wave.getCircle(time).isInside(hitbox) && (pastMe == null ||
                    !wave.getCircle(time - 1).isInside(pastMe.getHitBox()))) {

                onEnemyWavePass(wave, me);
            }

            if (wave.getDistanceTraveled(time) > 2 * longestSide) {
                it.remove();
            }
        }
    }

    @Override
    public void setupPaintings(PaintManager manager) {
        manager.add(KeyEvent.VK_W, "waves", new Painting() {
            @Override
            public void paint(G g) {
                long time = getMediator().getTime();

                for (EnemyWave wave : waves) {
                    if(wave.everyoneInside(getMediator()))
                        continue;

                    g.drawCircle(wave.getSource(), wave.getDistanceTraveled(time), Color.DARK_GRAY);
                }
            }
        }, true);
    }

    @Override
    public void onEnemyWaveFired(EnemyWave wave) {
        for (Object object : getListeners()) {
            if (object instanceof EnemyWaveListener) {
                EnemyWaveListener listener = (EnemyWaveListener) object;
                listener.onEnemyWaveFired(wave);
            }
        }
    }

    @Override
    public void onEnemyWaveBreak(EnemyWave wave, MyRobot me) {
        for (Object object : getListeners()) {
            if (object instanceof EnemyWaveListener) {
                EnemyWaveListener listener = (EnemyWaveListener) object;
                listener.onEnemyWaveBreak(wave, me);
            }
        }
    }

    @Override
    public void onEnemyWaveHitMe(EnemyWave wave, HitByBulletEvent e) {
        for (Object object : getListeners()) {
            if (object instanceof EnemyWaveListener) {
                EnemyWaveListener listener = (EnemyWaveListener) object;
                listener.onEnemyWaveHitMe(wave, e);
            }
        }
    }

    @Override
    public void onEnemyWaveHitBullet(EnemyWave wave, BulletHitBulletEvent e) {
        for (Object object : getListeners()) {
            if (object instanceof EnemyWaveListener) {
                EnemyWaveListener listener = (EnemyWaveListener) object;
                listener.onEnemyWaveHitBullet(wave, e);
            }
        }
    }

    @Override
    public void onEnemyWavePass(EnemyWave wave, MyRobot me) {
        for (Object object : getListeners()) {
            if (object instanceof EnemyWaveListener) {
                EnemyWaveListener listener = (EnemyWaveListener) object;
                listener.onEnemyWavePass(wave, me);
            }
        }

        if(hasPreciseListener()) {
            onEnemyWavePreciselyIntersects(wave, me, Wave.preciseIntersection(wave, MyLog.getInstance(), getMediator().getTime()));
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        check();
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        for (EnemyWave wave : waves) {
            if (wave.wasFiredBy(e.getBullet(), e.getTime())) {
                wave.setHit(e.getBullet());
                wave.setHitTime(getMediator().getTime());

                onEnemyWaveHitMe(wave, e);
                break;
            }
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {

    }

    @Override
    public void onHitWall(HitWallEvent e) {

    }

    @Override
    public void onBulletHitBullet(BulletHitBulletEvent e) {
        for (EnemyWave wave : waves) {
            if (wave.wasFiredBy(e.getBullet(), e.getTime())) {
                wave.setBulletHit(e.getHitBullet());
                wave.setHitTime(getMediator().getTime());

                onEnemyWaveHitBullet(wave, e);
                break;
            }
        }
    }

    @Override
    public void onBulletHit(BulletHitEvent e) {

    }

    @Override
    public void onBulletMissed(BulletMissedEvent e) {
    }

    public EnemyWave[] earliestWaves(int K, Point dest, long time, EnemyWaveCondition condition) {
        K = Math.min(K, waves.size());
        ArrayList<EnemyWave> res = new ArrayList<>();
        HashSet<EnemyWave> seen = new HashSet<>();
        for (int i = 0; i < K; i++) {
            double earliestTime = Double.POSITIVE_INFINITY;
            EnemyWave earliest = null;

            for (EnemyWave wave : waves) {
                if (!seen.contains(wave) && condition.test(wave)) {
                    double breakAt = wave.getBreakTime(dest);
                    if (breakAt >= time && breakAt < earliestTime) {
                        earliestTime = breakAt;
                        earliest = wave;
                    }
                }
            }

            if (earliest != null) {
                seen.add(earliest);
                res.add(earliest);
            }
        }

        return res.toArray(new EnemyWave[0]);
    }

    public EnemyWave earliestWave(MyRobot me, EnemyRobot enemy, long time, EnemyWaveCondition condition) {
        EnemyWave[] waves = earliestWaves(1, me.getPoint(), time, new EnemyWaveCondition() {
            @Override
            public boolean test(EnemyWave wave) {
                return wave.getEnemy().getName().equals(enemy.getName()) && condition.test(wave);
            }
        });

        if (waves.length == 0)
            return null;

        return waves[0];
    }

    public EnemyWave earliestWave(MyRobot me, long time, EnemyWaveCondition condition) {
        EnemyWave[] waves = earliestWaves(1, me.getPoint(), time, new EnemyWaveCondition() {
            @Override
            public boolean test(EnemyWave wave) {
                return condition.test(wave);
            }
        });

        if (waves.length == 0)
            return null;

        return waves[0];
    }

    public EnemyWave earliestWave(PredictedPoint predictedPoint, long time, EnemyWaveCondition condition) {
        EnemyWave[] waves = earliestWaves(1, predictedPoint, time, new EnemyWaveCondition() {
            @Override
            public boolean test(EnemyWave wave) {
                return condition.test(wave);
            }
        });

        if (waves.length == 0)
            return null;

        return waves[0];
    }

    public EnemyWave earliestWave(PredictedPoint predictedPoint, EnemyWaveCondition condition) {
        return earliestWave(predictedPoint, predictedPoint.time, condition);
    }

    public EnemyWave earliestWave(MyRobot me, EnemyRobot enemy, EnemyWaveCondition condition) {
        return earliestWave(me, enemy, me.getTime(), condition);
    }

    public EnemyWave earliestWave(MyRobot me, EnemyRobot enemy, long time) {
        return earliestWave(me, enemy, time, new EnemyWaveCondition() {
            @Override
            public boolean test(EnemyWave wave) {
                return true;
            }
        });
    }

    public EnemyWave earliestWave(MyRobot me, EnemyRobot enemy) {
        return earliestWave(me, enemy, me.getTime());
    }

    @Override
    public void onEnemyWavePreciselyIntersects(EnemyWave wave, MyRobot me, AngularRange intersection) {
        for (Object object : getListeners()) {
            if (object instanceof EnemyWavePreciseListener) {
                EnemyWavePreciseListener listener = (EnemyWavePreciseListener) object;
                listener.onEnemyWavePreciselyIntersects(wave, me, intersection);
            }
        }
    }

    @Override
    public void onEnemyCooled(EnemyCooledEvent e) {
//        System.out.println("Creating gunheat wave!");
        onEnemyFire(e);
    }

    @Override
    public void onEnemyPreCooled(EnemyCooledEvent e) {
//        System.out.println("Creating pre-gunheat wave!");
        onEnemyFire(e);
    }
}
