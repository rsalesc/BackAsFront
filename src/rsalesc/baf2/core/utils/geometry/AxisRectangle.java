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

import rsalesc.baf2.core.utils.R;

import java.io.Serializable;

/**
 * Created by Roberto Sales on 23/07/17.
 */
public class AxisRectangle implements Serializable {
    private static final long serialVersionUID = 4142424242L;
    public double minx;
    public double maxx;
    public double miny;
    public double maxy;

    public AxisRectangle(double minx, double maxx, double miny, double maxy) {
        this.minx = minx;
        this.maxx = maxx;
        this.miny = miny;
        this.maxy = maxy;
    }

    public AxisRectangle(Point center, double size) {
        this.minx = center.x - size / 2;
        this.maxx = center.x + size / 2;
        this.miny = center.y - size / 2;
        this.maxy = center.y + size / 2;
    }

    public void add(Point point) {
        minx = Math.min(minx, point.getX());
        maxx = Math.max(maxx, point.getX());
        miny = Math.max(miny, point.getY());
        maxy = Math.max(maxy, point.getY());
    }

    public double getWidth() {
        return maxx - minx;
    }

    public double getHeight() {
        return maxy - miny;
    }

    public Point getCenter() {
        return new Point((minx + maxx) / 2, (miny + maxy) / 2);
    }

    public boolean contains(Point point) {
        return R.nearOrBetween(minx, point.getX(), maxx)
                && R.nearOrBetween(miny, point.getY(), maxy);
    }

    public boolean strictlyContains(Point point) {
        return R.strictlyBetween(minx, point.getX(), maxx)
                && R.strictlyBetween(miny, point.getY(), maxy);
    }

    public AxisRectangle shrinkX(double amount) {
        if (amount * 2 > getWidth()) {
            double x = (minx + maxx) / 2;
            return new AxisRectangle(x, x, miny, maxy);
        } else {
            return new AxisRectangle(minx + amount, maxx - amount, miny, maxy);
        }
    }

    public AxisRectangle shrinkY(double amount) {
        if (amount * 2 > getHeight()) {
            double y = (miny + maxy) / 2;
            return new AxisRectangle(minx, maxx, y, y);
        } else {
            return new AxisRectangle(minx, maxx, miny + amount, maxy - amount);
        }
    }

    public AxisRectangle shrink(double amountX, double amountY) {
        return this.shrinkX(amountX).shrinkY(amountY);
    }

    public double distanceToEdges(Point point) {
        double x = point.getX();
        double y = point.getY();
        return R.sqrt(
                Math.min(sqr(minx - x), sqr(maxx - x)) +
                        Math.min(sqr(miny - y), sqr(maxy - y)));
    }

    public double sqr(double x) {
        return x * x;
    }

    public Point[] getCorners() {
        Point[] corners = new Point[4];
        corners[0] = new Point(minx, miny);
        corners[1] = new Point(maxx, miny);
        corners[2] = new Point(maxx, maxy);
        corners[3] = new Point(minx, maxy);
        return corners;
    }

    public AxisRectangle transposed() {
        return new AxisRectangle(miny, maxy, minx, maxx);
    }
}
