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

import rsalesc.baf2.core.utils.BattleTime;
import robocode.StatusEvent;
import rsalesc.baf2.core.utils.Pair;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Roberto Sales on 13/09/17.
 */
public class StatData implements Serializable {
    private final static long serialVersionUID = 901201892819210L;

    private BattleTime time;

    private double totalDamageReceived = 0;
    private double totalDamageInflicted = 0;
    private double totalDamageFired = 0;

    private int totalShotsReceived = 0;
    private int totalShotsInflicted = 0;
    private int totalShotsFired = 0;

    private HashMap<String, Double> damageReceived = new HashMap<>();
    private HashMap<String, Double> damageInflicted = new HashMap<>();
    private HashMap<String, Double> damageFired = new HashMap<>();
    private HashMap<String, Double> damageFelt = new HashMap<>();

    private HashMap<String, Integer> shotsReceived = new HashMap<>();
    private HashMap<String, Integer> shotsInflicted = new HashMap<>();
    private HashMap<String, Integer> shotsFired = new HashMap<>();
    private HashMap<String, Integer> shotsFelt = new HashMap<>();

    private HashMap<Pair<String, Integer>, TreeSet<Integer>> meetings = new HashMap<>();

    public void onStatus(StatusEvent e) {
        time = new BattleTime(e.getTime(), e.getStatus().getRoundNum());
    }

    public void logMeeting(String name, int others) {
        Pair<String, Integer> pair = new rsalesc.baf2.core.utils.Pair<>(name, others);
        if(!meetings.containsKey(pair))
            meetings.put(pair, new TreeSet<Integer>());
        meetings.get(pair).add(time.getRound());
    }

    public double getTotalDamageReceived() {
        return totalDamageReceived;
    }

    public double getTotalDamageInflicted() {
        return totalDamageInflicted;
    }

    public double getDamageReceived(String name) {
        return damageReceived.getOrDefault(name, 0.0);
    }

    public double getDamageInflicted(String name) {
        return damageInflicted.getOrDefault(name, 0.0);
    }

    public double getDamageFired(String name)  { return damageFired.getOrDefault(name, 0.0); }

    public double getDamageFelt(String name) {
        return damageFelt.getOrDefault(name, 0.0);
    }

    public int getShotsReceived(String name) {
        return shotsReceived.getOrDefault(name, 0);
    }

    public int getShotsInflicted(String name) {
        return shotsInflicted.getOrDefault(name, 0);
    }

    public int getShotsFired(String name)  { return shotsFired.getOrDefault(name, 0); }

    public int getShotsFelt(String name) {
        return shotsFelt.getOrDefault(name, 0);
    }

    public void logShotReceived(String name, double x) {
        totalDamageReceived += x;
        totalShotsReceived++;

        logShotDodged(name, x);
        damageReceived.put(name, getDamageReceived(name) + x);
        shotsReceived.put(name, getShotsReceived(name) + 1);
    }

    public void logShotInflicted(String name, double x) {
        totalDamageInflicted += x;
        totalShotsInflicted++;

        logShotMissed(name, x);
        damageInflicted.put(name, getDamageInflicted(name) + x);
        shotsInflicted.put(name, getShotsInflicted(name) + 1);
    }

    public void logShotMissed(double x) {
        totalShotsFired++;
        totalDamageFired += x;
    }

    public void logShotMissed(String name, double x) {
        logShotMissed(x);
        damageFired.put(name, getDamageFired(name) + x);
        shotsFired.put(name, getShotsFired(name) + 1);
    }

    public void logShotDodged(String name, double x) {
        damageFelt.put(name, getDamageFelt(name) + x);
        shotsFelt.put(name, getShotsFelt(name) + 1);
    }

    public int getRound() {
        return time.getRound();
    }

    public long getTime() {
        return time.getTime();
    }

    public BattleTime getBattleTime() {
        return time;
    }

    public double getTotalDamageFired() {
        return totalDamageFired;
    }

    public int getTotalShotsReceived() {
        return totalShotsReceived;
    }

    public int getTotalShotsInflicted() {
        return totalShotsInflicted;
    }

    public int getTotalShotsFired() {
        return totalShotsFired;
    }

    public double getEnemyWeightedHitPercentage(String name) {
        return getDamageReceived(name) / (getDamageFelt(name) + 1e-12);
    }

    public double getEnemyHitPercentage(String name) {
        return (double) getShotsReceived(name) / (getShotsFelt(name) + 1e-12);
    }

    public double getWeightedHitPercentage(String name) {
        return getDamageInflicted(name) / (getDamageFired(name) + 1e-12);
    }

    public double getHitPercentage(String name) {
        return (double) getShotsInflicted(name) / (getShotsFired(name) + 1e-12);
    }

    public int getMeetings(String name, int L, int R) {
        HashSet<Integer> rounds = new HashSet<>();
        for(int i = L; i <= R; i++) {
            Pair<String, Integer> pair = new Pair<>(name, i);
            rounds.addAll(meetings.getOrDefault(pair, new TreeSet<Integer>()));
        }

        return rounds.size();
    }

    public int getMeetings(String name, int cnt) {
        return getMeetings(name, cnt, cnt);
    }

    public int getMeetings(String name) {
        return getMeetings(name, 1);
    }

    public List<String> getEnemies() {
        TreeSet<String> met = new TreeSet<>();

        for(Map.Entry<Pair<String, Integer>, TreeSet<Integer>> entry : meetings.entrySet()) {
            met.add(entry.getKey().first);
        }

        ArrayList<String> res = new ArrayList<>();
        res.addAll(met);

        return res;
    }
}
