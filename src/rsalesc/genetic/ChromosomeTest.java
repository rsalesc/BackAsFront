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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import rsalesc.baf2.core.utils.Timer;
import rsalesc.genetic.crossover.SinglePointCrossoverStrategy;
import rsalesc.genetic.crossover.TwoPointCrossoverStrategy;
import rsalesc.genetic.crossover.UniformCrossoverStrategy;
import rsalesc.genetic.evolution.MutateAndSurviveStrategy;

import javax.lang.model.type.UnionType;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Roberto Sales on 30/09/17.
 */
class ChromosomeTest {
    @Test
    public void test() {
        ChromosomeLayout layout = new ChromosomeLayout();
        LongDecimalGene ldGene = new LongDecimalGene(0, 100, 2);
        LongGene lGene = new LongGene(0, 1000);
        layout.addGene(ldGene);
        layout.addGene(lGene);

        Population population = Population.random(200, layout);

        FitnessFunction<Double> fitnessFn = new FitnessFunction<Double>() {
            @Override
            public Double getFitness(Chromosome chromosome) {
                return chromosome.getAllele(lGene).getValue() + chromosome.getAllele(ldGene).getValue();
            }
        };

        MutateAndSurviveStrategy<Double> evolution = new MutateAndSurviveStrategy<Double>(
                fitnessFn, 0.02, 0.03,
                0.75, new TwoPointCrossoverStrategy());

        for(int i = 0; i < 500; i++) {
            ArrayList<ChromosomePerformance<Double>> perfs = fitnessFn.getFitness(population);
//            System.out.println(GeneticUtils.getFittest(perfs).getFitness());

            population = evolution.evolve(perfs, population.getLayout());
        }
    }

    @Test
    public void testStringify() {
        LongGene lGene = new LongGene(0, 1000);

        for(int i = 0; i <= 1000; i++) {
            Assertions.assertEquals(lGene.stringifyValue((long) i).length(), lGene.getBitLength());
        }
    }

    private void dump(Chromosome chromosome) {
        for(ConcreteGene gene : chromosome.getLayout().getConcreteGenes()) {
            Allele allele = chromosome.getAllele(gene);
            System.out.print(allele.toString() + " ");
            System.out.println(allele.getValue());
        }
    }
}