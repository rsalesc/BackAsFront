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
import rsalesc.mega.movement.strategies.vcs.SlicingAdaptiveStrategy;
import rsalesc.mega.movement.strategies.vcs.SlicingFlattenerStrategy;
import rsalesc.mega.utils.NamedStatData;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.mega.utils.segmentation.GFSegmentationSet;
import rsalesc.mega.utils.segmentation.GFSegmentationView;
import rsalesc.mega.utils.segmentation.SegmentationView;
import rsalesc.structures.Knn;

/**
 * Created by Roberto Sales on 13/09/17.
 */
public abstract class KnightVCSSurfer extends SegmentedDataSurfer {
    private static Knn.ParametrizedCondition ADAPTIVE_CONDITION =
            new NamedStatData.HitCondition(new Range(0.035, Double.POSITIVE_INFINITY), 0);

    private static Knn.ParametrizedCondition NORMAL_CONDITION =
            new Knn.Tautology();

    private static Knn.ParametrizedCondition FLATTENING_CONDITION =
            new Knn.AndCondition().add(ADAPTIVE_CONDITION)
                .add(new Knn.OrCondition().add(new NamedStatData.HitCondition(new Range(0.07, Double.POSITIVE_INFINITY), 1)))
            ;

    @Override
    public SegmentationView<TimestampedGFRange> getNewSegmentationView() {
        GFSegmentationView view = new GFSegmentationView();

//        view.add(
//            new GFSegmentationSet()
//            .setScanWeight(0.5)
//            .setStrategy(new SlicingSimpleStrategy())
//            .logsHit()
//        );

        view.add(
                new GFSegmentationSet()
                .setScanWeight(0.5)
                .setStrategy(new SlicingAdaptiveStrategy())
                .setCondition(ADAPTIVE_CONDITION)
                .logsHit()
        );

//        view.add(
//            new GFSegmentationSet()
//            .setScanWeight(0.5)
//            .setCrossoverStrategy(new SlicingAdaptiveStrategy())
//            .logsHit()
//        );

        view.add(
            new GFSegmentationSet()
            .setScanWeight(0.35)
            .setStrategy(new SlicingFlattenerStrategy())
            .setCondition(FLATTENING_CONDITION)
            .logsHit()
            .logsBreak()
        );

        return view;
    }
}
