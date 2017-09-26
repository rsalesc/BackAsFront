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

package rsalesc.melee.movement;


import rsalesc.baf2.core.listeners.RoundStartedListener;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.controllers.Controller;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.EnemyTracker;
import rsalesc.baf2.tracking.MyLog;
import rsalesc.baf2.tracking.MyRobot;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Roberto Sales on 25/09/17.
 */
public class GotoMinimumRisk extends MinimumRisk implements RoundStartedListener {
    private static final int NUM_POINTS = 120;
    private static final double STICK_LENGTH = 100;
    private static final double DISTANCE_TO_WALL = 40;
    private static final double WALL_STICK = 240;
    private static EnemyTracker tracker = EnemyTracker.getInstance();
    private ArrayList<MoveCandidate> candidates;

    private RiskEvaluation evaluation;
    private Point lastDest;

    @Override
    public void onRoundStarted(int round) {
        candidates = new ArrayList<>();
    }

    public double getClosestDistance() {
        EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();
        double best = Double.POSITIVE_INFINITY;
        for(EnemyRobot enemy : enemies) {
            best = Math.min(best, enemy.getDistance());
        }

        return best;
    }

    @Override
    public void run() {
        if(lastDest == null || getMediator().getPoint().distance(lastDest) < 2 || getClosestDistance() < 80)
            lastDest = bestDestination();

        double angle = Physics.absoluteBearing(getMediator().getPoint(), lastDest);
        double distance = lastDest.distance(getMediator().getPoint());
        if (distance < 0.01)
            angle = getMediator().getHeadingRadians();

        Controller controller = getMediator().getBodyControllerOrDummy();

        controller.setBackAsFront(angle, distance);
        controller.release();
    }

    public Point bestDestination() {
        MyRobot me = MyLog.getInstance().getLatest();
        Point myPoint = getMediator().getPoint();
        EnemyRobot[] enemies = tracker.getLatest();
        AxisRectangle field = getMediator().getBattleField();
        AxisRectangle shrinkedField = field.shrink(DISTANCE_TO_WALL, DISTANCE_TO_WALL);
        AxisRectangle botField = field.shrink(Physics.BOT_WIDTH, Physics.BOT_WIDTH);

        int direction = me.getDirection(botField.getCenter());
        if (direction == 0) direction = 1;

        double[] pairwiseClosestDistance = getPairwiseClosestDistance(enemies);

        double maxDistance = STICK_LENGTH;
        for (EnemyRobot enemy : enemies) {
            maxDistance = Math.min(maxDistance, enemy.getDistance());
        }

        double bestDanger = Double.POSITIVE_INFINITY;
        Point bestDest = myPoint;
        candidates = new ArrayList<>();

        double ratio = R.DOUBLE_PI / NUM_POINTS;
        for (int i = 0; i < NUM_POINTS; i++) {
            double angle = ratio * i;
            Point dest = myPoint.project(angle, Math.random() * maxDistance).clip(shrinkedField);

            double danger = getEvaluation().evaluateDanger(getMediator(), dest, maxDistance, pairwiseClosestDistance);
            candidates.add(new MoveCandidate(danger, dest));

            if (danger < bestDanger) {
                bestDanger = danger;
                bestDest = dest;
            }
        }

        return bestDest;
    }

    public double[] getPairwiseClosestDistance(EnemyRobot[] enemies) {
        double[] res = new double[enemies.length];

        for (int i = 0; i < enemies.length; i++) {
            for (int j = i + 1; j < enemies.length; j++) {
                double distance = enemies[i].getPoint().distance(enemies[i].getPoint());
                res[i] = Math.min(res[i], distance);
                res[j] = Math.min(res[j], distance);
            }
        }

        return res;
    }

    public RiskEvaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(RiskEvaluation evaluation) {
        this.evaluation = evaluation;
    }

    @Override
    public void onPaint(Graphics2D gr) {

    }

    private class MoveCandidate {
        public final double danger;
        public final Point destination;

        private MoveCandidate(double danger, Point destination) {
            this.danger = danger;
            this.destination = destination;
        }
    }
}
