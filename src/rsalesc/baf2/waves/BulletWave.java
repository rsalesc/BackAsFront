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

import robocode.Bullet;
import rsalesc.baf2.core.utils.BattleTime;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.tracking.MyRobot;

/**
 * Created by Roberto Sales on 22/07/17.
 */
public class BulletWave extends RobotWave {
    private final double angle;

    public BulletWave(MyRobot robot, double velocity, double angle) {
        super(robot.getPoint(), robot.getBattleTime(), velocity);
        this.angle = angle;
    }

    public BulletWave(Point source, BattleTime time, double velocity, double angle) {
        super(source, time, velocity);
        this.angle = angle;
    }

    public double getAngle() {
        return angle;
    }

    public boolean wasFiredBy(Bullet bullet, long time) {
        return R.isNear(angle, bullet.getHeadingRadians()) && R.isNear(getVelocity(), bullet.getVelocity())
                && R.isNear(new Point(bullet.getX(), bullet.getY()).distance(getSource()), getDistanceTraveled(time), 40);
    }
}
