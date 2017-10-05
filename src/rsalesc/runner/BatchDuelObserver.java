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

import robocode.BattleResults;
import robocode.control.events.BattleAdaptor;
import robocode.control.events.BattleCompletedEvent;
import robocode.control.events.RoundEndedEvent;
import robocode.control.events.TurnEndedEvent;
import robocode.control.snapshot.IDebugProperty;
import robocode.control.snapshot.IRobotSnapshot;
import robocode.control.snapshot.ITurnSnapshot;
import robocode.control.snapshot.RobotState;
import rsalesc.mega.utils.StatData;

import java.util.Optional;

/**
 * Created by Roberto Sales on 05/10/17.
 */
public class BatchDuelObserver extends BattleAdaptor {
    private StatData data;
    private DuelResult duelResult;
    private String myself;

    public BatchDuelObserver(String myself) {
        this.myself = myself;
    }

    @Override
    public void onTurnEnded(TurnEndedEvent event) {
        ITurnSnapshot snapshot = event.getTurnSnapshot();

        IRobotSnapshot[] rs = snapshot.getRobots();

        for(IRobotSnapshot r : rs) {
            if(r.getState() == RobotState.DEAD)
                continue;

            if(r.getName().startsWith(myself)) {
                IDebugProperty[] properties = r.getDebugProperties();

                for(IDebugProperty property : properties) {
                    if(property.getKey().equals("duel-statdata") && property.getValue() != null) {
                        Optional localData = SerializeHelper.convertFrom(property.getValue());
                        if(localData.isPresent()) {
                            data = (StatData) localData.get();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onBattleCompleted(BattleCompletedEvent event) {
        BattleResults[] results = event.getIndexedResults();

        double bulletSum = 0;
        double survivalSum = 0;
        double scoreSum = 0;
        String enemy = null;

        for(BattleResults result : results) {
            if(!result.getTeamLeaderName().startsWith(myself))
                enemy = result.getTeamLeaderName();

            bulletSum += result.getBulletDamage();
            survivalSum += result.getSurvival();
            scoreSum += result.getScore();
        }

        if(enemy == null)
            throw new IllegalStateException();

        for(BattleResults result : results) {
            if(result.getTeamLeaderName().startsWith(myself)) {
                duelResult = new DuelResultBuilder()
                        .setMyself(myself)
                        .setEnemy(enemy)
                        .setBulletDamage(result.getBulletDamage())
                        .setBulletDamageSum(bulletSum)
                        .setScore(result.getScore())
                        .setScoreSum(scoreSum)
                        .setSurvival(result.getSurvival())
                        .setSurvivalSum(survivalSum)
                        .setRounds(event.getBattleRules().getNumRounds())
                        .setEnemyWeightedHitRate(data.getEnemyWeightedHitPercentage(enemy))
                        .setWeightedHitRate(data.getWeightedHitPercentage(enemy))
                        .createDuelResult();
                break;
            }
        }
    }

    public DuelResult getDuelResult() {
        return duelResult;
    }
}
