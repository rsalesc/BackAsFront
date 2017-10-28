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

/**
 * Created by Roberto Sales on 05/10/17.
 */
public class BatchDuelResults implements Iterable<IDuelResult>, IDuelResult {
    private ArrayList<IDuelResult> results = new ArrayList<>();
    private final String groupName;

    public BatchDuelResults(String groupName) {
        this.groupName = groupName;
    }

    public BatchDuelResults() {
        this(null);
    }

    public boolean isGroup() {
        return groupName != null;
    }

    public String getGroupName() {
        return groupName;
    }

    public void add(IDuelResult ...result) {
        results.addAll(Arrays.asList(result));
    }

    public IDuelResult[] getResults() {
        return results.toArray(new IDuelResult[0]);
    }

    public IDuelResult[] filterByEnemy(String name) {
        ArrayList<IDuelResult> res = new ArrayList<>();
        for(IDuelResult result : results) {
            if(result instanceof DuelResult) {
                if (((DuelResult) result).getEnemy().equals(name))
                    res.add(result);
            } else if(result instanceof BatchDuelResults)
                res.addAll(Arrays.asList(((BatchDuelResults) result).filterByEnemy(name)));
        }

        return res.toArray(new IDuelResult[0]);
    }

    @Override
    public Iterator<IDuelResult> iterator() {
        return results.iterator();
    }

    public static BatchDuelResults merge(BatchDuelResults...results) {
        BatchDuelResults res = new BatchDuelResults();

        for(BatchDuelResults result : results) {
            res.add(result.getResults());
        }

        return res;
    }

    @Override
    public double getAPS() {
        double sum = 0;
        for(IDuelResult result : getResults()) {
            sum += result.getAPS();
        }

        return sum / getResults().length;
    }

    @Override
    public double getAvgPercentageBulletDamage() {
        double sum = 0;
        for(IDuelResult result : getResults()) {
            sum += result.getAvgPercentageBulletDamage();
        }

        return sum / getResults().length;
    }

    @Override
    public double getAvgPercentageSurvival() {
        double sum = 0;
        for(IDuelResult result : getResults()) {
            sum += result.getAvgPercentageSurvival();
        }

        return sum / getResults().length;
    }

    @Override
    public double getEnemyWeightedHitRate() {
        double sum = 0;
        for(IDuelResult result : getResults()) {
            sum += result.getEnemyWeightedHitRate();
        }

        return sum / getResults().length;
    }

    @Override
    public double getWeightedHitRate() {
        double sum = 0;
        for(IDuelResult result : getResults()) {
            sum += result.getWeightedHitRate();
        }

        return sum / getResults().length;
    }

    @Override
    public double getTCScore() {
        double sum = 0;
        for(IDuelResult result : getResults()) {
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
