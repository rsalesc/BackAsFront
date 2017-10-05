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

import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;
import robocode.control.events.BattleAdaptor;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Roberto Sales on 01/10/17.
 */
public class QuickBattleRunner {
    private final String[] robots;
    private final int rounds;
    private final BattlefieldSpecification specification;

    public QuickBattleRunner(String[] robots, int rounds, BattlefieldSpecification specification) {
        this.robots = robots;
        this.rounds = rounds;
        this.specification = specification;
    }

    public void run(RobocodeEngineProvider provider, BattleAdaptor observer) {
        RobocodeEngine engine = provider.getEngine();
        engine.addBattleListener(observer);

        List<String> robotNames = Arrays.asList(robots);
        RobotSpecification[] selectedRobots = engine.getLocalRepository(String.join(",", robotNames));

        BattleSpecification battleSpec = new BattleSpecification(rounds, specification, selectedRobots);

        // Run our specified battle and let it run till it is over
        engine.runBattle(battleSpec, true); // waits till the battle finishes

        engine.removeBattleListener(observer);
    }

    public void run(RobocodeEngine engine, BattleAdaptor observer) {
        run(new RobocodeEngineProvider(engine), observer);
    }

    public void run(BattleAdaptor observer) {
        run(getEngine(), observer);
    }

    public static RobocodeEngine getEngine() {
        return getEngine("/home/rsalesc/robocode");
    }

    public static RobocodeEngine getEngine(String path) {
        // Disable log messages from Robocode
        RobocodeEngine.setLogMessagesEnabled(false);

        // Create the RobocodeEngine
        RobocodeEngine engine = new RobocodeEngine(new java.io.File(path));

        // Show the Robocode battle view
        engine.setVisible(false);

        return engine;
    }
}
