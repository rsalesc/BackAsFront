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
import rsalesc.baf2.BackAsFrontRobot2;
import rsalesc.baf2.core.ControlManager;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public class GunController extends AimController {
    public GunController(BackAsFrontRobot2 robot, ControlManager manager) {
        super(robot, manager);
    }

    @Override
    public void setFire(double power) {
        robot.setFire(power);
    }

    @Override
    public Bullet setFireBullet(double power) {
        return robot.setFireBullet(power);
    }

    @Override
    public boolean acquire() {
        if (manager.isAimUsed() || manager.isTriggerUsed())
            return false;
        manager.acquireAim();
        manager.acquireTrigger();
        return true;
    }

    @Override
    public void release() {
        manager.releaseTrigger();
        manager.releaseAim();
    }
}
