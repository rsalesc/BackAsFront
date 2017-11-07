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

package rsalesc.baf2.core.benchmark;

import rsalesc.baf2.core.utils.Timer;

import java.util.Objects;

/**
 * Created by Roberto Sales on 12/10/17.
 */
public class BenchmarkNode implements Comparable<BenchmarkNode> {
    private final Benchmark benchmark;
    private final String group;
    public final BenchmarkNode parent;

    private Timer timer;

    public BenchmarkNode(Benchmark benchmark, String group, BenchmarkNode parent) {
        this.benchmark = benchmark;
        this.group = group;
        this.timer = new Timer();
        this.parent = parent;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public int hashCode() {
        if(parent == null)
            return group.hashCode();

        return Objects.hash(parent, group);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof BenchmarkNode))
            return false;

        BenchmarkNode rhs = (BenchmarkNode) obj;

        return group.equals(rhs.group) && (parent == null && rhs.parent == null || parent != null && rhs.parent != null && parent.equals(rhs.parent));
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        benchmark.log(this, (double) timer.stop() / 1e6);
    }

    @Override
    public int compareTo(BenchmarkNode o) {
        if(parent != null && o.parent != null) {
            int res = parent.compareTo(o.parent);
            if(res != 0)
                return res;

            return group.compareTo(o.group);
        } else {
            int res = group.compareTo(o.group);
            if(res != 0)
                return res;

            if(parent != null)
                return +1;
            if(o.parent != null)
                return -1;

            return 0;
        }
    }
}
