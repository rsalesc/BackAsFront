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

import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.utils.BattleTime;
import rsalesc.baf2.core.utils.R;

/**
 * Created by Roberto Sales on 12/10/17.
 */
public class BenchmarkAccumulator {
    private double totalTime = 0;
    private double totalSquare = 0;
    private long totalExperiments = 0;
    private double worstTime = 0;

    private final RobotMediator mediator;
    private BenchmarkAccumulator tickAccumulator;
    private BenchmarkAccumulator execAccumulator;
    private BattleTime lastTime;

    private double tickTotalTime = 0;
    private long tickExperiments = 0;

    public BenchmarkAccumulator(RobotMediator mediator) {
        this.mediator = mediator;
        if(mediator != null) {
            tickAccumulator = new BenchmarkAccumulator(null);
            execAccumulator = new BenchmarkAccumulator(null);
        }
    }

    public void log(double delta) {
        if(mediator != null) {
            if (lastTime != null && !mediator.getBattleTime().equals(lastTime)) {
                tickAccumulator.log(tickTotalTime); // TODO: maybe it will lose last tick
                execAccumulator.log(tickExperiments);

                tickTotalTime = 0;
                tickExperiments = 0;
            }

            lastTime = mediator.getBattleTime();
            tickTotalTime += delta;
            tickExperiments++;
        }

        totalTime += delta;
        totalSquare += delta * delta;
        totalExperiments++;
        worstTime = Math.max(worstTime, delta);
    }

    public BenchmarkAccumulator getTickAccumulator() {
        return tickAccumulator;
    }

    public BenchmarkAccumulator getTickExecutionAccumulator() {
        return execAccumulator;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public double getAverageTime() {
        return totalTime / totalExperiments;
    }

    public double getError() {
        return R.sqrt(totalSquare / totalExperiments - R.sqr(getAverageTime()));
    }

    public long getExperiments() {
        return totalExperiments;
    }

    public double getWorstTime() {
        return worstTime;
    }
}
