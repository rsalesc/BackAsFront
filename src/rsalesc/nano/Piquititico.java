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

package rsalesc.nano;

import robocode.AdvancedRobot;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/**
 * Created by Roberto Sales on 27/09/17.
 */
public class Piquititico extends AdvancedRobot {
    public static final int PREFERRED_DISTANCE = 280;
    public static final double PREFERRED_DISTANCE_DEN = 2000;

    public static final int ANTI_RAMBOT_DISTANCE = 127;
    public static final int BULLET_POWER = 2;
    public static final int BULLET_VELOCITY = 20 - 3 * BULLET_POWER;

    // no member variables
    static double enemyEnergy;
    static double mDirection;
    static int acc;

    @Override
    public void run() {
        setAdjustRadarForGunTurn(true);
        setTurnRadarRightRadians(mDirection = Double.POSITIVE_INFINITY);
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        int inteiro = 40;
        int matchPosition;
        double absBearing;

        setAhead(mDirection *= REV.charAt(
           99 + (int) (enemyEnergy - (enemyEnergy = e.getEnergy()))
        ) * Math.random() - 1);

        enemyHistory = String.valueOf((char) (e.getVelocity() *
                (Math.sin(e.getHeadingRadians() - (absBearing = e.getBearingRadians() + getHeadingRadians())))))
                .concat(enemyHistory);

        while((matchPosition = enemyHistory.indexOf(enemyHistory.substring(0, --inteiro), 64)) < 0);

        setTurnRightRadians(Utils.normalRelativeAngle(
                e.getBearingRadians() - Math.PI * (0.5 - ((inteiro = (int) e.getDistance()) - PREFERRED_DISTANCE) / PREFERRED_DISTANCE_DEN * Math.signum(mDirection))
        ));

        setFire(BULLET_POWER + (ANTI_RAMBOT_DISTANCE / inteiro));

        do {
            absBearing += ((short) enemyHistory.charAt(--matchPosition)) /  e.getDistance();
        } while ((inteiro -= BULLET_VELOCITY) > 0);

        setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians()));

        // it works because the remaining degrees always > than the remaining radians
        setTurnRadarLeftRadians(getRadarTurnRemaining());
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        mDirection = -mDirection;
    }

    public static final char PROB0 = 65535;
    public static final char PROB100 = 0;
    public static final char PROB1 = 100;
    public static final char PROB10 = 10;

    static String enemyHistory = ""
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 1
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 1 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 1 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 1 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
            + (char)-1 + (char)-2 + (char)-3 + (char)-4 + (char)-5 + (char)-6
            + (char)-7 + (char)-8 + (char) 8 + (char) 7 + (char) 6 + (char) 5
            + (char) 4 + (char) 3 + (char) 2 + (char) 1 + (char) 0 + (char) 0
            ;

    public static final String REV = "" +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB100    + PROB100    + PROB100    + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0 + PROB0 + PROB0 + PROB0 +
            PROB0 + PROB0;
}
