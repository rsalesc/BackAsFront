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
import java.util.function.Function;

/**
 * Created by Roberto Sales on 12/10/17.
 */
public class Benchmark extends Component implements LastBreathListener {
    private static final String SEP = "|";
    private static final int COL_SIZE = 11;

    private static final Benchmark SINGLETON = new Benchmark();
    private static final HashMap<Pair<BenchmarkNode, Integer>, BenchmarkAccumulator> results = new PredictedHashMap<>(30);

    private boolean enabled = false;
    private BenchmarkNode lastParent;

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

    public Set<Map.Entry<Pair<BenchmarkNode, Integer>, BenchmarkAccumulator>> getResults() {
        return results.entrySet();
    }

    public List<Pair<BenchmarkNode, BenchmarkAccumulator>> getRoundResults() {
        Set<Map.Entry<Pair<BenchmarkNode, Integer>, BenchmarkAccumulator>> st = getResults();
        ArrayList<Pair<BenchmarkNode, BenchmarkAccumulator>> res = new ArrayList<>();

        for(Map.Entry<Pair<BenchmarkNode, Integer>, BenchmarkAccumulator> entry : st) {
            if(entry.getKey().second == getMediator().getRoundNum())
                res.add(new Pair<>(entry.getKey().first, entry.getValue()));
        }

        Comparator<Pair<BenchmarkNode, BenchmarkAccumulator>> comparator = new Comparator<Pair<BenchmarkNode, BenchmarkAccumulator>>() {
            @Override
            public int compare(Pair<BenchmarkNode, BenchmarkAccumulator> o1, Pair<BenchmarkNode, BenchmarkAccumulator> o2) {
                return o1.first.compareTo(o2.first);
            }
        };

        res.sort(comparator);

        TreeMap<BenchmarkNode, List<Pair<BenchmarkNode, BenchmarkAccumulator>>> adj = new TreeMap<>();

        for(Pair<BenchmarkNode, BenchmarkAccumulator> pair : res) {
            BenchmarkNode ptr = pair.first;

            outerWhile:
            while(ptr.parent != null) {
                ptr = ptr.parent;

                for(Pair<BenchmarkNode, BenchmarkAccumulator> pair2 : res) {
                    if(pair2.first.equals(ptr)) {
                        if(!adj.containsKey(ptr)) {
                            adj.put(ptr, new ArrayList<>());
                        }

                        adj.get(ptr).add(pair);
                        break outerWhile;
                    }
                }
            }
        }

        for(Map.Entry<BenchmarkNode, List<Pair<BenchmarkNode, BenchmarkAccumulator>>> entry : adj.entrySet()) {
            entry.getValue().sort(comparator);
        }

        ArrayList<Pair<BenchmarkNode, BenchmarkAccumulator>> actualRes = new ArrayList<>();

        Function<Pair<BenchmarkNode, BenchmarkAccumulator>, Object> dfs = new Function<Pair<BenchmarkNode, BenchmarkAccumulator>, Object>() {
            @Override
            public Object apply(Pair<BenchmarkNode, BenchmarkAccumulator> pair) {
                actualRes.add(pair);

                if(adj.containsKey(pair.first)) {
                    for (Pair<BenchmarkNode, BenchmarkAccumulator> child : adj.get(pair.first)) {
                        this.apply(child);
                    }
                }

                return null;
            }
        };

        for(Pair<BenchmarkNode, BenchmarkAccumulator> pair : res) {
            if(pair.first.parent == null)
                dfs.apply(pair);
        }

        return actualRes;
    }

    public BenchmarkAccumulator ensure(BenchmarkNode node) {
        Pair<BenchmarkNode, Integer> key = new Pair<>(node, getMediator().getRoundNum());
        if(!results.containsKey(key))
            results.put(key, new BenchmarkAccumulator(getMediator()));

        return results.get(key);
    }

    public void start(String group) {
        if(!enabled)
            return;

        lastParent = new BenchmarkNode(this, group, lastParent);
        lastParent.start();
    }

    public void stop() {
        if(!enabled || lastParent == null)
            return;

        lastParent.stop();
        lastParent = lastParent.parent;
    }

    public void log(BenchmarkNode node, double delta) {
        if(enabled)
            ensure(node).log(delta);
    }

    public String formattedTime(double ms) {
        return R.formattedDouble(ms) + " ms";
    }

    public String getColumn(String x) {
        return " " + String.format("%" + Math.max(x.length(), COL_SIZE) + "s", x) + " ";
    }

    public String getPadding(int level) {
        StringBuilder res = new StringBuilder();
        for(int i = 0; i < level; i++)
            res.append(" ");

        return res.toString();
    }

    @Override
    public void onLastBreath() {
        if(!enabled)
            return;

        System.out.println("Profiling -----------------------------");

        System.out.println(getColumn("avg.") + SEP + getColumn("worst") + SEP + getColumn("error") + SEP
            + getColumn("execs.") + SEP + getColumn("tick avg.") + SEP + getColumn("tick worst") + SEP + " group");

        BenchmarkNode last = null;
        int level = 0;

        for(Pair<BenchmarkNode, BenchmarkAccumulator> entry : getRoundResults()) {
            while(last != null && !last.equals(entry.first.parent)) {
                last = last.parent;
                level--;
            }

            level++;
            last = entry.first;

            BenchmarkAccumulator acc = entry.second;
//            System.out.println(entry.first + ": " + R.formattedDouble(acc.getAverageTime()) +  " ms avg., "
//                + R.formattedDouble(acc.getWorstTime()) + " ms worst, " + R.formattedDouble(acc.getError()) + " ms error.");

            System.out.print(getColumn(formattedTime(acc.getAverageTime()))
                + SEP + getColumn(formattedTime(acc.getWorstTime()))
                + SEP + getColumn(formattedTime(acc.getError()))
                + SEP + getColumn(String.valueOf(acc.getExperiments())));

            BenchmarkAccumulator execAcc = acc.getTickExecutionAccumulator();
            acc = acc.getTickAccumulator();
//
//            System.out.println("\t Tick work: " + R.formattedDouble(acc.getAverageTime()) +  " ms avg., "
//                    + R.formattedDouble(acc.getWorstTime()) + " ms worst, " + R.formattedDouble(acc.getError()) + " ms error, "
//                    + R.formattedDouble(execAcc.getAverageTime()) + " execution(s) avg, "
//                    + (long) (execAcc.getWorstTime()) + " execution(s) worst.");

            System.out.print(SEP + getColumn(formattedTime(acc.getAverageTime())) + SEP + getColumn(formattedTime(acc.getWorstTime())));

            System.out.println(SEP + getPadding(level) + entry.first.getGroup());
        }
    }
}
