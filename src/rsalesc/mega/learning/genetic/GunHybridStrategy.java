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

package rsalesc.mega.learning.genetic;

import rsalesc.baf2.core.utils.*;
import rsalesc.baf2.core.utils.Timer;
import rsalesc.genetic.*;
import rsalesc.runner.ConsoleProgress;

import java.util.*;

/**
 * Created by Roberto Sales on 04/10/17.
 */
public class GunHybridStrategy extends EvolutionStrategy<Double> {
    private final double mutationRate;
    private final double survivalRate;
    private final double offspringRate;
    private final double learningRate;
    private final GunChromosomeLayoutProvider provider;
    private final CrossoverStrategy strategy;
    private final boolean localSearch;

    public boolean logs = false;

    public GunHybridStrategy(FitnessFunction<Double> fitnessFunction,
                             double mutationRate1, double survivalRate1, double offspringRate1,
                             double learningRate1, boolean ls, GunChromosomeLayoutProvider provider1, CrossoverStrategy strategy) {
        super(fitnessFunction);
        this.mutationRate = mutationRate1;
        this.survivalRate = survivalRate1;
        this.offspringRate = offspringRate1;
        this.learningRate = learningRate1;
        this.provider = provider1;
        this.strategy = strategy;
        this.localSearch = ls;
    }

    public void log() {
        logs = true;
    }

    @Override
    public Population evolve(ArrayList<ChromosomePerformance<Double>> perfs, ChromosomeLayout layout) {
        if(!provider.getLayout().equals(layout))
            throw new IllegalStateException();

        // First we will select the best chromosomes from each generation to perpetuate them as they are
        int genSize = perfs.size();

        Population nextGen = new Population();
        int elitismK = Math.max((int) (genSize * survivalRate) - 1, 0);

        ArrayList<Chromosome> survived = new ArrayList<>();

        if(elitismK > 0)
            survived.addAll(GeneticUtils.tournamentSelection(perfs, elitismK, Math.max((int) Math.sqrt(genSize), 1), 0.6));

        // Then we will surely select the best of them, if it was not selected yet
        Chromosome fittest = GeneticUtils.getFittest(perfs).getChromosome();
        if(!survived.contains(fittest))
            survived.add(fittest);

        nextGen.add(survived);

        int remaining = genSize - survived.size();

        ArrayList<OffspringChoice> offspring = new ArrayList<>();
        // Now we will build the offspring: the best chromosomes of each gen, but with the purpose of
        // cross-overing and/or optimizing them with LS + GD
        offspring = pickOffspring(perfs, layout, genSize, remaining);

        ArrayList<OffspringChoice> mutated = new ArrayList<>();

        for(OffspringChoice off : offspring)
            mutated.add(new OffspringChoice(Chromosome.mutate(off.getChromosome(), mutationRate), off.getParents()));

        // Now we gonna work at the gun-level. Let's pick our gradient, which will simply be a vector of doubles
        // corresponding to each gene in the order they appear in the chromosome.

        String lastBuilt = "";
        int cnt = 0;
        Timer timer = new Timer();
        for(OffspringChoice off : mutated) {
            if(logs) {
                String etaString = cnt > 0 ? " (ETA: " + Timer.getFormattedMinutes(Timer.getEta(timer.spent(), cnt, mutated.size() - cnt)) + ")": "";
                System.out.print("\r" + (lastBuilt = ConsoleProgress.build(cnt++, mutated.size(), "Improving offspring..." + etaString)));
            }
            timer.start();
            List<double[]> gradients = new ArrayList<>();

            if(localSearch) gradients.addAll(pickOffspringGradients(off));

            gradients.add(pickGradient(off.getChromosome(), fittest));

            for(double[] gradient : gradients) {
                for(int j = 0; j < 4; j++) {
                    if(!tryImprovement(off.getChromosome(), gradient))
                        break;
                }
            }
            timer.pause();

            nextGen.add(off.getChromosome());
        }

        if(logs)
            System.out.print("\r" + String.join("", Collections.nCopies(lastBuilt.length(), " ")) + "\r");

        remaining = genSize - nextGen.size();

        nextGen.unique();

        if(remaining > 0) {
            nextGen.add(Population.random(remaining, layout).getChromosomes());
        }

        return nextGen;
    }

