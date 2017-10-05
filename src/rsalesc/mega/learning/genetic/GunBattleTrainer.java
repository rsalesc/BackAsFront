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

import com.sun.xml.internal.ws.encoding.soap.SerializationException;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.Timer;
import rsalesc.genetic.*;
import rsalesc.genetic.crossover.TwoPointCrossoverStrategy;
import rsalesc.genetic.evolution.MutateAndSurviveStrategy;
import rsalesc.mega.learning.recording.DuelRecordSuperPack;
import rsalesc.mega.utils.Strategy;
import rsalesc.runner.FileUtils;
import rsalesc.runner.SerializeHelper;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by Roberto Sales on 01/10/17.
 */
public class GunBattleTrainer {
    private final int popSize;
    private final DuelRecordSuperPack superPack;
    private final Class<? extends GeneticGunTargeting> targetingClazz;
    private final GeneticStrategy strategy;
    private final int threads;
    private File cache = null;
    private boolean logs = false;

    public GunBattleTrainer(int popSize, DuelRecordSuperPack superPack, Class<? extends GeneticGunTargeting> targetingClazz,
                            GeneticStrategy strategy, int threads) {
        this.popSize = popSize;
        this.superPack = superPack;
        this.targetingClazz = targetingClazz;
        this.strategy = strategy;
        this.threads = threads;
    }

    public void log() {
        logs = true;
    }

    public void setCache(File cache) {
        this.cache = cache;
    }

    public Strategy train(int gens) throws IOException, NoSuchMethodException {
        if(cache != null && cache.exists() && !cache.isFile())
            throw new IllegalStateException("cache file exists but it's actually a directory");

        GunChromosomeLayoutProvider layoutProvider = new GunChromosomeLayoutProvider(strategy);

        GunFitnessFunction fitnessFn = new GunFitnessFunction(superPack, layoutProvider, targetingClazz, threads);

        MutateAndSurviveStrategy<Double> evolution = new MutateAndSurviveStrategy<>(
                fitnessFn, 0.02, 0.3, 0.5, new TwoPointCrossoverStrategy()
        );

//        GunHybridStrategy evolution = new GunHybridStrategyBuilder()
//                .setLearningRate(0.3)
//                .setOffspringRate(0.5)
//                .setSurvivalRate(0.3)
//                .setMutationRate(0.02)
//                .setFitnessFunction(fitnessFn)
//                .setCrossoverStrategy(new TwoPointCrossoverStrategy())
//                .setProvider(layoutProvider)
//                .createGunGradientDescentStrategy();

//        GunLocalSearchStrategy evolution = new GunLocalSearchStrategyBuilder()
//                .setStrategy(new TwoPointCrossoverStrategy())
//                .setFitnessFunction(fitnessFn)
//                .setProvider(layoutProvider)
//                .setCrosses(4)
//                .setLearningRate(0.7)
//                .setNeighborThreshold(0.4)
//                .setMaxIterations(3)
//                .createGunLocalSearchStrategy();

//        evolution.log();

        Generation<Double> cachedGeneration = loadGeneration(cache, layoutProvider.getLayout());

        int initialIteration;
        Population population;

        Timer measure = new Timer();
        measure.start();

        if(cachedGeneration != null) {
            initialIteration = cachedGeneration.getIndex() + 1;
            fitnessFn.cache(cachedGeneration.getPerformance());

            // if population size is too small, let's generate some random individuals
            while(cachedGeneration.getPopulation().size() < popSize) {
                Chromosome chromosome = Chromosome.random(layoutProvider.getLayout());
                cachedGeneration.getPopulation().add(chromosome);
            }

            Collections.sort(cachedGeneration.getPerformance());

            // if population size is too big, let's discard the worst individuals
            while(cachedGeneration.getPopulation().size() > popSize) {
                cachedGeneration.getPopulation().remove(cachedGeneration.getPerformance().get(0).getChromosome());
                cachedGeneration.getPerformance().remove(0);
            }

            if(cachedGeneration.getIndex() >= gens) {
                ChromosomePerformance<Double> fittest = GeneticUtils.getFittest(cachedGeneration.getPerformance());

                System.out.println("Last generation performance (after " + cachedGeneration.getIndex() + " iterations): "
                        + R.formattedPercentage(fittest.getFitness())
                        + " (cached)");

                return layoutProvider.extractStrategy(fittest.getChromosome());
            } else {
                System.out.println(cachedGeneration.getIndex() + "-th generation performance: "
                        + R.formattedPercentage(GeneticUtils.getFittest(cachedGeneration.getPerformance()).getFitness())
                        + " (cached)");

                ArrayList<ChromosomePerformance<Double>> perfs = fitnessFn.getFitness(cachedGeneration.getPopulation());
                population = evolution.evolve(perfs, layoutProvider.getLayout());
            }
        } else {
            population = Population.random(popSize, layoutProvider.getLayout());
            initialIteration = 0;
        }

        if(population.size() < popSize) {
            population.add(Population.random(popSize - population.size(), layoutProvider.getLayout()).getChromosomes());
        }


        for(int i = initialIteration; i < gens; i++) {
            FitnessFunction<Double> evolutionFn = evolution.getFitnessFunction();
            evolutionFn.log();

            ArrayList<ChromosomePerformance<Double>> perfs = evolutionFn.getFitness(population);

            for(ChromosomePerformance<Double> perf : perfs) {
                System.out.println(perf);
            }

            ChromosomePerformance<Double> fittest = GeneticUtils.getFittest(perfs);

            long spent = measure.spent();
            long delta = measure.pause();
            long eta = (long) ((double) spent / (i - initialIteration + 1) * (gens - initialIteration));

            System.out.println(i + "-th generation performance: "
                    + R.formattedPercentage(fittest.getFitness())
                    + " (spent " + Timer.getFormattedMinutes(delta)
                    + ", total " + Timer.getFormattedMinutes(spent)
                    + ", ETA " + Timer.getFormattedMinutes(eta) + ")");


            if(logs) {
                System.out.println("This was the fittest strategy:");
                System.out.println(layoutProvider.extractStrategy(fittest.getChromosome()));
            }

            Generation<Double> generation = new Generation<>(i, population, perfs);

            if(generation.getPopulation().size() != popSize)
                System.err.println("Population size decreased from one generation to the next!");

            saveGeneration(cache, generation);

            measure.start();
            population = evolution.evolve(perfs, layoutProvider.getLayout());
        }

        ArrayList<ChromosomePerformance<Double>> perfs = evolution.getFitnessFunction().getFitness(population);
        ChromosomePerformance<Double> fittest = GeneticUtils.getFittest(perfs);

        long spent = measure.spent();
        long delta = measure.pause();
        System.out.println("Last generation (after " +  gens + " iterations): "
                + R.formattedPercentage(fittest.getFitness())
                + " (spent " + Timer.getFormattedMinutes(delta)
                + ", total " + Timer.getFormattedMinutes(spent) + ");");

        saveGeneration(cache, new Generation<Double>(gens, population, perfs));

        return layoutProvider.extractStrategy(fittest.getChromosome());
    }

