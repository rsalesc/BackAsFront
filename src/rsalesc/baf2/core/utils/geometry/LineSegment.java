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

import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Created by Roberto Sales on 09/08/17.
 */
public class LineSegment {
    public Point p1;
    public Point p2;

    public LineSegment(Point a, Point b) {
        this.p1 = a;
        this.p2 = b;
    }

    public static Point2D.Double jkIntersection(Point2D.Double l1, Point2D.Double l2, Circle w) {
        double xd = l2.x - l1.x;
        double yd = l2.y - l1.y;
        double a = sqr(xd) + sqr(yd);
        double b = 2 * (xd * (l1.x - w.center.x) + yd * (l1.y - w.center.y));
        double c = sqr(l1.x - w.center.x) + sqr(l1.y - w.center.y) - sqr(w.radius);
        double det = b * b - 4 * a * c;
        if (det < 0)
            throw new IllegalArgumentException();
        det = Math.sqrt(det);
        double t = (-b + det) / (2 * a);
        if (0 <= t && t <= 1)
            return new Point2D.Double(l1.x + xd * t, l1.y + yd * t);
        t = (-b - det) / (2 * a);
        if (0 <= t && t <= 1)
            return new Point2D.Double(l1.x + xd * t, l1.y + yd * t);

        throw new IllegalArgumentException("t is out of range [0;1]: " + t);
    }

    private static double sqr(double x) {
        return x * x;
    }

    public Point middle() {
        return p1.weighted(p2, 0.5);
    }

    public Point[] intersect(Circle circle) {
        Point[] res = new Point[2];
        Point H = (circle.center.subtract(p1)).project(p2.subtract(p1)).add(p1);
        double h = (H.subtract(circle.center)).norm();
        if (circle.radius < h - R.EPSILON)
            return new Point[0];
        Point v = p2.subtract(p1).resized(R.sqrt(sqr(circle.radius) - sqr(h)));
        res[0] = H.add(v);
        res[1] = H.subtract(v);

        ArrayList<Point> inter = new ArrayList<>();
        for (int i = 0; i < res.length; i++) {
            if (isContained(res[i]))
                inter.add(res[i]);
        }
        return inter.toArray(new Point[0]);
    }

    public Point rayLikeIntersect(Circle circle) {
        Point2D.Double pd = jkIntersection(p1.to2D(), p2.to2D(), circle);
        return new Point(pd.x, pd.y);
//        Point[] p = intersect(circle);
//        for(int i = 0; i < p.length; i++)
//            if(isContained(p[i]))
//                return p[i];

//        throw new IllegalStateException("no intersection was found");
    }

    public boolean isContained(Point p) {
        return R.isNear(p.subtract(p1).cross(p.subtract(p2)), 0)
                && p.subtract(p1).dot(p.subtract(p2)) < R.EPSILON;
    }

    public double getLength() {
        return p1.distance(p2);
    }
}
