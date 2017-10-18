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

        for(EnemyRobot enemy : enemies) {
            // check if last enemy wasn't inside of this wave and if so, check hit
            EnemyRobot lastEnemy = EnemyTracker.getInstance().getLog(enemy).atMostAt(getMediator().getTime() - 1);
            if(lastEnemy != null && lastEnemy.getTime() < getMediator().getTime() - Tracker.SEEN_THRESHOLD)
                lastEnemy = null;

            boolean isCool = heatTracker.ensure(enemy).isCool();

            EnemyWave bestWave = null;
            EnemyWave bestBothWave = null;

            for(EnemyWave enemyWave : waves) {
                if(enemyWave.hasAnyHit() && enemy.getName().equals(enemyWave.getEnemy().getName()))
                    continue;

                double waveDamage = enemyWave.getDamage();

                if ((lastEnemy == null || !enemyWave.getCircle(lastEnemy.getTime()).isInside(lastEnemy.getHitBox()))
                        && enemyWave.getCircle(getMediator().getTime()).countInside(enemy.getHitBox().getCorners()) > 0) {

                    double enemyDifferential = heatTracker.getDifferential(enemy);

                    if (R.isNear(enemyDifferential, -waveDamage)) {
                        bestWave = enemyWave;
                        break;
                    }

                    if ((isCool && waveDamage > 3.001 && waveDamage < -enemyDifferential - 0.01 && -enemyDifferential < waveDamage + 3)) {
                        bestBothWave = enemyWave;
                    }
                }
            }

            if(bestWave == null)
                bestWave = bestBothWave;

            if(bestWave != null) {
                bestWave.setCrossHit(enemy);

                for(Object listener : getListeners()) {
                    if(listener instanceof CrossFireListener) {
                        CrossFireListener lis = (CrossFireListener) listener;
                        lis.onCrossHit(bestWave, enemy);
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
