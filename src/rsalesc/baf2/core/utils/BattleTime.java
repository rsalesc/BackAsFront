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

package rsalesc.baf2.core.utils;

/**
 * Created by Roberto Sales on 21/08/17.
 */
public class BattleTime implements Comparable<BattleTime> {
    private final Integer round;
    private Long time;

    public BattleTime(Long time, Integer round) {
        this.time = time;
        this.round = round;
    }

    public int getRound() {
        return round;
    }

    public long getTime() {
        return time;
    }

    @Override
    public int compareTo(BattleTime o) {
        if ((long) round == o.round)
            return time.compareTo(o.time);
        return round.compareTo(o.round);
    }
}
