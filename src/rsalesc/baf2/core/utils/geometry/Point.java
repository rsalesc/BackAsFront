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

package rsalesc.baf2.core.utils.geometry;

import robocode.util.Utils;
import rsalesc.baf2.core.utils.R;

import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * Created by Roberto Sales on 22/07/17.
 */
public class Point implements Serializable {
    private static final long serialVersionUID = 424242231214242L;

    public double x;
    public double y;

    public Point() {
        setX(0.0);
        setY(0.0);
    }

    public Point(double x, double y) {
        setX(x);
        setY(y);
    }

    public Point(Point a, Point b) {
        setX(b.x - a.x);
        setY(b.y - a.y);
    }

    public Point(Point2D.Double p) {
        setX(p.x);
        setY(p.y);
    }

    public Point2D.Double to2D() {
        return new Point2D.Double(x, y);
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double squaredDistance(Point rhs) {
        return (x - rhs.x) * (x - rhs.x) + (y - rhs.y) * (y - rhs.y);
    }

    public double distance(Point rhs) {
        return R.sqrt(squaredDistance(rhs));
    }

    public double manhattanDistance(Point rhs) {
        return Math.abs(x - rhs.x) + Math.abs(y - rhs.y);
    }

    public double chebyshevDistance(Point rhs) {
        return Math.max(Math.abs(x - rhs.x), Math.abs(y - rhs.y));
    }

    public double dot(Point rhs) {
        return x * rhs.x + y * rhs.y;
    }

    public double cross(Point rhs) {
        return x * rhs.y - y * rhs.x;
    }

    public double squaredNorm() {
        return this.dot(this);
    }

    public double norm() {
        return R.sqrt(squaredNorm());
    }

    public Point add(Point rhs) {
        return new Point(x + rhs.x, y + rhs.y);
    }

    public Point subtract(Point rhs) {
        return new Point(x - rhs.x, y - rhs.y);
    }

    public Point multiply(double alpha) {
        return new Point(x * alpha, y * alpha);
    }

    public Point divide(double alpha) {
        return new Point(x / alpha, y / alpha);
    }

    public void scale(double alpha) {
        x *= alpha;
        y *= alpha;
    }

    public boolean isNull() {
        return (x == 0 && y == 0) || squaredNorm() == 0;
    }

    public Point resized(double alpha) {
        if (isNull())
            return new Point();
        return this.divide(norm()).multiply(alpha);
    }

    public Point versor() {
        return resized(1.0);
    }

    public Point reversed() {
        return new Point(-x, -y);
    }

    public Point transposed() {
        return new Point(y, x);
    }

    public Point weighted(Point rhs, double alpha) {
        return this.multiply(1.0 - alpha).add(rhs.multiply(alpha));
    }


    /**
     * Note that the angle return isn't in
     * an arabic reference system, but is related
     * to the robocode angle system. That's why
     * it's Math.atan2(x, y), not Math.atan2(y, x).
     *
     * @return angle in robocode notation
     */
    public double absoluteBearing() {
        return Utils.normalAbsoluteAngle(R.atan2(x, y));
    }

    public Point project(double angle, double length) {
        return new Point(x + R.sin(angle) * length, y + R.cos(angle) * length);
    }

    public Point project(Point rhs) {
        return rhs.multiply(this.dot(rhs)).divide(rhs.squaredNorm());
    }

    public boolean isNear(Point rhs) {
        return R.isNear(this.distance(rhs), 0);
    }

    public double similarity(Point rhs) {
        return dot(rhs) / norm() / rhs.norm();
    }

    public Point rotate(double theta) {
        theta = Utils.normalRelativeAngle(theta);
        return new Point(R.cos(-theta) * x - R.sin(-theta) * y, R.sin(-theta) * x + R.cos(-theta) * y);
    }

    public Point rotate(double theta, Point pivot) {
        return this.subtract(pivot).rotate(theta).add(pivot);
    }

    public Point clip(AxisRectangle rect) {
        double newX = R.constrain(rect.minx, x, rect.maxx);
        double newY = R.constrain(rect.miny, y, rect.maxy);
        return new Point(newX, newY);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
