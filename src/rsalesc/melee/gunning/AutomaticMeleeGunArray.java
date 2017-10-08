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

import rsalesc.baf2.tracking.EnemyLog;
import rsalesc.mega.gunning.guns.AutomaticGun;
import rsalesc.mega.gunning.guns.AutomaticGunArray;

/**
 * Created by Roberto Sales on 06/10/17.
 */
public abstract class AutomaticMeleeGunArray extends AutomaticGunArray implements MeleeGun {
    private Integer K;

    public Integer getK() {
        return K;
    }

    @Override
    public void addGun(AutomaticGun gun) {
        if(!(gun instanceof MeleeGun))
            throw new IllegalStateException();
        super.addGun(gun);
    }

    public void setK(Integer k) {
        K = k;
        for(AutomaticGun gun : getGuns()) {
            ((MeleeGun) gun).setK(k);
        }
    }

    @Override
    public int queryableData(EnemyLog enemyLog) {
        int res = 0;
        for(AutomaticGun gun : getGuns()) {
            res += ((MeleeGun) gun).queryableData(enemyLog);
        }

        return res;
    }

    @Override
    public int availableData(EnemyLog enemyLog) {
        int res = 0;
        for(AutomaticGun gun : getGuns()) {
            res += ((MeleeGun) gun).availableData(enemyLog);
        }

        return res;
    }
}
