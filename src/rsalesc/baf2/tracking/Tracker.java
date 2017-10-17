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

import robocode.*;
import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.listeners.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roberto Sales on 11/09/17.
 * TODO: handle onDeath event like damage
 */
public class Tracker extends Component implements TickListener, ScannedRobotListener, RoundStartedListener,
        RobotDeathListener, BulletListener, HitListener, EnemyFireListener {
    public static final int SEEN_THRESHOLD = 10;

    @Override
    public void onTick(long time) {
        MyLog.getInstance().push(new MyRobot(getMediator()));
        HeatTracker.getInstance().tick(getMediator().getTime());
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        EnemyRobot enemy = EnemyTracker.getInstance().push(e, getMediator());

        EnemyFireEvent fireEvent = HeatTracker.getInstance().push(enemy);
        if (fireEvent != null)
            onEnemyFire(fireEvent);
    }

    @Override
    public void onRoundStarted(int round) {
        EnemyTracker.getInstance().killAll();
        MyLog.getInstance().shrink(0);

        HeatTracker t = HeatTracker.getInstance();
        t.setCoolingRate(getMediator().getGunCoolingRate());
        t.setup();
    }

    @Override
    public void onRobotDeath(RobotDeathEvent e) {
        EnemyTracker.getInstance().kill(e.getName());
    }

    @Override
    public void onBulletHitBullet(BulletHitBulletEvent e) {

    }

    @Override
    public void onBulletHit(BulletHitEvent e) {
        HeatTracker.getInstance().onBulletHit(e);
    }

    @Override
    public void onBulletMissed(BulletMissedEvent e) {

    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        HeatTracker.getInstance().onHitByBullet(e);
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {

    }

    @Override
    public void onHitWall(HitWallEvent e) {

    }

    @Override
    public void onEnemyFire(EnemyFireEvent e) {
        for (Object obj : getListeners()) {
            if (obj instanceof EnemyFireListener) {
                EnemyFireListener listener = (EnemyFireListener) obj;
                listener.onEnemyFire(e);
            }
        }
    }
}
