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
import robocode.Condition;
import robocode.RoundEndedEvent;
import robocode.util.Utils;
import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.listeners.RoundEndedListener;
import rsalesc.baf2.core.utils.BattleTime;
import rsalesc.baf2.core.utils.Physics;
import rsalesc.baf2.core.utils.PredictedHashMap;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.EnemyTracker;
import rsalesc.baf2.waves.BulletWave;
import rsalesc.baf2.waves.BulletWaveListener;

import java.util.*;

/**
 * Created by Roberto Sales on 20/09/17.
 */
public abstract class AutomaticGunArray extends AutomaticGun implements BulletWaveListener, RoundEndedListener {
    private ArrayList<AutomaticGun> guns = new ArrayList<>();
    private boolean log = false;
    private boolean scoring = true;

    private Comparator<GunScorePair> comparator = new Comparator<GunScorePair>() {
        @Override
        public int compare(GunScorePair o1, GunScorePair o2) {
            return (int) Math.signum(o2.score - o1.score);
        }
    };

    public ArrayList<AutomaticGun> getGuns() {
        return guns;
    }

    public void setComparator(Comparator<GunScorePair> comp) {
        comparator = comp;
    }

    public void addGun(AutomaticGun gun) {
        guns.add(gun);
    }

    public void log() {
        log = true;
    }

    public void setScoring(boolean flag) {
        scoring = flag;
    }

    public boolean isScoring() {
        return scoring;
    }

    public Condition getScoringCondition() {
        return new Condition() {
            @Override
            public boolean test() {
                return isScoring();
            }
        };
    }

    @Override
    public void init(RobotMediator mediator) {
        super.init(mediator);
        if(guns.isEmpty())
            throw new IllegalStateException();

        for(AutomaticGun gun : guns) {
            if(getPowerSelector() != null)
                gun.setPowerSelector(getPowerSelector());
            gun.init(mediator);
        }
    }

    public void generateAll(EnemyLog enemyLog, double power) {
        for(AutomaticGun gun : guns) {
            getGunGeneratedAngles(gun, enemyLog, power, getMediator().getBattleTime());
            getGunBestAngle(gun, enemyLog, power, getMediator().getBattleTime());
        }
    }

    @Override
    public GeneratedAngle[] generateFiringAngles(EnemyLog enemyLog, double power) {
        BattleTime time = getMediator().getBattleTime();
        generateAll(enemyLog, power);

        return getGunGeneratedAngles(getBestGun(enemyLog, power, time), enemyLog, power, time);
    }

    @Override
    public double pickBestAngle(EnemyLog enemyLog, GeneratedAngle[] angles, double power) {
        BattleTime time = getMediator().getBattleTime();
        generateAll(enemyLog, power);

        // safely assume that these angles were generated by the best gun.
        return getGunBestAngle(getBestGun(enemyLog, power, time), enemyLog, power, time);
    }

    public StorageNamespace getGunSharedNamespace(AutomaticGun gun) {
        return getStorageNamespace().concat(gun.getStorageNamespace());
    }

    public AutomaticGun getLastBestGun(EnemyLog enemyLog) {
        StorageNamespace ns = getStorageNamespace().namespace(enemyLog.getName());
        if(!ns.contains("last-best"))
            return null;

        return (AutomaticGun) ns.get("last-best");
    }

    public void updateLastBestGun(EnemyLog enemyLog, AutomaticGun gun) {
        getStorageNamespace().namespace(enemyLog.getName()).put("last-best", gun);
    }

    public AutomaticGun getBestGun(EnemyLog enemyLog, double power, BattleTime time) {
        AutomaticGun lastBest = getLastBestGun(enemyLog);

        ArrayList<GunScorePair> tmp = new ArrayList<>();
        for(AutomaticGun gun : guns)
            tmp.add(new GunScorePair(gun, getGunScore(gun, enemyLog)));

        tmp.sort(comparator);

        AutomaticGun bestGun = null;
        for(GunScorePair pair : tmp) {
            if(bestGun == null && getGunBestAngle(pair.gun, enemyLog, power, time) != null) {
                bestGun = pair.gun;
            }
        }

        if(bestGun == null)
            return guns.get(0);

        if(lastBest == null || !bestGun.getStorageNamespace().equals(lastBest.getStorageNamespace())) {
            if(log)
                System.out.println("Switching to " + bestGun.getGunName() + " (" +
                        R.formattedDouble(getGunScore(bestGun, enemyLog)) + ") against " + enemyLog.getName());
            updateLastBestGun(enemyLog, bestGun);
        }

        return bestGun;
    }

    public ScoreKeeper getGunScoreKeeper(AutomaticGun gun) {
        StorageNamespace ns = getGunSharedNamespace(gun);
        if(!ns.contains("keeper"))
            ns.put("keeper", new ScoreKeeper());

        return (ScoreKeeper) ns.get("keeper");
    }

    public double getGunScore(AutomaticGun gun, EnemyLog enemyLog) {
        return getGunScoreKeeper(gun).get(enemyLog);
    }

    public GunCache getGunCache(AutomaticGun gun) {
        StorageNamespace ns = getGunSharedNamespace(gun);
        if(!ns.contains("cache"))
            ns.put("cache", new GunCache());

        return (GunCache) ns.get("cache");
    }

    public GeneratedAngle[] getGunGeneratedAngles(AutomaticGun gun, EnemyLog enemyLog, double power, BattleTime time) {
        GunCache cache = getGunCache(gun);
        GeneratedAngle[] angles = cache.getGeneratedAngles(enemyLog, power, time);
        if(angles == null && time.equals(getMediator().getBattleTime())) {
            angles = gun.generateFiringAngles(enemyLog, power);
            cache.updateAngles(enemyLog, angles, power, time);
        }

        return angles;
    }

