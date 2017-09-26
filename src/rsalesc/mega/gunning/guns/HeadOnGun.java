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

package rsalesc.mega.gunning.guns;

import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.EnemyTracker;
import rsalesc.mega.utils.TargetingLog;

/**
 * Created by Roberto Sales on 20/09/17.
 */
public class HeadOnGun extends AutomaticGun {
    @Override
    public StorageNamespace getStorageNamespace() {
        return getGlobalStorage().namespace("head-on-zinha");
    }

    @Override
    public String getGunName() {
        return "HOT";
    }

    @Override
    public GeneratedAngle[] generateFiringAngles(EnemyLog enemyLog, double power) {
        if(enemyLog == null) {
            EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();
            if(enemies.length == 0)
                return new GeneratedAngle[0];
            enemyLog = EnemyTracker.getInstance().getLog(enemies[0]);
        }

        if(!enemyLog.isAlive())
            return new GeneratedAngle[0];

        TargetingLog f = TargetingLog.getLog(enemyLog.getLatest(), getMediator(), power);

        return new GeneratedAngle[]{new GeneratedAngle(1.0, f.absBearing, f.distance)};
    }
}
