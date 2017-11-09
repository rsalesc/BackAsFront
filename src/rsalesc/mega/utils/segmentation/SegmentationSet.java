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

package rsalesc.mega.utils.segmentation;

import rsalesc.mega.utils.Ensurer;
import rsalesc.mega.utils.MultipleSlicingStrategy;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.structures.Knn;

import java.util.ArrayList;

/**
 * Created by Roberto Sales on 30/09/17.
 */
public class SegmentationSet<T> {
    private static final int DEFAULT_SIZE = 10;

    private MultipleSlicingStrategy strategy;
    private ArrayList<SegmentationEntry> segmentations;
    private SegmentationWeighter<T> weighter;
    private SegmentationNormalizer<T> normalizer;
    private boolean built = false;

    private int dataAmount = 0;

    private double scanWeight;
    private Knn.ParametrizedCondition condition;

    private boolean _logInitialized = false;
    private boolean _logVirtual = false;
    private boolean _logBreak = false;
    private boolean _logHit = true;

    private void initFlags() {
        if(!_logInitialized) {
            _logInitialized = true;
            _logHit = false;
        }
    }

    public SegmentationSet<T> logsBreak() {
        initFlags();
        _logBreak = true;
        return this;
    }

    public SegmentationSet<T> logsVirtual() {
        initFlags();
        _logBreak = true;
        return this;
    }

    public SegmentationSet<T> logsHit() {
        initFlags();
        _logHit = true;
        return this;
    }

    public SegmentationSet<T> logsEverything() {
        logsBreak();
        logsVirtual();
        logsHit();
        return this;
    }

    public boolean logsOnVirtual() {
        return _logVirtual;
    }

    public boolean logsOnBreak() {
        return _logBreak;
    }

    public boolean logsOnHit() {
        return _logHit;
    }

    public SegmentationSet<T> setStrategy(MultipleSlicingStrategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public SegmentationSet<T> setWeighter(SegmentationWeighter<T> weighter) {
        this.weighter = weighter;
        return this;
    }

    public SegmentationSet<T> setNormalizer(SegmentationNormalizer<T> normalizer) {
        this.normalizer = normalizer;
        return this;
    }

    public SegmentationSet<T> build() {
        built = true;
        segmentations = new ArrayList<>();

        for(double[][] slices : strategy.getSlices()) {
            segmentations.add(new SegmentationEntry(new Segmentation<>(slices)));
        }

        return this;
    }

    public boolean isBuilt() {
        return built;
    }

    private int getMaxSize(SegmentationEntry entry) {
        if(weighter != null)
            return weighter.getDataLimit(entry);
        else
            return DEFAULT_SIZE;
    }

    private double getWeight(SegmentationEntry entry, SegmentedData<T> data) {
        if(weighter != null)
            return weighter.getWeight(entry, data);
        else
            return 1.0;
    }

    private double getDepth(SegmentationEntry entry) {
        if(weighter != null)
            return weighter.getDepth(entry);
        else
            return Double.POSITIVE_INFINITY;
    }

    private SegmentedData<T> getDataFrom(SegmentationEntry segmentationEntry, double[] vals) {
        SegmentedData<T> data = segmentationEntry.getSegmentation().getOrEnsure(vals, segmentationEntry.getEnsurer());

//        if(data == null) {
//            segmentationEntry.getSegmentation().add(vals, data = new SegmentedData<T>(getMaxSize(segmentationEntry)));
//        }

        return data;
    }

    public ArrayList<WeightedSegmentedData<T>> getData(double[] vals) {
        ArrayList<WeightedSegmentedData<T>> res = new ArrayList<>();

        for(SegmentationEntry entry : segmentations) {
            SegmentedData<T> data = getDataFrom(entry, vals);
            res.add(data.weight(getWeight(entry, data), getDepth(entry)));
        }

        if(normalizer != null)
            normalizer.normalize(res);

        for(WeightedSegmentedData<T> entry : res)
            entry.setWeight(entry.getWeight() * scanWeight);

        return res;
    }

    public int size() {
        return dataAmount;
    }

    public void log(double[] vals, T payload) {
        dataAmount++;
        for(SegmentationEntry entry : segmentations) {
            getDataFrom(entry, vals).add(payload);
        }
    }

    public ArrayList<WeightedSegmentedData<T>> getData(TargetingLog f) {
        return getData(strategy.getQuery(f));
    }

    public void log(TargetingLog f, T payload) {
        log(strategy.getQuery(f), payload);
    }

    public double getScanWeight() {
        return scanWeight;
    }

    public SegmentationSet<T> setScanWeight(double scanWeight) {
        this.scanWeight = scanWeight;
        return this;
    }

    public Knn.ParametrizedCondition getCondition() {
        return condition;
    }

    public SegmentationSet<T> setCondition(Knn.ParametrizedCondition condition) {
        this.condition = condition;
        return this;
    }

    public boolean isEnabled(Object o) {
        return condition == null || condition.test(o);
    }

    public class SegmentationEntry {
        private final Segmentation<SegmentedData<T>> segmentation;
        private final int sliceCount;
        private final Ensurer<SegmentedData<T>> ensurer;

        private SegmentationEntry(Segmentation<SegmentedData<T>> segmentation) {
            this.segmentation = segmentation;
            this.sliceCount = segmentation.getSliceCount();
            final SegmentationEntry temp = this;
            ensurer = new Ensurer<SegmentedData<T>>() {
                @Override
                public SegmentedData<T> ensure() {
                    return new SegmentedData<T>(getMaxSize(temp));
                }
            };
        }

        public Segmentation<SegmentedData<T>> getSegmentation() {
            return segmentation;
        }

        public int getSliceCount() {
            return sliceCount;
        }

        public Ensurer<SegmentedData<T>> getEnsurer() {
            return ensurer;
        }
    }
}
