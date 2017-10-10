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

package rsalesc.melee.movement.risk;

/**
 * Created by Roberto Sales on 25/09/17.
 */
public class Monk3rdGenMR extends TrueMinimumRisk {
    public Monk3rdGenMR() {
        setEvaluation(new RiskEvaluation3rd());
    }

//    @Override
//    public void onPaint(Graphics2D gr) {
//        super.onPaint(gr);
//        G g = new G(gr);
//
//        long time = getMediator().getTime();
//
//        for(EnemyWave wave : getManager().getWaves()) {
//            MyRobot pastMe = MyLog.getInstance().exactlyAt(wave.getTime() - 1);
//            double headOnAngle = Physics.absoluteBearing(wave.getSource(), pastMe.getPoint());
//            Point projection = wave.project(headOnAngle, time);
//
//            g.drawCircle(projection, 3.0, Color.MAGENTA);
//        }
//    }
}