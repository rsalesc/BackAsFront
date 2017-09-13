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

package rsalesc.baf2.core.controllers;

import robocode.Bullet;
import rsalesc.baf2.core.utils.geometry.Point;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public interface IController {
    void setTurnGunRightRadians(double radians);

    void setTurnRadarRightRadians(double radians);

    void setTurnRightRadians(double radians);

    void setFire(double power);

    Bullet setFireBullet(double power);

    void setMaxTurnRate(double rate);

    void setMaxVelocity(double velocity);

    void setBackAsFront(double bearing);

    void setBackAsFront(double bearing, double distance);

    void setTurnTo(double radians);

    void setQuickTurnTo(double radians);

    void setGunTo(double radians);

    void setRadarTo(double radians);

    void setGoTo(Point dest);
}
