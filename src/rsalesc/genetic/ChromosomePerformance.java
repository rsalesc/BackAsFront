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

/**
 * Created by Roberto Sales on 30/09/17.
 */
public class ChromosomePerformance<T extends Comparable<T> & Serializable> implements Comparable<ChromosomePerformance<T>>, Serializable {
    private static final long serialVersionUID = 31231231L;
    private final Chromosome chromosome;
    private final T fitness;

    public ChromosomePerformance(Chromosome chromosome, T fitness) {
        this.chromosome = chromosome;
        this.fitness = fitness;
    }

    public Chromosome getChromosome() {
        return chromosome;
    }

    public T getFitness() {
        return fitness;
    }

    @Override
    public String toString() {
        return getChromosome().toString() + " " + getFitness().toString();
    }

    @Override
    public int compareTo(ChromosomePerformance<T> o) {
        return fitness.compareTo(o.getFitness());
    }
}