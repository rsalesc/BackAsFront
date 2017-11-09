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

/**
 * Created by Roberto Sales on 30/09/17.
 */
public class DrussSegmentationWeighter<T> extends SegmentationWeighter<T> {
    @Override
    public double getWeight(SegmentationSet.SegmentationEntry segmentationEntry, SegmentedData<T> data) {
//        return segmentationEntry.getSliceCount();
        double roll = 1.0 - 1.0 / (1.0 + getDepth(segmentationEntry));

//        return data.size() * (1-roll) / (roll-Math.pow(roll, data.size() + 1));
        return data.size() * segmentationEntry.getSliceCount();
    }

    @Override
    public int getDataLimit(SegmentationSet.SegmentationEntry segmentationEntry) {
        return (int) Math.ceil(2 * getDepth(segmentationEntry) + 1);
    }

    @Override
    public double getDepth(SegmentationSet.SegmentationEntry segmentationEntry) {
        int sliceCount = segmentationEntry.getSliceCount();
        double depth;
        if(sliceCount < 2)
            depth = 3;
        else if(sliceCount < 4)
            depth = 1;
        else if(sliceCount < 10)
            depth = 0.7;
        else if(sliceCount < 30)
            depth = 0.5;
        else if(sliceCount < 100)
            depth = 0.2;
        else
            depth = 0.1;

        return depth;
    }
}
