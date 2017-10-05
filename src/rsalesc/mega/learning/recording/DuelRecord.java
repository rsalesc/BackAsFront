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

package rsalesc.mega.learning.recording;

import rsalesc.baf2.core.utils.Pair;
import rsalesc.baf2.waves.BreakType;
import rsalesc.mega.utils.TargetingLog;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Roberto Sales on 01/10/17.
 */
public class DuelRecord implements Serializable {
    private final static long serialVersionUID = 129109231L;

    private final String enemyName;
    private final ArrayList<Pair<TargetingLog, BreakType>> logs;
    private final int rounds;

    public DuelRecord(String enemyName, ArrayList<Pair<TargetingLog, BreakType>> logs, int rounds) {
        this.enemyName = enemyName;
        this.logs = logs;
        this.rounds = rounds;
    }

    public ArrayList<Pair<TargetingLog, BreakType>> getLogs() {
        return logs;
    }

    public String getEnemyName() {
        return enemyName;
    }

    public int getRounds() {
        return rounds;
    }
}
