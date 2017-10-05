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
import java.util.*;

/**
 * Created by Roberto Sales on 30/09/17.
 */
public class Population implements Serializable {
    private static final long serialVersionUID = 1902391888888231L;

    private ArrayList<Chromosome> chromosomes = new ArrayList<>();
    private ChromosomeLayout layout;

    public void add(Chromosome chromosome) {
        chromosomes.add(chromosome);
        if(layout != null && !layout.equals(chromosome.getLayout()))
            throw new IllegalStateException("individuals of the same population cannot have different ChromosomeLayout");

        layout = chromosome.getLayout();
    }

    public ChromosomeLayout getLayout() {
        return layout;
    }

    public void remove(Chromosome chromosome) {
        chromosomes.remove(chromosome);
    }

    public void setLayout(ChromosomeLayout newLayout) {
        if(layout != null && !layout.compatibleWith(newLayout))
            throw new IllegalStateException("old layout is not compatible with new layout");

        for(Chromosome chromosome : chromosomes)
            chromosome.setLayout(newLayout);

        layout = newLayout;
    }

    public void add(Chromosome ...chromosomes) {
        for(Chromosome chromosome : chromosomes) {
            add(chromosome);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Population) {
            return chromosomes.equals(((Population) obj).getChromosomes());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(chromosomes.toArray());
    }

    public void add(Collection<Chromosome> chromosomes) {
        add(chromosomes.toArray(new Chromosome[0]));
    }

    public ArrayList<Chromosome> getChromosomes() {
        return chromosomes;
    }

    public int size() {
        return chromosomes.size();
    }

    public static Population random(int n, ChromosomeLayout layout) {
        Population population = new Population();
        for(int i = 0; i < n; i++)
            population.add(Chromosome.random(layout));

        return population;
    }

    public void unique() {
        Set<Chromosome> set = new HashSet<>();
        set.addAll(chromosomes);

        chromosomes.clear();
        chromosomes.addAll(set);
    }
}
