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

package rsalesc.mega.gunning.guns;

import robocode.Rules;
import robocode.util.Utils;
import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.StoreComponent;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.mega.tracking.EnemyMovie;
import rsalesc.mega.utils.structures.Knn;
import rsalesc.mega.utils.structures.KnnProvider;
import rsalesc.mega.utils.structures.KnnView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roberto Sales on 19/09/17.
 */
public abstract class KnnPlayer extends StoreComponent implements Player, KnnProvider<EnemyMovie> {
    public abstract KnnView<EnemyMovie> getNewKnnSet();

    public KnnView<EnemyMovie> getKnnSet(String name) {
        StorageNamespace ns = getStorageNamespace().namespace("knn");
        if(!ns.contains(name))
            ns.put(name, getNewKnnSet());

        return (KnnView) ns.get(name);
    }

    public void log(TargetingLog f, EnemyMovie movie) {
        getKnnSet(movie.getLeadActor().getName()).add(f, movie);
    }

    @Override
    public int availableData(EnemyLog enemyLog) {
        return getKnnSet(enemyLog.getName()).availableData();
    }

    @Override
    public int queryableData(EnemyLog enemyLog) {
        return getKnnSet(enemyLog.getName()).queryableData();
    }

    @Override
    public GeneratedAngle[] getFiringAngles(EnemyLog enemyLog, TargetingLog f, Integer K) {
        double power = f.bulletPower;
        double bulletSpeed = Rules.getBulletSpeed(power);

        long time = f.time;
        Point nextPosition = f.source;

        ArrayList<CandidateAngle> angles = new ArrayList<>();

        EnemyRobot enemy = enemyLog.getLatest();

        long extraTime = time - enemy.getTime();

        KnnView<EnemyMovie> knn = getKnnSet(enemyLog.getName());

        if(knn.availableData() == 0)
            return new GeneratedAngle[0];

        List<Knn.Entry<EnemyMovie>> entries = K != null ? knn.query(f, K) : knn.query(f);

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
            if (!f.field.contains(lastPosition))
                continue;

            angles.add(new CandidateAngle(Physics.absoluteBearing(nextPosition, lastPosition),
                    entry.weight,
                    lastPosition));
        }

        ArrayList<GeneratedAngle> res = new ArrayList<>();
        for(CandidateAngle angle : angles) {
            res.add(new GeneratedAngle(angle.weight, angle.angle, nextPosition.distance(angle.point)));
        }

        return res.toArray(new GeneratedAngle[0]);
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
