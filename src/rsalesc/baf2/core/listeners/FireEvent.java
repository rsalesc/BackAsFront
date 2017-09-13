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

package rsalesc.baf2.core.listeners;

import robocode.Bullet;
import rsalesc.baf2.core.utils.geometry.Point;

/**
 * Created by Roberto Sales on 25/07/17.
 */
public class FireEvent {
    private final Point source;
    private final long time;
    private final Bullet bullet;

    public FireEvent(Point source, Bullet bullet, long time) {
        this.source = source;
        this.time = time;
        this.bullet = bullet;
    }

    public Bullet getBullet() {
        return bullet;
    }

    public Point getSource() {
        return source;
    }

    public double getPower() {
        return bullet.getPower();
    }

    public long getTime() {
        return time;
    }

    public double getHeadingRadians() {
        return bullet.getHeadingRadians();
    }

    public double getSpeed() {
        return bullet.getVelocity();
    }
}
