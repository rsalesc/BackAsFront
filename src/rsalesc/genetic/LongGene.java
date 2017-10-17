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

import rsalesc.baf2.core.utils.R;

import java.io.Serializable;

/**
 * Created by Roberto Sales on 30/09/17.
 */
public class LongGene extends ConcreteGene<Long> implements Serializable {
    private static final long serialVersionUID = 19023910000000231L;

    protected long integerOffset;
    protected long minValue;
    protected long maxValue;
    protected long diff;
    protected int bitLength;

    public LongGene(long max) {
        bitLength = getNeededBits(0, max);
    }

    public LongGene(long min, long max) {
        bitLength = getNeededBits(min, max);
    }

    @Override
    public int getBitLength() {
        return bitLength;
    }

    private int getNeededBits(long min, long max) {
        if(max <= min) throw new IllegalStateException("max <= min can not be true");
        minValue = min;
        maxValue = max;
        diff = max-min+1;

        integerOffset = min;
        max -= min;

        int bits = 0;
        while(max > 0) {
            max /= 2;
            bits++;
        }

        return bits;
    }


    @Override
    public String stringifyValue(Long value) {
        if(value < minValue)
            value = minValue;
        if(value > maxValue)
            value = maxValue;

        long binary = value - integerOffset;
        long gray = binaryToGray(binary);

        StringBuilder builder = new StringBuilder();
        while(gray > 0) {
            builder.append((char) ('0' + (gray & 1)));
            gray >>= 1;
        }

        while(builder.length() < getBitLength())
            builder.append('0');

        if(builder.length() > getBitLength())
            throw new IllegalStateException();

        builder.reverse();

        return builder.toString();
    }

    @Override
    public Long interpretString(String bitString) {
        if(bitString.length() != getBitLength())
            throw new IllegalStateException("interpreted bitString length != from gene bitLength");

        long gray = 0;
        for(char c : bitString.toCharArray()) {
            gray = (gray << 1) + c - '0';
        }

        return (grayToBinary(gray) % diff) + integerOffset;
    }

    public static long grayToBinary(long gray) {
        long mask;
        for(mask = gray >> 1; mask != 0; mask >>= 1)
            gray ^= mask;
        return gray;
    }

    public static long binaryToGray(long binary) {
        return binary ^ (binary >> 1);
    }
}
