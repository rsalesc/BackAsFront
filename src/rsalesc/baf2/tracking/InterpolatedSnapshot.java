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

import rsalesc.baf2.core.utils.BattleTime;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.predictor.PredictedPoint;

/**
 * Created by Roberto Sales on 11/10/17.
 */
public class InterpolatedSnapshot implements RobotSnapshot {
    private final RobotSnapshot base;
    private final PredictedPoint predictedPoint;

    public InterpolatedSnapshot(RobotSnapshot base, PredictedPoint predicted) {
        this.base = base;
        this.predictedPoint = predicted;
    }

    @Override
    public String getName() {
        return base.getName();
    }

    @Override
    public long getTime() {
        return predictedPoint.time;
    }

    @Override
    public BattleTime getBattleTime() {
        return new BattleTime(getTime(), base.getBattleTime().getRound());
    }

    @Override
    public Point getPoint() {
        return predictedPoint;
    }

    @Override
    public double getLateralVelocity(Point from) {
        return Physics.getLateralVelocityFromStationary(Physics.absoluteBearing(from, predictedPoint),
                predictedPoint.velocity,
                predictedPoint.heading);
    }

    @Override
    public double getAdvancingVelocity(Point from) {
        return Physics.getApproachingVelocityFromStationary(Physics.absoluteBearing(from, predictedPoint),
                predictedPoint.velocity,
                predictedPoint.heading);
    }

    @Override
    public int getDirection(Point from) {
        return predictedPoint.getDirection(from);
    }

    @Override
    public int getAhead() {
        return predictedPoint.ahead;
    }

    @Override
    public AxisRectangle getHitBox() {
        return predictedPoint.getHitBox();
    }

    @Override
    public double getEnergy() {
        return base.getEnergy();
    }

    @Override
    public double getVelocity() {
        return predictedPoint.velocity;
    }

    @Override
    public double getHeading() {
        return predictedPoint.heading;
    }

    @Override
    public double getBafHeading() {
        return predictedPoint.getBafHeading();
    }

    @Override
    public int getOthers() {
        return base.getOthers();
    }

    @Override
    public boolean isFuture() {
        return false;
    }

    public RobotSnapshot getBase() {
        return base;
    }

    public boolean isMe() {
        return base instanceof MyRobot;
    }

    public boolean isEnemy() {
        return base instanceof EnemyRobot;
    }
}
