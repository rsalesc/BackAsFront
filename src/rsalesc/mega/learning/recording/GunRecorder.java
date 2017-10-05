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

package rsalesc.mega.learning.recording;

import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.listeners.TickListener;
import rsalesc.baf2.core.utils.Pair;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.waves.*;
import rsalesc.mega.gunning.guns.GuessFactorGun;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.runner.SerializeHelper;

import java.util.ArrayList;

/**
 * Created by Roberto Sales on 01/10/17.
 */
public class GunRecorder extends Component
        implements TickListener, TickBulletListener, BulletWaveListener, BulletWavePreciseListener {
    public static final String GUN_RECORD_HINT = "gun-recorder-events";
    private ArrayList<Pair<TargetingLog, BreakType>> accumulator = new ArrayList<>();
    private boolean logTicks = false;

    @Override
    public void afterRun() {
        getMediator().setDebugProperty(GUN_RECORD_HINT, SerializeHelper.convertToString(accumulator).get());
    }

    @Override
    public void onTick(long time) {
        accumulator.clear();
    }

    public void logTicks() {
        logTicks = true;
    }


    @Override
    public void onBulletWaveFired(BulletWave wave) {
        TargetingLog f = (TargetingLog) wave.getData(GuessFactorGun.LOG_HINT);
        if(f != null)
            accumulator.add(new Pair<>(f, BreakType.FIRED));
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
    public void onBulletWavePreciselyIntersects(BulletWave wave, EnemyRobot enemy, AngularRange intersection) {
        checkIntersection(wave);
    }

    @Override
    public void onTickWavePreciselyIntersects(TickWave wave, EnemyRobot enemy, AngularRange intersection) {
        if(!logTicks)
            return;
        checkIntersection(wave);
    }

    private void checkIntersection(RobotWave wave) {
        TargetingLog f = (TargetingLog) wave.getData(GuessFactorGun.LOG_HINT);
        if(f != null) {
            BreakType type = wave instanceof TickWave ? BreakType.VIRTUAL_BREAK :
                    (wave.hasHit() && !wave.hasBulletHit() ? BreakType.BULLET_HIT : BreakType.BULLET_BREAK);
            accumulator.add(new Pair<>(f, type));
        }
    }

    @Override
    public void onTickWaveFired(TickWave wave) {
        if(!logTicks)
            return;

        TargetingLog f = (TargetingLog) wave.getData(GuessFactorGun.LOG_HINT);
        if(f != null)
            accumulator.add(new Pair<>(f, BreakType.FIRED));
    }

    @Override
    public void onTickWaveBreak(TickWave wave, EnemyRobot enemy) {

    }
}
