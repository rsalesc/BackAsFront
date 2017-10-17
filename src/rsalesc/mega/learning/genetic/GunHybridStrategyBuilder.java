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

public class GunHybridStrategyBuilder {
    private FitnessFunction<Double> fitnessFunction;
    private double mutationRate = 0;
    private double survivalRate = 0;
    private double offspringRate;
    private double learningRate;
    private GunChromosomeLayoutProvider provider;
    private CrossoverStrategy strategy;
    private boolean localSearch = true;

    public GunHybridStrategyBuilder setFitnessFunction(FitnessFunction<Double> fitnessFunction) {
        this.fitnessFunction = fitnessFunction;
        return this;
    }

    public GunHybridStrategyBuilder setMutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
        return this;
    }

    public GunHybridStrategyBuilder disableLocalSearch() {
        this.localSearch = false;
        return this;
    }

    public GunHybridStrategyBuilder setSurvivalRate(double survivalRate) {
        this.survivalRate = survivalRate;
        return this;
    }

    public GunHybridStrategyBuilder setOffspringRate(double offspringRate) {
        this.offspringRate = offspringRate;
        return this;
    }

    public GunHybridStrategyBuilder setLearningRate(double learningRate) {
        this.learningRate = learningRate;
        return this;
    }

    public GunHybridStrategy createGunGradientDescentStrategy() {
        return new GunHybridStrategy(fitnessFunction, mutationRate, survivalRate,
                offspringRate, learningRate, localSearch,
                provider, strategy);
    }

    public GunHybridStrategyBuilder setProvider(GunChromosomeLayoutProvider provider) {
        this.provider = provider;
        return this;
    }

    public GunHybridStrategyBuilder setCrossoverStrategy(CrossoverStrategy strategy) {
        this.strategy = strategy;
        return this;
    }
}