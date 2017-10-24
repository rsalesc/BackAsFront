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

import robocode.Rules;
import rsalesc.baf2.core.utils.geometry.Point;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public class EnemyFireEvent {
    private final EnemyRobot enemy;
    private final Point source;
    private final long time;
    private final int deviation;
    private final double power;

    public EnemyFireEvent(EnemyRobot enemy, Point source, long time, int deviation, double power) {
        this.enemy = enemy;
        this.source = source;
        this.time = time;
        this.deviation = deviation;
        this.power = power;
    }

    public long getTime() {
        return time;
    }

    public EnemyRobot getEnemy() {
        return enemy;
    }

    public Point getSource() {
        return source;
    }

    public double getPower() {
        return power;
    }

    public double getSpeed() {
        return Rules.getBulletSpeed(power);
    }

    public int getDeviation() {
        return deviation;
    }
}
