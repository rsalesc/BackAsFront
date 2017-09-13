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

import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.Rules;
import rsalesc.baf2.core.utils.BattleTime;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;

/**
 * Created by Roberto Sales on 29/08/17.
 */
public class HeatLog {
    double coolingRate = 0.1;

    EnemyRobot lastEnemy;
    double lastEnergy;

    BattleTime lastShot;
    double lastPower;
    long currentTime;

    public HeatLog() {
    }

    public void setup() {
        currentTime = 0;
        lastShot = new BattleTime(-1L, 0);
        lastPower = 10.5;
    }

    public void tick(long time) {
        currentTime = time;
    }

    public void setCoolingRate(double x) {
        coolingRate = x;
    }

    public boolean hasShot(long time) {
        return lastShot != null && lastShot.getTime() == time;
    }

    public boolean hasShot(BattleTime battleTime) {
        return lastShot != null && battleTime.equals(lastShot);
    }

    public boolean hasJustCooled() {
        return ticksToCool() == 0 && ticksSinceCool() == 0;
    }

    public double getLastShotPower() {
        if (lastShot == null)
            throw new IllegalStateException();
        return lastPower;
    }

    public long getLastShotTime() {
        if (lastShot == null)
            throw new IllegalStateException();
        return lastShot.getTime();
    }

    public EnemyFireEvent push(EnemyRobot enemy) {
        EnemyFireEvent fireEvent = checkShot(enemy);
        lastEnemy = enemy;
        lastEnergy = lastEnemy.getEnergy();

        return fireEvent;
    }

    private EnemyFireEvent checkShot(EnemyRobot enemy) {
        double energyDelta = enemy.getEnergy() - lastEnergy;
        if (ticksSinceCool() > 0 && R.nearOrBetween(-Physics.MAX_POWER, energyDelta, -Physics.MIN_POWER)) {
            long lastCool = currentTime - ticksSinceCool();
            EnemyFireEvent fireEvent = new EnemyFireEvent(enemy, Math.max(lastCool, lastEnemy.getTime()), -energyDelta);

            lastPower = -energyDelta;
            lastShot = new BattleTime(fireEvent.getTime(), enemy.getBattleTime().getRound());

            return fireEvent;
        }

        return null;
    }

    public boolean isCool() {
        return ticksToCool() == 0;
    }

    public long ticksToCool() {
        return (long) Math.ceil(getHeat() / coolingRate);
    }

    public long ticksSinceCool() {
        long ticksNeeded = (long) Math.ceil((1.0 + lastPower / 5) / coolingRate);
        return Math.max(-1, currentTime - (lastShot.getTime() + ticksNeeded));
    }

    public void onHitByBullet(HitByBulletEvent e) {
        lastEnergy += Rules.getBulletHitBonus(e.getBullet().getPower());
    }

    public void onBulletHit(BulletHitEvent e) {
        lastEnergy -= Rules.getBulletDamage(e.getBullet().getPower());
    }

    public double getHeat() {
        return Math.max(getUnconstrainedHeat(), 0.0);
    }

    private double getUnconstrainedHeat() {
        return 1.0 + lastPower / 5 - (currentTime - lastShot.getTime()) * coolingRate;
    }
}
