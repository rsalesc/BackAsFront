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

package rsalesc.melee.movement.risk;

import robocode.util.Utils;
import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.EnemyTracker;
import rsalesc.baf2.tracking.MyLog;
import rsalesc.baf2.tracking.MyRobot;

/**
 * Created by Roberto Sales on 25/09/17.
 */
public class RiskEvaluation3rd implements RiskEvaluation {
    @Override
    public double evaluateDanger(RobotMediator mediator, Point dest, double maxDistance, double[] closestDist) {
        MyRobot me = MyLog.getInstance().getLatest();
        MyRobot meTenAgo = MyLog.getInstance().atLeastAt(mediator.getTime() - 60);

        final double T = maxDistance;

        EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();
        AxisRectangle field = mediator.getBattleField();

        double res = 0;

        for (EnemyRobot enemy : enemies) {
            res += Math.max(3 * T - dest.distance(enemy.getPoint()), 0);
//            res += evalDistance(dest.distance(enemy.getPoint()), maxDistance);

            double absBearing = Physics.absoluteBearing(enemy.getPoint(), dest);
            double bafHeading = Physics.absoluteBearing(me.getPoint(), dest);
            double diff = Utils.normalRelativeAngle(bafHeading - absBearing);

            double factor = Math.abs(Math.cos(diff)) * (T - enemy.getDistance()) / T;
            res += factor * 3;
        }

        res += Math.max(T - me.getDistanceToWall(), 0) * 2;

        res += Math.max(2.5 * T - field.getCenter().distance(dest), 0) * 4;
        res += Math.max(T - me.getPoint().distance(dest), 0) * 2;

//        if (meTenAgo != null && meTenAgo.getTime() == mediator.getTime() - 10) {
//            res += Math.max((T - meTenAgo.getPoint().distance(dest)) * 3, 0);
//        }

        for (int i = 0; i < enemies.length; i++) {
            res += Math.max((closestDist[i] + 10 - dest.distance(enemies[i].getPoint())) * 5, 0);
        }

        return res;
    }
}
