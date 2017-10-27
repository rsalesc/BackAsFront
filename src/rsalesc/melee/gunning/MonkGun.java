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

package rsalesc.melee.gunning;

import robocode.Rules;
import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.StoreComponent;
import rsalesc.baf2.core.controllers.Controller;
import rsalesc.baf2.core.listeners.PaintListener;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.painting.G;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.EnemyTracker;
import rsalesc.baf2.tracking.RobotSnapshot;
import rsalesc.mega.tracking.EnemyMovie;
import rsalesc.mega.tracking.MovieListener;
import rsalesc.mega.utils.Strategy;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.structures.Knn;
import rsalesc.structures.KnnTree;
import rsalesc.structures.KnnView;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public class MonkGun extends StoreComponent implements MovieListener, PaintListener {
    private static final int MAX_K = 100;
    private double decidedPower = 0;
    private ArrayList<CandidateAngle> lastAngles;

    @Override
    public StorageNamespace getStorageNamespace() {
        return getGlobalStorage().namespace("monk-gun");
    }

    public int getCommonK() {
        EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();
        int res = MAX_K / (enemies.length == 0 ? 10 : enemies.length);

        for(EnemyRobot enemy : enemies) {
            res = Math.min(res, getKnnSet(enemy).queryableData());
        }

        return Math.max(res, 1);
    }

    @Override
    public void run() {
        int K = getCommonK();

        double power = selectPower();
        double bulletSpeed = Rules.getBulletSpeed(power);
        long time = getMediator().getTime();
        Point nextPosition = getMediator().getNextPosition();

        ArrayList<CandidateAngle> angles = new ArrayList<>();

        for (EnemyRobot enemy : EnemyTracker.getInstance().getLatest()) {
            long extraTime = time - enemy.getTime();

            KnnView<EnemyMovie> knn = getKnnSet(enemy);
            if (knn.availableData() == 0) {
                continue;
            }

            TargetingLog f = TargetingLog.getLog(enemy, getMediator(), power, true);

            List<Knn.Entry<EnemyMovie>> entries = knn.query(f, K);

            for (Knn.Entry<EnemyMovie> entry : entries) {
                boolean ok = true;
                EnemyMovie movie = entry.payload;
                int ptr = 0;
                long firstTime = movie.get(0).getTime();

                double rotation = R.normalRelativeAngle(movie.get(0).getBafHeading() - enemy.getBafHeading());
                Point translation = movie.get(0).getPoint().subtract(enemy.getPoint());

                Point transformedMe = nextPosition.add(translation).rotate(rotation, movie.get(0).getPoint());
                while (ptr < movie.size() && movie.get(ptr).getPoint().distance(transformedMe)
                        > (movie.get(ptr).getTime() - firstTime - extraTime) * bulletSpeed) {
                    if (ptr + 1 < movie.size()) {
                        long diff = movie.get(ptr + 1).getTime() - movie.get(ptr).getTime();
                        if (diff > 10) {
                            ok = false;
                            break;
                        }
                    }

                    ptr++;
                }

                ok = ok && ptr < movie.size();
                if (!ok) {
                    continue;
                }

                EnemyRobot current = movie.get(ptr);
                EnemyRobot last = movie.get(Math.max(0, ptr - 1));

                long diff = current.getTime() - last.getTime();
                Point impactPoint = current.getPoint();

                // interpolate
                if (diff > 0) {
                    long l = 0;
                    long r = diff;
                    while (l < r) {
                        long mid = (l + r) / 2;
                        double percent = (double) mid / diff;
                        Point estimated = last.getPoint().weighted(current.getPoint(), percent);
                        if ((last.getTime() + mid - firstTime - extraTime) * bulletSpeed
                                > transformedMe.distance(estimated)) {
                            l = mid + 1;
                        } else {
                            r = mid;
                        }
                    }

                    double percent = (double) l / diff;
                    impactPoint = last.getPoint().weighted(current.getPoint(), percent);
                }

                Point lastPosition = impactPoint.rotate(-rotation, movie.get(0).getPoint()).subtract(translation);
                if (!getMediator().getBattleField().contains(lastPosition))
                    continue;

                angles.add(new CandidateAngle(Physics.absoluteBearing(nextPosition, lastPosition),
                        entry.weight,
                        lastPosition));
            }
        }

        int remaining = getMediator().getTicksToCool();

        double delta = Math.max(R.PI, Rules.GUN_TURN_RATE_RADIANS * Math.max(remaining * 1.1, 1));
        AngularRange range = new AngularRange(getMediator().getGunHeadingRadians(), -delta, +delta);

        double bestDensity = Double.NEGATIVE_INFINITY;
        CandidateAngle bestAngle = null;

        for (CandidateAngle shootAngle : angles) {
            if(!range.isAngleNearlyContained(shootAngle.angle))
                continue;

            double density = 0;
            for (CandidateAngle candidate : angles) {
                double distance = candidate.point.distance(nextPosition);
                double angle = candidate.angle;
                double off = R.normalRelativeAngle(shootAngle.angle - angle);

                double x = off / (Physics.hitAngle(distance) * 0.9);
                if (Math.abs(x) < 1) {
                    density += R.cubicKernel(Math.abs(x)) * candidate.weight / R.sqrt(distance);
                }
            }

            if (density > bestDensity) {
                bestDensity = density;
                bestAngle = shootAngle;
            }
        }

        Controller controller = getMediator().getAimControllerOrDummy();

        if (bestAngle != null) {
            controller.setGunTo(bestAngle.angle);
        } else {
            EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();

            if (enemies.length > 0) {
                Arrays.sort(enemies, new Comparator<EnemyRobot>() {
                    @Override
                    public int compare(EnemyRobot o1, EnemyRobot o2) {
                        return (int) Math.signum(o1.getDistance() - o2.getDistance());
                    }
                });

                controller.setGunTo(Physics.absoluteBearing(nextPosition, enemies[0].getPoint()));
            }
        }

        decidedPower = power;
        lastAngles = angles;

        controller.release();
    }

    @Override
    public void beforeRun() {
        if (getMediator().getGunHeat() == 0 && getMediator().getGunTurnRemainingRadians() == 0 && decidedPower > 0.09) {
            Controller controller = getMediator().getGunControllerOrDummy();
            controller.setFireBullet(decidedPower);
            controller.release();
        }
    }

    public double selectPower() {
        EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();
        Arrays.sort(enemies, new Comparator<EnemyRobot>() {
            @Override
            public int compare(EnemyRobot o1, EnemyRobot o2) {
                return (int) Math.signum(o1.getDistance() - o2.getDistance());
            }
        });

        if (enemies.length == 0)
            return 0.0;

        int others = getMediator().getOthers();
        double energy = getMediator().getEnergy();

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
                return Math.min(enemy.getEnergy() * 0.25, Math.min(2.9999, Math.max(getMediator().getEnergy() - 0.4, 0)));

            double distance = getMediator().getPoint().distance(enemy.getPoint());
            double myEnergy = getMediator().getEnergy();
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

            return R.basicSurferRounding(R.constrain(0.15, Math.min(power, hisEnergy * 0.25),
                    Math.max(myEnergy - 0.1, 0.1)));
        }

//        return 1.95;
    }

    public KnnView<EnemyMovie> getKnnSet(RobotSnapshot robot) {
        StorageNamespace ns = getStorageNamespace().namespace(robot.getName());
        if (ns.contains("knn"))
            return (KnnView) ns.get("knn");

        KnnView<EnemyMovie> knn = new KnnView<>();

        knn.setDistanceWeighter(new Knn.GaussDistanceWeighter<EnemyMovie>(1.0));
        knn.add(new KnnTree<EnemyMovie>()
                .setMode(KnnTree.Mode.MANHATTAN)
                .setRatio(0.5)
                .setK(24)
                .setStrategy(new MonkGunStrategy())
                .logsEverything());

        ns.put("knn", knn);
        return knn;
    }

    @Override
    public void onNewMovie(EnemyMovie movie) {
        TargetingLog f = TargetingLog.getLog(movie.getLeadActor(), getMediator(), selectPower(), false);
        getKnnSet(movie.getLeadActor()).add(f, movie);
    }

    @Override
    public void onPaint(Graphics2D gr) {
        G g = new G(gr);

        if (lastAngles != null) {
            for (CandidateAngle angle : lastAngles) {
                g.drawPoint(angle.point, Physics.BOT_WIDTH * 2, Color.DARK_GRAY);
            }
        }
    }

    class MonkGunStrategy extends Strategy {
        @Override
        public double[] getQuery(TargetingLog f) {
            return new double[]{
                    1.0 / (1.0 + 2 * f.timeDecel),
                    1.0 / (1.0 + 2 * f.timeRevert),
                    Math.min(f.others - 1, 1),
                    Math.min(f.closestDistance / 400, 1),
                    (f.accel + 1) * .5,
                    f.heat() / 16,
                    Math.abs(f.velocity) / 8.,
            };
        }

        @Override
        public double[] getWeights() {
            return new double[]{2, 1, 2, 6, 2, 2, 4};
        }
    }

    private class CandidateAngle {
        double angle;
        double weight;
        Point point;

        public CandidateAngle(double angle, double weight, Point point) {
            this.angle = angle;
            this.weight = weight;
            this.point = point;
        }
    }
}
