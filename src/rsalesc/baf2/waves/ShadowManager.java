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
import robocode.HitByBulletEvent;
import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.listeners.PaintListener;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.painting.G;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.MyRobot;

import java.awt.*;

/**
 * Created by Roberto Sales on 28/09/17.
 */
public class ShadowManager extends Component implements BulletWaveListener, EnemyWaveListener, PaintListener {
    private final BulletManager bulletManager;
    private final WaveManager waveManager;

    public ShadowManager(BulletManager bulletManager, WaveManager waveManager) {
        this.bulletManager = bulletManager;
        this.waveManager = waveManager;

        bulletManager.addListener(this);
        waveManager.addListener(this);
    }

    @Override
    public void onBulletWaveFired(BulletWave bullet) {
        for(EnemyWave wave : waveManager.getWaves())
            wave.cast(bullet);
    }

    @Override
    public void onBulletWaveBreak(BulletWave wave, EnemyRobot enemy) {

    }

    @Override
    public void onBulletWaveHitEnemy(BulletWave bullet, BulletHitEvent e) {
        for(EnemyWave wave : waveManager.getWaves())
            wave.cast(bullet);
    }

    @Override
    public void onBulletWaveHitBullet(BulletWave bullet, BulletHitBulletEvent e) {
        for(EnemyWave wave : waveManager.getWaves())
            wave.cast(bullet);
    }

    @Override
    public void onBulletWavePass(BulletWave wave, EnemyRobot enemy) {

    }

    @Override
    public void onEnemyWaveFired(EnemyWave wave) {
        for(BulletWave bullet : bulletManager.getWaves())
            wave.cast(bullet);
    }

    @Override
    public void onEnemyWaveBreak(EnemyWave wave, MyRobot me) {

    }

    @Override
    public void onEnemyWaveHitMe(EnemyWave wave, HitByBulletEvent e) {

    }

    @Override
    public void onEnemyWaveHitBullet(EnemyWave wave, BulletHitBulletEvent e) {

    }

    @Override
    public void onEnemyWavePass(EnemyWave wave, MyRobot me) {

    }

    @Override
    public void onPaint(Graphics2D gr) {
        G g = new G(gr);

        long time = getMediator().getTime();
        for(EnemyWave wave : waveManager.getWaves()) {
            for(Shadow shadow : wave.getShadows()) {
                Point p1 = wave.project(shadow.getIntersection().getStartingAngle(), time);
                Point p2 = wave.project(shadow.getIntersection().getEndingAngle(), time);
                g.drawLine(p1, p2, Color.MAGENTA);
            }
        }
    }
}
