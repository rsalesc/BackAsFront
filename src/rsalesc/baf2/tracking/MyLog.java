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

import robocode.util.Utils;
import rsalesc.baf2.core.GlobalStorage;
import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.utils.BattleTime;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AngularRange;

/**
 * Created by Roberto Sales on 23/07/17.
 */
public class MyLog implements RobotLog {
    private static final int LOG_SIZE = 2000;

    private MyRobot[] log;
    private int length;
    private int removed;

    private MyLog() {
        log = new MyRobot[LOG_SIZE];
        length = 0;
        removed = 0;
    }

    public static MyLog getInstance() {
        StorageNamespace ns = GlobalStorage.getInstance().namespace("tracker");
        MyLog log = (MyLog) ns.get("mylog");
        if (log == null) {
            log = new MyLog();
            ns.put("mylog", log);
        }

        return log;
    }

    private int realAt(int i) {
        int idx = (removed + i) % LOG_SIZE;
        if (idx < 0)
            idx += LOG_SIZE;
        return idx;
    }

    public MyRobot getLatest() {
        if (length == 0)
            return null;
        return log[realAt(length - 1)];
    }

    public MyRobot getKthLatest(int k) {
        if (length < k)
            return null;
        return log[realAt(length - k)];
    }

    public MyRobot getAtLeastKthLatest(int k) {
        if (length < k)
            k = length;
        return log[realAt(length - k)];
    }

    @Override
    public MyRobot before(RobotSnapshot robot) {
        return atMostAt(robot.getTime() - 1);
    }

    @Override
    public MyRobot after(RobotSnapshot robot) {
        return atLeastAt(robot.getTime() + 1);
    }

    @Override
    public MyRobot exactlyAt(long time) {
        MyRobot res = atLeastAt(time);
        if(res == null || res.getTime() != time)
            return null;

        return res;
    }

    public MyRobot atLeastAt(long time) {
        if (length == 0)
            return null;

        int latestRound = log[realAt(length - 1)].getBattleTime().getRound();

        int l = 0, r = length;
        while (l < r) {
            int mid = (l + r) / 2;
            MyRobot cur = log[realAt(mid)];
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

    public MyRobot atMostAt(long time) {
        if (length == 0)
            return null;

        int latestRound = log[realAt(length - 1)].getBattleTime().getRound();

        int l = 0, r = length;
        while (l < r) {
            int mid = (l + r) / 2;
            MyRobot cur = log[realAt(mid)];
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
            MyRobot cur = log[realAt(mid)];
            BattleTime curBattleTime = cur.getBattleTime();
            if (curBattleTime.getTime() <= time)
                l = mid;
            else r = mid - 1;
        }

        if (l < baseL)
            return null;

        return log[realAt(l)];
    }

    public MyRobot at(int i) {
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

    public MyRobot push(MyRobot me) {
        int newAhead = (int) Math.signum(me.getVelocity());
        if (newAhead == 0 && length > 0)
            newAhead = log[realAt(length - 1)].getAhead();

        me.setAhead(newAhead);

        log[realAt(length++)] = me;
        if (length > LOG_SIZE) {
            length = LOG_SIZE;
            removed++;
            if (removed >= LOG_SIZE)
                removed = 0;
        }

        return me;
    }

    public void pop() {
        if (length == 0)
            throw new ArrayIndexOutOfBoundsException("popping empty MyLog");

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

    public AngularRange getRadarRange() {
        MyRobot me = getLatest();
        if (me == null)
            return null;

        MyRobot pastMe = getAtLeastKthLatest(2);
        double diff = Utils.normalRelativeAngle(me.getRadarHeading() - pastMe.getRadarHeading());
        double center = Utils.normalAbsoluteAngle(pastMe.getRadarHeading() + diff / 2);

        AngularRange range = new AngularRange(center, -diff / 2, diff / 2);
        return range;
    }
}
