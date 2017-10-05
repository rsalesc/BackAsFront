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

package rsalesc.runner;

import rsalesc.baf2.core.utils.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Roberto Sales on 05/10/17.
 */
public class BatchDuelResults implements Iterable<DuelResult>, IDuelResult {
    private ArrayList<DuelResult> results = new ArrayList<>();

    public void add(DuelResult ...result) {
        results.addAll(Arrays.asList(result));
    }

    public DuelResult[] getResults() {
        return results.toArray(new DuelResult[0]);
    }

    public DuelResult[] filterByEnemy(String name) {
        ArrayList<DuelResult> res = new ArrayList<>();
        for(DuelResult result : results) {
            if(result.getEnemy().equals(name))
                res.add(result);
        }

        return res.toArray(new DuelResult[0]);
    }

    @Override
    public Iterator<DuelResult> iterator() {
        return results.iterator();
    }

    public static BatchDuelResults merge(BatchDuelResults ...results) {
        BatchDuelResults res = new BatchDuelResults();

        for(BatchDuelResults result : results) {
            res.add(result.getResults());
        }

        return res;
    }

    @Override
    public double getAPS() {
        double sum = 0;
        for(DuelResult result : getResults()) {
            sum += result.getAPS();
        }

        return sum / getResults().length;
    }

    @Override
    public double getAvgPercentageBulletDamage() {
        double sum = 0;
        for(DuelResult result : getResults()) {
            sum += result.getAvgPercentageBulletDamage();
        }

        return sum / getResults().length;
    }

    @Override
    public double getAvgPercentageSurvival() {
        double sum = 0;
        for(DuelResult result : getResults()) {
            sum += result.getAvgPercentageSurvival();
        }

        return sum / getResults().length;
    }

    @Override
    public double getEnemyWeightedHitRate() {
        double sum = 0;
        for(DuelResult result : getResults()) {
            sum += result.getEnemyWeightedHitRate();
        }

        return sum / getResults().length;
    }

    @Override
    public double getWeightedHitRate() {
        double sum = 0;
        for(DuelResult result : getResults()) {
            sum += result.getWeightedHitRate();
        }

        return sum / getResults().length;
    }

    @Override
    public double getTCScore() {
        double sum = 0;
        for(DuelResult result : getResults()) {
            sum += result.getTCScore();
        }

        return sum / getResults().length;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("APS: ").append(R.formattedPercentage(getAPS())).append(", ");
        builder.append("Survival: ").append(R.formattedPercentage(getAvgPercentageSurvival())).append(", ");
        builder.append("BD: ").append(R.formattedPercentage(getAvgPercentageBulletDamage())).append(", ");
        builder.append("TC: ").append(R.formattedPercentage(getTCScore())).append(", ");
        builder.append("+wHit: ").append(R.formattedPercentage(getWeightedHitRate())).append(", ");
        builder.append("-wHit: ").append(R.formattedPercentage(getEnemyWeightedHitRate()));

        return builder.toString();
    }
}
