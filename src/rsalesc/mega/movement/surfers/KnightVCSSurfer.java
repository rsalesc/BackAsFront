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

package rsalesc.mega.movement.surfers;

import rsalesc.baf2.core.utils.geometry.Range;
import rsalesc.mega.movement.SegmentedDataSurfer;
import rsalesc.mega.movement.strategies.vcs.SlicingFlattenerStrategy;
import rsalesc.mega.movement.strategies.vcs.SlicingMixedStrategy;
import rsalesc.mega.utils.NamedStatData;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.mega.utils.segmentation.*;
import rsalesc.structures.Knn;

/**
 * Created by Roberto Sales on 13/09/17.
 */
public abstract class KnightVCSSurfer extends SegmentedDataSurfer {
    private static Knn.ParametrizedCondition ADAPTIVE_CONDITION =
            new NamedStatData.HitCondition(new Range(0.03, Double.POSITIVE_INFINITY), 0);


    @Override
    public SegmentationView<TimestampedGFRange> getNewSegmentationView() {
        GFSegmentationView view = new GFSegmentationView();

        view.add(
            new GFSegmentationSet()
            .setScanWeight(1.0)
            .setStrategy(new SlicingMixedStrategy())
            .setWeighter(new DrussSegmentationWeighter<>())
            .setNormalizer(new SegmentationHeightNormalizer<>())
            .logsHit()
        );

        view.add(
            new GFSegmentationSet()
            .setScanWeight(0.7)
            .setStrategy(new SlicingFlattenerStrategy())
            .setCondition(KnightDCSurfer.MFLAT_CONDITION)
            .setWeighter(new DrussSegmentationWeighter<>())
            .setNormalizer(new SegmentationHeightNormalizer<>())
            .logsHit()
            .logsBreak()
        );

        return view;
    }
}