    public Double getGunBestAngle(AutomaticGun gun, EnemyLog enemyLog, double power, BattleTime time) {
        GunCache cache = getGunCache(gun);
        Double best = cache.getBestAngle(enemyLog, power, getMediator().getBattleTime());
        if(best == null) {
            GeneratedAngle[] angles = getGunGeneratedAngles(gun, enemyLog, power, time);
            if(angles != null && angles.length > 0) {
                best = gun.pickBestAngle(enemyLog, angles, power);
                cache.updateBestAngle(enemyLog, best, power, time);
            }
        }

        return best;
    }

    @Override
    public void onBulletWaveFired(BulletWave wave) {
        if(isScoring()) {
            BattleTime time = getMediator().getBattleTime();

            for (AutomaticGun gun : guns) {
                for (String enemyName : getGunCache(gun).getEnemies(time.prev())) {
                    EnemyLog enemyLog = EnemyTracker.getInstance().getLog(enemyName);
                    Double best = getGunBestAngle(gun, enemyLog, wave.getPower(), time.prev());
                    if (best != null)
                        wave.setData(getGunWaveHint(gun, enemyLog), best);
                }
            }
        }
    }

    @Override
    public void onBulletWaveBreak(BulletWave wave, EnemyRobot enemy) {
        if(isScoring()) {
            double bandwidth = Physics.hitAngle(enemy.getDistance()) / 2;
            double angle = Physics.absoluteBearing(wave.getSource(), enemy.getPoint());
            EnemyLog enemyLog = EnemyTracker.getInstance().getLog(enemy);

            for (AutomaticGun gun : guns) {
                Double best = (Double) wave.getData(getGunWaveHint(gun, enemyLog));
                if (best == null)
                    continue;

                double x = Utils.normalRelativeAngle(angle - best) / bandwidth;
                if (Math.abs(x) < 1) {
                    getGunScoreKeeper(gun).log(enemyLog, R.gaussKernel(x));
                }
            }
        }
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

    public String getGunWaveHint(AutomaticGun gun, EnemyLog enemyLog) {
        return getGunSharedNamespace(gun).namespace(enemyLog.getName()).getPath() + "fired-angle";
    }

    @Override
    public void onRoundEnded(RoundEndedEvent e) {
        if(log) {
            for(AutomaticGun gun : guns) {
                ScoreKeeper keeper = getGunScoreKeeper(gun);
                System.out.println("Scores of gun " + gun.getGunName());

                for(String name : keeper.score.keySet()) {
                    System.out.println(name + ": " + R.formattedDouble(keeper.get(name)));
                }
            }
        }
    }

    private static class ScoreKeeper {
        HashMap<String, Double> score = new PredictedHashMap<>(15);

        public double get(String name) {
            return score.getOrDefault(name, 0.0);
        }

        public double get(EnemyLog log) {
            return get(log.getName());
        }

        public double get(EnemyRobot log) {
            return get(log.getName());
        }

        public void log(String name, double x) {
            score.put(name, get(name) + x);
        }

        public void log(EnemyLog log, double x) {
            log(log.getName(), x);
        }
    }

    private static class GunCache {
        TreeMap<String, GeneratedAngle[]> generatedAngles = new TreeMap<>();
        TreeMap<String, Double> bestAngle = new TreeMap<>();
        BattleTime lastTime;
        double lastPower;

        public GeneratedAngle[] getGeneratedAngles(String name, double power, BattleTime time) {
            if(!time.equals(lastTime) || !R.isNear(power, lastPower))
                return null;

            return generatedAngles.get(name);
        }

        public GeneratedAngle[] getGeneratedAngles(EnemyLog enemyLog, double power, BattleTime time) {
            return getGeneratedAngles(enemyLog.getName(), power, time);
        }

        public Double getBestAngle(String name, double power, BattleTime time) {
            if(!time.equals(lastTime) || !R.isNear(power, lastPower))
                return null;

            return bestAngle.get(name);
        }

        public Double getBestAngle(EnemyLog enemyLog, double power, BattleTime time) {
            return getBestAngle(enemyLog.getName(), power, time);
        }

        public String[] getEnemies(BattleTime time) {
            if(!time.equals(lastTime))
                return new String[0];
            return generatedAngles.keySet().toArray(new String[0]);
        }

        public void updateAngles(String name, GeneratedAngle[] angles, double power, BattleTime time) {
            if(!time.equals(lastTime) || !R.isNear(lastPower, power)) {
                generatedAngles.clear();
                bestAngle.clear();
            }

            lastTime = time;
            lastPower = power;
            generatedAngles.put(name, angles);
        }

        public void updateAngles(EnemyLog enemyLog, GeneratedAngle[] angles, double power, BattleTime time) {
            updateAngles(enemyLog.getName(), angles, power, time);
        }

        public void updateBestAngle(String name, double best, double power, BattleTime time) {
            if(!time.equals(lastTime) || !R.isNear(lastPower, power)) {
                generatedAngles.clear();
                bestAngle.clear();
            }

            lastTime = time;
            lastPower = power;
            bestAngle.put(name, best);
        }

        public void updateBestAngle(EnemyLog enemyLog, double best, double power, BattleTime time) {
            updateBestAngle(enemyLog.getName(), best, power, time);
        }
    }

    public static class GunScorePair {
        public final AutomaticGun gun;
        public final double score;

        public GunScorePair(AutomaticGun gun, double score) {
            this.gun = gun;
            this.score = score;
        }
    }
}
