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

package rsalesc.baf2.waves;

import robocode.Bullet;
import robocode.ScannedRobotEvent;
import rsalesc.baf2.core.utils.BattleTime;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.core.utils.geometry.LineSegment;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.tracking.EnemyRobot;

import java.util.ArrayList;
import java.util.function.Predicate;

/**
 * Created by Roberto Sales on 22/07/17.
 */
public class EnemyWave extends RobotWave {
    private EnemyRobot enemy;
    ArrayList<Shadow> shadows = new ArrayList<>();

    public EnemyWave(EnemyRobot robot, Point source, BattleTime time, double velocity) {
        super(source, time, velocity);
        setEnemy(robot);
    }

    public EnemyRobot getEnemy() {
        return enemy;
    }

    public void setEnemy(EnemyRobot enemy) {
        this.enemy = enemy;
    }

    public boolean isFrom(String name) {
        return enemy.getName().equals(name);
    }

    public boolean isFrom(EnemyRobot robot) {
        return isFrom(robot.getName());
    }

    public boolean isFrom(ScannedRobotEvent e) {
        return isFrom(e.getName());
    }

    public boolean wasFiredBy(Bullet bullet, long time) {
        return bullet.getName().equals(enemy.getName()) && R.isNear(getVelocity(), bullet.getVelocity())
                && R.isNear(new Point(bullet.getX(), bullet.getY()).distance(getSource()), getDistanceTraveled(time), 40);
    }

    public boolean isShadowed(double angle) {
        for(Shadow shadow : shadows) {
            if(shadow.isInside(angle))
                return true;
        }

        return false;
    }

    public double getShadowFactor(AngularRange intersection) {
        AngularRange whole = new AngularRange(intersection.getAngle(0), intersection.min, intersection.max);
        ArrayList<AngularRange> pieces = new ArrayList<>();
        pieces.add(whole);

        for(Shadow shadow : shadows) {
            ArrayList<AngularRange> nextPieces = new ArrayList<>();
            AngularRange shadowRange = shadow.getIntersection();

            for(AngularRange piece : pieces) {
                AngularRange shadowIntersection = piece.intersectAngles(shadowRange);
                if(R.isNear(shadowIntersection.getLength(), 0))
                    nextPieces.add(piece);
                else {
                    nextPieces.add(new AngularRange(piece.getAngle(0), piece.min, shadowIntersection.min));
                    nextPieces.add(new AngularRange(piece.getAngle(0), shadowIntersection.max, piece.max));
                }
            }

            pieces = nextPieces;
        }

        double totalLength = 0;
        for(AngularRange range : pieces)
            totalLength += range.getLength();

        double res = totalLength / intersection.getLength();
        if(Double.isNaN(res))
            return 1.0;

        return 1.0 - res;
    }

    public void cast(BulletWave bulletWave) {
        shadows.removeIf(new Predicate<Shadow>() {
            @Override
            public boolean test(Shadow shadow) {
                return shadow.getBulletWave() == bulletWave;
            }
        });

        Shadow shadow = getShadow(this, bulletWave);
        if(shadow != null)
            shadows.add(shadow);
    }

    public ArrayList<Shadow> getShadows() {
        return shadows;
    }

    public static Shadow getShadow(EnemyWave wave, BulletWave bullet) {
        LineSegment intersection = getShadowSegment(wave, bullet);
        if(intersection == null)
            return null;

        Point middle = intersection.middle();

        double absBearing = Physics.absoluteBearing(wave.getSource(), middle);
        AngularRange range = new AngularRange(absBearing, -1e-8, +1e-8);

        range.pushAngle(Physics.absoluteBearing(wave.getSource(), intersection.p1));
        range.pushAngle(Physics.absoluteBearing(wave.getSource(), intersection.p2));

        return new Shadow(range, bullet);
    }

    public static LineSegment getShadowSegment(EnemyWave wave, BulletWave bullet) {
        long time = Math.max(bullet.getTime(), wave.getTime());
        long maxTime = Integer.MAX_VALUE;
        if(bullet.hasAnyHit())
            maxTime = Math.min(maxTime, bullet.getHitTime());

        if(wave.getSource().distance(bullet.project(time)) < wave.getDistanceTraveled(time) - R.EPSILON)
            return null;

        int iterations = 0;
        while(wave.getSource().distance(bullet.project(time)) > wave.getDistanceTraveled(time) && iterations++ < 120 && time <= maxTime) {
            time++;
        }

        if(iterations > 120 || time > maxTime)
            return null;

        LineSegment segment = new LineSegment(bullet.project(time - 1), bullet.project(time));

        Point pA;
        Point pB;

        if(wave.getCircle(time - 1).isInside(segment.p2))
            pB = segment.rayLikeIntersect(wave.getCircle(time - 1));
        else
            pB = segment.p2;

        if(wave.getCircle(time).isInside(segment.p1))
            pA = segment.p1;
        else
            pA = segment.rayLikeIntersect(wave.getCircle(time));

        return new LineSegment(pA, pB);
    }
}
