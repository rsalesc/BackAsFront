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
import java.util.Objects;
import java.util.Random;

/**
 * Created by Roberto Sales on 30/09/17.
 */
public class Chromosome implements Cloneable, Serializable {
    private static final long serialVersionUID = 1902999995556661231L;

    private ChromosomeSource source;
    private ChromosomeSequence sequence;
    private ChromosomeLayout layout;

    public Chromosome(ChromosomeLayout layout) {
        this.layout = layout;
        this.sequence = new ChromosomeSequence(layout.getBitLength());
        this.source = ChromosomeSource.EMPTY;
    }

    public Chromosome(ChromosomeLayout layout, ChromosomeSequence sequence, ChromosomeSource source) {
        if(sequence.size() != layout.getBitLength())
            throw new IllegalStateException("layout and sequence have different bit-lengths");

        this.layout = layout;
        this.sequence = sequence;
        this.source = source;
    }

    public void setLayout(ChromosomeLayout newLayout) {
        if(!newLayout.compatibleWith(sequence))
            throw new IllegalStateException("new layout is not compatible with chromosome sequence");

        layout = newLayout;
    }

    public ChromosomeSequence getSequence() {
        return sequence;
    }

    public ChromosomeLayout getLayout() {
        return layout;
    }

    public ChromosomeSource getSource() {
        return source;
    }

    public int size() {
        return sequence.size();
    }

    public static Chromosome random(ChromosomeLayout layout) {
        return new Chromosome(layout, ChromosomeSequence.getRandomSequence(layout.getBitLength()), ChromosomeSource.RANDOM);
    }

    public static Chromosome random(ChromosomeLayout layout, Random rng) {
        return new Chromosome(layout, ChromosomeSequence.getRandomSequence(layout.getBitLength(), rng), ChromosomeSource.RANDOM);
    }

    public static Chromosome mutate(Chromosome chromosome, double mutationRate) {
        ChromosomeSequence sequence = (ChromosomeSequence) chromosome.sequence.clone();
        boolean mutated = false;
        for(int i = 0; i < sequence.size(); i++) {
            if (Math.random() < mutationRate) {
                sequence.flip(i);
                mutated = true;
            }
        }

        return new Chromosome(chromosome.layout, sequence, mutated ? ChromosomeSource.MUTATION : chromosome.source);
    }

    public static Chromosome survive(Chromosome chromosome) {
        ChromosomeSequence sequence = (ChromosomeSequence) chromosome.sequence.clone();
        return new Chromosome(chromosome.layout, sequence, ChromosomeSource.ELITISM);
    }

    public static ArrayList<Chromosome> crossover(CrossoverStrategy strategy, Chromosome ...chromosomes) {
        return strategy.crossover(chromosomes);
    }

    public <T> Allele<T> getAllele(ConcreteGene<T> gene) {
        return new Allele<>(gene, sequence.substring(layout.getConcreteGeneOffset(gene), gene.getBitLength()));
    }

    public <T> void setAllele(ConcreteGene<T> gene, T value) {
        String toSet = gene.stringifyValue(value);
        sequence.setBitString(layout.getConcreteGeneOffset(gene), toSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLayout(), getSequence());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Chromosome)
            return layout.equals(((Chromosome) obj).getLayout()) && sequence.equals(((Chromosome) obj).getSequence());
        return false;
    }

    @Override
    protected Object clone() {
        return new Chromosome(layout, (ChromosomeSequence) sequence.clone(), source);
    }

    @Override
    public String toString() {
        return sequence.toString();
    }
}
