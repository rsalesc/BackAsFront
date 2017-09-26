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

package rsalesc.runner;

import net.sf.robocode.battle.Battle;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;
import robocode.BattleResults;
import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;
import robocode.control.events.*;
import robocode.control.snapshot.IDebugProperty;
import robocode.control.snapshot.IRobotSnapshot;
import robocode.control.snapshot.ITurnSnapshot;
import robocode.control.snapshot.RobotState;
import rsalesc.baf2.core.utils.R;
import rsalesc.mega.utils.StatData;

import java.util.*;

/**
 * Created by Roberto Sales on 14/09/17.
 */
public class BattleRunner {

    public static void main(String[] args) {
        runDuel();
    }

    public static void runMelee() {
        runBattle(35, new String[]{
                "rsalesc.melee.Monk*",
                "rz.HawkOnFire",
                "positive.Portia",
                "tzu.TheArtOfWar",
                "dsekercioglu.TomahawkM 1.1",
                "kawigi.micro.Shiz"
        }, true);
    }

    public static void runDuel() {
        runBattle(35, new String[]{
                "rsalesc.mega.Knight*",
                "wiki.mc2k7.Splinter MC2K7"
        }, false);
    }

    public static void runBattle(int rounds, String[] robots, boolean melee) {
        // Disable log messages from Robocode
        RobocodeEngine.setLogMessagesEnabled(false);

        // Create the RobocodeEngine
        //   RobocodeEngine engine = new RobocodeEngine(); // Run from current working directory
        RobocodeEngine engine = new RobocodeEngine(new java.io.File("/home/rsalesc/robocode")); // Run from C:/Robocode

        // Add our own battle listener to the RobocodeEngine
        engine.addBattleListener(new MonkObserver(robots[0]));

        // Show the Robocode battle view
        engine.setVisible(false);

        // Setup the battle specification

        BattlefieldSpecification battlefield = melee ? new BattlefieldSpecification(1000, 1000) // 800x600
            : new BattlefieldSpecification(800, 600);

        List<String> robotNames = Arrays.asList(robots);
        RobotSpecification[] selectedRobots = engine.getLocalRepository(String.join(",", robotNames));

        BattleSpecification battleSpec = new BattleSpecification(rounds, battlefield, selectedRobots);

        // Run our specified battle and let it run till it is over
        engine.runBattle(battleSpec, true); // waits till the battle finishes

        // Cleanup our RobocodeEngine
        engine.close();

        // Make sure that the Java VM is shut down properly
//        System.exit(0);
    }
}

class MonkObserver extends BattleAdaptor {
    private String name = "rsalesc.melee.Monk";
    private ArrayList<StatData> meleeData = new ArrayList<>();
    private ArrayList<StatData> duelData = new ArrayList<>();

    public MonkObserver() {}

    public MonkObserver(String name) {
        this.name = name;
    }

    @Override
    public void onRoundEnded(RoundEndedEvent event) {
        System.out.println("=== Round " + event.getRound() + " ended with " + event.getTurns() + " turns");

        if(duelData.size() > 0 && R.getLast(duelData) != null) {
            System.out.println("- Duel Data");
            StatData data = R.getLast(duelData);

            for(String name : data.getEnemies()) {
                System.out.println(name + ":\t\t\t\t\t"
                        + R.formattedPercentage(data.getEnemyWeightedHitPercentage(name)) + " whit");
            }
        }

        if(meleeData.size() > 0 && R.getLast(meleeData) != null) {
            System.out.println("- Melee Data");
            StatData data = R.getLast(meleeData);

            for(String name : data.getEnemies()) {
                System.out.println(name + ":\t\t\t\t\t"
                        + R.formattedDouble(data.getDamageReceived(name)) + " dmg received, "
                        + R.formattedDouble(data.getDamageInflicted(name)) + " dmg inflicted");
            }

            System.out.println("Total damage received: " + R.formattedDouble(data.getTotalDamageReceived()) + " dmg");
            System.out.println("Total damage inflicted: " + R.formattedDouble(data.getTotalDamageInflicted()) + " dmg");
        }

        System.out.println();
    }

    @Override
    public void onBattleCompleted(BattleCompletedEvent event) {
//        showChart(meleeData);
//        showChart(duelData);

        System.out.println();
        BattleResults[] results = event.getSortedResults();

        double sum = 0;
        for(BattleResults result : results) {
            sum += result.getScore();
        }

        for(BattleResults result : results) {
            double aps = (double) result.getScore() / (sum + 1e-12);
            System.out.println(result.getTeamLeaderName() + "\t\t\t\t\t\t"
                    + result.getScore() + " (" + R.formattedPercentage(aps) + ")");
        }
    }

    void showChart(ArrayList<StatData> data) {

        XYChart chart = new XYChart(500, 400);

        HashSet<String> enemies = new HashSet<>();
        for(StatData cur : data) {
            enemies.addAll(cur.getEnemies());
        }

        HashMap<String, double[]> x = new HashMap<>();
        HashMap<String, double[]> y = new HashMap<>();
        for(String name : enemies) {
            double[] xs = new double[data.size()];
            for(int i = 0; i < xs.length; i++) xs[i] = i;
            x.put(name, xs);
            y.put(name, new double[data.size()]);
        }

        for(int i = 0; i < data.size(); i++) {
            for(String name : enemies) {
                y.get(name)[i] = data.get(i).getEnemyWeightedHitPercentage(name);
            }
        }

        for(String name : enemies) {
            XYSeries series = chart.addSeries(name, x.get(name), y.get(name));
            series.setMarker(SeriesMarkers.NONE);
        }

        new SwingWrapper<>(chart).displayChart();
    }

    @Override
    public void onTurnEnded(TurnEndedEvent event) {
        ITurnSnapshot snapshot = event.getTurnSnapshot();

        IRobotSnapshot[] rs = snapshot.getRobots();

        for(IRobotSnapshot r : rs) {
            if(r.getState() == RobotState.DEAD)
                continue;

            if(r.getName().startsWith(name)) {
                IDebugProperty[] properties = r.getDebugProperties();

                for(IDebugProperty property : properties) {
                    if(property.getKey().equals("melee-statdata")) {
                        Optional data = SerializeHelper.convertFrom(property.getValue());
                        if(data.isPresent()) {
                            meleeData.add((StatData) data.get());
                        }
                    } else if(property.getKey().equals("duel-statdata")) {
                        Optional data = SerializeHelper.convertFrom(property.getValue());
                        if(data.isPresent()) {
                            duelData.add((StatData) data.get());
                        }
                    }
                }
            }
        }
    }
}
