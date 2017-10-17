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
import java.util.Random;

/**
 * Created by Roberto Sales on 30/09/17.
 */
public class ChromosomeSequence implements Cloneable, Serializable {
    private static final long serialVersionUID = 190231001201020121L;

    StringBuilder builder = new StringBuilder();

    public ChromosomeSequence(int n) {
        appendBits(n);
    }
    public ChromosomeSequence(String bitString) {
        append(bitString);
    }

    public int size() {
        return builder.length();
    }

    public int appendBits(int n) {
        int res = builder.length();
        while(n-- > 0)
            builder.append('0');

        return res;
    }

    public int append(String bitString) {
        int res = builder.length();
        builder.append(bitString);

        return res;
    }

    public boolean isOn(int pos) {
        return builder.charAt(pos) == '1';
    }

    public char get(int pos) {
        return builder.charAt(pos);
    }

    public void flip(int pos) {
        builder.setCharAt(pos, (char) ('0' + ((short) (builder.charAt(pos) - '0') ^ 1)));
    }

    public void on(int pos) {
        builder.setCharAt(pos, '1');
    }

    public void off(int pos) {
        builder.setCharAt(pos, '0');
    }

    public void set(int pos, boolean on) {
        builder.setCharAt(pos, on ? '1' : '0');
    }

    public void set(int pos, char c) {
        builder.setCharAt(pos, c);
    }

    public void setBitString(int pos, String bitString) {
        builder.replace(pos, pos + bitString.length(), bitString);
    }

    public String substring(int start, int length) {
        return builder.substring(start, start + length);
    }

    public static ChromosomeSequence getRandomSequence(int n) {
        ChromosomeSequence sequence = new ChromosomeSequence(n);
        for(int i = 0; i < n; i++)
            if(Math.random() < 0.5)
                sequence.on(i);

        return sequence;
    }

    public static ChromosomeSequence getRandomSequence(int n, Random rng) {
        ChromosomeSequence sequence = new ChromosomeSequence(n);
        for(int i = 0; i < n; i++)
            if(rng.nextDouble() < 0.5)
                sequence.on(i);

        return sequence;
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    @Override
    public Object clone() {
        return new ChromosomeSequence(this.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ChromosomeSequence)
            return obj.toString().equals(toString());

        return false;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
