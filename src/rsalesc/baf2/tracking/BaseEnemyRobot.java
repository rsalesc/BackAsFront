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
import rsalesc.baf2.core.utils.Physics;
import rsalesc.mega.predictor.PredictedPoint;

/**
 * Created by Roberto Sales on 21/07/17.
 */
public class BaseEnemyRobot {
    private double bearing;
    private double distance;
    private double energy;
    private double heading;
    private double velocity;

    private String name;

    BaseEnemyRobot() {
        this.clear();
    }

    BaseEnemyRobot(ScannedRobotEvent e) {
        this.update(e);
    }

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void clear() {
        bearing = 0.0;
        distance = 0.0;
        energy = 0.0;
        velocity = 0.0;
        heading = 0.0;
        velocity = 0.0;

        name = "";
    }

    public boolean populated() {
        return !name.equals("");
    }

    public boolean nil() {
        return !populated();
    }

    public void update(ScannedRobotEvent e) {
        bearing = e.getBearingRadians();
        distance = e.getDistance();
        energy = e.getEnergy();
        heading = e.getHeadingRadians();
        name = e.getName();
        velocity = e.getVelocity();
    }
}
