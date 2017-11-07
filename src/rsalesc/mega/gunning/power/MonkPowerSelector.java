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
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.EnemyTracker;
import rsalesc.mega.utils.StatData;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Roberto Sales on 24/09/17.
 */
public class MonkPowerSelector implements PowerSelector {
    @Override
    public double selectPower(RobotMediator mediator, StatData data) {
        EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();
        Arrays.sort(enemies, new Comparator<EnemyRobot>() {
            @Override
            public int compare(EnemyRobot o1, EnemyRobot o2) {
                return (int) Math.signum(o1.getDistance() - o2.getDistance());
            }
        });

        if (enemies.length == 0)
            return 0.0;

        int others = mediator.getOthers();
        double energy = mediator.getEnergy();

        if (others == 0)
            return 0.0;
        else if (others > 1) {
            double sumInv = 0;
            double rawAvg = 0;
            double avg = 0;
            for (EnemyRobot enemy : enemies) {
                sumInv += 1.0 / enemy.getDistance();
                avg += enemy.getEnergy() / enemy.getDistance();
                rawAvg += enemy.getEnergy();
            }

            rawAvg /= enemies.length;
            avg /= sumInv;

            double power = 3.0;

            if (others < 4) {
                power = 1.95;
            }

            if (others <= 5 && enemies[0].getDistance() > 500) {
                power = 1.4;
            }

            if (others <= 6 && energy < avg && enemies.length > 280 || enemies[0].getDistance() > 650) {
                power = 1.0;
            }

            if (energy < 30 && energy < rawAvg) {
                power = Math.min(power, 3 - (30 - energy) / 40);
            }

            return R.constrain(0.1, power, energy);
        } else {
            EnemyRobot enemy = enemies[0];

            if (enemy.getDistance() < 150)
                return Math.min(enemy.getEnergy() * 0.25, Math.min(2.9999, Math.max(mediator.getEnergy() - 0.4, 0)));

            double distance = mediator.getPoint().distance(enemy.getPoint());
            double myEnergy = mediator.getEnergy();
            double hisEnergy = enemy.getEnergy();
            double basePower = (hisEnergy * 2.5 <= myEnergy && myEnergy >= 15 ? 2.4 : 1.95);

            double expectedPower = Math.max((myEnergy - 20 + R.constrain(-10, myEnergy - hisEnergy, +30) / 2) / 25, 0);

            if (distance > 500) {
                expectedPower -= (distance - 500) / 300;
            }

            if (distance < 300) {
                expectedPower += (300 - distance) / 200;
            }

            double power = R.constrain(0.1, Math.min(basePower, expectedPower), 3.0);

            if (myEnergy < 0.4)
                return 0;

            return R.basicSurferRounding(myEnergy, R.constrain(0.15, Math.min(power, hisEnergy * 0.25),
                    Math.max(myEnergy - 0.1, 0.1)));
        }

    }
}
