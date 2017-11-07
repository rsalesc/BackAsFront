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

import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import rsalesc.baf2.core.benchmark.Benchmark;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.painting.G;
import rsalesc.baf2.painting.PaintManager;
import rsalesc.baf2.painting.Painting;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.EnemyTracker;
import rsalesc.baf2.tracking.RobotSnapshot;
import rsalesc.baf2.waves.*;
import rsalesc.mega.movement.DangerPoint;
import rsalesc.mega.utils.IMea;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.mega.utils.TimestampedGFRange;
import rsalesc.structures.Knn;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Roberto Sales on 15/09/17.
 */
public abstract class GuessFactorGun extends AutomaticGun
        implements BulletWaveListener, BulletWavePreciseListener, TickBulletListener {
    public static final String LOG_HINT = "loghint";
    public static final String FOUND_HINT = "foundhint";
    private static final double GF_SCALE = 0.9;

    private final GFTargeting targeting;
    private TargetingLog lastTargetingLog;

    private GeneratedAngle[] lastGenerated;

    private double lastGf = 0;

    private BulletManager waves;

    @Override
    public String getGunName() {
        return "GuessFactor Gun";
    }

    public GuessFactorGun(GFTargeting targeting, BulletManager waves) {
        this.targeting = targeting;
        this.waves = waves;
    }

    public BulletManager getManager() {
        return waves;
    }


    @Override
    public GeneratedAngle[] generateFiringAngles(EnemyLog enemyLog, double power) {
        Benchmark.getInstance().start("GuessFactorGun.generateFiringAngles()");
        if(enemyLog == null) {
            EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();
            if(enemies.length == 0)
                return new GeneratedAngle[0];
            enemyLog = EnemyTracker.getInstance().getLog(enemies[0]);
        }

        if(!enemyLog.isAlive())
            return new GeneratedAngle[0];

        lastTargetingLog = TargetingLog.getLog(enemyLog.getLatest(), getMediator(), power, true);
        lastTargetingLog.lastGf = lastGf;

        lastGenerated = targeting.getFiringAngles(enemyLog, lastTargetingLog, lastTargetingLog);
        Benchmark.getInstance().stop();

        return lastGenerated;
    }

    @Override
    public void setupPaintings(PaintManager manager) {
        Painting painting = new Painting() {
            @Override
            public void paint(G g) {
                final int WAVE_DIVISIONS = 200;
                long time = getMediator().getTime();

                for (BulletWave wave : getManager().getWaves()) {
                    if(wave.everyoneInside(getMediator()))
                        continue;

                    TargetingLog log = (TargetingLog) wave.getData(LOG_HINT);
                    if (log == null)
                        continue;

                    double dt = wave.getDistanceTraveled(time);

                    if(log.preciseIntersection != null) {
                        g.drawRadial(wave.getSource(), log.preciseIntersection.getStartingAngle(), dt, dt+12, Color.RED);
                        g.drawRadial(wave.getSource(), log.preciseIntersection.getEndingAngle(), dt, dt+12, Color.RED);
                    }

                    List<Knn.Entry<TimestampedGFRange>> found = (List) wave.getData(FOUND_HINT);

                    if(found == null)
                        continue;

                    IMea mea = log;

                    double angle = 0;
                    double ratio = R.DOUBLE_PI / WAVE_DIVISIONS;
                    double maxDanger = 0;

                    ArrayList<DangerPoint> dangerPoints = new ArrayList<>();

                    for (int i = 0; i < WAVE_DIVISIONS; i++) {
                        angle += ratio;
                        rsalesc.baf2.core.utils.geometry.Point hitPoint = wave.getSource().project(angle, wave.getDistanceTraveled(time));

                        double gf = mea.getUnconstrainedGfFromAngle(angle);

                        if (!R.nearOrBetween(-1, gf, +1))
                            continue;

                        double value = 0;

                        for(Knn.Entry<TimestampedGFRange> entry : found) {
                            if(R.nearOrBetween(entry.payload.min, gf, entry.payload.max))
                                value += entry.weight;
                        }

                        dangerPoints.add(new DangerPoint(hitPoint, value));
                        maxDanger = Math.max(maxDanger, value);
                    }

                    if (R.isNear(maxDanger, 0)) continue;

                    Collections.sort(dangerPoints);

                    int cnt = 0;
                    for (DangerPoint dangerPoint : dangerPoints) {
                        Color dangerColor = dangerPoint.getDanger() > -0.01
                                ? G.getWaveDangerColor(dangerPoint.getDanger() / maxDanger)
                                : Color.DARK_GRAY;

                        Point base = wave.getSource().project(wave.getAngle(dangerPoint), wave.getDistanceTraveled(time) - 10);
                        g.fillCircle(base, 2, dangerColor);
                    }
                }
            }
        };



        manager.add(KeyEvent.VK_R, "guns", painting);
    }

    public void checkBulletIntersection(RobotWave wave, EnemyRobot enemy, AngularRange intersection) {
        TargetingLog f = (TargetingLog) wave.getData(LOG_HINT);

        if(f == null)
            return;

        Benchmark.getInstance().start("GuessFactorGun.checkBulletIntersection()");

        EnemyLog enemyLog = EnemyTracker.getInstance().getLog(enemy);
//        RobotSnapshot interpolated = enemyLog.interpolate(getMediator().getTime() - 2);
        RobotSnapshot interpolated = enemy;
        if(interpolated == null)
            interpolated = enemy;

        f.hitAngle = Physics.absoluteBearing(wave.getSource(), interpolated.getPoint());
        f.hitDistance = wave.getSource().distance(interpolated.getPoint());

        f.preciseIntersection = intersection;
        if(f.preciseIntersection == null) {
            double hitAngle = Physics.hitAngle(f.hitDistance);
            f.preciseIntersection = new AngularRange(f.hitAngle, -hitAngle, +hitAngle);
        }

        BreakType type = wave instanceof TickWave ? BreakType.VIRTUAL_BREAK :
                (wave.hasHit() ? BreakType.BULLET_HIT : BreakType.BULLET_BREAK);

        lastGf = f.imprecise().getGfFromAngle(f.hitAngle);

        targeting.log(EnemyTracker.getInstance().getLog(enemy), f, f, type);
        Benchmark.getInstance().stop();
    }

    @Override
    public void onBulletWavePreciselyIntersects(BulletWave wave, EnemyRobot enemy, AngularRange intersection) {
        checkBulletIntersection(wave, enemy, intersection);
    }

    public void checkBulletFired(RobotWave wave) {
        if(lastTargetingLog != null && lastTargetingLog.time + 1 == getMediator().getTime()) {
            wave.setData(LOG_HINT, lastTargetingLog);

            if(targeting instanceof KnnGuessFactorTargeting)
                wave.setData(FOUND_HINT, ((KnnGuessFactorTargeting) targeting).lastFound);
        }
    }

    @Override
    public void onTickWaveFired(TickWave wave) {
        EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();
        if(enemies.length == 0)
            return;

        TargetingLog f = TargetingLog.getLog(enemies[0], getMediator(), wave.getPower(), false); // TODO: maybe rollback to usual log
        f.lastGf = lastGf;
        wave.setData(LOG_HINT, f);
    }

    @Override
    public void onTickWaveBreak(TickWave wave, EnemyRobot enemy) {

    }

    @Override
    public void onBulletWaveFired(BulletWave wave) {
        checkBulletFired(wave);
    }

    @Override
    public void onTickWavePreciselyIntersects(TickWave wave, EnemyRobot enemy, AngularRange intersection) {
        checkBulletIntersection(wave, enemy, intersection);
    }

    @Override
    public void onBulletWaveBreak(BulletWave wave, EnemyRobot enemy) {
    }

    @Override
    public void onBulletWaveHitEnemy(BulletWave wave, BulletHitEvent e) {

    }

    @Override
    public void onBulletWaveHitBullet(BulletWave wave, BulletHitBulletEvent e) {

    }

    @Override
    public void onBulletWavePass(BulletWave wave, EnemyRobot enemy) {

    }
}
