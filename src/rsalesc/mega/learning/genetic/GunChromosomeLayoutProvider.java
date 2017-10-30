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
import rsalesc.baf2.core.utils.geometry.Range;
import rsalesc.genetic.*;
import rsalesc.mega.utils.Strategy;
import rsalesc.mega.utils.TargetingLog;

import java.util.ArrayList;

/**
 * Created by Roberto Sales on 01/10/17.
 */
public class GunChromosomeLayoutProvider {
    private final LongDecimalGene[] weightGenes;
    private final LongDecimalGene[] paramGenes;
    private final GeneticStrategy strategy;
    private ChromosomeLayout layout;

    public GunChromosomeLayoutProvider(GeneticStrategy strategy, int places) {
        this.strategy = strategy;
        this.layout = new ChromosomeLayout();

        this.weightGenes = new LongDecimalGene[strategy.getWeightsScheme().length];
        Range[] weightRanges = strategy.getWeightsScheme();

        for(int i = 0; i < weightGenes.length; i++) {
            weightGenes[i] = new LongDecimalGene(weightRanges[i].min, weightRanges[i].max, places);
            layout.addGene(weightGenes[i]);
        }

        this.paramGenes = new LongDecimalGene[strategy.getParamsScheme().length];
        Range[] paramRanges = strategy.getParamsScheme();

        for(int i = 0; i < paramGenes.length; i++) {
            paramGenes[i] = new LongDecimalGene(paramRanges[i].min, paramRanges[i].max, places);
            layout.addGene(paramGenes[i]);
        }
    }

    public ChromosomeLayout getLayout() {
        return layout;
    }

    public LongDecimalGene[] getWeightGenes() {
        return weightGenes;
    }

    public LongDecimalGene[] getParamGenes() {
        return paramGenes;
    }

    public int getDimensions() {
        return weightGenes.length;
    }

    public GeneticStrategy getStrategy() {
        return strategy;
    }

    public Allele<Double> getWeightAllele(Chromosome chromo, int i) {
        if(!chromo.getLayout().equals(this.layout))
            throw new IllegalStateException();

        LongDecimalGene[] weightGenes = this.getWeightGenes();

        return chromo.getAllele(weightGenes[i]);
    }

    public Allele<Double> getParamAllele(Chromosome chromo, int i) {
        if(!chromo.getLayout().equals(this.layout))
            throw new IllegalStateException();

        LongDecimalGene[] paramGenes = this.getParamGenes();

        return chromo.getAllele(paramGenes[i]);
    }

    public ArrayList<Allele<Double>> getEveryAllele(Chromosome chromo) {
        ArrayList<Allele<Double>> alleles = new ArrayList<>();
        for(int i = 0; i < weightGenes.length; i++) {
            alleles.add(chromo.getAllele(weightGenes[i]));
        }
        for(int i = 0; i < paramGenes.length; i++) {
            alleles.add(chromo.getAllele(paramGenes[i]));
        }

        return alleles;
    }

    public Strategy extractStrategy(Chromosome chromosome) {
        if(!chromosome.getLayout().equals(this.layout))
            throw new IllegalStateException();

        GeneticStrategy baseStrategy = this.getStrategy();

        double[] geneticWeights = new double[this.getDimensions()];

        for(int i = 0; i < geneticWeights.length; i++) {
            geneticWeights[i] = getWeightAllele(chromosome, i).getValue();
        }

        double[] geneticParams = new double[this.getParamGenes().length];

        for(int i = 0; i < geneticParams.length; i++) {
            geneticParams[i] = getParamAllele(chromosome, i).getValue();
        }

        Strategy resultantStrategy = new Strategy() {
            // getQuery must be synchronized because there is a non-atomic access to baseStrategy,
            // which is shared between the same resultantStrategy of different threads

            @Override
            public synchronized double[] getQuery(TargetingLog f) {
                baseStrategy.forceParams(getParams());
                return baseStrategy.getQuery(f);
            }

            @Override
            public double[] getParams() {
                return geneticParams;
            }

            @Override
            public double[] getWeights() {
                return geneticWeights;
            }
        };

        return resultantStrategy;
    }

    public Chromosome createChromosome(Strategy newStrategy) {
        if(newStrategy.getWeights().length != strategy.getWeightsScheme().length
                || newStrategy.getForcedParams().length != strategy.getParamsScheme().length)
            throw new IllegalStateException();

        Chromosome chromosome = new Chromosome(getLayout());

        for(int i = 0; i < getWeightGenes().length; i++) {
            chromosome.setAllele(getWeightGenes()[i], newStrategy.getWeights()[i]);
        }

        for(int i = 0; i < getParamGenes().length; i++) {
            chromosome.setAllele(getParamGenes()[i], newStrategy.getForcedParams()[i]);
        }

        return chromosome;
    }

    public double distance(Chromosome a, Chromosome b) {
        ArrayList<Allele<Double>> aValues = getEveryAllele(a);
        ArrayList<Allele<Double>> bValues = getEveryAllele(b);

        double distance = 0;

        for(int i = 0; i < aValues.size(); i++) {
            distance += R.sqr(aValues.get(i).getValue() - bValues.get(i).getValue());
        }

        return R.sqrt(distance);
    }
}
