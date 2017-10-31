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

package rsalesc.mega.movement;

import robocode.BulletHitBulletEvent;
import robocode.HitByBulletEvent;
import robocode.Rules;
import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.StoreComponent;
import rsalesc.baf2.core.controllers.Controller;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.core.utils.geometry.AngularRange;
import rsalesc.baf2.core.utils.geometry.AxisRectangle;
import rsalesc.baf2.core.utils.geometry.Point;
import rsalesc.baf2.core.utils.geometry.Range;
import rsalesc.baf2.painting.G;
import rsalesc.baf2.painting.PaintManager;
import rsalesc.baf2.painting.Painting;
import rsalesc.baf2.tracking.*;
import rsalesc.baf2.waves.*;
import rsalesc.mega.movement.distancing.FallbackSurfingDistancer;
import rsalesc.baf2.predictor.PredictedPoint;
import rsalesc.baf2.predictor.WallSmoothing;
import rsalesc.mega.utils.IMea;
import rsalesc.mega.utils.NamedStatData;
import rsalesc.mega.utils.StatTracker;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.mega.utils.stats.GaussianKernelDensity;
import rsalesc.mega.utils.stats.GuessFactorStats;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Roberto Sales on 12/09/17.
 */
public abstract class BaseSurfing extends StoreComponent implements EnemyWaveListener, EnemyWavePreciseListener {
    protected static final boolean PRECISE = true;
    protected static final String LOG_HINT = "surfing-log";
    protected static final int WALL_STICK = 160;
    private int breaks = 0;

    private final Surfer surfer;
    private final WaveManager manager;
    private final StatTracker statTracker;

    public BaseSurfing(Surfer surfer, WaveManager manager) {
        this.surfer = surfer;
        this.manager = manager;
        this.statTracker = StatTracker.getInstance();
    }

    public EnemyLog getEnemyLog() {
        EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();
        if (enemies.length == 0) {
            enemies = EnemyTracker.getInstance().getLatestDeadOrAlive();
            if(enemies.length == 0)
                return null;
        }

        return EnemyTracker.getInstance().getLog(enemies[0]);
    }

    public EnemyRobot getEnemy() {
        if (getEnemyLog() == null)
            return null;
        return getEnemyLog().getLatest();
    }

    public IMea getMea(TargetingLog f) {
        return PRECISE ? f : f.imprecise();
    }

    public static GuessFactorStats getFallbackStats(double distance, double velocity) {
        GuessFactorStats stats = new GuessFactorStats(new GaussianKernelDensity());
        double hitAngle = Physics.hitAngle(distance);
        double mea = Physics.maxEscapeAngle(velocity);

        stats.logGuessFactor(0, 1, hitAngle / mea);
        stats.logGuessFactor(PRECISE ? 0.9 : 0.85, 0.6, hitAngle / mea);

        return stats;
    }

    @Override
    public StorageNamespace getStorageNamespace() {
        return getGlobalStorage().namespace("base-surfing");
    }

    public Surfer getSurfer() {
        return surfer;
    }

    public WaveManager getManager() {
        return manager;
    }

    public StatTracker getStatTracker() { return statTracker; }

    @Override
    public void onEnemyWaveFired(EnemyWave wave) {
        MyRobot decisionMe = MyLog.getInstance().atLeastAt(wave.getTime() - 1);

        TargetingLog f = TargetingLog.getEnemyLog(decisionMe, wave.getSource(),
                getMediator(), Physics.bulletPower(wave.getVelocity()));

        wave.setData(LOG_HINT, f);
    }

    @Override
    public void onEnemyWaveBreak(EnemyWave wave, MyRobot me) {
        breaks++;
    }


    @Override
    public void onEnemyWavePreciselyIntersects(EnemyWave wave, MyRobot me, AngularRange intersection) {
        if(wave.hasAnyHit())
            return;

        TargetingLog f = (TargetingLog) wave.getData(LOG_HINT);
        if (f == null)
            return;

        f.hitAngle = intersection.getAngle(intersection.getCenter());
        f.hitDistance = me.getPoint().distance(wave.getSource());

        double hitAngle = Physics.hitAngle(f.hitDistance) / 2;
        f.preciseIntersection = intersection == null ? new AngularRange(f.hitAngle, -hitAngle, +hitAngle) : intersection;

        surfer.log(EnemyTracker.getInstance().getLog(wave.getEnemy().getName()), f, getMea(f), BreakType.BULLET_BREAK);
    }

    @Override
    public void onEnemyWaveHitMe(EnemyWave wave, HitByBulletEvent e) {
        double angle = e.getBullet().getHeadingRadians();
        TargetingLog f = (TargetingLog) wave.getData(LOG_HINT);
        if (f == null)
            return;

        breaks++;

        f.hitAngle = angle;
        f.hitDistance = getMediator().getPoint().distance(wave.getSource());

        double hitAngle = Physics.hitAngle(f.hitDistance) / 2;
        f.preciseIntersection = new AngularRange(f.hitAngle, -hitAngle, +hitAngle);

        surfer.log(EnemyTracker.getInstance().getLog(wave.getEnemy().getName()), f, getMea(f), BreakType.BULLET_HIT);
    }

    @Override
    public void onEnemyWaveHitBullet(EnemyWave wave, BulletHitBulletEvent e) {
        double angle = e.getHitBullet().getHeadingRadians();
        TargetingLog f = (TargetingLog) wave.getData(LOG_HINT);
        if (f == null)
            return;

        breaks++;

        f.hitAngle = angle;
        f.hitDistance = getMediator().getPoint().distance(wave.getSource());

        double hitAngle = Physics.hitAngle(f.hitDistance) / 2;
        f.preciseIntersection = new AngularRange(f.hitAngle, -hitAngle, +hitAngle);

        surfer.log(EnemyTracker.getInstance().getLog(e.getHitBullet().getName()), f, getMea(f), BreakType.BULLET_HIT);
    }

