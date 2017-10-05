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

/**
 * Created by Roberto Sales on 05/10/17.
 */
public class DuelResult implements IDuelResult {
    private final String myself;
    private final String enemy;

    private final int rounds;

    private final double score;
    private final double scoreSum;

    private final double bulletDamage;
    private final double bulletDamageSum;

    private final double survival;
    private final double survivalSum;

    private final double weightedHitRate;
    private final double enemyWeightedHitRate;

    public DuelResult(String myself, String enemy, int rounds, double score, double scoreSum, double bulletDamage, double bulletDamageSum, double survival, double survivalSum, double weightedHitRate, double enemyWeightedHitRate) {
        this.myself = myself;
        this.enemy = enemy;
        this.rounds = rounds;
        this.score = score;
        this.scoreSum = scoreSum;
        this.bulletDamage = bulletDamage;
        this.bulletDamageSum = bulletDamageSum;
        this.survival = survival;
        this.survivalSum = survivalSum;
        this.weightedHitRate = weightedHitRate;
        this.enemyWeightedHitRate = enemyWeightedHitRate;
    }

    @Override
    public double getAPS() {
        return score / scoreSum;
    }

    @Override
    public double getAvgPercentageBulletDamage() {
        return bulletDamage / bulletDamageSum;
    }

    @Override
    public double getAvgPercentageSurvival() {
        return survival / survivalSum;
    }

    @Override
    public double getEnemyWeightedHitRate() {
        return enemyWeightedHitRate;
    }

    @Override
    public double getWeightedHitRate() {
        return weightedHitRate;
    }

    public String getMyself() {
        return myself;
    }

    public String getEnemy() {
        return enemy;
    }

    public double getScore() {
        return score;
    }

    public double getScoreSum() {
        return scoreSum;
    }

    public double getBulletDamage() {
        return bulletDamage;
    }

    public double getBulletDamageSum() {
        return bulletDamageSum;
    }

    public double getSurvival() {
        return survival;
    }

    public double getSurvivalSum() {
        return survivalSum;
    }

    public double getTCScore() {
        return bulletDamage / (100 * rounds);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getEnemy());
        builder.append('\t');
        builder.append("APS: ").append(R.formattedPercentage(getAPS())).append(", ");
        builder.append("Survival: ").append(R.formattedPercentage(getAvgPercentageSurvival())).append(", ");
        builder.append("BD: ").append(R.formattedPercentage(getAvgPercentageBulletDamage())).append(", ");
        builder.append("TC: ").append(R.formattedPercentage(getTCScore())).append(", ");
        builder.append("+wHit: ").append(R.formattedPercentage(getWeightedHitRate())).append(", ");
        builder.append("-wHit: ").append(R.formattedPercentage(getEnemyWeightedHitRate()));

        return builder.toString();
    }
}
