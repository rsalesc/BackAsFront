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

import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.waves.BreakType;
import rsalesc.mega.utils.IMea;
import rsalesc.mega.utils.TargetingLog;

/**
 * Created by Roberto Sales on 15/09/17.
 */
public interface GFTargeting {
    boolean hasData(EnemyLog enemyLog);
    GeneratedAngle[] getFiringAngles(EnemyLog enemyLog, TargetingLog f, IMea mea);
    void log(EnemyLog enemyLog, TargetingLog f, IMea mea, BreakType type);
}
