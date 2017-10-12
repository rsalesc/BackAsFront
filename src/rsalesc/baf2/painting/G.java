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

package rsalesc.baf2.painting;

import robocode.util.Utils;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.painting.colors.Gradient;

import java.awt.*;
import java.awt.geom.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by Roberto Sales on 24/07/17.
 */
public class G {
    private static final Gradient HEAT_GRADIENT;
    private static final Gradient WAVE_GRADIENT;
    private static final Gradient SAFE_DISCRETE_GRADIENT;

    static {
        HEAT_GRADIENT = new Gradient(new Gradient.GradientColor[]{
                new Gradient.GradientColor(Color.WHITE, 0),
                new Gradient.GradientColor(Color.YELLOW, 0.5),
                new Gradient.GradientColor(Color.RED, 1)
        });

        WAVE_GRADIENT = new Gradient(new Gradient.GradientColor[]{
                new Gradient.GradientColor(Color.DARK_GRAY, 0),
//                new Gradient.GradientColor(Color.YELLOW, 0.5),
                new Gradient.GradientColor(new Color(255, 218, 0), 1)
        });

        SAFE_DISCRETE_GRADIENT = new Gradient(new Gradient.GradientColor[]{
                new Gradient.GradientColor(Color.GREEN, 0),
                new Gradient.GradientColor(Color.GREEN, 0.65 - R.EPSILON),
                new Gradient.GradientColor(Color.YELLOW, 0.65),
                new Gradient.GradientColor(Color.YELLOW, 0.875 - R.EPSILON),
                new Gradient.GradientColor(Color.RED, 0.875),
                new Gradient.GradientColor(Color.RED, 1)
        });
    }

    private Graphics2D g;
    private LinkedList<Color> colorStack;

    public G(Graphics2D g) {
        this.g = g;
        colorStack = new LinkedList<Color>();
        colorStack.add(g.getColor());
    }

    public Graphics2D getGraphics() {
        return g;
    }

    public static Color getHeatColor(double alpha) {
        return HEAT_GRADIENT.evaluate(alpha);
    }

    public static Color getWaveDangerColor(double alpha) {
        return WAVE_GRADIENT.evaluate(alpha);
    }

    public static Color getDiscreteSafeColor(double alpha) {
        return SAFE_DISCRETE_GRADIENT.evaluate(alpha);
    }

    public void setColor(Color color) {
        colorStack.clear();
        colorStack.push(color);
        g.setColor(color);
    }

    public void pushColor(Color color) {
        colorStack.push(color);
        g.setColor(color);
    }

    public void popColor() {
        if (colorStack.size() <= 1)
            throw new IllegalStateException();

        colorStack.pop();
        g.setColor(colorStack.peek());
    }

    public Shape getCircleShape(Point center, double radius) {
        return new Ellipse2D.Double(center.x - radius, center.y - radius, radius * 2, radius * 2);
    }

    public void drawCircle(Point center, double radius) {
        g.draw(getCircleShape(center, radius));
    }

    public void drawCircle(Point center, double radius, Color color) {
        pushColor(color);
        drawCircle(center, radius);
        popColor();
    }

    public void fillCircle(Point center, double radius) {
        g.fill(getCircleShape(center, radius));
    }

    public void fillCircle(Point center, double radius, Color color) {
        pushColor(color);
        fillCircle(center, radius);
        popColor();
    }

    public Shape getRectShape(Point pivot, double sizeX, double sizeY) {
        return new Rectangle2D.Double(pivot.x, pivot.y, sizeX, sizeY);
    }

    public Shape getSquareShape(Point pivot, double size) {
        return getRectShape(pivot, size, size);
    }

    public Shape getCenteredSquareShape(Point center, double size) {
        return getSquareShape(new Point(center.x - size * 0.5, center.y - size * 0.5), size);
    }

    public void drawPoint(Point point, double width) {
        g.draw(getCenteredSquareShape(point, width));
    }

    public void drawPoint(Point point, double width, Color color) {
        pushColor(color);
        drawPoint(point, width);
        popColor();
    }

    public Shape getArcShape(Point center, double radius, double startAngle, double angle) {
        if (angle > 0) {
            angle = -angle;
            startAngle = Utils.normalAbsoluteAngle(startAngle + angle);
        }
        double fixedStart = Utils.normalAbsoluteAngle(-startAngle + R.HALF_PI);
        return new Arc2D.Double(center.x, center.y, radius, radius, fixedStart, -angle, Arc2D.OPEN);
    }

    public void drawArc(Point center, double radius, double startAngle, double angle) {
        g.draw(getArcShape(center, radius, startAngle, angle));
    }

    public void drawArc(Point center, double radius, double startAngle, double angle, Color color) {
        pushColor(color);
        drawArc(center, radius, startAngle, angle);
        popColor();
    }

    public Shape getLine(Point a, Point b) {
        Line2D.Double line = new Line2D.Double(a.to2D(), b.to2D());
        return line;
    }

    public void drawLine(Point a, Point b) {
        g.draw(getLine(a, b));
    }

    public void drawLine(Point a, Point b, Color color) {
        pushColor(color);
        drawLine(a, b);
        popColor();
    }

    public void drawRadial(Point center, double angle, double start, double end) {
        drawLine(center.project(angle, start), center.project(angle, end));
    }

    public void drawRadial(Point center, double angle, double start, double end, Color color) {
        pushColor(color);
        drawRadial(center, angle, start, end);
        popColor();
    }

    public GeneralPath getPath(Collection<Point> points) {
        GeneralPath path = new GeneralPath(Path2D.WIND_EVEN_ODD, points.size());
        int cnt = 0;
        for(Point point : points) {
            if(cnt == 0)
                path.moveTo(point.x, point.y);
            else
                path.lineTo(point.x, point.y);

            cnt++;
        }

        return path;
    }

    public GeneralPath getPolygon(Collection<Point> points) {
        GeneralPath path = getPath(points);
        path.closePath();
        return path;
    }

    public void drawBeam(Point center, double startAngle, double endAngle) {
        g.draw(getPolygon(Arrays.asList(center, center.project(startAngle, 1500), center.project(endAngle, 1500))));
    }

    public void drawBeam(Point center, double startAngle, double endAngle, Color color) {
        pushColor(color);
        drawBeam(center, startAngle, endAngle);
        popColor();
    }

    public void fillBeam(Point center, double startAngle, double endAngle) {
        g.fill(getPolygon(Arrays.asList(center, center.project(startAngle, 1500), center.project(endAngle, 1500))));
    }

    public void fillBeam(Point center, double startAngle, double endAngle, Color color) {
        pushColor(color);
        fillBeam(center, startAngle, endAngle);
        popColor();
    }

    public void drawString(Point p, String s) {
        g.drawString(s, (float) p.x, (float) p.y);
    }

    public void drawString(Point p, String s, Color c) {
        pushColor(c);
        drawString(p, s);
        popColor();
    }
}
