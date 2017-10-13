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

package rsalesc.baf2.core.benchmark;

import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.listeners.LastBreathListener;
import rsalesc.baf2.core.utils.Pair;
import rsalesc.baf2.core.utils.PredictedHashMap;
import rsalesc.baf2.core.utils.R;

import java.util.*;

/**
 * Created by Roberto Sales on 12/10/17.
 */
public class Benchmark extends Component implements LastBreathListener {
    private static final Benchmark SINGLETON = new Benchmark();
    private static final HashMap<Pair<String, Integer>, BenchmarkAccumulator> results = new PredictedHashMap<>(30);

    private boolean enabled = false;

    private Benchmark() {}

    public static Benchmark getInstance() {
        return SINGLETON;
    }

    public void enable() {
        enabled = true;
    }

    public BenchmarkAccumulator getResult(String group) {
        return results.get(new Pair<>(group, getMediator().getRoundNum()));
    }

    public Set<Map.Entry<Pair<String, Integer>, BenchmarkAccumulator>> getResults() {
        return results.entrySet();
    }

    public List<Pair<String, BenchmarkAccumulator>> getRoundResults() {
        Set<Map.Entry<Pair<String, Integer>, BenchmarkAccumulator>> st = getResults();
        ArrayList<Pair<String, BenchmarkAccumulator>> res = new ArrayList<>();

        for(Map.Entry<Pair<String, Integer>, BenchmarkAccumulator> entry : st) {
            if(entry.getKey().second == getMediator().getRoundNum())
                res.add(new Pair<>(entry.getKey().first, entry.getValue()));
        }

        res.sort(new Comparator<Pair<String, BenchmarkAccumulator>>() {
            @Override
            public int compare(Pair<String, BenchmarkAccumulator> o1, Pair<String, BenchmarkAccumulator> o2) {
                return o1.first.compareTo(o2.first);
            }
        });

        return res;
    }

    public BenchmarkAccumulator ensure(String group) {
        Pair<String, Integer> key = new Pair<>(group, getMediator().getRoundNum());
        if(!results.containsKey(key))
            results.put(key, new BenchmarkAccumulator(getMediator()));

        return results.get(key);
    }

    public BenchmarkNode getNode(String group) {
        return new BenchmarkNode(this, group);
    }

    public void log(String group, double delta) {
        if(enabled)
            ensure(group).log(delta);
    }

    @Override
    public void onLastBreath() {
        for(Pair<String, BenchmarkAccumulator> entry : getRoundResults()) {
            BenchmarkAccumulator acc = entry.second;
            System.out.println(entry.first + ": " + R.formattedDouble(acc.getAverageTime()) +  " ms avg., "
                + R.formattedDouble(acc.getWorstTime()) + " ms worst, " + R.formattedDouble(acc.getError()) + " ms error.");

            BenchmarkAccumulator execAcc = acc.getTickExecutionAccumulator();
            acc = acc.getTickAccumulator();

            System.out.println("\t Tick work: " + R.formattedDouble(acc.getAverageTime()) +  " ms avg., "
                    + R.formattedDouble(acc.getWorstTime()) + " ms worst, " + R.formattedDouble(acc.getError()) + " ms error, "
                    + R.formattedDouble(execAcc.getAverageTime()) + " execution(s) avg, "
                    + (long) (execAcc.getWorstTime()) + " execution(s) worst.");
        }
    }
}
