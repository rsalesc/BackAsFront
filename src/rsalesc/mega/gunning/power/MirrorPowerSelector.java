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

import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.painting.G;
import rsalesc.baf2.painting.PaintManager;
import rsalesc.baf2.painting.Painting;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.EnemyTracker;
import rsalesc.mega.utils.NamedStatData;
import rsalesc.mega.utils.StatData;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Created by Roberto Sales on 19/09/17.
 */
public class MirrorPowerSelector extends Component implements PowerSelector {

    private final PowerPredictor predictor;
    private Double lastSelectedPower;
    private Double lastPredictedPower;

    public MirrorPowerSelector(PowerPredictor predictor) {
        this.predictor = predictor;
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
            hisPower = predictor.predict(enemyLog.getLatest());
            lastPredictedPower = hisPower;
        } else if(enemyData.getWeightedHitPercentage() < 0.17) {
            hisPower = Math.min(predictor.predict(enemyLog.getLatest()) + 0.1, 3.0);
            lastPredictedPower = hisPower;
        }

        double distance = mediator.getPoint().distance(enemy.getPoint());
        double myEnergy = mediator.getEnergy();
        double hisEnergy = enemy.getEnergy();
        double basePower;

        if (enemyData.getWeightedHitPercentage() > 0.31 && mediator.getBattleTime().getRound() >= 1) basePower = 2.95;
        else if (hisEnergy * 2.5 <= myEnergy && myEnergy >= 15) basePower = 2.4;
        else basePower = 1.95;

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

        return lastSelectedPower = R.basicSurferRounding(myEnergy, R.constrain(0.15, Math.min(power, hisEnergy * 0.25),
                Math.max(Math.min(hisPower - 0.1, myEnergy - 0.1), 0.1)));
    }

    @Override
    public void setupPaintings(PaintManager manager) {
        manager.add(KeyEvent.VK_M, "power", new Painting() {
            @Override
            public void paint(G g) {
                if(lastSelectedPower != null) {
                    g.drawCenteredString(new Point(getMediator().getBattleField().getWidth() / 2, 12),
                            "Selected power: " + R.formattedDouble(lastSelectedPower)
                                + ", Predicted power: " + R.formattedDouble(lastPredictedPower), Color.LIGHT_GRAY);
                }
            }
        }, true);
    }
}
