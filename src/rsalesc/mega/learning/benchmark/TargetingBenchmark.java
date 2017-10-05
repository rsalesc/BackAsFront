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

package rsalesc.mega.learning.benchmark;

import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.Timer;
import rsalesc.genetic.Chromosome;
import rsalesc.mega.learning.genetic.GeneticAdaptiveTargeting;
import rsalesc.mega.learning.genetic.BaseAdaptiveStrategy;
import rsalesc.mega.learning.genetic.GunChromosomeLayoutProvider;
import rsalesc.mega.learning.genetic.GunFitnessFunction;
import rsalesc.mega.learning.recording.DuelRecordSuperPack;
import rsalesc.mega.learning.recording.DuelRecorderRunner;

import static rsalesc.mega.learning.Tuning.ensureAdaptive;

/**
 * Created by Roberto Sales on 04/10/17.
 */
public class TargetingBenchmark {
    public static void main(String[] args) throws NoSuchMethodException {
        DuelRecorderRunner runner = new DuelRecorderRunner();
        DuelRecordSuperPack adaptivePack = ensureAdaptive(runner, 0);

        GunChromosomeLayoutProvider provider = new GunChromosomeLayoutProvider(new BaseAdaptiveStrategy());
        GunFitnessFunction fitnessFn = new GunFitnessFunction(adaptivePack, provider, GeneticAdaptiveTargeting.class, 4);

        Chromosome chromosome = provider.createChromosome(new HandAdaptiveStrategy());

        Timer measure = new Timer();
        measure.start();

        System.out.println("\rEvaluating strategy...\r");
        System.out.println("Performance: " + R.formattedPercentage(fitnessFn.getFitness(chromosome)));
        System.out.println("Took " + R.formattedDouble((double) Timer.getInMilliseconds(measure.stop()) / 1000) + " seconds.");

        runner.getEngineProvider().close();
        System.exit(0);
    }
}
