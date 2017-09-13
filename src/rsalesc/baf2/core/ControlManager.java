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

/**
 * Created by Roberto Sales on 11/09/17.
 */
public class ControlManager {
    private final BackAsFrontRobot2 robot;
    private boolean aimUsed = false;
    private boolean triggerUsed = false;
    private boolean bodyUsed = false;
    private boolean radarUsed = false;

    public ControlManager(BackAsFrontRobot2 robot) {
        this.robot = robot;
    }

    public boolean isAimUsed() {
        return aimUsed;
    }

    public boolean isTriggerUsed() {
        return triggerUsed;
    }

    public boolean isBodyUsed() {
        return bodyUsed;
    }

    public boolean isRadarUsed() {
        return radarUsed;
    }

    public void acquireAim() {
        if (aimUsed)
            throw new IllegalStateException();
        aimUsed = true;
    }

    public void acquireTrigger() {
        if (triggerUsed)
            throw new IllegalStateException();
        triggerUsed = true;
    }

    public void acquireBody() {
        if (bodyUsed)
            throw new IllegalStateException();
        bodyUsed = true;
    }

    public void acquireRadar() {
        if (radarUsed)
            throw new IllegalStateException();
        radarUsed = true;
    }

    public void releaseAim() {
        aimUsed = false;
    }

    public void releaseTrigger() {
        triggerUsed = false;
    }

    public void releaseBody() {
        bodyUsed = false;
    }

    public void releaseRadar() {
        radarUsed = false;
    }

    public GunController getGunController() {
        GunController controller = new GunController(robot, this);
        if (controller.acquire())
            return controller;
        return null;
    }

    public AimController getAimController() {
        AimController controller = new AimController(robot, this);
        if (controller.acquire())
            return controller;
        return null;
    }

    public RadarController getRadarController() {
        RadarController controller = new RadarController(robot, this);
        if (controller.acquire())
            return controller;
        return null;
    }

    public BodyController getBodyController() {
        BodyController controller = new BodyController(robot, this);
        if (controller.acquire())
            return controller;
        return null;
    }

    public DummyController getDummyController() {
        return new DummyController(robot, this);
    }
}
