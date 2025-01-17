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

package rsalesc.mega.gunning.power;

import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.RobotMediator;
import rsalesc.mega.utils.StatData;

/**
 * Created by Roberto Sales on 20/09/17.
 */
public class MirrorSwarmSelector extends Component implements PowerSelector {
    MirrorPowerSelector duelSelector;
    MeleePowerSelector meleeSelector = new MeleePowerSelector();

    public MirrorSwarmSelector(PowerPredictor predictor) {
        duelSelector = new MirrorPowerSelector(predictor);
    }

    @Override
    public void init(RobotMediator mediator) {
        super.init(mediator);
    }

    @Override
    public double selectPower(RobotMediator mediator, StatData o) {
        if(mediator.getOthers() == 1)
            return duelSelector.selectPower(mediator, o);
        else
            return meleeSelector.selectPower(mediator, o);
    }
}
