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

package rsalesc.baf2.predictor;

import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.tracking.RobotSnapshot;
import rsalesc.baf2.waves.RobotWave;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by Roberto Sales on 07/08/17.
 */
public class PredictedPoint extends Point {
    public final double heading;
    public final double velocity;
    public final long time;
    public final int ahead;

    private PredictedPoint(double x, double y, double heading, double velocity, long time) {
        super(x, y);
        this.heading = heading;
        this.velocity = velocity;
        this.time = time;
        this.ahead = (int) Math.signum(velocity);
    }

    private PredictedPoint(Point point, double heading, double velocity, long time) {
        this(point.x, point.y, heading, velocity, time);
    }

    public PredictedPoint(double x, double y, double heading, double velocity, long time, int ahead) {
        super(x, y);
        this.heading = heading;
        this.velocity = velocity;
        this.time = time;
        this.ahead = ahead;
    }

    public PredictedPoint(Point point, double heading, double velocity, long time, int ahead) {
        this(point.x, point.y, heading, velocity, time, ahead);
    }

    public static PredictedPoint from(RobotSnapshot robot) {
        return new PredictedPoint(robot.getPoint(), robot.getHeading(), robot.getVelocity(), robot.getTime());
    }

    public static <T extends RobotWave> void filterBreakable(List<PredictedPoint> path, List<T> waves) {
        long timeDelta = path.size() - 1;
        waves.removeIf(new Predicate<T>() {
            @Override
            public boolean test(T t) {
                double breakTime = t.getBreakTime(path.get(0));
                return breakTime < path.get(0).time || breakTime > path.get(0).time + 2 * timeDelta
                        || t.hasPassed(path.get(0), path.get(0).time) || t.hasAnyHit();
            }
        });

        waves.sort(new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return (int) Math.signum(o1.getBreakTime(path.get(0)) - o2.getBreakTime(path.get(0)));
            }
        });
    }

    public PredictedPoint tick(double newHeading, double newVelocity) {
        int newAhead = newVelocity == 0 && velocity != 0 || newVelocity * velocity > 0
                ? ahead
                : (int) Math.signum(newVelocity);
        return new PredictedPoint(this.project(newHeading, newVelocity), newHeading, newVelocity, time + 1, newAhead);
    }

    public PredictedPoint fakeTick(double newHeading, double newVelocity, double jumpAngle, double jumpSize) {
        int newAhead = newVelocity == 0 && velocity != 0 || newVelocity * velocity > 0
                ? ahead
                : (int) Math.signum(newVelocity);
        return new PredictedPoint(this.project(jumpAngle, jumpSize), newHeading, newVelocity, time + 1, newAhead);
    }

//    public double getHeading() {
//        return heading;
//    }

//    public double getVelocity() {
//        return velocity;
//    }

    public double getSpeed() {
        return Math.abs(velocity);
    }

//    public long getTime() {
//        return time;
//    }

//    public int getAhead() {
//        return ahead;
//    }

    public int getDirection(Point from) {
        double head = heading;
        if (ahead < 0)
            head += R.PI;
        double absBearing = Physics.absoluteBearing(this, from);
        double off = R.normalRelativeAngle(head - absBearing);
        if (off > 0) return -1;
        else if (off < 0) return 1;
        else return 0;
    }

    public double getBafHeading() {
        if (ahead < 0)
            return R.normalAbsoluteAngle(heading + R.PI);
        return heading;
    }

    public AxisRectangle getHitBox() {
        return new AxisRectangle(this, Physics.BOT_WIDTH);
    }
}
