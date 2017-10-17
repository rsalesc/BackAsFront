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

import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.Timer;
import rsalesc.runner.ConsoleProgress;
import rsalesc.runner.SerializeHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by Roberto Sales on 30/09/17.
 */
public abstract class FitnessFunction<T extends Comparable<T> & Serializable> {
    private boolean logs = false;

    public abstract T getFitness(Chromosome chromosome);

    public void log() {
        logs = true;
    }

    public ArrayList<ChromosomePerformance<T>> getFitness(Population population) {
        ArrayList<ChromosomePerformance<T>> res = new ArrayList<>();

        int processed = 0;
        String lastBuilt = "";

        Timer timer = new Timer();

        for(Chromosome chromosome : population.getChromosomes()) {
            String etaString = "";
            if(processed > 0) {
                etaString = " (ETA " + Timer.getFormattedMinutes(Timer.getEta(timer.spent(), processed, (population.size() - processed))) + ")";
            }

            if(logs) System.out.print("\r" + (lastBuilt = ConsoleProgress.build(processed, population.size(),
                    "Evaluating population..." + etaString)));
            timer.start();
            res.add(new ChromosomePerformance<>(chromosome, getFitness(chromosome)));
            timer.pause();
            processed++;
        }

        if(logs) System.out.print("\r"
            + String.join("", Collections.nCopies(lastBuilt.length(), " "))
            + "\r");

        return res;
    }

    public ChromosomePerformance<T> getFittest(Population population) {
        Chromosome best = null;
        T bestValue = null;

        for(Chromosome chromosome : population.getChromosomes()) {
            T cur = getFitness(chromosome);
            if(bestValue == null || cur.compareTo(bestValue) > 0) {
                bestValue = cur;
                best = chromosome;
            }
        }

        return new ChromosomePerformance<>(best, bestValue);
    }
}
