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

import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import rsalesc.baf2.tracking.EnemyRobot;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public interface BulletWaveListener {
    void onBulletWaveFired(BulletWave wave);

    void onBulletWaveBreak(BulletWave wave, EnemyRobot enemy);

    void onBulletWaveHitEnemy(BulletWave wave, BulletHitEvent e);

    void onBulletWaveHitBullet(BulletWave wave, BulletHitBulletEvent e);

    void onBulletWavePass(BulletWave wave, EnemyRobot enemy);
}
