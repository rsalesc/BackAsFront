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

import robocode.ScannedRobotEvent;
import rsalesc.baf2.core.GlobalStorage;
import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.StorageNamespace;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Roberto Sales on 21/07/17.
 */
public class EnemyTracker {
    private HashMap<String, EnemyLog> seenEnemies;

    private EnemyTracker() {
        seenEnemies = new HashMap<>();
    }

    public static EnemyTracker getInstance() {
        StorageNamespace ns = GlobalStorage.getInstance().namespace("tracker");
        EnemyTracker tracker = (EnemyTracker) ns.get("enemytracker");
        if (tracker == null) {
            tracker = new EnemyTracker();
            ns.put("enemytracker", tracker);
        }

        return tracker;
    }

    public int size() {
        return seenEnemies.size();
    }

    public int sizeSeen() {
        int res = 0;
        for (Map.Entry<String, EnemyLog> entry : seenEnemies.entrySet()) {
            if (entry.getValue().getLatest() != null)
                res++;
        }

        return res;
    }

    public int sizeAlive() {
        int res = 0;
        for (Map.Entry<String, EnemyLog> entry : seenEnemies.entrySet()) {
            if (entry.getValue().isAlive())
                res++;
        }

        return res;
    }

    public EnemyRobot push(ScannedRobotEvent e, RobotMediator from) {
        if (!seenEnemies.containsKey(e.getName())) {
            EnemyLog log = new EnemyLog(e.getName());
            EnemyRobot res = log.push(e, from);
            seenEnemies.put(e.getName(), log);
            return res;
        } else {
            return seenEnemies.get(e.getName()).push(e, from);
        }
    }

    public EnemyLog getLog(String name) {
        return seenEnemies.get(name);
    }

    public EnemyLog getLog(ScannedRobotEvent e) {
        return getLog(e.getName());
    }

    public EnemyLog getLog(EnemyRobot e) {
        return getLog(e.getName());
    }

    public EnemyRobot getLatestState(String name) {
        return getLog(name).getLatest();
    }

    public EnemyRobot getLatestState(ScannedRobotEvent e) {
        return getLatestState(e.getName());
    }

    public EnemyRobot getLatestState(EnemyRobot e) {
        return getLatestState(e.getName());
    }

    public void clear() {
        seenEnemies.clear();
    }

    public EnemyRobot[] getLatest() {
        EnemyRobot[] res = new EnemyRobot[sizeAlive()];

        int cnt = 0;
        for (Map.Entry<String, EnemyLog> entry : seenEnemies.entrySet()) {
            if (entry.getValue().isAlive()) {
                res[cnt++] = entry.getValue().getLatest();
            }
        }

        Arrays.sort(res, new LatestSeenComparator());

        return res;
    }

    public EnemyRobot[] getLatestDeadOrAlive() {
        EnemyRobot[] res = new EnemyRobot[sizeSeen()];
        int cnt = 0;
        for (Map.Entry<String, EnemyLog> entry : seenEnemies.entrySet()) {
            if(entry.getValue().getLatest() != null)
                res[cnt++] = entry.getValue().getLatest();
        }

        Arrays.sort(res, new LatestSeenComparator());

        return res;

    }

    public void kill(String name) {
        if (getLog(name) != null)
            getLog(name).kill();
    }

    public void killAll() {
        for (EnemyRobot enemy : getLatest()) {
            getLog(enemy).clear();
            getLog(enemy).kill();
        }
    }

    private static class LatestSeenComparator implements Comparator<EnemyRobot> {

        @Override
        public int compare(EnemyRobot o1, EnemyRobot o2) {
            return -o1.getBattleTime().compareTo(o2.getBattleTime());
        }
    }
}
