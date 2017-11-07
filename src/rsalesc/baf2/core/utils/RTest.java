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

package rsalesc.baf2.core.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.*;

/**
 * Created by Roberto Sales on 04/10/17.
 */
public class RTest {
    @Test
    public void subsequences() {
        Comparator<List<Integer>> comparator = new Comparator<List<Integer>>() {
            @Override
            public int compare(List<Integer> o1, List<Integer> o2) {
                int n = Math.min(o1.size(), o2.size());
                for(int i = 0; i < n; i++) {
                    if(o1.get(i) < o2.get(i))
                        return -1;
                    else if(o1.get(i) > o2.get(i))
                        return +1;
                }

                return new Integer(o1.size()).compareTo(o2.size());
            }
        };

        Set<List<Integer>> expected = new TreeSet<>(comparator);

        expected.add(Arrays.asList(1));
        expected.add(Arrays.asList(1, 2));
        expected.add(Arrays.asList(1, 3));
        expected.add(Arrays.asList(2, 3));
        expected.add(Arrays.asList(3));
        expected.add(Arrays.asList(2));
        expected.add(Arrays.asList(1, 2, 3));

        ArrayList<ArrayList<Integer>> result = R.subsequences(Arrays.asList(1, 2, 3));
        Set<List<Integer>> resultSet = new TreeSet<>(comparator);
        resultSet.addAll(result);

        Assertions.assertTrue(expected.equals(resultSet));
    }

    @Test
    public void exploit() {
        double energy = 100.0;
        double[] e = R.getExploitablePowers(energy);

        for(double x : e)
            System.out.println(x);
    }
}
