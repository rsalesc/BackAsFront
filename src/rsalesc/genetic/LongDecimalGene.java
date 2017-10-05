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

package rsalesc.genetic;

import java.io.Serializable;

/**
 * Created by Roberto Sales on 30/09/17.
 */
public class LongDecimalGene extends ConcreteGene<Double> implements Serializable {
    private static final long serialVersionUID = 1912121212102391231L;

    private final LongGene longGene;
    private final long places;

    @Override
    public int getBitLength() {
        return longGene.getBitLength();
    }

    public LongDecimalGene(long min, long max, long places) {
        this.places = places;
        while(places > 0) {
            max *= 10;
            min *= 10;
            places--;
        }

        longGene = new LongGene(min, max);
    }

    public LongDecimalGene(long max, long places) {
        this(0, max, places);
    }

    public LongDecimalGene(double min, double max, long places) {
        this.places = places;
        while(places > 0) {
            max *= 10;
            min *= 10;
            places--;
        }

        longGene = new LongGene(Math.round(min), Math.round(max));
    }

    public LongDecimalGene(double max, long places) {
        this(0.0, max, places);
    }

    @Override
    public Double interpretString(String bitString) {
        double res = longGene.interpretString(bitString);
        for(int i = 0; i < places; i++) res /= 10;
        return res;
    }

    @Override
    public String stringifyValue(Double value) {
        for(int i = 0; i < places; i++) value *= 10;
        long longValue = Math.round(value);

        return longGene.stringifyValue(longValue);
    }
}
