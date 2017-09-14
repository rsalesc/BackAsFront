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

import rsalesc.baf2.core.utils.BattleTime;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.*;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.MyRobot;
import rsalesc.mega.predictor.PredictedPoint;

import java.util.List;
import java.util.TreeMap;

/**
 * Created by Roberto Sales on 22/07/17.
 */
public class Wave {
    private Point source;
    private BattleTime battleTime;
    private double velocity;

    private TreeMap<String, Object> data = new TreeMap<>();

    public Wave(Point source, BattleTime battleTime, double velocity) {
        setSource(source);
        setBattleTime(battleTime);
        setVelocity(velocity);
    }

    public static AngularRange preciseIntersection(Wave wave, List<PredictedPoint> predicted) {
        double refAngle = R.getLast(predicted).getHeading();

        int ptr = predicted.size() - 1;
        while (ptr > 0 && wave.hasTouchedRobot(predicted.get(ptr), predicted.get(ptr).getTime()))
            ptr--;

        AngularRange range = new AngularRange(refAngle, new Range());
        PredictedPoint me = predicted.get(ptr);

        while (ptr + 1 < predicted.size()) {
            PredictedPoint nextEnemy = predicted.get(ptr + 1);
            AxisRectangle botRect = new AxisRectangle(nextEnemy, Physics.BOT_WIDTH * 2);

            for (Point corner : botRect.getCorners()) {
                if (wave.hasPassed(corner, nextEnemy.getTime()) && !wave.hasPassed(corner, me.getTime())) {
                    range.pushAngle(wave.getAngle(corner));
                }
            }

            for (Point intersect : wave.getCircle(me.getTime()).intersect(botRect)) {
                range.pushAngle(wave.getAngle(intersect));
            }

            for (Point intersect : wave.getCircle(nextEnemy.getTime()).intersect(botRect)) {
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
}
