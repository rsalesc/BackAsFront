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

import rsalesc.genetic.CrossoverStrategy;
import rsalesc.genetic.FitnessFunction;

public class GunLocalSearchStrategyBuilder {
    private FitnessFunction<Double> fitnessFunction;
    private double neighborThreshold;
    private double learningRate;
    private int maxIterations;
    private int crosses;
    private CrossoverStrategy strategy;
    private GunChromosomeLayoutProvider provider;

    public GunLocalSearchStrategyBuilder setFitnessFunction(FitnessFunction<Double> fitnessFunction) {
        this.fitnessFunction = fitnessFunction;
        return this;
    }

    public GunLocalSearchStrategyBuilder setCrosses(int n) {
        this.crosses = n;
        return this;
    }

    public GunLocalSearchStrategyBuilder setNeighborThreshold(double neighborThreshold) {
        this.neighborThreshold = neighborThreshold;
        return this;
    }

    public GunLocalSearchStrategyBuilder setLearningRate(double learningRate) {
        this.learningRate = learningRate;
        return this;
    }

    public GunLocalSearchStrategyBuilder setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
        return this;
    }

    public GunLocalSearchStrategyBuilder setStrategy(CrossoverStrategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public GunLocalSearchStrategyBuilder setProvider(GunChromosomeLayoutProvider provider) {
        this.provider = provider;
        return this;
    }

    public GunLocalSearchStrategy createGunLocalSearchStrategy() {
        return new GunLocalSearchStrategy(fitnessFunction, neighborThreshold, learningRate, maxIterations, strategy, provider, crosses);
    }
}