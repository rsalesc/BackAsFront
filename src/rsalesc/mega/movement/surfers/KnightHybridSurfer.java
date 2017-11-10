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

import rsalesc.baf2.core.StorageNamespace;
import rsalesc.mega.movement.HybridFlattenedSurfer;
import rsalesc.mega.utils.NamedStatData;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.mega.utils.segmentation.SegmentationView;
import rsalesc.structures.KnnView;

/**
 * Created by Roberto Sales on 13/09/17.
 */
public abstract class KnightHybridSurfer extends HybridFlattenedSurfer {
    private final KnightDCSurfer knnSurfer = new KnightDCSurfer() {
        @Override
        public StorageNamespace getStorageNamespace() {
            return KnightHybridSurfer.this.getStorageNamespace().namespace("a");
        }
    };

    @Override
    public boolean flattenerEnabled(NamedStatData o) {
        return knnSurfer.flattenerEnabled(o);
    }

    @Override
    public boolean lightFlattenerEnabled(NamedStatData o) {
        return knnSurfer.lightFlattenerEnabled(o);
    }

    private final KnightVCSSurfer vcsSurfer = new KnightVCSSurfer() {
        @Override
        public StorageNamespace getStorageNamespace() {
            return KnightHybridSurfer.this.getStorageNamespace().namespace("b");
        }
    };

    @Override
    public KnnView<TimestampedGFRange> getNewFlattenerKnnSet() {
        return knnSurfer.getNewFlattenerKnnSet();
    }

    @Override
    public SegmentationView<TimestampedGFRange> getNewSegmentationView() {
        return vcsSurfer.getNewNormalSegmentationView();
    }
}
