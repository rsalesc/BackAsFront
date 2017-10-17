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

package rsalesc.runner;

import codesize.Codesize;

import java.io.File;

/**
 * Created by Roberto Sales on 27/09/17.
 */
public class Codesizer {
    public static void main(String[] args) {
        int res;
        if(args[0].endsWith(".jar"))
            res = processPackage(args[0]);
        else
            res = processClass(args[0]);

        System.out.println("Codesize: " + res);
    }

    public static int processPackage(String path) {
        Codesize.Item item = Codesize.processZipFile(new File(path));
        return item.getCodeSize();
    }

    public static int processClass(String path) {
        Codesize.Item item = Codesize.processClassFile(new File("src/" + path.replace(".", "/") + ".java"));
        return item.getCodeSize();
    }
}
