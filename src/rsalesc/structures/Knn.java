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

package rsalesc.structures;

import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.Range;
import rsalesc.mega.utils.Strategy;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.mega.utils.Timestamped;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Created by Roberto Sales on 13/08/17.
 */
public abstract class Knn<T extends Timestamped> {
    private boolean hasSpecificLog = false;
    private boolean loggingHit = true;
    private boolean loggingBreak = false;
    private boolean loggingVirtual = false;

    private double scanWeight = 1.0;
    private int defaultK;
    private double defaultRatio = 1.0;
    private Function<Integer, Double> portionDecider;
    private Strategy strategy;
    private boolean built = false;
    private ParametrizedCondition parametrizedCondition = null;
    private DistanceWeighter<T> weighter;

    public static <T> double getTotalWeight(List<Entry<T>> entries) {
        double total = 0;
        for (Knn.Entry<T> entry : entries) {
            total += entry.weight;
        }

        return total;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public Knn<T> setStrategy(Strategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public Knn<T> setK(int K) {
        this.defaultK = K;
        return this;
    }

    public int getK() {
        return defaultK;
    }

    public Knn<T> setRatio(double ratio) {
        this.defaultRatio = ratio;
        return this;
    }

    public Knn<T> setRatio(Function<Integer, Double> fn) {
        this.portionDecider = fn;
        return this;
    }

    public Knn<T> setCondition(ParametrizedCondition parametrizedCondition) {
        this.parametrizedCondition = parametrizedCondition;
        return this;
    }

    public double getScanWeight() {
        return this.scanWeight;
    }

    public Knn<T> setScanWeight(double weight) {
        this.scanWeight = weight;
        return this;
    }

    private void setupLogging() {
        if (!hasSpecificLog) {
            loggingHit = false;
            loggingBreak = false;
            loggingVirtual = false;
        }
        hasSpecificLog = true;
    }

    public Knn<T> logsHit() {
        setupLogging();
        loggingHit = true;
        return this;
    }

    public Knn<T> logsBreak() {
        setupLogging();
        loggingBreak = true;
        return this;
    }

    public Knn<T> logsVirtual() {
        setupLogging();
        loggingVirtual = true;
        return this;
    }

    public Knn<T> logsEverything() {
        setupLogging();
        loggingHit = true;
        loggingBreak = true;
        loggingVirtual = true;
        return this;
    }

    public boolean logsOnHit() {
        return loggingHit;
    }

    public boolean logsOnBreak() {
        return loggingBreak;
    }

    public boolean logsOnVirtual() {
        return loggingVirtual;
    }

    protected abstract void buildStructure();

    public abstract int size();

    public abstract void add(double[] point, T payload);

    public abstract List<Entry<T>> query(double[] point, int K, double alpha);

    public boolean isEnabled(Object o) {
        if (parametrizedCondition == null)
            return true;
        return parametrizedCondition.test(o);
    }

    public void mutate(ConditionMutation mutation) {
        if (parametrizedCondition != null)
            parametrizedCondition.mutate(mutation);
    }

    public boolean isBuilt() {
        return this.built;
    }

    public Knn<T> build() {
        buildStructure();
        built = true;
        return this;
    }

    public void add(TargetingLog f, T payload) {
        add(getStrategy().getQuery(f), payload);
    }

    public List<Entry<T>> query(double[] point) {
        return query(point, defaultK);
    }

    public int getQueryableData() {
        if(portionDecider != null)
            return Math.max(1, (int) Math.ceil(portionDecider.apply(size())));
        return Math.max(1, (int) Math.ceil(size() * defaultRatio));
    }

    public List<Entry<T>> query(double[] point, int K) {
        List<Entry<T>> res =
                query(point, Math.min(K, getQueryableData()), 1.0);

        if (weighter != null)
            res = weighter.getWeightedEntries(res);

        return res;
    }

    public List<Entry<T>> query(TargetingLog f) {
        return query(getStrategy().getQuery(f));
    }

    public List<Entry<T>> query(TargetingLog f, int K) {
        return query(getStrategy().getQuery(f), K);
    }

    protected Entry<T> makeEntry(double distance, T payload) {
        return new Entry<T>(scanWeight, distance, payload);
    }

    public abstract Iterator iterator(double[] query, double alpha);

    public Iterator iterator(double[] query) {
        return iterator(query, 1.0);
    }

    public Iterator iterator(TargetingLog f) {
        return iterator(getStrategy().getQuery(f));
    }

    protected class Iterator implements java.util.Iterator<Knn.Entry<T>> {
        private final java.util.Iterator<Knn.Entry<T>> iterator;
        private int K = 0;

        public Iterator(java.util.Iterator<Knn.Entry<T>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Entry<T> next() {
            Knn.Entry<T> entry = iterator.next();
            if(weighter != null)
                entry = weighter.getWeightedEntry(entry, K);

            K++;
            return entry;
        }
    }

    public Knn<T> setDistanceWeighter(DistanceWeighter<T> weighter) {
        this.weighter = weighter;
        return this;
    }

    public static class Entry<T> implements Comparable<Entry<T>> {
        public final double distance;
        public final T payload;
        public double weight;

        public Entry(double weight, double distance, T payload) {
            this.weight = weight;
            this.distance = distance;
            this.payload = payload;
        }

        @Override
        public int compareTo(Entry<T> o) {
            return (int) Math.signum(distance - o.distance);
        }
    }

    public static abstract class ParametrizedCondition {
        public abstract boolean test(Object o);

        public abstract void mutate(ConditionMutation mutation);
    }

    public static class OrCondition extends ParametrizedCondition {
        ArrayList<ParametrizedCondition> conditions = new ArrayList<>();

        public OrCondition add(ParametrizedCondition condition) {
            conditions.add(condition);
            return this;
        }

        @Override
        public boolean test(Object o) {
            boolean res = false;

            // cant return immediately because of mutation
            for (ParametrizedCondition condition : conditions) {
                res = res || condition.test(o);
            }

            return res;
        }

        @Override
        public void mutate(ConditionMutation mutation) {
            for (ParametrizedCondition condition : conditions)
                condition.mutate(mutation);
        }
    }

    public static class AndCondition extends ParametrizedCondition {
        ArrayList<ParametrizedCondition> conditions = new ArrayList<>();

        public AndCondition add(ParametrizedCondition condition) {
            conditions.add(condition);
            return this;
        }

        @Override
        public boolean test(Object o) {
            boolean res = true;

            // cant return immediately because of mutation
            for (ParametrizedCondition condition : conditions) {
                res = res && condition.test(o);
            }

            return res;
        }

        @Override
        public void mutate(ConditionMutation mutation) {
            for (ParametrizedCondition condition : conditions)
                condition.mutate(mutation);
        }
    }

    public static class NotCondition extends ParametrizedCondition {
        ParametrizedCondition condition;

        public NotCondition(ParametrizedCondition condition) {
            this.condition = condition;
        }

        @Override
        public boolean test(Object o) {
            return !condition.test(o);
        }

        @Override
        public void mutate(ConditionMutation mutation) {
            condition.mutate(mutation);
        }
    }

    public static class ConditionMutation {
        public final long time;
        public final int round;

        public ConditionMutation(long time, int round) {
            this.time = time;
            this.round = round;
        }
    }

    public static class HitCondition extends ParametrizedCondition {
        protected Range limits;
        protected int rounds;

        public HitCondition(double min, double max, int rounds) {
            limits = new Range(min, max);
            this.rounds = rounds;
        }

        public HitCondition(Range limits, int rounds) {
            this(limits.min, limits.max, rounds);
        }

        public boolean test(Object o) {
            HitLeastCondition range = (HitLeastCondition) o;
            if (limits.isNearlyContained(range.limits.min) && range.rounds >= rounds)
                return true;
            else
                return false;
        }

        @Override
        public void mutate(ConditionMutation mutation) {
        }
    }

    public static class HitLeastCondition extends HitCondition {
        public HitLeastCondition(double min, int rounds) {
            super(min, Double.MAX_VALUE, rounds);
        }
    }

    public static class Tautology extends ParametrizedCondition {

        @Override
        public boolean test(Object o) {
            return true;
        }

        @Override
        public void mutate(ConditionMutation mutation) {

        }
    }

    public static abstract class DistanceWeighter<T extends Timestamped> {
        public abstract List<Entry<T>> getWeightedEntries(List<Entry<T>> entries);
        public abstract Entry<T> getWeightedEntry(Entry<T> entry, int K);
    }

    public static class InverseDistanceWeighter<T extends Timestamped> extends DistanceWeighter<T> {
        private double ratio;

        public InverseDistanceWeighter() {
            this(1.0);
        }

        public InverseDistanceWeighter(double ratio) {
            this.ratio = ratio;
        }

        @Override
        public List<Entry<T>> getWeightedEntries(List<Entry<T>> entries) {
            List<Entry<T>> res = new ArrayList<>();
            for (Knn.Entry<T> entry : entries) {
                res.add(new Knn.Entry<>(entry.weight / Math.pow(entry.distance + 1e-10, ratio),
                        entry.distance, entry.payload));
            }

            return res;
        }

        @Override
        public Entry<T> getWeightedEntry(Entry<T> entry, int K) {
            return new Knn.Entry<>(entry.weight / Math.pow(entry.distance + 1e-10, ratio), entry.distance, entry.payload);
        }
    }

    public static class NormalizeManhattanWeighter<T extends Timestamped> extends DistanceWeighter<T> {
        private double modifier;
        private double ratio;

        public NormalizeManhattanWeighter(Strategy strategy, double ratio) {
            this.modifier = getStrategyWeight(strategy);
            this.ratio = ratio;
        }

        public NormalizeManhattanWeighter(Strategy strategy, Strategy strategy2) {
            this.ratio = getStrategyWeight(strategy2);
            this.modifier = getStrategyWeight(strategy);
        }

        private static double getStrategyWeight(Strategy strategy) {
            double sum = 1e-22;
            double[] weights = strategy.getWeights();
            for (double weight : weights) sum += weight;

            return sum;
        }

        @Override
        public List<Entry<T>> getWeightedEntries(List<Entry<T>> entries) {
            List<Entry<T>> res = new ArrayList<>();

            for (Knn.Entry<T> entry : entries) {
                res.add(new Knn.Entry<>(entry.weight,
                        entry.distance / modifier * ratio, entry.payload));
            }

            return res;
        }

        @Override
        public Entry<T> getWeightedEntry(Entry<T> entry, int K) {
            return new Knn.Entry<>(entry.weight / Math.pow(entry.distance + 1e-10, ratio), entry.distance, entry.payload);
        }
    }

    public static class GaussDistanceWeighter<T extends Timestamped> extends DistanceWeighter<T> {
        private double ratio;
        private int ignore = 0;

        public GaussDistanceWeighter() {
            this(1.0);
        }

        public GaussDistanceWeighter(double ratio) {
            this.ratio = ratio;
        }

        public GaussDistanceWeighter(double ratio, int ignore) {
            this.ratio = ratio;
            this.ignore = ignore;
        }

        public List<Entry<T>> getWeightedEntries(List<Entry<T>> entries) {
            Collections.sort(entries);

            double sum = 1e-9;
            int skipped = 0;
            for (Knn.Entry<T> entry : entries) {
                if(skipped < ignore && skipped + 1 < entries.size()) {
                    skipped++;
                    continue;
                }

                sum += entry.distance;
            }

            double invAvg = (entries.size() - skipped) / sum;

            List<Entry<T>> res = new ArrayList<>();

            for (Knn.Entry<T> entry : entries) {
                res.add(new Knn.Entry<T>(entry.weight * R.gaussKernel(entry.distance * invAvg * ratio),
                        entry.distance, entry.payload));
            }

            return res;
        }

        @Override
        public Entry<T> getWeightedEntry(Entry<T> entry, int K) {
            throw new UnsupportedOperationException();
        }
    }

    public static class DecayWeighter<T extends Timestamped> extends DistanceWeighter<T> {
        private double ratio;

        public DecayWeighter(double ratio) {
            this.ratio = 1.0 - 1.0 / (1.0 + ratio);
        }

        public List<Entry<T>> getWeightedEntries(List<Entry<T>> entries) {
            entries.sort(new Comparator<Entry<T>>() {
                @Override
                public int compare(Entry<T> o1, Entry<T> o2) {
                    return -o1.payload.compareTo(o2.payload);
                }
            });

            List<Entry<T>> res = new ArrayList<>();
            double factor = 1.0;
            for (Knn.Entry<T> entry : entries) {
                res.add(new Knn.Entry<T>(entry.weight * factor,
                        entry.distance, entry.payload));

                factor *= ratio;
            }

            return res;
        }

        @Override
        public Entry<T> getWeightedEntry(Entry<T> entry, int K) {
            return new Knn.Entry<>(entry.weight * Math.pow(ratio, K), entry.distance, entry.payload);
        }
    }

    public static class DecayedGaussWeighter<T extends Timestamped> extends DistanceWeighter<T> {
        private GaussDistanceWeighter<T> gauss;
        private DecayWeighter<T> decay;

        public DecayedGaussWeighter(double ratio) {
            gauss = new GaussDistanceWeighter<>(1.0);
            decay = new DecayWeighter<>(ratio);
        }

        public List<Entry<T>> getWeightedEntries(List<Entry<T>> entries) {
            return decay.getWeightedEntries(gauss.getWeightedEntries(entries));
        }

        @Override
        public Entry<T> getWeightedEntry(Entry<T> entry, int K) {
            throw new UnsupportedOperationException();
        }
    }
}
