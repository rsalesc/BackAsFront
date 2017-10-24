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

package rsalesc.mega.gunning.power;

import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.StoreComponent;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.tracking.*;
import rsalesc.mega.utils.*;
import rsalesc.mega.utils.structures.Knn;
import rsalesc.mega.utils.structures.KnnTree;

import java.util.List;

/**
 * Created by Roberto Sales on 19/09/17.
 */
public class MirrorPowerSelector extends StoreComponent implements PowerSelector, EnemyFireListener {

    public Knn<TimestampedGFRange> getNewKnn() {
        return new KnnTree<TimestampedGFRange>().setMode(KnnTree.Mode.MANHATTAN).setLimit(750).setK(12)
                .setRatio(0.15).setStrategy(new ConservativeStrategy()).build();
    }

    public Knn<TimestampedGFRange> getKnn(String name) {
        StorageNamespace ns = getStorageNamespace();
        if(!ns.contains("knn"))
            ns.put("knn", getNewKnn());

        return (Knn) ns.get("knn");
    }

    public double[] getQuery(RobotMediator mediator, EnemyRobot enemy) {
        return new double[]{
                Math.min(mediator.getEnergy() / 100.0, 1),
                Math.min(enemy.getEnergy() / 100.0, 1),
                Math.min(enemy.getDistance() / 600, 1.0)
        };
    }

    public double predictEnemyPower(RobotMediator mediator, EnemyLog enemyLog, NamedStatData data) {
        EnemyRobot enemy = enemyLog.getLatest();
        if(enemy == null)
            return 3.0;

        List<Knn.Entry<TimestampedGFRange>> list = getKnn(enemyLog.getName()).query(getQuery(mediator, enemy));

        double distSum = 1e-9;
        for(Knn.Entry<TimestampedGFRange> entry : list) {
            distSum += entry.distance;
        }

        double invAvg = list.size() / distSum;

        double best = 0.1;
        double best_h = 0;
        final double bandwidth = 0.25;

        for(double i = 0.1; i <= 3.0; i += 0.1) {
            double acc = 0;
            for(Knn.Entry<TimestampedGFRange> entry : list) {
                double diff = entry.distance * invAvg;
                double xdelta = (entry.payload.mean - i) / bandwidth;
                double h = R.exp(-0.5 * diff * diff) * R.exp(-0.5 * xdelta * xdelta) * entry.weight;
                acc += h;
            }

            if(acc > best_h) {
                best = i;
                best_h = acc;
            }
        }

        if(best_h == 0)
            return 1.5;

        return best;
    }

    public void log(RobotMediator mediator, EnemyLog enemyLog, NamedStatData data, double power) {
        if(mediator.getOthers() != 1)
            return;

        EnemyRobot enemy = enemyLog.getLatest();
        if(enemy == null)
            return;
        getKnn(enemy.getName()).add(getQuery(mediator, enemy), new TimestampedGFRange(null, power, power));
    }

    @Override
    public double selectPower(RobotMediator mediator, StatData data) {
        EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();
        EnemyRobot enemy = enemies[0];

        if (enemy.getDistance() < 150)
            return Math.min(enemy.getEnergy() * 0.25, Math.min(2.9999, Math.max(mediator.getEnergy() - 0.4, 0)));

        EnemyLog enemyLog = EnemyTracker.getInstance().getLog(enemy);

        NamedStatData enemyData = new NamedStatData(data, enemy.getName());
        double hisPower = Double.POSITIVE_INFINITY;
        if(enemyData.getWeightedHitPercentage() < 0.12) {
            hisPower = predictEnemyPower(mediator, enemyLog, enemyData);
        } else if(enemyData.getWeightedHitPercentage() < 0.17) {
            hisPower = Math.min(predictEnemyPower(mediator, enemyLog, enemyData) + 0.1, 3.0);
        }

        double distance = mediator.getPoint().distance(enemy.getPoint());
        double myEnergy = mediator.getEnergy();
        double hisEnergy = enemy.getEnergy();
        double basePower = (enemyData.getWeightedHitPercentage() > 0.31 && mediator.getBattleTime().getRound() >= 1)
                ? 2.95
                : (hisEnergy*2.5 <= myEnergy && myEnergy >= 15 ? 2.4 : 1.95);

        double expectedPower = Math.max((myEnergy - 20 + R.constrain(-10, myEnergy - hisEnergy, +30) / 2) / 25, 0);

        if (distance > 500) {
            expectedPower -= (distance - 500) / 300;
        }
//
        if (distance < 300) {
            expectedPower += (300 - distance) / 200;
        }

        double power = R.constrain(0.1, Math.min(basePower, expectedPower), 3.0);

        if (myEnergy < 0.4)
            return 0;

        return R.basicSurferRounding(R.constrain(0.15, Math.min(power, hisEnergy * 0.25),
                Math.max(Math.min(hisPower - 0.1, myEnergy - 0.1), 0.1)));
    }

    @Override
    public StorageNamespace getStorageNamespace() {
        return getGlobalStorage().namespace("mirror-power-selector");
    }

    @Override
    public void onEnemyFire(EnemyFireEvent e) {
        EnemyRobot robot = e.getEnemy();

        EnemyLog enemyLog = EnemyTracker.getInstance().getLog(robot);
        log(getMediator(), enemyLog, null, e.getPower());
    }

    private static class ConservativeStrategy extends Strategy {

        @Override
        public double[] getQuery(TargetingLog f) {
            return new double[0];
        }

        @Override
        public double[] getWeights() {
            return new double[]{4.0, 4.0, 1.0};
        }
    }
}
