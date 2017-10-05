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

package rsalesc.mega.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Roberto Sales on 02/10/17.
 */
public class BatchIterator<T> {
    private final Iterator<T> iterator;

    public BatchIterator(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public T next() {
        return iterator.next();
    }

    public Iterable<T> consume(int n) {
        ArrayList<T> consumed = new ArrayList<>();
        while(consumed.size() < n && hasNext()) {
            consumed.add(next());
        }

        return consumed;
    }
}
