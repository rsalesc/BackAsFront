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

package rsalesc.mega.movement;

import robocode.BulletHitBulletEvent;
import robocode.HitByBulletEvent;
import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.listeners.RoundStartedListener;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.tracking.MyRobot;
import rsalesc.baf2.waves.EnemyWave;
import rsalesc.baf2.waves.EnemyWaveListener;
import rsalesc.baf2.waves.EnemyWavePreciseListener;
import rsalesc.baf2.waves.WaveManager;

/**
 * Created by Roberto Sales on 13/09/17.
 */
public class KnightStance extends Component implements RoundStartedListener, EnemyWaveListener, EnemyWavePreciseListener {
    private TrueSurfing surfing;

    public KnightStance(WaveManager manager) {
        Surfer surfer = new OldKnightDCSurfer() {
            @Override
            public StorageNamespace getStorageNamespace() {
                return getGlobalStorage().namespace("knight-stance-surfer");
            }
        };

        surfing = new TrueSurfing(surfer, manager);
    }

    @Override
    public void onRoundStarted(int round) {
        surfing.init(getMediator());
    }

    @Override
    public void run() {
        surfing.run();
    }

    @Override
    public void onEnemyWaveFired(EnemyWave wave) {
        surfing.onEnemyWaveFired(wave);
    }

    @Override
    public void onEnemyWaveBreak(EnemyWave wave, MyRobot me) {
        surfing.onEnemyWaveBreak(wave, me);
    }

    @Override
    public void onEnemyWaveHitMe(EnemyWave wave, HitByBulletEvent e) {
        surfing.onEnemyWaveHitMe(wave, e);
    }

    @Override
    public void onEnemyWaveHitBullet(EnemyWave wave, BulletHitBulletEvent e) {
        surfing.onEnemyWaveHitBullet(wave, e);
    }

    @Override
    public void onEnemyWavePass(EnemyWave wave, MyRobot me) {
        surfing.onEnemyWavePass(wave, me);
    }

    @Override
    public void onEnemyWavePreciselyIntersects(EnemyWave wave, MyRobot me, AngularRange intersection) {
        surfing.onEnemyWavePreciselyIntersects(wave, me, intersection);
    }
}
