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

package rsalesc.mega.utils;

import org.omg.CORBA.ObjectHelper;
import robocode.*;
import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.StoreComponent;
import rsalesc.baf2.core.listeners.BulletListener;
import rsalesc.baf2.core.listeners.HitListener;
import rsalesc.baf2.core.listeners.RoundEndedListener;
import rsalesc.baf2.core.listeners.StatusListener;
import rsalesc.baf2.core.utils.R;
import rsalesc.baf2.tracking.*;
import rsalesc.baf2.waves.EnemyWave;
import rsalesc.baf2.waves.EnemyWaveListener;
import rsalesc.runner.SerializeHelper;

/**
 * Created by Roberto Sales on 13/09/17.
 */
public class StatTracker extends StoreComponent implements StatusListener, BulletListener,
        EnemyWaveListener, RoundEndedListener {
    private static final int MEETING_THRESHOLD = 35;
    private static final StatTracker SINGLETON = new StatTracker();

    private boolean log = false;

    public void log() {
        this.log = true;
    }

    private StatTracker() {

    }

    public static StatTracker getInstance() {
        return SINGLETON;
    }

    @Override
    public void afterRun() {
        getMediator().setDebugProperty("duel-statdata", null);
        getMediator().setDebugProperty("melee-statdata", null);
    }

    @Override
    public StorageNamespace getStorageNamespace() {
        return getGlobalStorage().namespace("stat-tracker");
    }

    public StatData getDuelStatData() {
        if(!getStorageNamespace().contains("duel-data")) {
            StatData data = new StatData();
            getStorageNamespace().put("duel-data", data);
        }

        return (StatData) getStorageNamespace().get("duel-data");
    }

    public StatData getMeleeStatData() {
        if(!getStorageNamespace().contains("melee-data")) {
            StatData data = new StatData();
            getStorageNamespace().put("melee-data", data);
        }

        return (StatData) getStorageNamespace().get("melee-data");
    }

    public StatData getCurrentStatData() {
        return getMediator().getOthers() <= 1 ? getDuelStatData() : getMeleeStatData();
    }

    @Override
    public void onStatus(StatusEvent e) {
        getMeleeStatData().onStatus(e);
        getDuelStatData().onStatus(e);

        // log from past round
        int others = getMediator().getOthers();
        for(EnemyRobot enemy : EnemyTracker.getInstance().getLatest()) {
            EnemyLog enemyLog = EnemyTracker.getInstance().getLog(enemy.getName());
            EnemyRobot pastEnemy = enemyLog.atMostAt(getMediator().getTime() - MEETING_THRESHOLD);

            if(pastEnemy == null)
                continue;

            MyRobot pastMe = MyLog.getInstance().exactlyAt(pastEnemy.getTime());

            if(pastMe == null)
                continue;

            if(pastEnemy.getTime() <= getMediator().getTime() - MEETING_THRESHOLD
                    && pastMe.getOthers() == others) {

                getMeleeStatData().logMeeting(enemy.getName(), others);
                getDuelStatData().logMeeting(enemy.getName(), others);
            }
        }
    }

    @Override
    public void onBulletHitBullet(BulletHitBulletEvent e) {
        // not considered at all, rethink that
    }

    @Override
    public void onBulletHit(BulletHitEvent e) {
        getCurrentStatData().logShotInflicted(e.getName(), Rules.getBulletDamage(e.getBullet().getPower()));
    }

    @Override
    public void onBulletMissed(BulletMissedEvent e) {
        if(getMediator().getOthers() <= 1) {
            EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();
            if(enemies.length > 0) {
                EnemyRobot enemy = enemies[0];

                getDuelStatData().logShotMissed(enemy.getName(), Rules.getBulletDamage(e.getBullet().getPower()));
            }
        } else {
            getDuelStatData().logShotMissed(Rules.getBulletDamage(e.getBullet().getPower()));
        }
    }

    @Override
    public void onEnemyWaveFired(EnemyWave wave) {

    }

    @Override
    public void onEnemyWaveBreak(EnemyWave wave, MyRobot me) {

    }

    @Override
    public void onEnemyWaveHitMe(EnemyWave wave, HitByBulletEvent e) {
        getCurrentStatData().logShotReceived(e.getBullet().getName(), Rules.getBulletDamage(e.getBullet().getPower()));
    }

    @Override
    public void onEnemyWaveHitBullet(EnemyWave wave, BulletHitBulletEvent e) {

    }

    @Override
    public void onEnemyWavePass(EnemyWave wave, MyRobot me) {
        if(!wave.hasAnyHit()) {
            getCurrentStatData().logShotDodged(wave.getEnemy().getName(), Rules.getBulletDamage(wave.getPower()));
        }
    }

    @Override
    public void onRoundEnded(RoundEndedEvent e) {
        getMediator().setDebugProperty("duel-statdata", SerializeHelper.convertToString(getDuelStatData()).get());
        getMediator().setDebugProperty("melee-statdata", SerializeHelper.convertToString(getMeleeStatData()).get());

        if(log) {
            StatData data = getDuelStatData();
            for(String name : data.getEnemies()) {
                System.out.println(name + ": "
                    + R.formattedPercentage(data.getEnemyWeightedHitPercentage(name)) + " whit movement, "
                    + R.formattedPercentage(data.getWeightedHitPercentage(name)) + " whit gun");
            }
        }
    }
}
