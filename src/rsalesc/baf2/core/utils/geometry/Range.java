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

package rsalesc.baf2.core.utils.geometry;

import rsalesc.baf2.core.utils.R;

/**
 * Created by Roberto Sales on 25/07/17.
 */
public class Range {
    public double min, max;

    public Range(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public Range() {
        this(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    public boolean isEmpty() {
        return this.min - R.EPSILON > this.max;
    }

    public double getLength() {
        return Math.max(max - min, 0);
    }

    public boolean isNearlyContained(double x) {
        return R.nearOrBetween(min, x, max);
    }

    public boolean isNearlyContained(double x, double error) {
        return R.nearOrBetween(min, x, max, error);
    }

    public void push(double x) {
        min = Math.min(x, min);
        max = Math.max(x, max);
    }

    public double maxAbsolute() {
        return Math.max(Math.abs(min), Math.abs(max));
    }

    public double minAbsolute() {
        return Math.min(Math.abs(min), Math.abs(max));
    }

    public double getCenter() {
        return (min + max) * 0.5;
    }

    public double getRadius() {
        return getLength() * 0.5;
    }

    public Range intersect(Range rhs) {
        Range res = new Range(Math.max(min, rhs.min), Math.min(max, rhs.max));
        if (res.isEmpty())
            return new Range();
        return res;
    }
}
