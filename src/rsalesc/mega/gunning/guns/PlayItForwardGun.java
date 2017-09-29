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

package rsalesc.mega.gunning.guns;

import robocode.Rules;
import robocode.util.Utils;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.EnemyTracker;
import rsalesc.mega.tracking.EnemyMovie;
import rsalesc.mega.tracking.MovieListener;
import rsalesc.mega.utils.StatTracker;
import rsalesc.mega.utils.TargetingLog;

/**
 * Created by Roberto Sales on 19/09/17.
 */
public abstract class PlayItForwardGun extends AutomaticGun implements MovieListener {
    private Player player;

    @Override
    public String getGunName() {
        return "Play-It Forward Gun";
    }

    public PlayItForwardGun(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public double pickBestAngle(EnemyLog enemyLog, GeneratedAngle[] angles, double power) {
        if(angles.length == 0)
            throw new IllegalStateException();

        double bestDensity = Double.NEGATIVE_INFINITY;
        double bestAngle = 0;

        for(GeneratedAngle shootAngle : angles) {
            double density = 0;
            for(GeneratedAngle angle : angles) {
                double diff = Utils.normalRelativeAngle(angle.angle - shootAngle.angle);
                double bandwidth = Physics.hitAngle(angle.distance) * 0.9;
                double x = diff / bandwidth;
                if(Math.abs(x) < 1)
                    density += angle.weight * R.cubicKernel(x);
            }

            if(density > bestDensity) {
                bestDensity = density;
                bestAngle = shootAngle.angle;
            }
        }

        return bestAngle;
    }

    @Override
    public GeneratedAngle[] generateFiringAngles(EnemyLog enemyLog, double power) {
        if(enemyLog == null || !enemyLog.isAlive()) {
            EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();
            if(enemies.length == 0)
                return new GeneratedAngle[0];
            enemyLog = EnemyTracker.getInstance().getLog(enemies[0]);
        }

        TargetingLog f = TargetingLog.getLog(enemyLog.getLatest(), getMediator(), power);

        return getPlayer().getFiringAngles(enemyLog, f);
    }

    @Override
    public void onNewMovie(EnemyMovie movie) {
        double power = getPowerSelector().selectPower(getMediator(), StatTracker.getInstance().getCurrentStatData());
        TargetingLog f = TargetingLog.getLog(movie.getLeadActor(), getMediator(), power);
        getPlayer().log(f, movie);
    }
}
