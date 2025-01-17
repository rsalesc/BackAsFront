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

package rsalesc.mega.gunning.guns;

import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.StoreComponent;
import rsalesc.baf2.core.benchmark.Benchmark;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.waves.BreakType;
import rsalesc.mega.utils.IMea;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.structures.Knn;
import rsalesc.structures.KnnProvider;
import rsalesc.structures.KnnView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Roberto Sales on 15/09/17.
 * TODO: need some caching?
 *
 * TC1: had payload bandwidth and inverse distance weighter, besides ratio 0.1
 * TC2: had hit-angle bandwidth and gauss distance weighter, besides ratio 0.33
 */
public abstract class KnnGuessFactorTargeting extends StoreComponent implements GFTargeting, KnnProvider<TimestampedGFRange> {
    public List<Knn.Entry<TimestampedGFRange>> lastFound;

    public abstract KnnView<TimestampedGFRange> getNewKnnSet();

    public KnnView<TimestampedGFRange> getKnnSet(String name) {
        StorageNamespace ns = getStorageNamespace().namespace(name);
        if (ns.contains("knn"))
            return (KnnView) ns.get("knn");

        KnnView<TimestampedGFRange> knn = getNewKnnSet();
        ns.put("knn", knn);
        return knn;
    }

    @Override
    public boolean hasData(EnemyLog enemyLog) {
        return getKnnSet(enemyLog.getName()).availableData() > 0;
    }

    @Override
    public GeneratedAngle[] getFiringAngles(EnemyLog enemyLog, TargetingLog f, IMea mea) {
        Benchmark.getInstance().start("KnnGuessFactorTargeting.getFiringAngles()");
        KnnView<TimestampedGFRange> view = getKnnSet(enemyLog.getName());

        List<Knn.Entry<TimestampedGFRange>> found = view.query(f);

//        double bandwidth = Physics.hitAngle(f.distance) / 2 /
//                Math.min(f.preciseMea.minAbsolute(), f.preciseMea.maxAbsolute());
//
//        GuessFactorStats stats = new GuessFactorStats(new GaussianKernelDensity());
//
//        for(Knn.Entry<TimestampedGFRange> entry : found) {
//            stats.logGuessFactor(entry.payload.mean, entry.weight, bandwidth);
//        }
//
//        double bestGf = 0;
//        double bestDensity = 0;
//
//        for(Knn.Entry<TimestampedGFRange> entry : found) {
//            double gf = entry.payload.mean;
//            double density = stats.getValue(gf);
//
//            if(density > bestDensity) {
//                bestDensity = density;
//                bestGf = gf;
//            }
//        }

        double bestGf = maxOverlapCenter(found, mea);
        lastFound = found;

        Benchmark.getInstance().stop();
        return new GeneratedAngle[]{new GeneratedAngle(1.0, mea.getAngle(bestGf), f.distance)};
    }

    @Override
    public void log(EnemyLog enemyLog, TargetingLog f, IMea mea, BreakType type) {
        double gfMean = mea.getGfFromAngle(f.preciseIntersection.getAngle(f.preciseIntersection.getCenter()));
        double gfLow = mea.getGfFromAngle(f.preciseIntersection.getStartingAngle());
        double gfHigh = mea.getGfFromAngle(f.preciseIntersection.getEndingAngle());

        if(gfLow > gfHigh) {
            double tmp = gfLow;
            gfLow = gfHigh;
            gfHigh = tmp;
        }

        getKnnSet(enemyLog.getName()).add(f, new TimestampedGFRange(f.battleTime, gfLow, gfHigh, gfMean), type);
    }

    public static double maxOverlap(List<Knn.Entry<TimestampedGFRange>> list) {
        List<SweepEvent> eventList = new ArrayList<>();
        for(Knn.Entry<TimestampedGFRange> entry : list) {
            eventList.add(new SweepEvent(SweepEvent.EventType.START, entry.weight, entry.payload.min));
            eventList.add(new SweepEvent(SweepEvent.EventType.END, entry.weight, entry.payload.max));
            eventList.add(new SweepEvent(SweepEvent.EventType.QUERY, entry.weight, entry.payload.mean));
        }

        SweepEvent[] events = eventList.toArray(new SweepEvent[0]);
        Arrays.sort(events);

        double bestGf = 0;
        double bestAcc = 0;
        double acc = 0;
        for(SweepEvent event : events) {
            if(event.type == SweepEvent.EventType.START)
                acc += event.weight;
            else if(event.type == SweepEvent.EventType.END)
                acc -= event.weight;

            {
                if(acc > bestAcc) {
                    bestAcc = acc;
                    bestGf = event.axis;
                }
            }
        }

        return bestGf;
    }

    public static double maxOverlapCenter(List<Knn.Entry<TimestampedGFRange>> list, IMea mea) {
        List<SweepEvent> eventList = new ArrayList<>();
        for(Knn.Entry<TimestampedGFRange> entry : list) {
            eventList.add(new SweepEvent(SweepEvent.EventType.START, entry.weight, entry.payload.min));
            eventList.add(new SweepEvent(SweepEvent.EventType.END, entry.weight, entry.payload.max));
        }

        SweepEvent[] events = eventList.toArray(new SweepEvent[0]);
        Arrays.sort(events);

        double bestGf = 0;
        double bestAcc = 0;
        double acc = 0;

//        System.out.println("=======");
        for(int i = 0; i < events.length; i++) {
            SweepEvent event = events[i];

            if(event.type == SweepEvent.EventType.START) {
                acc += event.weight;
                if(acc > bestAcc) {
                    bestAcc = acc;
                    bestGf = i+1 < events.length ? (events[i+1].axis + events[i].axis) / 2 : event.axis;
                }
            } else if(event.type == SweepEvent.EventType.END)
                acc -= event.weight;

//            System.out.println(event.axis + " " + (event.type == SweepEvent.EventType.START ? "+" + event.weight : "-" + event.weight)
//                    + " acc: " + acc);
        }

//        System.out.println("gf: " + bestGf + ", bacc: " + bestAcc);

        return bestGf;
    }

    private static class SweepEvent implements Comparable<SweepEvent> {
        public final EventType type;
        public final double weight;
        public final double axis;

        private SweepEvent(EventType type, double weight, double axis) {
            this.type = type;
            this.weight = weight;
            this.axis = axis;
        }

        @Override
        public int compareTo(SweepEvent o) {
            if(axis == o.axis)
                return type.index - o.type.index;
            return (int) Math.signum(axis - o.axis);
        }

        private enum EventType {
            END(0),
            QUERY(1),
            START(2);

            private final int index;

            EventType(int index) {
                this.index = index;
            }
        }


    }
}
