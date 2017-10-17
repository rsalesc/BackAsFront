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

package rsalesc.genetic.evolution;

import rsalesc.genetic.*;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Roberto Sales on 30/09/17.
 */
public class MutateAndSurviveStrategy<T extends Comparable<T> & Serializable> extends EvolutionStrategy<T> {
    private double mutationRate;
    private double elitismRate;
    private double randomRate;
    private double crossRate;
    private CrossoverStrategy crossoverStrategy;

    public MutateAndSurviveStrategy(FitnessFunction<T> fitnessFunction, double mutationRate, double elitismRate,
                                                                    double crossRate, CrossoverStrategy strategy) {
        super(fitnessFunction);
        this.mutationRate = mutationRate;
        this.elitismRate = elitismRate;
        this.crossRate = crossRate;
        this.crossoverStrategy = strategy;
    }

    @Override
    public Population evolve(ArrayList<ChromosomePerformance<T>> perfs, ChromosomeLayout layout) {
        int genSize = perfs.size();

        Population nextGen = new Population();
        int elitismK = Math.max((int) (genSize * elitismRate) - 1, 0);


        ArrayList<Chromosome> selected = new ArrayList<>();
        if(elitismK > 0)
            selected.addAll(GeneticUtils.tournamentSelection(perfs, elitismK, Math.max(genSize / 10, 1), 0.75));

        nextGen.add(selected);

        int rem = genSize - selected.size() - 1;

        if(rem > 0) {
            int crossK = Math.min(rem, (int) (genSize * crossRate) / 2);

            int crossed = 0;
            for(int i = 0; i < crossK; i++) {
                ArrayList<Chromosome> paired =
                        GeneticUtils.tournamentSelection(perfs, 2, (int) (genSize * 0.15), 0.5);
                if(paired.size() < 2)
                    break;
                crossed += 2;
                nextGen.add(crossoverStrategy.crossover(paired.get(0), paired.get(1)));
            }
        }

        for(Chromosome chromosome : nextGen.getChromosomes()) {
            Chromosome.mutate(chromosome, mutationRate);
        }

        nextGen.add(GeneticUtils.getFittest(perfs).getChromosome());

        nextGen.unique();

        if(nextGen.size() < genSize) {
            nextGen.add(Population.random(genSize - nextGen.size(), layout).getChromosomes());
        }

        return nextGen;
    }
}
