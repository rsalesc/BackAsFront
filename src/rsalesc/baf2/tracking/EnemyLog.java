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

/**
 * Created by Roberto Sales on 21/07/17.
 */

import robocode.ScannedRobotEvent;
import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.utils.BattleTime;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This class assumes that the enemy logs
 * will be added in increasing order of time
 */

public class EnemyLog implements RobotLog {
    private static int LOG_SIZE = 2000;
    private static double RAM_THRESHOLD = 6.25;

    private final String name;
    private long approaching = 0;
    private long everLength = 0;
    private double distanceSum = 0;

    private EnemyRobot[] log;
    private int length;
    private int removed;
    private boolean alive = false;

    public EnemyLog(String name) {
        this.name = name;
        log = new EnemyRobot[LOG_SIZE];
        removed = 0;
        length = 0;
    }

    public String getName() {
        return name;
    }

    public boolean isAlive() {
        return alive;
    }

    public void kill() {
        alive = false;
    }

    private int realAt(int i) {
        int idx = (removed + i) % LOG_SIZE;
        if (idx < 0)
            idx += LOG_SIZE;
        return idx;
    }

    public EnemyRobot getLatest() {
        if (length == 0)
            return null;
        return log[realAt(length - 1)];
    }


    /**
     * @param k is 1-indexed
     * @return
     */
    public EnemyRobot getKthLatest(int k) {
        if (length < k)
            return null;
        return log[realAt(length - k)];
    }

    public EnemyRobot getAtLeastKthLatest(int k) {
        if (length < k)
            k = length;
        return log[realAt(length - k)];
    }

    @Override
    public EnemyRobot exactlyAt(long time) {
        EnemyRobot res = atLeastAt(time);
        if(res == null || res.getTime() != time)
            return null;
        return res;
    }

    @Override
    public EnemyRobot atLeastAt(long time) {
        if (length == 0)
            return null;

        int latestRound = log[realAt(length - 1)].getBattleTime().getRound();

        int l = 0, r = length;
        while (l < r) {
            int mid = (l + r) / 2;
            EnemyRobot cur = log[realAt(mid)];
            BattleTime curBattleTime = cur.getBattleTime();
            if (curBattleTime.getRound() >= latestRound && curBattleTime.getTime() >= time)
                r = mid;
            else
                l = mid + 1;
        }

        if (l == length)
            return null;

        return log[realAt(l)];
    }

    public EnemyRobot atMostAt(long time) {
        if (length == 0)
            return null;

        int latestRound = log[realAt(length - 1)].getBattleTime().getRound();

        int l = 0, r = length;
        while (l < r) {
            int mid = (l + r) / 2;
            EnemyRobot cur = log[realAt(mid)];
            BattleTime curBattleTime = cur.getBattleTime();
            if (curBattleTime.getRound() >= latestRound)
                r = mid;
            else
                l = mid + 1;
        }

        if (l == length)
            return null;

        int baseL = l;
        l--;
        r = length - 1;
        while (l < r) {
            int mid = (l + r + 1) / 2;
            EnemyRobot cur = log[realAt(mid)];
            BattleTime curBattleTime = cur.getBattleTime();
            if (curBattleTime.getTime() <= time)
                l = mid;
            else r = mid - 1;
        }

        if (l < baseL)
            return null;

        return log[realAt(l)];
    }

    public EnemyRobot before(RobotSnapshot robot) {
        return atMostAt(robot.getTime() - 1);
    }

    public EnemyRobot after(RobotSnapshot robot) {
        return atLeastAt(robot.getTime() + 1);
    }

    public EnemyRobot at(int i) throws ArrayIndexOutOfBoundsException {
        if (i >= length)
            throw new ArrayIndexOutOfBoundsException();
        return log[realAt(i)];
    }

    public boolean isEmpty() {
        return length == 0;
    }

    public int size() {
        return length;
    }

    public EnemyRobot push(EnemyRobot enemy) {
        alive = true;

        int direction = (int) Math.signum(enemy.getLateralVelocity());
        if (length > 0) {
            EnemyRobot last = getLatest();
            if (direction == 0)
                direction = last.getDirection();
        }

        if (direction != 0)
            enemy.setDirection(direction);

        int newAhead = (int) Math.signum(enemy.getVelocity());
        if (newAhead == 0 && length > 0)
            newAhead = log[realAt(length - 1)].getAhead();

        enemy.setAhead(newAhead);

        if (enemy.getAdvancingVelocity() > RAM_THRESHOLD) {
            approaching++;
        }

        distanceSum += enemy.getDistance();
        everLength++;

        log[realAt(length++)] = enemy;
        if (length > LOG_SIZE) {
            length = LOG_SIZE;
            removed++;
            if (removed >= LOG_SIZE)
                removed = 0;
        }

        return enemy;
    }

    public EnemyRobot push(ScannedRobotEvent e, RobotMediator from) {
        return push(new EnemyRobot(e, from));
    }

    public void pop() {
        if (length == 0)
            throw new ArrayIndexOutOfBoundsException("popping empty EnemyLog");

        log[removed] = null;
        removed++;
        length--;
        if (removed >= LOG_SIZE)
            removed = 0;
    }

    public void shrink(int count) {
        int discard = Math.max(0, length - count);
        for (int i = 0; i < discard; i++)
            pop();
    }

    public void clear() {
        shrink(0);
    }

    public EnemyRobot[] getSequence(long time) {
        ArrayList<EnemyRobot> res = new ArrayList<>();
        int i;
        for (i = 1; i <= length && getKthLatest(i).getTime() >= time; i++) {
            res.add(getKthLatest(i));
        }

        if (i > length)
            return new EnemyRobot[0];

        res.add(getKthLatest(i)); // lead
        Collections.reverse(res);

        return res.toArray(new EnemyRobot[0]);
    }

    public double getRamming() {
        return (double) approaching / everLength;
    }

    public double getAverageDistance() {
        return distanceSum / everLength;
    }
}
