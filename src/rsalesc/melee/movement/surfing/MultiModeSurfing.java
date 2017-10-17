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

package rsalesc.melee.movement.surfing;

import robocode.BulletHitBulletEvent;
import robocode.HitByBulletEvent;
import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.tracking.CrossFireListener;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.MyRobot;
import rsalesc.baf2.waves.EnemyWave;
import rsalesc.baf2.waves.EnemyWaveListener;
import rsalesc.baf2.waves.EnemyWavePreciseListener;
import rsalesc.mega.movement.BaseSurfing;

// TODO: fix painting
// TODO: create a generic conditioned multi-component component
public class MultiModeSurfing extends Component implements EnemyWaveListener, EnemyWavePreciseListener, CrossFireListener {
    private final MeleeSurfing melee;
    private final BaseSurfing duel;

    public MultiModeSurfing(MeleeSurfing melee, BaseSurfing duel) {
        this.melee = melee;
        this.duel = duel;
    }

    @Override
    public void init(RobotMediator mediator) {
        super.init(mediator);
        melee.init(mediator);
        duel.init(mediator);
    }

    public void run() {
        if(getMediator().getOthers() > 1)
            melee.run();
        else
            duel.run();
    }

    public void onCrossHit(EnemyWave wave, EnemyRobot hitEnemy) {
        melee.onCrossHit(wave, hitEnemy);
    }

    public void onEnemyWaveFired(EnemyWave wave) {
        if(getMediator().getOthers() > 1)
            melee.onEnemyWaveFired(wave);
        else
            duel.onEnemyWaveFired(wave);
    }

    public void onEnemyWaveBreak(EnemyWave wave, MyRobot me) {
        if(getMediator().getOthers() > 1)
            melee.onEnemyWaveBreak(wave, me);
        else
            duel.onEnemyWaveBreak(wave, me);
    }

    public void onEnemyWaveHitMe(EnemyWave wave, HitByBulletEvent e) {
        if(getMediator().getOthers() > 1)
            melee.onEnemyWaveHitMe(wave, e);
        else
            duel.onEnemyWaveHitMe(wave, e);
    }

    public void onEnemyWavePreciselyIntersects(EnemyWave wave, MyRobot me, AngularRange intersection) {
        if(getMediator().getOthers() == 1)
            duel.onEnemyWavePreciselyIntersects(wave, me, intersection);
    }

    public void onEnemyWaveHitBullet(EnemyWave wave, BulletHitBulletEvent e) {
        if(getMediator().getOthers() > 1)
            melee.onEnemyWaveHitBullet(wave, e);
        else
            duel.onEnemyWaveHitBullet(wave, e);
    }

    public void onEnemyWavePass(EnemyWave wave, MyRobot me) {
        if(getMediator().getOthers() > 1)
            melee.onEnemyWavePass(wave, me);
        else
            duel.onEnemyWavePass(wave, me);
    }
}