    private static void saveGeneration(File cache, Generation generation) throws IOException {
        if(cache != null) {
            if(!cache.getParentFile().isDirectory())
                if(!cache.getParentFile().mkdirs())
                    throw new IOException("parent directory of cached generation could not be created");

            try {
                Optional<byte[]> opt = SerializeHelper.convertToByteArray(generation);
                if(!opt.isPresent()) {
                    System.err.println(generation.getIndex() + "-th generation could not be serialized!");
                } else {
                    FileUtils.backupedWrite(cache.getAbsolutePath(), opt.get());
                }
            } catch(IOException ex) {
                System.err.println(generation.getIndex() + "-th generation could not be saved!");
                ex.printStackTrace();
            }
        }
    }

    private static <T extends Comparable<T> & Serializable> Generation<T> loadGeneration(File cache, ChromosomeLayout layout) throws IOException {
        if(cache == null)
            return null;

        if(!cache.exists())
            return null;

        byte[] data = Files.readAllBytes(Paths.get(cache.getAbsolutePath()));

        Optional<Generation<T>> opt = SerializeHelper.convertFrom(data);

        if(!opt.isPresent())
            throw new SerializationException("could not deserialize the cached generation");

        Generation<T> generation = opt.get();

        if(!generation.getPopulation().getLayout().compatibleWith(layout))
            throw new SerializationException("the cached generation has a different layout from the provided one");

        generation.getPopulation().setLayout(layout);

        return generation;
    }

}
