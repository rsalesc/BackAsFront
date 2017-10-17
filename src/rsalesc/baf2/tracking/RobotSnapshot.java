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

package rsalesc.baf2.tracking;

import rsalesc.baf2.core.utils.BattleTime;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.core.utils.geometry.Point;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public interface RobotSnapshot {
    String getName();

    long getTime();

    BattleTime getBattleTime();

    Point getPoint();

    double getLateralVelocity(Point from);

    double getAdvancingVelocity(Point from);

    int getDirection(Point from);

    int getAhead();

    AxisRectangle getHitBox();

    double getEnergy();

    double getVelocity();

    double getHeading();

    double getBafHeading();

    int getOthers();

    boolean isFuture();
}
