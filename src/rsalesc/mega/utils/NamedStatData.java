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

package rsalesc.mega.utils;

import rsalesc.baf2.core.utils.geometry.Range;
import rsalesc.mega.utils.structures.Knn;

/**
 * Created by Roberto Sales on 13/09/17.
 */
public class NamedStatData {
    public final StatData data;
    public final String name;

    public NamedStatData(StatData data, String name) {
        this.data = data;
        this.name = name;
    }

    public double getEnemyWeightedHitPercentage() {
        return data.getEnemyWeightedHitPercentage(name);
    }

    public double getEnemyHitPercentage() {
        return data.getEnemyHitPercentage(name);
    }

    public int getRound() {
        return data.getRound();
    }

    public int getMeetings(int L, int R) {
        return data.getMeetings(name, L, R);
    }

    public int getMeetings(int cnt) {
        return getMeetings(cnt, cnt);
    }

    public int getMeetings() {
        return getMeetings(1);
    }

    public static class HitCondition extends Knn.ParametrizedCondition {
        private final Range range;
        private final int meetings;

        public HitCondition(Range range, int meetings) {
            this.range = range;
            this.meetings = meetings;
        }

        @Override
        public boolean test(Object o) {
            if(!(o instanceof NamedStatData))
                throw new IllegalStateException();

            NamedStatData data = (NamedStatData) o;
            return range.isNearlyContained(data.getEnemyWeightedHitPercentage())
                    && data.getMeetings() >= meetings;
        }

        @Override
        public void mutate(Knn.ConditionMutation mutation) {

        }
    }
}
