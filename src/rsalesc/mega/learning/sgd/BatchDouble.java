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

package rsalesc.mega.learning.sgd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class BatchDouble implements Iterable<BatchResult>, Comparable<BatchDouble>, Serializable {
    private final ArrayList<BatchResult> results;

    public BatchDouble(ArrayList<BatchResult> results) {
        this.results = results;
    }

    @Override
    public Iterator<BatchResult> iterator() {
        return results.iterator();
    }

    public Double mean() {
        double sum = 0;
        for(BatchResult d : results)
            sum += d.mean();

        return sum / Math.max(results.size(), 1);
    }

    public Double normalizedMean() {
        double sum = 0;
        int lengthSum = 0;

        for(BatchResult d : results) {
            sum += d.value;
            lengthSum += d.length;
        }

        return sum / Math.max(lengthSum, 1);
    }

    @Override
    public int compareTo(BatchDouble o) {
        return mean().compareTo(o.mean());
    }
}
