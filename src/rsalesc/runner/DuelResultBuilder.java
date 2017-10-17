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

public class DuelResultBuilder {
    private String myself;
    private String enemy;
    private double score;
    private double scoreSum;
    private double bulletDamage;
    private double bulletDamageSum;
    private double survival;
    private double survivalSum;
    private double weightedHitRate = 0;
    private double enemyWeightedHitRate = 0;
    private int rounds;

    public DuelResultBuilder setMyself(String myself) {
        this.myself = myself;
        return this;
    }

    public DuelResultBuilder setEnemy(String enemy) {
        this.enemy = enemy;
        return this;
    }

    public DuelResultBuilder setScore(double score) {
        this.score = score;
        return this;
    }

    public DuelResultBuilder setScoreSum(double scoreSum) {
        this.scoreSum = scoreSum;
        return this;
    }

    public DuelResultBuilder setBulletDamage(double bulletDamage) {
        this.bulletDamage = bulletDamage;
        return this;
    }

    public DuelResultBuilder setBulletDamageSum(double bulletDamageSum) {
        this.bulletDamageSum = bulletDamageSum;
        return this;
    }

    public DuelResultBuilder setSurvival(double survival) {
        this.survival = survival;
        return this;
    }

    public DuelResultBuilder setSurvivalSum(double survivalSum) {
        this.survivalSum = survivalSum;
        return this;
    }

    public DuelResultBuilder setWeightedHitRate(double weightedHitRate) {
        this.weightedHitRate = weightedHitRate;
        return this;
    }

    public DuelResultBuilder setEnemyWeightedHitRate(double enemyWeightedHitRate) {
        this.enemyWeightedHitRate = enemyWeightedHitRate;
        return this;
    }

    public DuelResult createDuelResult() {
        return new DuelResult(myself, enemy, rounds, score, scoreSum, bulletDamage, bulletDamageSum, survival, survivalSum, weightedHitRate, enemyWeightedHitRate);
    }

    public DuelResultBuilder setRounds(int rounds) {
        this.rounds = rounds;
        return this;
    }
}