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

package rsalesc.mega.learning.recording;

import robocode.control.RobocodeEngine;
import robocode.control.events.BattleAdaptor;
import robocode.control.events.RoundEndedEvent;
import robocode.control.events.RoundStartedEvent;
import robocode.control.events.TurnEndedEvent;
import robocode.control.snapshot.IDebugProperty;
import robocode.control.snapshot.IRobotSnapshot;
import robocode.control.snapshot.ITurnSnapshot;
import robocode.control.snapshot.RobotState;
import rsalesc.baf2.core.utils.Pair;
import rsalesc.baf2.waves.BreakType;
import rsalesc.mega.utils.TargetingLog;
import rsalesc.runner.DuelBattleRunner;
import rsalesc.runner.RobocodeEngineProvider;
import rsalesc.runner.SerializeHelper;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by Roberto Sales on 01/10/17.
 */
public class DuelRecorderRunner {
    private final RobocodeEngineProvider engineProvider;
    private boolean logs = false;

    public DuelRecorderRunner() {
        this.engineProvider = new RobocodeEngineProvider();
    }

    public DuelRecorderRunner(String robocodePath) {
        this.engineProvider = new RobocodeEngineProvider(robocodePath);
    }

    public DuelRecorderRunner(RobocodeEngine engine) {
        this.engineProvider = new RobocodeEngineProvider(engine);
    }

    public void log() {
        logs = true;
    }

    public RobocodeEngine getEngine() {
        return engineProvider.getEngine();
    }

    public RobocodeEngineProvider getEngineProvider() {
        return engineProvider;
    }

    public DuelRecord run(String yourBot, String enemyBot, int rounds) {
        DuelBattleRunner runner = new DuelBattleRunner(yourBot, enemyBot, rounds);
        DuelObserver observer = new DuelObserver(yourBot, enemyBot);
        if(logs) observer.log();
        runner.run(getEngine(), observer);

        System.gc();

        return new DuelRecord(enemyBot, observer.getLogs(), rounds);
    }

    private static class DuelObserver extends BattleAdaptor {
        private final String myself;
        private final String enemyName;
        private ArrayList<Pair<TargetingLog, BreakType>> logs = new ArrayList<>();

        private boolean logFlag = false;

        public DuelObserver(String myself, String enemyName) {
            this.myself = myself;
            this.enemyName = enemyName;
        }

        public void log() {
            logFlag = true;
        }

        public ArrayList<Pair<TargetingLog, BreakType>> getLogs() {
            return logs;
        }

        @Override
        public void onRoundStarted(RoundStartedEvent event) {
            if(logFlag)
                System.out.println("Round " + event.getRound() + " against " + enemyName + " has just started.");
        }

        @Override
        public void onTurnEnded(TurnEndedEvent e) {
            ITurnSnapshot turnSnapshot = e.getTurnSnapshot();

            IRobotSnapshot[] robotSnapshots = turnSnapshot.getRobots();

            for(IRobotSnapshot robotSnapshot : robotSnapshots) {
                // RETHINK THAT
                if(robotSnapshot.getState() == RobotState.DEAD)
                    continue;

                if(robotSnapshot.getName().startsWith(myself)) {
                    IDebugProperty[] properties = robotSnapshot.getDebugProperties();

                    for(IDebugProperty property : properties) {
                        if(property.getKey().equals(GunRecorder.GUN_RECORD_HINT)) {
                            Optional data = SerializeHelper.convertFrom(property.getValue());
                            if(data.isPresent()) {
                                logs.addAll((ArrayList) data.get());
                            }
                        }
                    }
                }
            }
        }

        public String getEnemyName() {
            return enemyName;
        }
    }
}
