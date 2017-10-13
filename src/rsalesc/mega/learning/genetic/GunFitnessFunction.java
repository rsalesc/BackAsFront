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

import robocode.util.Utils;
import rsalesc.baf2.core.GlobalStorage;
import rsalesc.baf2.core.utils.BattleTime;
import rsalesc.baf2.core.utils.Pair;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.waves.BreakType;
import rsalesc.genetic.Chromosome;
import rsalesc.genetic.ChromosomePerformance;
import rsalesc.genetic.FitnessFunction;
import rsalesc.mega.gunning.guns.GeneratedAngle;
import rsalesc.mega.learning.recording.DuelRecord;
import rsalesc.mega.learning.recording.DuelRecordSuperPack;
import rsalesc.mega.utils.*;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.*;

/**
 * Created by Roberto Sales on 01/10/17.
 */
public class GunFitnessFunction extends FitnessFunction<Double> {
    private final GunChromosomeLayoutProvider provider;
    private final DuelRecordSuperPack pack;
    private final Constructor<? extends GeneticGunTargeting> targetingConstructor;
    private final int threads;

    private HashMap<Chromosome, Double> cache = new HashMap<>();

    public GunFitnessFunction(DuelRecordSuperPack pack, GunChromosomeLayoutProvider provider,
                              Class<? extends GeneticGunTargeting> targetingClazz, int threads) throws NoSuchMethodException {
        this.provider = provider;
        this.targetingConstructor = targetingClazz.getConstructor(int.class, Strategy.class);
        this.threads = threads;
        this.pack = pack;
    }

    public void cache(ArrayList<ChromosomePerformance<Double>> perfs) {
        for(ChromosomePerformance<Double> perf : perfs) {
            cache.put(perf.getChromosome(), perf.getFitness());
        }
    }

    public boolean isCached(Chromosome chromosome) {
        return cache.containsKey(chromosome);
    }

    @Override
    public Double getFitness(Chromosome chromosome) {
        if(!(chromosome.getLayout().equals(provider.getLayout())))
            throw new IllegalStateException();

        if(cache.containsKey(chromosome)) {
            return cache.get(chromosome);
        }

        Strategy chromosomeStrategy = provider.extractStrategy(chromosome);

        double totalValue = 0;
        int totalPlayed = 0;

        ExecutorService executorService = Executors.newFixedThreadPool(threads);

        BatchIterator<DuelRecord> batchIterator = pack.batchIterator();

        while(batchIterator.hasNext()) {
            Iterable<DuelRecord> records = batchIterator.consume(threads);

            // Empty global storage
            GlobalStorage.getInstance().clear();
            System.gc();

            ArrayList<Callable<Double>> callables = new ArrayList<>();

            int pointer = 0;
            for(DuelRecord record : records) {
                final int finalPointer = pointer;
                callables.add(new Callable<Double>() {
                    @Override
                    public Double call() throws Exception {
                        GeneticGunTargeting targeting = targetingConstructor.newInstance(finalPointer, chromosomeStrategy);
                        return evaluateBattle(targeting, record.getLogs());
                    }
                });

                pointer++;
            }

            try {
                List<Future<Double>> outcomes = executorService.invokeAll(callables);

                for(Future<Double> outcome : outcomes) {
                    totalValue += outcome.get();
                    totalPlayed++;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        double res;

        if(totalPlayed == 0)
            res = 0.0;
        else
            res = totalValue / totalPlayed;

        cache.put(chromosome, res);

        return res;
    }

    private static double evaluateBattle(GeneticGunTargeting targeting, ArrayList<Pair<TargetingLog, BreakType>> events) {
        EnemyLog fakeLog = new EnemyLog("fake-name");

        TreeMap<BattleTime, Double> firedAngles = new TreeMap<>();

        double res = 0;
        long resCnt = 0;

        for(Pair<TargetingLog, BreakType> event : events) {
            TargetingLog f = event.first;
            BreakType type = event.second;

            if(type == BreakType.FIRED) {
                GeneratedAngle[] angles = targeting.getFiringAngles(fakeLog, f);
                if(angles.length > 0) {
                    firedAngles.put(f.battleTime, angles[0].angle);
                }
            } else {
                if(firedAngles.containsKey(f.battleTime)) {
                    double firedAngle = firedAngles.get(f.battleTime);
                    AngularRange intersection = f.preciseIntersection;

                    if(intersection != null) {
//                        System.out.println(firedAngle + " " + intersection.getStartingAngle() + " " + intersection.getEndingAngle()
//                         + " " + intersection.isAngleNearlyContained(firedAngle, -1e-8));
                        resCnt++;
                        double bandwidth = intersection.getRadius();
                        res += R.gaussKernel(R.normalRelativeAngle(
                                firedAngle - intersection.getAngle(intersection.getCenter()))
                                / bandwidth);
//                        res += intersection.isAngleNearlyContained(firedAngle, 1e-11) ? 1 : 0;
                    }
                }

                targeting.log(fakeLog, f, type);
            }
        }

        return res / resCnt;
    }
}
