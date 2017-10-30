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

import robocode.Rules;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.core.utils.geometry.Range;
import rsalesc.baf2.waves.Wave;

import java.util.ArrayList;
import java.util.Arrays;

public class EnemyPredictor {
    private static final double STICK = 120;
    private static final double SMALLER_STICK = 90;
    private static final boolean UPDATE_ANGLE = false;

    public static Range getPreciseMea(AxisRectangle field, PredictedPoint initialPoint, Wave wave, int direction) {
        ArrayList<PredictedPoint> pts = new ArrayList<>();

        if(direction == 0)
            direction = 1;

        double gfZero = wave.getAngle(initialPoint);

        PredictedPoint[] prev;
        pts.addAll(Arrays.asList(prev = getSmoothPoints(field, initialPoint, wave)));
        pts.addAll(Arrays.asList(getDirectPoints(field, initialPoint, wave, prev)));
        pts.addAll(Arrays.asList(getSharpPoints(field, initialPoint, wave)));

        Range range = new Range(-1e-8, +1e-8);

        for(PredictedPoint pt : pts) {
            double absBearing = wave.getAngle(pt);
            double offset = R.normalRelativeAngle(absBearing - gfZero);

            range.push(offset * direction);
        }

        return range;
    }

    public static PredictedPoint[] getSmoothPoints(AxisRectangle field, PredictedPoint initialPoint, Wave wave) {
        PredictedPoint[] pts = new PredictedPoint[2];

        AxisRectangle shrinkedField = field.shrink(18, 18);

        for(int i = -1; i <= 1; i += 2) {
            double absBearing = Physics.absoluteBearing(wave.getSource(), initialPoint);
            double angle = R.normalAbsoluteAngle(absBearing + R.HALF_PI * i);

            PredictedPoint cur = initialPoint;

            while(!wave.hasPassed(cur, cur.time)) {
                double smoothAngle = WallSmoothing.weirdSmoothing(shrinkedField, wave.getSource(), cur, angle, STICK);
                cur = PrecisePredictor.tick(cur, smoothAngle, Rules.MAX_VELOCITY, Double.POSITIVE_INFINITY);

                if(!shrinkedField.contains(cur))
                    break;

                pts[(i+1)/2] = cur;

                if(UPDATE_ANGLE)
                    angle = R.normalAbsoluteAngle(Physics.absoluteBearing(wave.getSource(), cur) + R.HALF_PI * i);
            }
        }

        return pts;
    }

    public static PredictedPoint[] getDirectPoints(AxisRectangle field, PredictedPoint initialPoint, Wave wave, PredictedPoint[] prev) {
        PredictedPoint[] pts = new PredictedPoint[2];

        AxisRectangle shrinkedField = field.shrink(18, 18);

        for(int i = -1; i <= 1; i += 2) {
            double angle = Physics.absoluteBearing(initialPoint, prev[(i+1)/2]);

            PredictedPoint cur = initialPoint;

            while(!wave.hasPassed(cur, cur.time)) {
                double smoothAngle = WallSmoothing.weirdSmoothing(shrinkedField, wave.getSource(), cur, angle, SMALLER_STICK);
                cur = PrecisePredictor.tick(cur, smoothAngle, Rules.MAX_VELOCITY, Double.POSITIVE_INFINITY);

                if(!shrinkedField.contains(cur))
                    break;

                pts[(i+1)/2] = cur;

                if(UPDATE_ANGLE)
                    angle = R.normalAbsoluteAngle(Physics.absoluteBearing(cur, prev[(i+1)/2]));
            }
        }

        return pts;
    }

    public static PredictedPoint[] getSharpPoints(AxisRectangle field, PredictedPoint initialPoint, Wave wave) {
        PredictedPoint[] pts = new PredictedPoint[2];

        AxisRectangle shrinkedField = field.shrink(18, 18);

        for(int i = -1; i <= 1; i += 2) {
            double absBearing = Physics.absoluteBearing(wave.getSource(), initialPoint);
            double angle = R.normalAbsoluteAngle(absBearing + R.HALF_PI * i);

            PredictedPoint cur = initialPoint;

            while(!wave.hasPassed(cur, cur.time)) {
                double smoothAngle = angle;
                cur = PrecisePredictor.tick(cur, smoothAngle, Rules.MAX_VELOCITY, Double.POSITIVE_INFINITY);

                if(!shrinkedField.contains(cur))
                    break;

                pts[(i+1)/2] = cur;

                if(UPDATE_ANGLE)
                    angle = R.normalAbsoluteAngle(Physics.absoluteBearing(wave.getSource(), cur) + R.HALF_PI * i);
            }
        }

        return pts;
    }
}