    private boolean tryImprovement(Chromosome chromosome, double[] gradient) {
        double oldFitness = getFitnessFunction().getFitness(chromosome);

        LongDecimalGene[] genes = getGenes();
        double[] oldValues = new double[genes.length];

        for(int i = 0; i < genes.length; i++) {
            oldValues[i] = chromosome.getAllele(genes[i]).getValue();
            chromosome.setAllele(genes[i],  oldValues[i] + gradient[i] * learningRate);
        }

        double newFitness = getFitnessFunction().getFitness(chromosome);

        // rollback if there was no improvement
        if(newFitness < oldFitness) {
            for (int i = 0; i < genes.length; i++) {
                chromosome.setAllele(genes[i], oldValues[i]);
            }

            return false;
        } else {
            return true;
        }
    }

    private LongDecimalGene[] getGenes() {
        LongDecimalGene[] weightGenes = provider.getWeightGenes();
        LongDecimalGene[] paramGenes = provider.getParamGenes();

        return R.concat(weightGenes, paramGenes);
    }

    private List<double[]> pickOffspringGradients(OffspringChoice off) {
        ArrayList<double[]> res = new ArrayList<>();

        int length = getGenes().length;
        for(ArrayList<Chromosome> parents : R.subsequences(off.getParents())) {
            double[] avgGradient = new double[length];

            // For now use only the direction from both parents
            if(parents.size() < 2)
                continue;

            boolean good = true;

            // There is ascending vector pointing from the parent to the offspring chromosome
            for(Chromosome parent : parents) {
                if(getFitnessFunction().getFitness(parent) > getFitnessFunction().getFitness(off.getChromosome())) {
                    good = false;
                    break;
                }
            }

            if(!good)
                break;

            for(Chromosome parent : parents) {
                double[] gradient = pickGradient(parent, off.getChromosome());
                for(int i = 0; i < avgGradient.length; i++) {
                    avgGradient[i] += gradient[i];
                }
            }

            for(int i = 0; i < avgGradient.length; i++)
                avgGradient[i] /= parents.size();

            res.add(avgGradient);
        }

        return res;
    }

    private double[] pickGradient(Chromosome cur, Chromosome fittest) {
        LongDecimalGene[] genes = getGenes();

        double[] res = new double[genes.length];

        for(int i = 0; i < res.length; i++) {
            double bestValue = fittest.getAllele(genes[i]).getValue();
            double curValue = cur.getAllele(genes[i]).getValue();

            res[i] = bestValue - curValue;
        }

        return res;
    }

    private ArrayList<OffspringChoice> pickOffspring(ArrayList<ChromosomePerformance<Double>> perfs,
                                                ChromosomeLayout layout,
                                                int genSize,
                                                int remaining) {
        if(remaining == 0) return new ArrayList<>();

        int offspringK = Math.max((int) Math.sqrt(genSize * offspringRate), 0);
        if(offspringK == 0) return new ArrayList<>();

        ArrayList<Chromosome> selected =
                GeneticUtils.tournamentSelection(perfs, offspringK, Math.max((int) R.combinatorialSqrt(genSize), 1), 0.4);

        ArrayList<OffspringChoice> crossed = new ArrayList<>();

        for(int i = 0; i < selected.size(); i++) {
            for(int j = i+1; j < selected.size(); j++) {
                Chromosome selectOne = selected.get(i);
                Chromosome selectTwo = selected.get(j);

                for(Chromosome result : strategy.crossover(selectOne, selectTwo)) {
                    crossed.add(new OffspringChoice(result, selectOne, selectTwo));
                }
            }
        }

        if(crossed.size() < offspringK) {
            for(Chromosome chromo : GeneticUtils.tournamentSelection(perfs, offspringK - crossed.size(), Math.max((int) Math.sqrt(genSize), 1), 0.1)) {
                crossed.add(new OffspringChoice(chromo));
            }
        }

        return crossed;
    }

    private static class OffspringChoice {
        private final Chromosome chromosome;
        private final List<Chromosome> parents;

        private OffspringChoice(Chromosome chromosome, List<Chromosome> parents) {
            this.chromosome = chromosome;
            this.parents = parents;
        }

        private OffspringChoice(Chromosome chromosome, Chromosome ...parents) {
            this.chromosome = chromosome;
            this.parents = Arrays.asList(parents);
        }

        public List<Chromosome> getParents() {
            return parents;
        }

        public Chromosome getChromosome() {
            return chromosome;
        }
    }
}
