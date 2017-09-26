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

package rsalesc.melee.gunning;

import robocode.Condition;
import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.StorageNamespace;
import rsalesc.baf2.core.listeners.TickListener;
import rsalesc.baf2.core.utils.R;
import rsalesc.mega.gunning.guns.AutomaticGun;
import rsalesc.mega.gunning.guns.AutomaticGunArray;
import rsalesc.mega.utils.structures.Knn;

import java.util.ArrayList;

/**
 * Created by Roberto Sales on 20/09/17.
 */
public class SegmentedSwarmGun extends SwarmGun implements TickListener {
    private ArrayList<GunEntry> guns = new ArrayList<>();

    public SegmentedSwarmGun() {
        super(null);
    }

    @Override
    public void init(RobotMediator mediator) {
        super.init(mediator);

        if(guns.isEmpty())
            throw new IllegalStateException();

        for(GunEntry entry : guns) {
            if(getPowerSelector() != null)
                entry.gun.setPowerSelector(getPowerSelector());
            entry.gun.init(mediator);
        }
    }

    public void addGun(AutomaticGun gun, int minOthers) {
        guns.add(new GunEntry(gun, minOthers));
    }

    public void addGun(GunEntry entry) {
        guns.add(entry);
    }

    @Override
    public StorageNamespace getStorageNamespace() {
        return super.getStorageNamespace().namespace("segmented");
    }

    @Override
    public String getGunName() {
        return "Segmented Swarm Gun";
    }

    @Override
    public void onTick(long time) {
        for(GunEntry entry : guns) {
            if(entry.gun instanceof AutomaticGunArray) {
                ((AutomaticGunArray) entry.gun).setScoring(entry.test());
            }
        }
    }

    @Override
    public AutomaticGun getGun() {
        for(GunEntry entry : guns) {
            if(entry.test()) {
                return entry.gun;
            }
        }

        return R.getLast(guns).gun;
    }

    private class GunEntry extends Condition {
        public final AutomaticGun gun;
        public final int others;

        private GunEntry(AutomaticGun gun, int others) {
            this.gun = gun;
            this.others = others;
        }

        @Override
        public boolean test() {
            return getMediator().getOthers() >= others;
        }
    }
}
