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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Roberto Sales on 11/09/17.
 */
public class GlobalStorage {
    private static final GlobalStorage SINGLETON = new GlobalStorage();

    Hashtable<String, Object> hash;

    private GlobalStorage() {
        hash = new Hashtable<>();
    }

    public void clear() {
        hash.clear();
    }

    public static GlobalStorage getInstance() {
        return SINGLETON;
    }

    public void put(String name, Object object) {
        hash.put(name, object);
    }

    public Object get(String name) {
        return hash.get(name);
    }

    public StorageNamespace namespace(String name) {
        return new StorageNamespace("/" + name + "/", this);
    }

    public boolean contains(String name) {
        return hash.containsKey(name);
    }
}