    protected double getDanger(EnemyWave wave, GuessFactorStats stats, AngularRange intersection, PredictedPoint pass, boolean precise) {
        TargetingLog log = (TargetingLog) wave.getData(LOG_HINT);
        if (log == null)
            throw new IllegalStateException();

        IMea mea = precise ? log : log.imprecise();

        if (intersection == null) {
            double distance = wave.getSource().distance(pass);
            double passBearing = Physics.absoluteBearing(wave.getSource(), pass);
            double width = Physics.hitAngle(distance) / 2;
            intersection = new AngularRange(passBearing, -width, width);
        }

        double gfLow = mea.getGfFromAngle(intersection.getStartingAngle());
        double gfHigh = mea.getGfFromAngle(intersection.getEndingAngle());

        Range gfRange = new Range();
        gfRange.push(gfLow);
        gfRange.push(gfHigh);

        double value = 0;

        int iBucket = stats.getBucket(gfRange.min);
        int jBucket = stats.getBucket(gfRange.max);

//        if(stats.getGuessFactor(iBucket) < gfRange.min) iBucket++;
//        if(stats.getGuessFactor(jBucket) > gfRange.max) jBucket--;

        for (int i = iBucket; i <= jBucket; i++) {
            if(wave.isShadowed(mea.getAngle(stats.getGuessFactor(i))))
                continue;

            value += stats.getValueFromBucket(i);
        }

        if (gfRange.getLength() > R.EPSILON) {
            value /= Math.max(jBucket - iBucket + 1, 1);
            value *= Math.abs(mea.getOffset(gfRange.max) - mea.getOffset(gfRange.min));
        }

        double shadowFactor = 1.0 - wave.getShadowFactor(intersection);
        value *= shadowFactor;

        return value;
    }

    protected long getCacheIndex(Wave wave) {
        return wave.getTime() * (long) 1e9 + breaks;
    }

    protected void fallback(Controller controller, EnemyRobot enemy) {
        MyRobot my = MyLog.getInstance().getLatest();

        double distance = my.getPoint().distance(enemy.getPoint());
        double absBearing = Physics.absoluteBearing(enemy.getPoint(), my.getPoint());
        double perp = new FallbackSurfingDistancer().getPerpendiculator(distance);

        AxisRectangle shrinkedField = getMediator().getBattleField().shrink(18, 18);

        double clockwiseAngle = WallSmoothing.smooth(
                shrinkedField,
                WALL_STICK,
                my.getPoint(),
                R.normalAbsoluteAngle(absBearing + perp),
                +1
        );
        double counterAngle = WallSmoothing.smooth(
                shrinkedField,
                WALL_STICK,
                my.getPoint(),
                R.normalAbsoluteAngle(absBearing - perp),
                -1
        );

        controller.setMaxVelocity(Rules.MAX_VELOCITY);

        if (Math.abs(R.normalRelativeAngle(clockwiseAngle - absBearing))
                < Math.abs(R.normalRelativeAngle(counterAngle - absBearing))) {
            controller.setBackAsFront(clockwiseAngle);
        } else {
            controller.setBackAsFront(counterAngle);
        }
    }

    public NamedStatData getViewCondition(String name) {
        return new NamedStatData(getStatTracker().getCurrentStatData(), name);
    }

    @Override
    public void setupPaintings(PaintManager manager) {
        Painting painting = new Painting() {
            @Override
            public void paint(G g) {
                final int WAVE_DIVISIONS = 200;
                long time = getMediator().getTime();

                for (EnemyWave wave : getManager().getWaves()) {
                    if(wave.everyoneInside(getMediator()))
                        continue;

                    TargetingLog log = (TargetingLog) wave.getData(LOG_HINT);
                    if (log == null)
                        continue;

                    double dt = wave.getDistanceTraveled(time);

                    if(log.preciseIntersection != null) {
                        g.drawRadial(wave.getSource(), log.preciseIntersection.getStartingAngle(), dt, dt + 8);
                        g.drawRadial(wave.getSource(), log.preciseIntersection.getEndingAngle(), dt, dt+8);
                    }

                    EnemyLog enemyLog = EnemyTracker.getInstance().getLog(wave.getEnemy());

                    IMea mea = getMea(log);

                    NamedStatData o = getViewCondition(enemyLog.getName());
                    GuessFactorStats st =
                            getSurfer().getStats(enemyLog, log, mea, getCacheIndex(wave), o);

                    Point zeroPoint = wave.getSource().project(mea.getZeroGf(), wave.getDistanceTraveled(time));

                    g.drawLine(wave.getSource(), zeroPoint, Color.DARK_GRAY);

                    double angle = 0;
                    double ratio = R.DOUBLE_PI / WAVE_DIVISIONS;
                    double maxDanger = 0;

                    ArrayList<DangerPoint> dangerPoints = new ArrayList<>();

                    for (int i = 0; i < WAVE_DIVISIONS; i++) {
                        angle += ratio;
                        Point hitPoint = wave.getSource().project(angle, wave.getDistanceTraveled(time));

                        double gf = mea.getUnconstrainedGfFromAngle(angle);

                        if (!R.nearOrBetween(-1, gf, +1))
                            continue;

                        double value = st.getValue(gf);
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

        manager.add(KeyEvent.VK_S, "surfing", painting, true);
    }
}
