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

package rsalesc.genetic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Roberto Sales on 30/09/17.
 */
public class GeneticUtils {
    public static <T extends Comparable<T> & Serializable> ArrayList<Chromosome>
        tournamentSelection(ArrayList<ChromosomePerformance<T>> perfs, int K, int setSize, double p) {
        ArrayList<ChromosomePerformance<T>> tmp = new ArrayList<>();
        tmp.addAll(perfs);

        K = Math.min(K, tmp.size());

        ArrayList<Chromosome> selected = new ArrayList<>();
        while(selected.size() < K) {
            ArrayList<ChromosomePerformance<T>> set = pickSubset(tmp, setSize);

            int i = 0;
            double pi = p;
            double x = Math.random();

            while(x > pi && i + 1 < setSize) {
                i++;
                pi = 1.0 - (1.0 - pi) * p;
            }

            selected.add(set.get(i).getChromosome());
            tmp.remove(set.get(i));
        }

        return selected;
    }

    public static <T extends Comparable<T> & Serializable> ArrayList<ChromosomePerformance<T>>
        pickSubset(ArrayList<ChromosomePerformance<T>> input, int setSize) {
        Collections.shuffle(input);
        setSize = Math.min(setSize, input.size());

        ArrayList<ChromosomePerformance<T>> set = new ArrayList<>();
        for(int i = 0; i < setSize; i++) set.add(input.get(i));

        Collections.sort(set);
        Collections.reverse(set);

        return set;
    }

    public static <T extends Comparable<T> & Serializable> ChromosomePerformance<T>
        getFittest(ArrayList<ChromosomePerformance<T>> input) {
        return Collections.max(input);
    }
}
