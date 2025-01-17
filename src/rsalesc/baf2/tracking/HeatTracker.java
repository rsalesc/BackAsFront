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
import rsalesc.baf2.core.utils.PredictedHashMap;
import rsalesc.baf2.waves.EnemyWave;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Roberto Sales on 29/08/17.
 */
public class HeatTracker implements CrossFireListener {
    private static final HeatTracker SINGLETON = new HeatTracker();

    double coolingRate = 0.1;

    private HashMap<String, HeatLog> logs;

    private HeatTracker() {
        logs = new PredictedHashMap<>(15);
    }

    public static HeatTracker getInstance() {
        return SINGLETON;
    }

    public Set<Map.Entry<String, HeatLog>> entries() {
        return logs.entrySet();
    }

    public void setup() {
        for (Map.Entry<String, HeatLog> entry : logs.entrySet()) {
            entry.getValue().setup();
        }
    }

    public void tick(long time) {
        for (Map.Entry<String, HeatLog> entry : logs.entrySet()) {
            entry.getValue().tick(time);
        }
    }

    public HeatLog ensure(String name) {
        if (!logs.containsKey(name)) {
            HeatLog log = new HeatLog();
            log.setCoolingRate(coolingRate);
            log.setup();
            logs.put(name, log);
            return log;
        }
        return logs.get(name);
    }

    public void setCoolingRate(double x) {
        coolingRate = x;
    }

    public HeatLog ensure(EnemyRobot e) {
        return ensure(e.getName());
    }

    public double getDifferential(EnemyRobot e) {
        return ensure(e).getDifferential(e);
    }

    public EnemyFireEvent push(EnemyRobot e) {
        return ensure(e).push(e);
    }

    public void onBulletHit(BulletHitEvent e) {
        ensure(e.getName()).onBulletHit(e);
    }

    public void onHitByBullet(HitByBulletEvent e) {
        ensure(e.getName()).onHitByBullet(e);
    }

    @Override
    public void onCrossHit(EnemyWave wave, RobotSnapshot hitEnemy) {
        ensure(hitEnemy.getName()).lostEnergy(wave.getDamage());
        ensure(wave.getEnemy()).gainedEnergy(wave.getBonus());
    }
}
