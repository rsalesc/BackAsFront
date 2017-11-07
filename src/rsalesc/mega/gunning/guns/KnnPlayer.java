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
import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.StoreComponent;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.mega.tracking.EnemyMovie;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.structures.Knn;
import rsalesc.structures.KnnProvider;
import rsalesc.structures.KnnView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Roberto Sales on 19/09/17.
 */
public abstract class KnnPlayer extends StoreComponent implements Player, KnnProvider<EnemyMovie> {
    public abstract KnnView<EnemyMovie> getNewKnnSet();
    public abstract Knn.DistanceWeighter<EnemyMovie> getLazyWeighter();

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

        if(K == null)
            K = knn.getTotalK();

        K = Math.min(K, knn.queryableData());

        Knn.DistanceWeighter<EnemyMovie> lazyWeighter = getLazyWeighter();
        List<Knn.Entry<EnemyMovie>> entries = new ArrayList<>();
        Iterator<Knn.Entry<EnemyMovie>> it = knn.iterator(f);

        while(it.hasNext() && angles.size() < K) {
            Knn.Entry<EnemyMovie> entry = it.next();

            EnemyMovie movie = entry.payload;

            int ptr = -1;
            int movieSize = movie.size();

            if(movieSize == 0)
                continue;

            int movieSizeLog = 31 - Integer.numberOfLeadingZeros(movieSize);

            long firstTime = movie.get(0).getTime();

            double rotation = R.normalRelativeAngle(movie.get(0).getBafHeading() - enemy.getBafHeading());
            Point translation = movie.get(0).getPoint().subtract(enemy.getPoint());

            Point transformedMe = nextPosition.add(translation).rotate(rotation, movie.get(0).getPoint());

            // safely assume that there are not big gaps between enemy frames
            for(int i = movieSizeLog + 1; i >= 0; i--) {
                if(ptr + (1 << i) < movieSize) {
                    int c = ptr + (1 << i);
                    if(movie.get(c).getPoint().distance(transformedMe)
                            > (movie.get(c).getTime() - firstTime - extraTime) * bulletSpeed) {
                        ptr = c;
                    }
                }
            }

            if(++ptr == movieSize)
                continue;

//            while (ptr < movie.size() && movie.get(ptr).getPoint().distance(transformedMe)
//                    > (movie.get(ptr).getTime() - firstTime - extraTime) * bulletSpeed) {
//                if (ptr + 1 < movie.size()) {
//                    long diff = movie.get(ptr + 1).getTime() - movie.get(ptr).getTime();
//                    if (diff > 10) {
//                        ok = false;
//                        break;
//                    }
//                }
//
//                ptr++;
//            }
//
//            ok = ok && ptr < movie.size();
//            if (!ok) {
//                continue;
//            }

            EnemyRobot current = movie.get(ptr);
            EnemyRobot last = movie.get(Math.max(0, ptr - 1));

            long diff = current.getTime() - last.getTime();
            Point impactPoint = current.getPoint();

            long impactTime = last.getTime();

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
                impactTime += l;
            }

            Point lastPosition = impactPoint.rotate(-rotation, movie.get(0).getPoint()).subtract(translation);
            if (!f.field.contains(lastPosition))
                continue;

            double angle = Physics.absoluteBearing(nextPosition, lastPosition);
            double offset = R.normalRelativeAngle(angle - Physics.absoluteBearing(nextPosition, enemy.getPoint()));

            if(Math.abs(offset) > Rules.MAX_VELOCITY * impactTime / nextPosition.distance(enemy.getPoint()))
                continue;

            angles.add(new CandidateAngle(angle,
                    entry.weight,
                    lastPosition));

            entries.add(entry);
        }

        // apply weighter
        if(lazyWeighter != null) {
            entries = lazyWeighter.getWeightedEntries(entries);
            for (int i = 0; i < entries.size(); i++) {
                angles.get(i).weight = entries.get(i).weight;
            }
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
