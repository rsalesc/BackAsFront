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

import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import rsalesc.baf2.core.listeners.PaintListener;
import rsalesc.baf2.core.listeners.TickListener;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.painting.G;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.EnemyTracker;
import rsalesc.baf2.waves.*;
import rsalesc.mega.utils.TargetingLog;

import java.awt.*;

/**
 * Created by Roberto Sales on 15/09/17.
 */
public abstract class GuessFactorGun extends AutomaticGun
        implements BulletWaveListener, BulletWavePreciseListener, PaintListener, TickBulletListener {
    public static final String LOG_HINT = "loghint";

    private final GFTargeting targeting;
    private TargetingLog lastTargetingLog;
    private BulletManager waves;

    @Override
    public String getGunName() {
        return "GuessFactor Gun";
    }

    public GuessFactorGun(GFTargeting targeting, BulletManager waves) {
        this.targeting = targeting;
        this.waves = waves;
    }

    public BulletManager getManager() {
        return waves;
    }

    @Override
    public GeneratedAngle[] generateFiringAngles(EnemyLog enemyLog, double power) {
        if(enemyLog == null) {
            EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();
            if(enemies.length == 0)
                return new GeneratedAngle[0];
            enemyLog = EnemyTracker.getInstance().getLog(enemies[0]);
        }

        if(!enemyLog.isAlive())
            return new GeneratedAngle[0];

        lastTargetingLog = TargetingLog.getLog(enemyLog.getLatest(), getMediator(), power, true);

        return targeting.getFiringAngles(enemyLog, lastTargetingLog);
    }

    public void checkBulletIntersection(RobotWave wave, EnemyRobot enemy, AngularRange intersection) {
        TargetingLog f = (TargetingLog) wave.getData(LOG_HINT);

        if(f == null)
            return;

        f.hitAngle = Physics.absoluteBearing(wave.getSource(), enemy.getPoint());
        f.hitDistance = wave.getSource().distance(enemy.getPoint());

        f.preciseIntersection = intersection;
        if(f.preciseIntersection == null) {
            double hitAngle = Physics.hitAngle(f.hitDistance);
            f.preciseIntersection = new AngularRange(f.hitAngle, -hitAngle, +hitAngle);
        }

        BreakType type = wave instanceof TickWave ? BreakType.VIRTUAL_BREAK :
                (wave.hasHit() && !wave.hasBulletHit() ? BreakType.BULLET_HIT : BreakType.BULLET_BREAK);

        targeting.log(EnemyTracker.getInstance().getLog(enemy), f, type);
    }

    @Override
    public void onBulletWavePreciselyIntersects(BulletWave wave, EnemyRobot enemy, AngularRange intersection) {
        checkBulletIntersection(wave, enemy, intersection);
    }

    public void checkBulletFired(RobotWave wave) {
        if(lastTargetingLog != null && lastTargetingLog.time + 1 == getMediator().getTime()) {
            wave.setData(LOG_HINT, lastTargetingLog);
        }
    }

    @Override
    public void onTickWaveFired(TickWave wave) {
        EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();
        if(enemies.length == 0)
            return;

        TargetingLog f = TargetingLog.getLog(enemies[0], getMediator(), wave.getPower(), false);
        wave.setData(LOG_HINT, f);
    }

    @Override
    public void onTickWaveBreak(TickWave wave, EnemyRobot enemy) {

    }

    @Override
    public void onBulletWaveFired(BulletWave wave) {
        checkBulletFired(wave);
    }

    @Override
    public void onTickWavePreciselyIntersects(TickWave wave, EnemyRobot enemy, AngularRange intersection) {
        checkBulletIntersection(wave, enemy, intersection);
    }

    @Override
    public void onBulletWaveBreak(BulletWave wave, EnemyRobot enemy) {
    }

    @Override
    public void onBulletWaveHitEnemy(BulletWave wave, BulletHitEvent e) {

    }

    @Override
    public void onBulletWaveHitBullet(BulletWave wave, BulletHitBulletEvent e) {

    }

    @Override
    public void onBulletWavePass(BulletWave wave, EnemyRobot enemy) {

    }

    @Override
    public void onPaint(Graphics2D gr) {
        G g = new G(gr);

        for(BulletWave wave : getManager().getWaves()) {
            TargetingLog f = (TargetingLog) wave.getData(LOG_HINT);
            if(f == null)
                continue;

            double dt = wave.getDistanceTraveled(getMediator().getTime());

            if(f.preciseIntersection != null) {
                g.drawRadial(wave.getSource(), f.preciseIntersection.getStartingAngle(), dt, dt+8, Color.RED);
                g.drawRadial(wave.getSource(), f.preciseIntersection.getEndingAngle(), dt, dt+8, Color.RED);
            }
        }
    }
}
