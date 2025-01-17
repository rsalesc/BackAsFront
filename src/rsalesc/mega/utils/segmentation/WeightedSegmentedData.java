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

import java.util.ArrayList;

/**
 * Created by Roberto Sales on 30/09/17.
 */
public class WeightedSegmentedData<T> extends SegmentedData<T> {
    private double weight;
    private double roll;

    public WeightedSegmentedData(int maxSize, double weight, double roll) {
        super(maxSize);
        this.weight = weight;
        this.roll = 1.0 - 1.0 / (1.0 + roll);
    }

    public double getWeight() {
        return weight;
    }

    public double getRolledWeight(int i) {
        return weight * Math.pow(roll, i);
    }

    public void setWeight(double x) {
        weight = x;
    }

    public ArrayList<WeightedEntry<T>> getWeightedData() {
        ArrayList<WeightedEntry<T>> rev = new ArrayList<>();

        double factor = 1.0;
        for(T t : getData()) {
            rev.add(new WeightedEntry<>(weight * factor, t));
            factor *= roll;
        }

        return rev;
    }
}
