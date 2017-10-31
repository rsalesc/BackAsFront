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

package rsalesc.baf2;

import robocode.*;
import rsalesc.baf2.core.utils.BattleTime;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.predictor.PrecisePredictor;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by Roberto Sales on 11/09/17.
 */
abstract class OldBackAsFrontRobot extends AdvancedRobot {
    private int idle = 0;

    private RobotStatus status;
    private AxisRectangle field;
    private double _maxVelocity = Rules.MAX_VELOCITY;

    public static double getQuickestTurn(double originalTurn) {
        if (Math.abs(originalTurn) < R.HALF_PI)
            return originalTurn;
        else
            return R.normalRelativeAngle(originalTurn + R.PI);
    }

    public void dissociate() {
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
    }

    @Override
    public void onStatus(StatusEvent e) {
        if (status != null && status.getGunHeat() == 0 && status.getGunHeat() == e.getStatus().getGunHeat())
            idle++;
        else
            idle = 0;

        status = e.getStatus();
        field = _getBattleField();
    }

    @Override
    public double getX() {
        return status.getX();
    }

    @Override
    public double getY() {
        return status.getY();
    }

    @Override
    public double getHeadingRadians() {
        return status.getHeadingRadians();
    }

    @Override
    public double getHeading() {
        return status.getHeading();
    }

    @Override
    public int getRoundNum() {
        return status.getRoundNum();
    }

    @Override
    public long getTime() {
        return status.getTime();
    }

    @Override
    public double getEnergy() {
        return status.getEnergy();
    }

    @Override
    public double getVelocity() {
        return status.getVelocity();
    }

    @Override
    public int getOthers() {
        return status.getOthers();
    }

    @Override
    public double getGunHeat() {
        return status.getGunHeat();
    }

    @Override
    public double getGunHeadingRadians() {
        return status.getGunHeadingRadians();
    }

    @Override
    public double getGunHeading() {
        return status.getGunHeading();
    }

    public double getMaxVelocity() {
        return _maxVelocity;
    }

    @Override
    public void setMaxVelocity(double x) {
        _maxVelocity = x;
        super.setMaxVelocity(x);
    }

    public BattleTime getBattleTime() {
        return new BattleTime(getTime(), getRoundNum());
    }

    public long getRealTime() {
        return super.getTime();
    }

    public Point getPoint() {
        return new Point(getX(), getY());
    }

    private AxisRectangle _getBattleField() {
        return new AxisRectangle(0, getBattleFieldWidth(), 0, getBattleFieldHeight());
    }

    public AxisRectangle getBattleField() {
        return field;
    }

    public void setBackAsFront(double bearing) {
        setBackAsFront(bearing, Double.POSITIVE_INFINITY);
    }

    public void setBackAsFront(double bearing, double distance) {
        int signal = R.isNear(distance, 0, 1e-12) ? 0 : setQuickTurnTo(bearing);
        setAhead(distance * signal);
    }

    public void moveWithBackAsFront(Point dest, double distance) {
        setBackAsFront(Physics.absoluteBearing(getPoint(), dest), distance);
    }

    public void runAwayWithBackAsFront(Point dest, double distance) {
        setBackAsFront(Physics.absoluteBearing(dest, getPoint()), distance);
    }

    public void moveWithBackAsFront(Point dest) {
        moveWithBackAsFront(dest, getPoint().distance(dest));
    }

    public double getMaxTurning() {
        return Physics.maxTurningRate(getVelocity());
    }

    public void setGoTo(Point dest) {
        moveWithBackAsFront(dest);
    }

    public int setQuickTurnTo(double radians) {
        radians = R.normalAbsoluteAngle(radians);
        double angle = R.normalRelativeAngle(radians - getHeadingRadians());
        double narrowAngle = getQuickestTurn(angle);
        setTurnRightRadians(narrowAngle);
        return (angle == narrowAngle ? 1 : -1);
    }

    public void setTurnTo(double radians) {
        radians = R.normalAbsoluteAngle(radians);
        double offset = R.normalRelativeAngle(radians - getHeadingRadians());
        setTurnRightRadians(offset);
    }

    public void setGunTo(double radians) {
        radians = R.normalAbsoluteAngle(radians);
        double offset = R.normalRelativeAngle(radians - getGunHeadingRadians());
        setTurnGunRightRadians(offset);
    }

    public void setRadarTo(double radians) {
        radians = R.normalAbsoluteAngle(radians);
        double offset = R.normalRelativeAngle(radians - getRadarHeadingRadians());
        setTurnRadarRightRadians(offset);
    }

    public AxisRectangle getHitBox() {
        return new AxisRectangle(getX() - 18, getX() + 18, getY() - 18, getY() + 18);
    }

    public double getNewVelocity() {
        return PrecisePredictor.getNewVelocity(getVelocity(), getMaxVelocity(), getDistanceRemaining());
    }

    public double getNewHeading() {
        return PrecisePredictor.getNewHeading(getHeadingRadians(), getVelocity(), getTurnRemainingRadians());
    }

    public Point getNextPosition() {
        return getPoint().project(getNewHeading(), getNewVelocity());
    }

    public int getTicksToCool() {
        return (int) Math.ceil(getGunHeat() / getGunCoolingRate());
    }

    /**
     * get the number of complete ticks with gunheat == 0
     */
    public int getIdleTicks() {
        return idle;
    }

    public void handle(Exception e) {
        System.out.println("got an exception");
        e.printStackTrace();

        try {
            PrintStream out = new PrintStream(new RobocodeFileOutputStream(getDataFile((int) (Math.random() * 1000) + ".error")));
            e.printStackTrace(out);
            out.flush();
            out.close();
        } catch (IOException ioex) {
        }
    }
}
