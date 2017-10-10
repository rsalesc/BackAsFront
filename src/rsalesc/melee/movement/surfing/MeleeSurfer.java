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

package rsalesc.melee.movement.surfing;

import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.waves.BreakType;
import rsalesc.mega.utils.IMea;
import rsalesc.mega.utils.NamedStatData;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.mega.utils.stats.GuessFactorStats;
import rsalesc.melee.utils.stats.CircularGuessFactorStats;

/**
 * Created by Roberto Sales on 12/09/17.
 */
public interface MeleeSurfer {
    boolean hasData(EnemyLog enemyLog, NamedStatData o);

    void log(EnemyLog enemyLog, TargetingLog log, IMea mea, BreakType type);

    CircularGuessFactorStats getStats(EnemyLog enemyLog, TargetingLog f, IMea mea, long cacheIndex);
}
