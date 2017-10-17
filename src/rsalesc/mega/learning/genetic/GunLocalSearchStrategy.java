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

import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.Timer;
import rsalesc.genetic.*;
import rsalesc.runner.ConsoleProgress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Roberto Sales on 04/10/17.
 */
public class GunLocalSearchStrategy extends EvolutionStrategy<Double> {
    private double neighborThreshold;
    private double promisingThreshold;
    private double learningRate;
    private final int maxIterations;
    private final CrossoverStrategy strategy;
    private final GunChromosomeLayoutProvider provider;
    private final int crosses;

    private ArrayList<Chromosome> tabu = new ArrayList<>();
    private Set<Chromosome> promising = new HashSet<>();
    private boolean logs = false;

    public GunLocalSearchStrategy(FitnessFunction<Double> fitnessFunction, double neighborThreshold, double learningRate, int maxIterations, CrossoverStrategy strategy, GunChromosomeLayoutProvider provider, int crosses) {
        super(fitnessFunction);
        this.crosses = crosses;
        this.setNeighborThreshold(neighborThreshold);
        this.learningRate = learningRate;
        this.maxIterations = maxIterations;
        this.strategy = strategy;
        this.provider = provider;
    }

    public void setTabuNeighborThreshold(double x) {
        this.neighborThreshold = x;
    }

    public void setPromisingNeighborThreshold(double x) {
        this.promisingThreshold = x;
    }

    public void setNeighborThreshold(double x) {
        this.neighborThreshold = x;
        this.promisingThreshold = x;
    }

    public void setLearningRate(double x) {
        this.learningRate = x;
    }

    public void log() {
        logs = true;
    }

    @Override
    public Population evolve(ArrayList<ChromosomePerformance<Double>> perfs, ChromosomeLayout layout) {
        int genSize = perfs.size();

        Population nextGen = new Population();
        Chromosome fittest = GeneticUtils.getFittest(perfs).getChromosome();

        promising.add(fittest);

        nextGen.add(fittest);

        int cnt = 0;
        int cntNeeded = Math.min(Math.max((genSize - nextGen.size() - 1) / 2 * 2, 0), crosses / 2 * 2);
        String lastBuilt = "";

        Timer timer = new Timer();

        while(nextGen.size() + 1 < genSize && cnt + 2 <= crosses) {
            if(logs) {
                String etaString = cnt > 0 ? " (ETA: " + Timer.getFormattedMinutes(Timer.getEta(timer.spent(), cnt, cntNeeded - cnt)) + ")": "";
                System.out.print("\r" + (lastBuilt = ConsoleProgress.build(cnt, cntNeeded, "Crossover + LS..." + etaString)));
            }

            cnt += 2;

            ArrayList<ChromosomePerformance<Double>> subset = GeneticUtils.pickSubset(perfs, 2);
            if(subset.size() < 2)
                break;

            timer.start();
            ArrayList<Chromosome> crossed = strategy.crossover(subset.get(0).getChromosome(), subset.get(1).getChromosome());
            nextGen.add(doLocalSearch(crossed.get(0)));
            nextGen.add(doLocalSearch(crossed.get(1)));
            timer.pause();
        }

        if(logs)
            System.out.print("\r" + String.join("", Collections.nCopies(lastBuilt.length(), " ")) + "\r");

        nextGen.unique();

        while(nextGen.size() < genSize)
            nextGen.add(Chromosome.random(layout));

        return nextGen;
    }

    public Chromosome doLocalSearch(Chromosome source) {
        double[] direction = pickBiasedDirection(source, learningRate, 2.5 * learningRate);
        int fails = 0;

        searchLoop:
        for(int i = 0; i < maxIterations; i++) {
            if(fails > 3 * maxIterations)
                break;

            // just found an optimum point already visited. randomize and crossover
            // there is no point in trying to optimize it since it can be really bad already
            // just go for the next gen and if it is good it will be picked
            for(Chromosome promise : promising) {
                if (provider.distance(promise, source) < promisingThreshold) {
                    source = strategy.crossover(Chromosome.random(provider.getLayout()), source).get(0);
                    return source;
                }
            }

            // past state and not promising, change direction
            for(Chromosome seen : tabu) {
                if (!promising.contains(seen) && provider.distance(seen, source) < neighborThreshold) {
                    direction = pickRandomDirection(3 * learningRate, 5 * learningRate);
                    i--;
                    fails++;
                    continue searchLoop;
                }
            }

            tabu.add(source);
            walk(source, direction);

            direction = rescaleDirection(direction, learningRate, learningRate * 2);
        }

        return source;
    }

    private void walk(Chromosome chromosome, double[] gradient) {
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
        }
    }

    private LongDecimalGene[] getGenes() {
        LongDecimalGene[] weightGenes = provider.getWeightGenes();
        LongDecimalGene[] paramGenes = provider.getParamGenes();

        return R.concat(weightGenes, paramGenes);
    }

    private double[] pickRandomDirection(double minRate, double maxRate) {
        double[] dir = new double[getGenes().length];
        double scale = Math.random() * (maxRate - minRate) + minRate;
        double total = 0;

        for(int i = 0; i < dir.length; i++) {
            dir[i] = (Math.random() - 0.5) * 2;
            total += dir[i] * dir[i];
        }

        total = R.sqrt(total);

        for(int i = 0; i < dir.length; i++) {
            dir[i] *= scale / total;
        }

        return dir;
    }

    private double[] pickBiasedDirection(Chromosome source, double minRate, double maxRate) {
        double[] dir = pickRandomDirection(minRate, maxRate);

        ArrayList<Chromosome> promisingList = new ArrayList<>();
        promisingList.addAll(promising);

        Chromosome promise = promisingList.get((int) (Math.random() * promisingList.size()));

        double[] gradient = pickGradient(source, promise);

        for(int i = 0; i < dir.length; i++) {
            dir[i] = (dir[i] + 2 * gradient[i]) / 3;
        }

        return dir;
    }

    private double[] pickUnbiasedDirection(Chromosome source, double minRate, double maxRate) {
        double[] dir = pickRandomDirection(minRate, maxRate);

        ArrayList<Chromosome> promisingList = new ArrayList<>();
        promisingList.addAll(promising);

        Chromosome promise = promisingList.get((int) (Math.random() * promisingList.size()));

        double[] gradient = pickGradient(source, promise);

        for(int i = 0; i < dir.length; i++) {
            dir[i] = (dir[i] - 1.5 * gradient[i]) / 2.5;
        }

        return dir;
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

    private double[] rescaleDirection(double[] direction, double minRate, double maxRate) {
        double scale = Math.random() * (maxRate - minRate) + minRate;
        double total = 0;

        for(int i = 0; i < direction.length; i++) {
            total += direction[i] * direction[i];
        }

        total = R.sqrt(total);

        for(int i = 0; i < direction.length; i++) {
            direction[i] *= scale / total;
        }

        return direction;
    }
}
