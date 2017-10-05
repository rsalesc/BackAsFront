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

import robocode.control.RobocodeEngine;

/**
 * Created by Roberto Sales on 02/10/17.
 * TODO: improve this with static keeper
 */
public class RobocodeEngineProvider {
    private String path;
    private RobocodeEngine engine;

    public RobocodeEngineProvider() {}

    public RobocodeEngineProvider(String path) {
        this.path = path;
    }

    public RobocodeEngineProvider(RobocodeEngine engine) {
        this.engine = engine;
    }

    public RobocodeEngine getEngine() {
        if(engine != null)
            return engine;

        return engine = (path != null ? QuickBattleRunner.getEngine(path) : QuickBattleRunner.getEngine());
    }

    public void close() {
        if(engine != null)
            engine.close();

        engine = null;
    }
}
