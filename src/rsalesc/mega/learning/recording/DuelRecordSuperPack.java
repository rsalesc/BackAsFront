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

package rsalesc.mega.learning.recording;

import rsalesc.mega.utils.BatchIterable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Roberto Sales on 02/10/17.
 */
public class DuelRecordSuperPack implements Iterable<DuelRecord>, BatchIterable<DuelRecord> {
    private final ArrayList<DuelRecordPackage> packs = new ArrayList<>();

    public DuelRecordSuperPack(Collection<DuelRecordPackage> packs) {
        this.packs.addAll(packs);
    }

    public DuelRecordSuperPack(DuelRecordPackage ...packs) {
        this(Arrays.asList(packs));
    }

    public ArrayList<DuelRecordPackage> getPackages() {
        ArrayList<DuelRecordPackage> res = new ArrayList<>();
        res.addAll(packs);
        return res;
    }

    public static DuelRecordSuperPack merge(DuelRecordSuperPack ...superPacks) {
        ArrayList<DuelRecordPackage> packs = new ArrayList<>();
        for(DuelRecordSuperPack superPack : superPacks) {
            packs.addAll(superPack.getPackages());
        }

        return new DuelRecordSuperPack(packs);
    }

    public void tryClosing() {
        for(DuelRecordPackage pack : packs) {
            try {
                pack.close();
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }
    }

    @Override
    public java.util.Iterator<DuelRecord> iterator() {
        return new Iterator();
    }

    public class Iterator implements java.util.Iterator<DuelRecord> {
        private int packageIndex = -1;
        private DuelRecordPackage.Iterator iterator = null;

        public void goAhead() {
            packageIndex++;
            iterator = packageIndex < packs.size() ? packs.get(packageIndex).iterator() : null;
        }

        @Override
        public boolean hasNext() {
            while(packageIndex < packs.size() && (iterator == null || !iterator.hasNext()))
                goAhead();

            return iterator != null;
        }

        @Override
        public DuelRecord next() {
            if(!hasNext())
                throw new IndexOutOfBoundsException("exceeded DuelRecordSuperPack bounds");

            return iterator.next();
        }
    }
}
