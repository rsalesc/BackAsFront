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

import rsalesc.baf2.core.listeners.FireEvent;
import rsalesc.baf2.core.listeners.FireListener;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.painting.G;
import rsalesc.baf2.painting.PaintManager;
import rsalesc.baf2.painting.Painting;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.EnemyTracker;
import rsalesc.mega.tracking.EnemyMovie;
import rsalesc.mega.tracking.MovieListener;
import rsalesc.mega.utils.StatTracker;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.melee.gunning.MeleeGun;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Created by Roberto Sales on 19/09/17.
 */
public abstract class PlayItForwardGun extends AutomaticGun implements MovieListener, MeleeGun, FireListener {
    private Player player;
    private Integer K;
    private GeneratedAngle[] lastGenerated;
    private GeneratedAngle[] lastFireGenerated;
    private GeneratedAngle lastFirePicked;
    private GeneratedAngle lastPicked;
    private Point lastFireSource;

    @Override
    public void onFire(FireEvent e) {
        lastFireSource = e.getSource();
        lastFireGenerated = lastGenerated;
        lastFirePicked = lastPicked;
    }

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
    public int availableData(EnemyLog enemyLog) {
        return getPlayer().availableData(enemyLog);
    }

    @Override
    public int queryableData(EnemyLog enemyLog) {
        return getPlayer().queryableData(enemyLog);
    }

    @Override
    public double pickBestAngle(EnemyLog enemyLog, GeneratedAngle[] angles, double power) {
        if(angles.length == 0)
            throw new IllegalStateException();

        double bestDensity = Double.NEGATIVE_INFINITY;
        GeneratedAngle bestAngle = null;

        for(GeneratedAngle shootAngle : angles) {
            double density = 0;
            for(GeneratedAngle angle : angles) {
                double diff = R.normalRelativeAngle(angle.angle - shootAngle.angle);
                double x = diff / (36 / angle.distance);
                density += angle.weight * R.gaussKernel(x);
            }

            if(density > bestDensity) {
                bestDensity = density;
                bestAngle = shootAngle;
            }
        }

        if(bestAngle == null)
            return 0;

        lastPicked = bestAngle;

        return bestAngle.angle;
    }

    @Override
    public GeneratedAngle[] generateFiringAngles(EnemyLog enemyLog, double power) {
        if(enemyLog == null || !enemyLog.isAlive()) {
            EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();
            if(enemies.length == 0)
                return new GeneratedAngle[0];
            enemyLog = EnemyTracker.getInstance().getLog(enemies[0]);
        }

        TargetingLog f = TargetingLog.getLog(enemyLog.getLatest(), getMediator(), power, true);

        return lastGenerated = getPlayer().getFiringAngles(enemyLog, f, K);
    }

    @Override
    public void onNewMovie(EnemyMovie movie) {
        double power = getPowerSelector().selectPower(getMediator(), StatTracker.getInstance().getCurrentStatData());
        TargetingLog f = TargetingLog.getLog(movie.getLeadActor(), getMediator(), power, false);
        getPlayer().log(f, movie);
    }

    @Override
    public void setupPaintings(PaintManager manager) {
        manager.add(KeyEvent.VK_P, "swarm", new Painting() {
            @Override
            public void paint(G g) {

                if(lastFireGenerated != null)
                for(GeneratedAngle angle : lastFireGenerated) {
                    Point p = lastFireSource.project(angle.angle, angle.distance);
                    g.drawPoint(p,Physics.BOT_WIDTH * 2, new Color(29, 29, 29, 200));
                }

                if(lastFirePicked != null) {
                    Point p = lastFireSource.project(lastFirePicked.angle, lastFirePicked.distance);
                    g.drawPoint(p,Physics.BOT_WIDTH * 2, new Color(119, 119, 119, 190));
                }
            }
        }, true);
    }

    public void setK(Integer k) {
        K = k;
    }
}
