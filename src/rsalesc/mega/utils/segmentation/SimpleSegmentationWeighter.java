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
public class SimpleSegmentationWeighter extends SegmentationWeighter {
    @Override
    public double getWeight(SegmentationSet.SegmentationEntry segmentationEntry) {
        return segmentationEntry.getSliceCount();
    }

    @Override
    public int getDataLimit(SegmentationSet.SegmentationEntry segmentationEntry) {
        int sliceCount = segmentationEntry.getSliceCount();
        double depth;
        if(sliceCount < 2)
            depth = 10;
        else if(sliceCount < 5)
            depth = 4;
        else if(sliceCount < 10)
            depth = 2.25;
        else if(sliceCount < 20)
            depth = 1.4;
        else if(sliceCount < 30)
            depth = 0.75;
        else
            depth = 0.3;

        return (int) Math.round(2 * depth + 1);
    }
}