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

import rsalesc.baf2.core.annotations.Modified;

import java.util.List;

public class SegmentationHeightNormalizer<T> extends SegmentationNormalizer<T> {
    public List<WeightedSegmentedData<T>> normalize(@Modified List<WeightedSegmentedData<T>> data) {
        double max = 1e-22;
        for(WeightedSegmentedData<T> seg : data) {
            max = Math.max(seg.getWeight(), max);
        }

        for(WeightedSegmentedData<T> seg : data) {
            seg.setWeight(seg.getWeight() / max);
        }

        return data;
    }
}
