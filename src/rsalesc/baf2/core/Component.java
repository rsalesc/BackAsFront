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

package rsalesc.baf2.core;

import robocode.Condition;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public abstract class Component {
    private RobotMediator mediator;
    private boolean virtual = false;
    private ArrayList<ConditionedListener> listeners = new ArrayList<>();

    public void init(RobotMediator mediator) {
        this.mediator = mediator;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public void setVirtual(boolean flag) {
        virtual = flag;
    }

    public RobotMediator getMediator() {
        if (mediator == null)
            throw new IllegalStateException();
        return mediator;
    }

    public void run() {
    }

    public void beforeRun() {
    }

    public void afterRun() {
    }

    protected ArrayList<Object> getListeners() {
        ArrayList<Object> res = new ArrayList<>();

        for(ConditionedListener cl : listeners) {
            if(cl.test())
                res.add(cl.getListener());
        }

        ArrayList<Object> filtered = new ArrayList<>();

        HashSet<Object> seen = new HashSet<>();
        for (Object obj : res) {
            if (!seen.contains(obj)) {
                filtered.add(obj);
                seen.add(obj);
            }
        }

        return filtered;
    }

    public void addListener(Object listener) {
        listeners.add(new ConditionedListener(listener, new Condition() {
            @Override
            public boolean test() {
                return true;
            }
        }));
    }

    public void addListener(Object listener, Condition condition) {
        listeners.add(new ConditionedListener(listener, condition));
    }

    private static class ConditionedListener {
        public final Object listener;
        public final Condition condition;

        private ConditionedListener(Object listener, Condition condition) {
            this.listener = listener;
            this.condition = condition;
        }

        public boolean test() {
            return condition.test();
        }

        public Object getListener() {
            return listener;
        }
    }
}
