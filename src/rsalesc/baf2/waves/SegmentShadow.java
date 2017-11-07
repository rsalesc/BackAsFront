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

import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.core.utils.geometry.LineSegment;
import rsalesc.baf2.core.utils.geometry.Point;

/**
 * Created by Roberto Sales on 28/09/17.
 */
public class SegmentShadow extends Shadow {
    private final LineSegment segment;
    private final Point d1;
    private final Point d2;

    public SegmentShadow(LineSegment segment, BulletWave wave, EnemyWave enemyWave) {
        super(getRange(segment, wave, enemyWave), wave);
        this.segment = segment;
        this.d1 = segment.p1.subtract(enemyWave.getSource());
        this.d2 = segment.p2.subtract(enemyWave.getSource());
    }

    private static AngularRange getRange(LineSegment segment, BulletWave wave, EnemyWave enemyWave) {
        AngularRange res = new AngularRange(enemyWave.getAngle(segment.middle()), -1e-20, 1e-20);
        res.pushAngle(enemyWave.getAngle(segment.p1));
        res.pushAngle(enemyWave.getAngle(segment.p2));

        return res;
    }

    @Override
    public boolean isInside(double angle) {
        Point vec = new Point(Math.sin(angle), Math.cos(angle));
        return vec.cross(d1) * vec.cross(d2) < 1e-12
                && vec.dot(d1) > 0 && vec.dot(d2) > 0;
    }
}
