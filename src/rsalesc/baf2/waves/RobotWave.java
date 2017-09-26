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
import rsalesc.baf2.core.utils.geometry.Point;

/**
 * Created by Roberto Sales on 23/07/17.
 */
public class RobotWave extends Wave {
    private Bullet hit;
    private Bullet bulletHit;
    private boolean missed = false;

    public RobotWave(Point source, BattleTime time, double velocity) {
        super(source, time, velocity);
    }

    public void setHit(Bullet bullet) {
        hit = bullet;
    }

    public boolean hasHit() {
        return hit != null;
    }

    public boolean hasMissed() {
        return missed;
    }

    public void setMissed(boolean flag) {
        missed = flag;
    }

    public Bullet getHit() {
        return hit;
    }

    public void setBulletHit(Bullet bullet) {
        bulletHit = bullet;
    }

    public boolean hasBulletHit() {
        return bulletHit != null;
    }

    public Bullet getBulletHit() {
        return bulletHit;
    }

    public boolean hasAnyHit() {
        return hasHit() || hasBulletHit();
    }
}
