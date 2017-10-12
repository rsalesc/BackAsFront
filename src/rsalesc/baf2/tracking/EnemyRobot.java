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
import robocode.util.Utils;
import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.utils.BattleTime;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.core.utils.geometry.Point;

/**
 * Created by Roberto Sales on 21/07/17.
 */
public class EnemyRobot extends BaseEnemyRobot implements RobotSnapshot {
    public Integer direction;
    private double absBearing;
    private double x;
    private double y;
    private double angularVelocity;
    private double lateralVelocity;
    private double approachingVelocity;
    private double distanceToWall;
    private int ahead;
    private int others;

    private BattleTime battleTime;

    EnemyRobot() {
        super();
    }

    EnemyRobot(ScannedRobotEvent e, RobotMediator from) {
        this.update(e, from);
    }

    public void update(ScannedRobotEvent e) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    private void update(ScannedRobotEvent e, RobotMediator mediator) {
        super.update(e);
        battleTime = mediator.getBattleTime();
        AxisRectangle field = mediator.getBattleField();

        absBearing = e.getBearingRadians() + mediator.getHeadingRadians();

        Point projected = mediator.getPoint().project(absBearing, e.getDistance());
        x = projected.x;
        y = projected.y;
        others = mediator.getOthers();

        lateralVelocity = Physics.getLateralVelocityFromStationary(absBearing, getVelocity(), getHeading());
        angularVelocity = Physics.getAngularVelocityFromStationary(absBearing, getDistance(), getVelocity(), getHeading());
        approachingVelocity = Physics.getApproachingVelocityFromStationary(absBearing, getVelocity(), getHeading());
        distanceToWall = R.sqrt(sqr(Math.min(getX(), field.getWidth() - getX())) +
                sqr(Math.min(getY(), field.getHeight() - getY())));
    }

    private double sqr(double x) {
        return x * x;
    }

    public long getTime() {
        return getBattleTime().getTime();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Point getPoint() {
        return new Point(x, y);
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
            head = Utils.normalAbsoluteAngle(head);
        }
        double absBearing = Physics.absoluteBearing(getPoint(), from);
        double off = Utils.normalRelativeAngle(head - absBearing);
        if (off > 0) return -1;
        else if (off < 0) return 1;
        else return 0;
    }

    public double getAngularVelocity() {
        return angularVelocity;
    }

    public double getLateralVelocity() {
        return lateralVelocity;
    }

    public double getAdvancingVelocity() {
        return approachingVelocity;
    }

    public double getDistanceToWall() {
        return distanceToWall;
    }

    public double getAbsoluteBearing() {
        return absBearing;
    }

    public AxisRectangle getHitBox() {
        return new AxisRectangle(x - 18, x + 18, y - 18, y + 18);
    }

    public int getDirection() {
        if (direction != null)
            return direction;
        return lateralVelocity >= 0 ? 1 : -1;
    }

    public void setDirection(int dir) {
        direction = dir;
    }

    public int getAhead() {
        return ahead;
    }

    public void setAhead(Integer ahead) {
        this.ahead = ahead;
    }

    public BattleTime getBattleTime() {
        return battleTime;
    }

    public double getBafHeading() {
        if (getAhead() < 0)
            return Utils.normalAbsoluteAngle(getHeading() + R.PI);
        return getHeading();
    }

    public int getOthers() {
        return others;
    }
}
