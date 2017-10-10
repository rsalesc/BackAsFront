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

import rsalesc.baf2.core.listeners.KeyPressedListener;
import rsalesc.baf2.core.utils.ComparablePair;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Roberto Sales on 09/10/17.
 */
public class KeyHandler extends StoreComponent implements KeyPressedListener {
    private static final KeyHandler SINGLETON = new KeyHandler();

    private KeyHandler() {}

    public static KeyHandler getInstance() {
        return SINGLETON;
    }

    @Override
    public void onKeyPressed(KeyEvent e) {
        Set<ComparablePair<Integer, Integer>> set = getPressedKeys();
        ComparablePair<Integer, Integer> ps = new ComparablePair<>(e.getKeyCode(), e.getModifiers());
        if(!set.add(ps))
            set.remove(ps);
    }

    public boolean enabled(int keyCode) {
        return enabled(AWTKeyStroke.getAWTKeyStroke(keyCode, 0));
    }

    public boolean enabled(KeyEvent e) {
        return enabled(AWTKeyStroke.getAWTKeyStroke(e.getKeyCode(), e.getModifiers()));
    }

    public boolean enabled(AWTKeyStroke stroke) {
        return getPressedKeys().contains(new ComparablePair<>(stroke.getKeyCode(), stroke.getModifiers()));
    }

    public Set<ComparablePair<Integer, Integer>> getPressedKeys() {
        StorageNamespace ns = getStorageNamespace();
        if(ns.contains("pressed"))
            return (Set) ns.get("pressed");

        Set<ComparablePair<Integer, Integer>> res = new TreeSet<>();
        ns.put("pressed", res);

        return res;
    }

    @Override
    public StorageNamespace getStorageNamespace() {
        return getGlobalStorage().namespace("static-key-handler");
    }
}
