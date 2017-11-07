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
import rsalesc.structures.Knn;

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

    public double getHitPercentage() {
        return data.getHitPercentage(name);
    }

    public double getWeightedHitPercentage() {
        return data.getWeightedHitPercentage(name);
    }

    public int getEnemyShotsFired() {
        return data.getShotsFelt(name);
    }

    public int getShotsFired() {
        return data.getShotsFired(name);
    }

    public int getRound() {
        return data.getRound();
    }

    public void setData(Object key, Object data) {
        this.data.setData(name, key, data);
    }

    public Object getData(Object key) {
        return data.getData(name, key);
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

    public double getEnemyRandomWeightedHitPercentage() {
        return data.getEnemyRandomWeightedHitPercentage(name);
    }

    public double getEnemyRelativeWeightedHitPercentage() {
        return data.getEnemyRelativeWeightedHitPercentage(name);
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
            double p = data.getEnemyHitPercentage();
            int samples = data.getEnemyShotsFired();

            return range.isNearlyContained(p, /*-R.marginOfError(p, samples)*/ 1e-9)
                    && data.getMeetings() >= meetings;
        }

        @Override
        public void mutate(Knn.ConditionMutation mutation) {

        }
    }

    public static class WeightedHitCondition extends Knn.ParametrizedCondition {
        private final Range range;
        private final int meetings;
        private final int atLeast;

        public WeightedHitCondition(Range range, int meetings) {
            this.range = range;
            this.meetings = meetings;
            this.atLeast = 10000;
        }

        public WeightedHitCondition(Range range, int meetings, int atLeast) {
            this.range = range;
            this.meetings = meetings;
            this.atLeast = atLeast;
        }

        @Override
        public boolean test(Object o) {
            if(!(o instanceof NamedStatData))
                throw new IllegalStateException();

            NamedStatData data = (NamedStatData) o;
            double p = data.getEnemyWeightedHitPercentage();
            int samples = data.getEnemyShotsFired();

            Integer lastMeeting = (Integer) data.getData(this);

            boolean turnOn = range.isNearlyContained(p, /*-R.marginOfError(p, samples)*/ 1e-9)
                    && data.getMeetings() >= meetings || (lastMeeting != null && lastMeeting >= atLeast);

            if(turnOn)
                data.setData(this, data.getMeetings());

            return turnOn;
        }

        @Override
        public void mutate(Knn.ConditionMutation mutation) {

        }
    }

    public static class RelativelyWeightedHitCondition extends Knn.ParametrizedCondition {
        private final double x;
        private final int meetings;
        private final int atLeast;

        public RelativelyWeightedHitCondition(double x, int meetings, int atLeast) {
            this.x = x;
            this.meetings = meetings;
            this.atLeast = atLeast;
        }

        @Override
        public boolean test(Object o) {
            if(!(o instanceof NamedStatData))
                throw new IllegalStateException();

            NamedStatData data = (NamedStatData) o;

            double p = data.getEnemyRelativeWeightedHitPercentage();

            Integer lastMeeting = (Integer) data.getData(this);

            boolean turnOn = p > x
                    && data.getMeetings() >= meetings || (lastMeeting != null && lastMeeting >= atLeast);

            if(turnOn)
                data.setData(this, data.getMeetings());

            return turnOn;
        }

        @Override
        public void mutate(Knn.ConditionMutation mutation) {

        }
    }
}
