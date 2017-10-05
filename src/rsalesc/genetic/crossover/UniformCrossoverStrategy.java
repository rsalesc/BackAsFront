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

package rsalesc.genetic.crossover;

import rsalesc.genetic.Chromosome;
import rsalesc.genetic.ChromosomeSequence;
import rsalesc.genetic.ChromosomeSource;
import rsalesc.genetic.CrossoverStrategy;

import java.util.ArrayList;

/**
 * Created by Roberto Sales on 30/09/17.
 */
public class UniformCrossoverStrategy extends CrossoverStrategy {
    @Override
    public ArrayList<Chromosome> crossover(Chromosome ...chromosomes) {
        if(chromosomes.length != 2)
            throw new IllegalStateException("uniform crossover must be applied to two (2) chromosomes");

        Chromosome left = chromosomes[0];
        Chromosome right = chromosomes[1];

        if(left.size() != right.size())
            throw new IllegalStateException("crossovered strings cannot have different sizes");

        int n = left.size();

        boolean mutated = false;
        ChromosomeSequence sequenceLeft = (ChromosomeSequence) left.getSequence().clone();
        ChromosomeSequence sequenceRight = (ChromosomeSequence) right.getSequence().clone();

        for(int i = 0; i < n; i++) {
            if(Math.random() < 0.5) {
                char leftChar = sequenceLeft.get(i);
                sequenceLeft.set(i, sequenceRight.get(i));
                sequenceRight.set(i, leftChar);
                mutated = true;
            }
        }

        ArrayList<Chromosome> res = new ArrayList<>();
        res.add(new Chromosome(left.getLayout(), sequenceLeft, mutated ? ChromosomeSource.CROSSOVER : left.getSource()));
        res.add(new Chromosome(right.getLayout(), sequenceRight, mutated ? ChromosomeSource.CROSSOVER : right.getSource()));

        return res;
    }
}
