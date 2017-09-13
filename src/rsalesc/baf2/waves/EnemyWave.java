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
import robocode.ScannedRobotEvent;
import rsalesc.baf2.core.utils.BattleTime;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.tracking.EnemyRobot;

/**
 * Created by Roberto Sales on 22/07/17.
 */
public class EnemyWave extends RobotWave {
    private EnemyRobot enemy;

    public EnemyWave(EnemyRobot robot, Point source, BattleTime time, double velocity) {
        super(source, time, velocity);
        setEnemy(robot);
    }

    public EnemyRobot getEnemy() {
        return enemy;
    }

    public void setEnemy(EnemyRobot enemy) {
        this.enemy = enemy;
    }

    public boolean isFrom(String name) {
        return enemy.getName().equals(name);
    }

    public boolean isFrom(EnemyRobot robot) {
        return isFrom(robot.getName());
    }

    public boolean isFrom(ScannedRobotEvent e) {
        return isFrom(e.getName());
    }

    public boolean wasFiredBy(Bullet bullet, long time) {
        return bullet.getName().equals(enemy.getName()) && R.isNear(getVelocity(), bullet.getVelocity())
                && R.isNear(new Point(bullet.getX(), bullet.getY()).distance(getSource()), getDistanceTraveled(time), 40);
    }
}
