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
import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.utils.BattleTime;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.core.utils.geometry.Point;

/**
 * Created by Roberto Sales on 22/07/17.
 */
public class MyRobot implements RobotSnapshot {
    private Point position;
    private double heading;
    private double radarHeading;
    private double gunHeading;
    private double velocity;
    private double energy;
    private double gunHeat;
    private double distanceToWall;
    private BattleTime battleTime;
    private String name;
    private int others;

    private int ahead;

    public MyRobot(RobotMediator robot) {
        this.update(robot);
    }

    public void update(RobotMediator robot) {
        setName(robot.getName());
        setPosition(new Point(robot.getX(), robot.getY()));
        setHeading(robot.getHeadingRadians());
        setRadarHeading(robot.getRadarHeadingRadians());
        setGunHeading(robot.getGunHeadingRadians());
        setVelocity(robot.getVelocity());
        setEnergy(robot.getEnergy());
        setGunHeat(robot.getGunHeat());
        setBattleTime(robot.getBattleTime());
        setOthers(robot.getOthers());

        AxisRectangle field = robot.getBattleField();
        distanceToWall = field.distanceToEdges(getPosition());
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public double getRadarHeading() {
        return radarHeading;
    }

    public void setRadarHeading(double radarHeading) {
        this.radarHeading = radarHeading;
    }

    public double getGunHeading() {
        return gunHeading;
    }

    public void setGunHeading(double gunHeading) {
        this.gunHeading = gunHeading;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public double getGunHeat() {
        return gunHeat;
    }

    public void setGunHeat(double gunHeat) {
        this.gunHeat = gunHeat;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public long getTime() {
        return getBattleTime().getTime();
    }

    public Point getPoint() {
        return getPosition();
    }

    public double getLateralVelocity(Point from) {
        double absBearing = Physics.absoluteBearing(from, getPoint());
        return Physics.getLateralVelocityFromStationary(absBearing, getVelocity(), getHeading());
    }

    public double getAdvancingVelocity(Point from) {
        double absBearing = Physics.absoluteBearing(from, getPoint());
        return Physics.getApproachingVelocityFromStationary(absBearing, getVelocity(), getHeading());
    }

    public int getDirection(Point from) {
        double head = getHeading();
        if (ahead < 0) {
            head += R.PI;
            head = R.normalAbsoluteAngle(head);
        }
        double absBearing = Physics.absoluteBearing(getPoint(), from);
        double off = R.normalRelativeAngle(head - absBearing);
        if (off > 0) return -1;
        else if (off < 0) return 1;
        else return 0;
    }

    public double getDistanceToWall() {
        return distanceToWall;
    }

    public int getAhead() {
        return ahead;
    }

    public void setAhead(int ahead) {
        this.ahead = ahead;
    }

    public AxisRectangle getHitBox() {
        double x = getPoint().getX();
        double y = getPoint().getY();
        return new AxisRectangle(x - 18, x + 18, y - 18, y + 18);
    }

    public BattleTime getBattleTime() {
        return battleTime;
    }

    public void setBattleTime(BattleTime battleTime) {
        this.battleTime = battleTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOthers() {
        return others;
    }

    @Override
    public boolean isFuture() {
        return false;
    }

    public void setOthers(int others) {
        this.others = others;
    }

    public double getBafHeading() {
        if (getAhead() < 0)
            return R.normalAbsoluteAngle(getHeading() + R.PI);
        return getHeading();
    }
}
