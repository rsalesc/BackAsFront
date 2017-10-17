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

import robocode.WinEvent;
import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.controllers.Controller;
import rsalesc.baf2.core.listeners.WinListener;

/**
 * Created by Roberto Sales on 12/09/17.
 */
public class WinDance extends Component implements WinListener {
    @Override
    public void onWin(WinEvent e) {
        Controller body = getMediator().getBodyControllerOrDummy();
        body.setTurnRightRadians(Double.POSITIVE_INFINITY);
        body.setMaxVelocity(0);

        Controller radar = getMediator().getRadarControllerOrDummy();
        radar.setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

        Controller gun = getMediator().getGunControllerOrDummy();
        gun.setTurnGunRightRadians(Double.POSITIVE_INFINITY);
    }
}
