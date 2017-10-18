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

package rsalesc.baf2.tracking;

import robocode.ScannedRobotEvent;
import rsalesc.baf2.core.listeners.BatchScannedRobotListener;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.waves.EnemyWave;
import rsalesc.baf2.waves.WaveManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roberto Sales on 10/10/17.
 */
public class MeleeTracker extends Tracker implements BatchScannedRobotListener {
    private final WaveManager waves;

    public MeleeTracker(WaveManager waves) {
        this.waves = waves;
        addListener(HeatTracker.getInstance());
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
//        super.onScannedRobot(e);
    }

    @Override
    public void onBatchScannedRobot(List<ScannedRobotEvent> events) {
        ArrayList<EnemyRobot> enemies = new ArrayList<>();
        for(ScannedRobotEvent e : events)
            enemies.add(EnemyTracker.getInstance().push(e, getMediator()));

        HeatTracker heatTracker = HeatTracker.getInstance();

        ArrayList<EnemyWave> waves = this.waves.getWaves();

        for(EnemyWave enemyWave : waves) {
            if(enemyWave.hasAnyHit())
                continue;

            EnemyRobot enemyHit = null;
            double waveDamage = enemyWave.getDamage();

            for(EnemyRobot enemy : enemies) {
                if(enemy.getName().equals(enemyWave.getEnemy().getName()))
                    continue;

                EnemyRobot lastEnemy = EnemyTracker.getInstance().getLog(enemy).atMostAt(getMediator().getTime() - 1);
                if(lastEnemy != null && lastEnemy.getTime() < getMediator().getTime() - Tracker.SEEN_THRESHOLD)
                    lastEnemy = null;

                if((lastEnemy == null || !enemyWave.getCircle(lastEnemy.getTime()).isInside(lastEnemy.getHitBox()))
                        && enemyWave.getCircle(getMediator().getTime()).countInside(enemy.getHitBox().getCorners()) > 0) {

                    double enemyDifferential = heatTracker.getDifferential(enemy);

                    if (R.isNear(enemyDifferential, -waveDamage)) {
                        enemyHit = enemy;
                        enemyWave.setCrossHit(enemy);
                        break;
                    }
                }
            }

            if(enemyHit != null) {
//                System.out.println(enemyHit.getName() + " was cross-hit!");

                for(Object listener : getListeners()) {
                    if(listener instanceof CrossFireListener) {
                        CrossFireListener lis = (CrossFireListener) listener;
                        lis.onCrossHit(enemyWave, enemyHit);
                    }
                }
            }
        }

        for(EnemyRobot enemy : enemies) {
            EnemyFireEvent fireEvent = HeatTracker.getInstance().push(enemy);
            if (fireEvent != null)
                onEnemyFire(fireEvent);
        }
    }

}
