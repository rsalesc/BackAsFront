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

import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.StoreComponent;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.tracking.*;
import rsalesc.mega.utils.Strategy;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.structures.Knn;
import rsalesc.structures.KnnTree;

import java.util.List;

public class DuelPowerPredictor extends StoreComponent implements PowerPredictor {
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

    public double[] getQuery(RobotSnapshot shooter, RobotSnapshot receiver) {
        return new double[]{
                Math.min(receiver.getEnergy() / 100.0, 1),
                Math.min(shooter.getEnergy() / 100.0, 1),
                Math.min(shooter.getPoint().distance(receiver.getPoint()) / 600, 1.0)
        };
    }

    @Override
    public StorageNamespace getStorageNamespace() {
        return getGlobalStorage().namespace("duel-predictor");
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

    @Override
    public void train(RobotSnapshot shooter, double power, long time) {
        MyRobot robot = MyLog.getInstance().atMostAt(time - 1);
        if(robot == null)
            return;

        getKnn(shooter.getName()).add(getQuery(shooter, robot), new TimestampedGFRange(null, power, power));
    }

    @Override
    public double predict(RobotSnapshot shooter) {
        MyRobot robot = MyLog.getInstance().getLatest();
        if(robot == null)
            return 3.0;

        List<Knn.Entry<TimestampedGFRange>> list = getKnn(shooter.getName()).query(getQuery(shooter, robot));

        double distSum = 1e-9;
        for(Knn.Entry<TimestampedGFRange> entry : list) {
            distSum += entry.distance;
        }

        double invAvg = list.size() / distSum;

        double best = 0.1;
        double best_h = 0;
        final double bandwidthInv = 1 / 0.25;

        for(double i = 0.1; i <= 3.0; i += 0.1) {
            double acc = 0;
            for(Knn.Entry<TimestampedGFRange> entry : list) {
                double diff = entry.distance * invAvg;
                double xdelta = (entry.payload.mean - i) * bandwidthInv;
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
}
