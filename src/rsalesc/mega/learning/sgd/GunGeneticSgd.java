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

import rsalesc.baf2.core.GlobalStorage;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.Timer;
import rsalesc.genetic.Chromosome;
import rsalesc.genetic.Generation;
import rsalesc.genetic.LongDecimalGene;
import rsalesc.genetic.Population;
import rsalesc.mega.learning.genetic.*;
import rsalesc.mega.learning.recording.DuelRecord;
import rsalesc.mega.learning.recording.DuelRecordSuperPack;
import rsalesc.mega.utils.BatchIterator;
import rsalesc.mega.utils.Strategy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.*;

public class GunGeneticSgd {
    private final static int THREADS = 5;

    private final DuelRecordSuperPack pack;
    private final double learningRate;
    private final int batchSize;
    private final int popSize;

    private final Class<? extends GeneticGunTargeting> targetingClazz;
    private final Constructor<? extends GeneticGunTargeting> targetingConstructor;
    private final GeneticStrategy strategy;

    private final GunChromosomeLayoutProvider layoutProvider;

    private boolean logs = false;
    private File cache = null;

    public GunGeneticSgd(DuelRecordSuperPack pack, double learningRate, int batchSize, int popSize, Class<? extends GeneticGunTargeting> targetingClazz, GeneticStrategy strategy) throws NoSuchMethodException {
        this.pack = pack;
        this.learningRate = learningRate;
        this.batchSize = batchSize;
        this.popSize = popSize;

        this.targetingClazz = targetingClazz;
        this.targetingConstructor = targetingClazz.getConstructor(int.class, Strategy.class);
        this.strategy = strategy;

        this.layoutProvider = new GunChromosomeLayoutProvider(strategy, 5);
    }

    public GunChromosomeLayoutProvider getLayoutProvider() {
        return layoutProvider;
    }

    public void setCache(File cache) {
        this.cache = cache;
    }

    public void log() {
        logs = true;
    }

    public List<Strategy> train(int iterations) throws IOException {
        if(cache != null && cache.exists() && !cache.isFile())
            throw new IllegalStateException("cache file exists but it's actually a directory");

        int popSize = this.popSize;

        Generation<Double> cachedGeneration = GunBattleTrainer.loadGeneration(cache, layoutProvider.getLayout());

        int initialIteration;
        Population population;

        Timer measure = new Timer();

        if(cachedGeneration != null) {
            initialIteration = cachedGeneration.getIndex() + 1;

            // if population size is too small, let's generate some random individuals
            while(cachedGeneration.getPopulation().size() < popSize) {
                Chromosome chromosome = Chromosome.random(layoutProvider.getLayout());
                cachedGeneration.getPopulation().add(chromosome);
            }

            // if population size is too big, let's update popSize
            popSize = Math.max(popSize, cachedGeneration.getPopulation().size());

            if(cachedGeneration.getIndex() >= iterations) {
                System.out.println("Last generation (after " + cachedGeneration.getIndex() + " iterations, cached)");

                ArrayList<Strategy> strategies = new ArrayList<>();

                for(Chromosome chromosome : cachedGeneration.getPopulation().getChromosomes()) {
                    strategies.add(layoutProvider.extractStrategy(chromosome));
                }

                return strategies;
            } else {
                System.out.println("Starting from " + cachedGeneration.getIndex() + "-th generation (cached)");
                population = cachedGeneration.getPopulation();
            }
        } else {
            System.out.println("Starting from a random population");
            population = Population.random(popSize, layoutProvider.getLayout());
            initialIteration = 0;
        }

        if(population.size() < popSize) {
            population.add(Population.random(popSize - population.size(), layoutProvider.getLayout()).getChromosomes());
        }

        for(int i = initialIteration; i < iterations; i++) {
            measure.start();

            List<BatchDouble> list = evolve(population);
            double bestMean = getBestMean(list);

            long spent = measure.spent();
            long delta = measure.pause();
            long eta = (long) ((double) spent / (i - initialIteration + 1) * (iterations - i - 1));

            System.out.println(i + "-th generation performance: " + R.formattedPercentage(bestMean)
                    + " (spent " + Timer.getFormattedMinutes(delta)
                    + ", total " + Timer.getFormattedMinutes(spent)
                    + ", ETA " + Timer.getFormattedMinutes(eta) + ")");

            for(int j = 0; j < list.size(); j++) {
                Strategy strategy = layoutProvider.extractStrategy(population.getChromosomes().get(j));
                System.out.println(strategy);
                System.out.println("Value: " + R.formattedPercentage(list.get(j).normalizedMean()));
                System.out.println();
            }

            Generation<Double> generation = new Generation<>(i, population, null);

            if(generation.getPopulation().size() != popSize)
                System.err.println("Population size decreased from one generation to the next!");

            GunBattleTrainer.saveGeneration(cache, generation);
        }

        ArrayList<Strategy> strategies = new ArrayList<>();

        for(Chromosome chromosome : population.getChromosomes()) {
            strategies.add(layoutProvider.extractStrategy(chromosome));
        }

        return strategies;
    }

    public double getBestMean(List<BatchDouble> list) {
        double best = 0;

        for(BatchDouble batch : list)
            best = Math.max(best, batch.normalizedMean());

        return best;
    }

