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

package rsalesc.mega.utils.segmentation;

/**
 * Created by Roberto Sales on 27/07/17.
 */
public class SegmentTrie<T> {
    private int[]       segments;
    private int[][]     transitions;
    private Object[]    data;
    private int         length;
    private int         created;
    private int         depth;
    private int         rootIndex;

    public SegmentTrie(int[] segments) {
        this.segments = segments;
        depth = segments.length;
        length = 2;
        created = 0;
        transitions = new int[length][];
        data = new Object[length];
        rootIndex = makeNode(0);
    }

    private int makeNode(int level) {
        if(created >= length) {
            int[][] newTransitions = new int[length * 2][];
            System.arraycopy(transitions, 0, newTransitions, 0, length);
            transitions = newTransitions;

            Object[] newData = new Object[length * 2];
            System.arraycopy(data, 0, newData, 0, length);
            data = newData;
            length = length * 2;
        }

        transitions[created] = new int[level >= depth ? 0 : segments[level]];
        for(int i = 0; i < transitions[created].length; i++)
            transitions[created][i] = -1;

        data[created] = null;
        return created++;
    }

    public void add(int[] attributes, T payload) {
        if(attributes.length != depth)
            throw new IllegalArgumentException();

        int cur = rootIndex;
        for(int i = 0; i < depth; i++) {
            int next = transitions[cur][attributes[i]];
            if(next == -1)
                next = (transitions[cur][attributes[i]] = makeNode(i+1));

            cur = next;
        }

        data[cur] = payload;
    }

    @SuppressWarnings("unchecked")
    public T get(int[] attributes) {
        if(attributes.length != depth)
            throw new IllegalArgumentException();

        int cur = rootIndex;
        for(int i = 0; i < depth; i++) {
            int next = transitions[cur][attributes[i]];
            if(next == -1) return null;
            cur = next;
        }

        return (T) data[cur];
    }

    public void add(int[] attributes) {
        add(attributes, null);
    }
}
