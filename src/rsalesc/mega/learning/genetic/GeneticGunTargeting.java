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

package rsalesc.mega.learning.genetic;

import rsalesc.baf2.core.StorageNamespace;
import rsalesc.mega.gunning.guns.KnnGuessFactorTargeting;
import rsalesc.mega.utils.Strategy;

/**
 * Created by Roberto Sales on 02/10/17.
 */
public abstract class GeneticGunTargeting extends KnnGuessFactorTargeting {
    private final int threadNumber;
    private final Strategy geneticStrategy;

    public GeneticGunTargeting(int threadNum, Strategy strategy) {
        this.threadNumber = threadNum;
        this.geneticStrategy = strategy;
    }

    public Strategy getGeneticStrategy() {
        return geneticStrategy;
    }

    @Override
    public StorageNamespace getStorageNamespace() {
        return getGlobalStorage().namespace("genetic-tuning-gun-" + threadNumber);
    }
}
