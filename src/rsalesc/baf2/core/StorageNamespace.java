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

/**
 * Created by Roberto Sales on 11/09/17.
 */
public class StorageNamespace {
    String path;
    GlobalStorage storage;

    public StorageNamespace(String path, GlobalStorage storage) {
        this.path = path;
        this.storage = storage;
    }

    public void put(String name, Object object) {
        storage.put(path + name, object);
    }

    public Object get(String name) {
        return storage.get(path + name);
    }

    public StorageNamespace namespace(String name) {
        return new StorageNamespace(path + "/" + name + "/", storage);
    }

    public boolean contains(String name) {
        return storage.contains(path + name);
    }
}