    public List<BatchDouble> evolve(Population population) {
        pack.setSeed(new Random().nextLong());

        ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
        BatchIterator<DuelRecord> batchIterator = pack.batchIterator();

        ArrayList<ArrayList<BatchResult>> results = new ArrayList<>();

        for(Chromosome ignored : population.getChromosomes()) {
            results.add(new ArrayList<>());
        }


        while(batchIterator.hasNext()) {
            Iterable<DuelRecord> records = batchIterator.consume(batchSize); // TODO: make it more efficient

            List<Chromosome> chromos = population.getChromosomes();
            for(int i = 0; i < chromos.size(); i++) {
                results.get(i).add(applyLineSgd(executorService, chromos.get(i), records));
            }
        }

        ArrayList<BatchDouble> ans = new ArrayList<>();

        for(ArrayList<BatchResult> batch : results) {
            ans.add(new BatchDouble(batch));
        }

        return ans;
    }

    private LongDecimalGene[] getGenes() {
        LongDecimalGene[] weightGenes = layoutProvider.getWeightGenes();
        LongDecimalGene[] paramGenes = layoutProvider.getParamGenes();

        return R.concat(weightGenes, paramGenes);
    }

    public BatchResult applySgd(ExecutorService executorService, Chromosome chromosome, Iterable<DuelRecord> records) {
        LongDecimalGene[] genes = getGenes();

        double[] gradient = new double[genes.length];

        for(int i = 0; i < gradient.length; i++) {
            double x = chromosome.getAllele(genes[i]).getValue();
            double h = learningRate; // TODO: ?

            chromosome.setAllele(genes[i], x + h);
            double posPerf = evaluate(executorService, chromosome, records).mean();

            chromosome.setAllele(genes[i], x - h);
            double negPerf = evaluate(executorService, chromosome, records).mean();

            chromosome.setAllele(genes[i], x);
            double curPerf = evaluate(executorService, chromosome, records).mean();

            if(curPerf > posPerf && curPerf > negPerf)
                continue;

            gradient[i] = (posPerf - negPerf) / (2*h);
        }

        for(int i = 0; i < gradient.length; i++) {
            chromosome.setAllele(genes[i], chromosome.getAllele(genes[i]).getValue() + gradient[i] * learningRate);
        }

        return evaluate(executorService, chromosome, records);
    }

    public BatchResult applyIncrementalSgd(ExecutorService executorService, Chromosome chromosome, Iterable<DuelRecord> records) {
        LongDecimalGene[] genes = getGenes();

        List<LongDecimalGene> acc = Arrays.asList(genes);
        Collections.shuffle(acc);

        genes = acc.toArray(new LongDecimalGene[0]);

        for (LongDecimalGene gene : genes) {
            double x = chromosome.getAllele(gene).getValue();
            double h = learningRate; // TODO: ?

            chromosome.setAllele(gene, x + h);
            double posPerf = evaluate(executorService, chromosome, records).mean();

            chromosome.setAllele(gene, x - h);
            double negPerf = evaluate(executorService, chromosome, records).mean();

            chromosome.setAllele(gene, x);
            double curPerf = evaluate(executorService, chromosome, records).mean();

            if (curPerf > posPerf && curPerf > negPerf)
                continue;

            chromosome.setAllele(gene, posPerf > negPerf ? x + h : x - h);
        }

        return evaluate(executorService, chromosome, records);
    }

    public BatchResult applyLineSgd(ExecutorService executorService, Chromosome chromosome, Iterable<DuelRecord> records) {
        LongDecimalGene[] genes = getGenes();

        List<LongDecimalGene> acc = Arrays.asList(genes);
        Collections.shuffle(acc);

        genes = acc.toArray(new LongDecimalGene[0]);

        double curPerf = evaluate(executorService, chromosome, records).mean();

        for (LongDecimalGene gene : genes) {
            for (int j = 0; j < 5; j++) {
                double x = chromosome.getAllele(gene).getValue();
                double h = learningRate * (1 << j); // TODO: ?

                chromosome.setAllele(gene, x + h);
                double posPerf = evaluate(executorService, chromosome, records).mean();

                chromosome.setAllele(gene, x - h);
                double negPerf = evaluate(executorService, chromosome, records).mean();

                chromosome.setAllele(gene, x);

                if (curPerf > posPerf && curPerf > negPerf)
                    continue;

                chromosome.setAllele(gene, posPerf > negPerf ? x + h : x - h);
            }
        }

        return evaluate(executorService, chromosome, records);
    }

    public BatchResult evaluate(ExecutorService executorService, Chromosome chromosome, Iterable<DuelRecord> records) {
        Strategy chromosomeStrategy = layoutProvider.extractStrategy(chromosome);

        double totalValue = 0;
        int totalPlayed = 0;

        // Empty global storage
        GlobalStorage.getInstance().clear();
        System.gc();

        ArrayList<Callable<Double>> callables = new ArrayList<>();

        int pointer = 0;
        for (DuelRecord record : records) {
            final int finalPointer = pointer;
            callables.add(new Callable<Double>() {
                @Override
                public Double call() throws Exception {
                    GeneticGunTargeting targeting = targetingConstructor.newInstance(finalPointer, chromosomeStrategy);
                    return GunFitnessFunction.evaluateBattle(targeting, record.getLogs());
                }
            });

            pointer++;
        }

        try {
            List<Future<Double>> outcomes = executorService.invokeAll(callables);

            for (Future<Double> outcome : outcomes) {
                totalValue += outcome.get();
                totalPlayed++;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return new BatchResult(totalValue, totalPlayed);
    }
}
