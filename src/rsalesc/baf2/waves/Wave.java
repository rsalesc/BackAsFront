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

package rsalesc.baf2.waves;

import robocode.Rules;
import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.utils.BattleTime;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.*;
import rsalesc.baf2.tracking.*;
import rsalesc.baf2.predictor.PredictedPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by Roberto Sales on 22/07/17.
 */
public class Wave {
    private static final int PASS_THRESHOLD = 24;

    private Point source;
    private BattleTime battleTime;
    private double velocity;

    private TreeMap<String, Object> data = new TreeMap<>();

    public Wave(Point source, BattleTime battleTime, double velocity) {
        setSource(source);
        setBattleTime(battleTime);
        setVelocity(velocity);
    }

    public static AngularRange preciseIntersection(Wave wave, RobotLog log, long passTime) {
        int i = 1;
        for(; log.getKthLatest(i) != null && log.getKthLatest(i).getTime() > passTime; i++);
        if(log.getKthLatest(i) == null || log.getKthLatest(i).getTime() != passTime)
            return null;

        ArrayList<PredictedPoint> points = new ArrayList<>();
        for(; log.getKthLatest(i) != null && log.getKthLatest(i).getTime() >= passTime - PASS_THRESHOLD; i++) {
            points.add(PredictedPoint.from(log.getKthLatest(i)));
        }

        Collections.reverse(points);

        return preciseIntersection(wave, points);
    }

    public static AngularRange preciseIntersection(Wave wave, List<PredictedPoint> predicted) {
        double refAngle = Physics.absoluteBearing(wave.getSource(), R.getLast(predicted));

        int ptr = predicted.size() - 1;
        while (ptr > 0 && wave.hasPreciselyTouchedRobot(predicted.get(ptr), predicted.get(ptr).time))
            ptr--;

        AngularRange range = new AngularRange(refAngle, new Range());
        PredictedPoint me = predicted.get(ptr);

        while (ptr + 1 < predicted.size()) {
            PredictedPoint nextEnemy = predicted.get(ptr + 1);
            AxisRectangle botRect = new AxisRectangle(nextEnemy, Physics.BOT_WIDTH * 2);

            for (Point corner : botRect.getCorners()) {
                if (wave.hasPassed(corner, nextEnemy.time) && !wave.hasPassed(corner, me.time)) {
                    range.pushAngle(wave.getAngle(corner));
                }
            }

            for (Point intersect : wave.getCircle(me.time).intersect(botRect)) {
                range.pushAngle(wave.getAngle(intersect));
            }

            for (Point intersect : wave.getCircle(nextEnemy.time).intersect(botRect)) {
                range.pushAngle(wave.getAngle(intersect));
            }

            me = nextEnemy;
            ptr++;
        }

        if (range.isEmpty())
            return null;

        return range;
    }

    public Point getSource() {
        return source;
    }

    public void setSource(Point source) {
        this.source = source;
    }

    public long getTime() {
        return battleTime.getTime();
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public boolean hasStarted(long time) {
        return time >= getTime();
    }

    public double getDistanceTraveled(long time) {
        return Math.max(velocity * (time - getTime()), 0);
    }

    public boolean hasTouchedRobot(Point point, long time) {
        return point.distance(source) - Physics.BOT_WIDTH <= getDistanceTraveled(time);
    }

    public boolean hasPreciselyTouchedRobot(Point point, long time) {
        Point[] corners = new AxisRectangle(point, 36).getCorners();

        for(Point pt : corners)
            if(hasPassed(pt, time))
                return true;

        return false;
    }

    public boolean hasPassedRobot(MyRobot robot) {
        return robot.getPosition().distance(source) + Physics.BOT_WIDTH < getDistanceTraveled(robot.getTime());
    }

    public boolean hasPassedRobot(EnemyRobot robot) {
        return robot.getPoint().distance(source) + Physics.BOT_WIDTH < getDistanceTraveled(robot.getTime());
    }

    public boolean hasPassedRobot(Point point, long time) {
        return point.distance(source) + Physics.BOT_WIDTH < getDistanceTraveled(time);
    }

    public boolean hasBullet() {
        return true;
    }

    public boolean hasPassed(Point point, long time) {
        return point.distance(source) < getDistanceTraveled(time);
    }

    public double getBreakTime(Point dest) {
        return dest.distance(source) / velocity + getTime();
    }

    public double getBreakTime(EnemyRobot robot) {
        return robot.getPoint().distance(source) / velocity + getTime();
    }

    public double getBreakTime(MyRobot robot) {
        return robot.getPoint().distance(source) / velocity + getTime();
    }

    public double getTouchTime(MyRobot robot)  {
        return (robot.getPoint().distance(source) - Physics.BOT_WIDTH) / velocity + getTime();
    }

    public double getAngle(Point point) {
        return Physics.absoluteBearing(source, point);
    }

    public Circle getCircle(long time) {
        return new Circle(source, getDistanceTraveled(time));
    }

    public Point project(double angle, long time) {
        return getSource().project(angle, getDistanceTraveled(time));
    }

    public BattleTime getBattleTime() {
        return battleTime;
    }

    public void setBattleTime(BattleTime battleTime) {
        this.battleTime = battleTime;
    }

    public Object getData(String path) {
        return data.get(path);
    }

    public void setData(String path, Object data) {
        this.data.put(path, data);
    }

    public double getPower() {
        return Physics.bulletPower(velocity);
    }

    public double getDamage() {
        return Rules.getBulletDamage(getPower());
    }

    public double getBonus() {
        return Rules.getBulletHitBonus(getPower());
    }

    public double getDifferential() {
        return getDamage() + getBonus();
    }

    public boolean everyoneInside(RobotMediator mediator) {
        EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();

        int cntInside = 0;
        long time = mediator.getTime();

        if (this.getCircle(time).isInside(mediator.getHitBox()))
            cntInside++;

        for (EnemyRobot enemy : enemies) {
            if (this.getCircle(time).isInside(enemy.getHitBox()))
                cntInside++;
        }

        if (cntInside >= enemies.length + 1)
            return true;
        else
            return false;
    }

    public PredictedPoint impactPoint(List<PredictedPoint> points) {
        PredictedPoint last = points.get(points.size() - 1);

        for(int i = points.size() - 2; i >= 0; i--) {
            PredictedPoint pt = points.get(i);
            if(!this.hasPassed(pt, pt.time))
                break;

            last = pt;
        }

        return last;
    }
}
