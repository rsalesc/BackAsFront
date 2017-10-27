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

package rsalesc.baf2.core;

import rsalesc.baf2.BackAsFrontRobot2;
import rsalesc.baf2.core.controllers.*;
import rsalesc.baf2.core.utils.BattleTime;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.painting.PaintManager;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public class RobotMediator {
    private final BackAsFrontRobot2 robot;
    private final ControlManager controlManager;
    private final PaintManager paintManager;

    public RobotMediator(BackAsFrontRobot2 robot, PaintManager paintManager) {
        this.robot = robot;
        this.paintManager = paintManager;
        controlManager = new ControlManager(robot);
    }

    public int getBulletsFired() {
        return robot.getBulletsFired();
    }

    public PaintManager getPaintManager() {
        return paintManager;
    }

    public static double getQuickestTurn(double originalTurn) {
        return BackAsFrontRobot2.getQuickestTurn(originalTurn);
    }

    public boolean isDev() {
        return robot.getName().endsWith("*");
    }

    public ArrayList<Component> getComponents() {
        return robot.getComponents();
    }

    public void setColors(Color bodyColor, Color gunColor, Color radarColor) {
        robot.setColors(bodyColor, gunColor, radarColor);
    }

    public void setDebugProperty(String key, String value) {
        robot.setDebugProperty(key, value);
    }

    public void setColors(Color bodyColor, Color gunColor, Color radarColor, Color bulletColor, Color scanArcColor) {
        robot.setColors(bodyColor, gunColor, radarColor, bulletColor, scanArcColor);
    }

    public void setAllColors(Color color) {
        robot.setAllColors(color);
    }

    public void setBodyColor(Color color) {
        robot.setBodyColor(color);
    }

    public void setGunColor(Color color) {
        robot.setGunColor(color);
    }

    public void setRadarColor(Color color) {
        robot.setRadarColor(color);
    }

    public void setBulletColor(Color color) {
        robot.setBulletColor(color);
    }

    public void setScanColor(Color color) {
        robot.setScanColor(color);
    }

    public double getX() {
        return robot.getX();
    }

    public double getY() {
        return robot.getY();
    }

    public double getHeadingRadians() {
        return robot.getHeadingRadians();
    }

    public int getRoundNum() {
        return robot.getRoundNum();
    }

    public BattleTime getBattleTime() {
        return robot.getBattleTime();
    }

    public long getTime() {
        return robot.getTime();
    }

    public double getEnergy() {
        return robot.getEnergy();
    }

    public double getVelocity() {
        return robot.getVelocity();
    }

    public int getOthers() {
        return robot.getOthers();
    }

    public double getGunHeat() {
        return robot.getGunHeat();
    }

    public double getGunHeadingRadians() {
        return robot.getGunHeadingRadians();
    }

    public double getMaxVelocity() {
        return robot.getMaxVelocity();
    }

    public Point getPoint() {
        return robot.getPoint();
    }

    public AxisRectangle getBattleField() {
        return robot.getBattleField();
    }

    public double getMaxTurning() {
        return robot.getMaxTurning();
    }

    public AxisRectangle getHitBox() {
        return robot.getHitBox();
    }

    public double getNewVelocity() {
        return robot.getNewVelocity();
    }

    public double getNewHeading() {
        return robot.getNewHeading();
    }

    public Point getNextPosition() {
        return robot.getNextPosition();
    }

    public int getTicksToCool() {
        return robot.getTicksToCool();
    }

    public double getDistanceRemaining() {
        return robot.getDistanceRemaining();
    }

    public boolean isAdjustGunForRobotTurn() {
        return robot.isAdjustGunForRobotTurn();
    }

    public boolean isAdjustRadarForRobotTurn() {
        return robot.isAdjustRadarForRobotTurn();
    }

    public boolean isAdjustRadarForGunTurn() {
        return robot.isAdjustRadarForGunTurn();
    }

    public double getRadarHeadingRadians() {
        return robot.getRadarHeadingRadians();
    }

    public double getGunTurnRemainingRadians() {
        return robot.getGunTurnRemainingRadians();
    }

    public double getRadarTurnRemainingRadians() {
        return robot.getRadarTurnRemainingRadians();
    }

    public double getTurnRemainingRadians() {
        return robot.getTurnRemainingRadians();
    }

    public String getName() {
        return robot.getName();
    }

    public double getGunCoolingRate() {
        return robot.getGunCoolingRate();
    }

    public int getNumRounds() {
        return robot.getNumRounds();
    }

    public int getSentryBorderSize() {
        return robot.getSentryBorderSize();
    }

    public int getNumSentries() {
        return robot.getNumSentries();
    }

    public GunController getGunController() {
        return controlManager.getGunController();
    }

    public AimController getAimController() {
        return controlManager.getAimController();
    }

    public RadarController getRadarController() {
        return controlManager.getRadarController();
    }

    public BodyController getBodyController() {
        return controlManager.getBodyController();
    }

    public DummyController getDummyController() {
        return controlManager.getDummyController();
    }

    public Controller getGunControllerOrDummy() {
        Controller controller = controlManager.getGunController();
        if (controller == null)
            return getDummyController();
        return controller;
    }

    public Controller getAimControllerOrDummy() {
        Controller controller = controlManager.getAimController();
        if (controller == null)
            return getDummyController();
        return controller;
    }

    public Controller getRadarControllerOrDummy() {
        Controller controller = controlManager.getRadarController();
        if (controller == null)
            return getDummyController();
        return controller;
    }

    public Controller getBodyControllerOrDummy() {
        Controller controller = controlManager.getBodyController();
        if (controller == null)
            return getDummyController();
        return controller;
    }
}
