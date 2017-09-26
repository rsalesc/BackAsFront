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

package rsalesc.melee.movement;

import robocode.BulletHitBulletEvent;
import robocode.HitByBulletEvent;
import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.listeners.PaintListener;
import rsalesc.baf2.core.listeners.RoundStartedListener;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.tracking.MyRobot;
import rsalesc.baf2.waves.EnemyWave;
import rsalesc.baf2.waves.EnemyWaveListener;
import rsalesc.baf2.waves.EnemyWavePreciseListener;
import rsalesc.baf2.waves.WaveManager;
import rsalesc.mega.movement.KnightSurfer;
import rsalesc.mega.movement.Surfer;
import rsalesc.mega.movement.TrueSurfing;
import rsalesc.mega.utils.StatTracker;

import java.awt.*;

/**
 * Created by Roberto Sales on 12/09/17.
 */
public class MonkFeet extends Component implements RoundStartedListener, PaintListener, EnemyWaveListener, EnemyWavePreciseListener {
    private MinimumRisk driver;
    private TrueSurfing surfing;

    public MonkFeet(WaveManager manager, StatTracker statTracker) {
        Surfer surfer = new KnightSurfer() {
            @Override
            public StorageNamespace getStorageNamespace() {
                return getGlobalStorage().namespace("knn-monk-feet");
            }
        };

        driver = new Monk1stGenMR();
        surfing = new TrueSurfing(surfer, manager, statTracker);
    }

    @Override
    public void init(RobotMediator mediator) {
        super.init(mediator);

        driver.init(getMediator());
        surfing.init(getMediator());
    }

    private boolean isMelee() {
        return getMediator().getOthers() > 1;
    }

    @Override
    public void run() {
        if (isMelee())
            driver.run();
        else
            surfing.run();
    }

    @Override
    public void onRoundStarted(int round) {
        driver.onRoundStarted(round);
    }


    @Override
    public void onPaint(Graphics2D gr) {
        if (isMelee())
            driver.onPaint(gr);
        else
            surfing.onPaint(gr);
    }

    @Override
    public void onEnemyWaveFired(EnemyWave wave) {
        if (!isMelee())
            surfing.onEnemyWaveFired(wave);
    }

    @Override
    public void onEnemyWaveBreak(EnemyWave wave, MyRobot me) {
        if (!isMelee())
            surfing.onEnemyWaveBreak(wave, me);
    }

    @Override
    public void onEnemyWaveHitMe(EnemyWave wave, HitByBulletEvent e) {
        if (!isMelee())
            surfing.onEnemyWaveHitMe(wave, e);
    }

    @Override
    public void onEnemyWaveHitBullet(EnemyWave wave, BulletHitBulletEvent e) {
        if (!isMelee())
            surfing.onEnemyWaveHitBullet(wave, e);
    }

    @Override
    public void onEnemyWavePass(EnemyWave wave, MyRobot me) {
        if(!isMelee())
            surfing.onEnemyWavePass(wave, me);
    }

    @Override
    public void onEnemyWavePreciselyIntersects(EnemyWave wave, MyRobot me, AngularRange intersection) {
        if(!isMelee())
            surfing.onEnemyWavePreciselyIntersects(wave, me, intersection);
    }
}
