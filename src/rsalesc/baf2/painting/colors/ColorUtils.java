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

package rsalesc.baf2.painting.colors;

import java.awt.*;

/**
 * Created by Roberto Sales on 24/07/17.
 */
public abstract class ColorUtils {
    public static Color interpolateRGB(Color a, Color b, double alpha) {
        float[] aComp = a.getRGBColorComponents(null);
        float[] bComp = b.getRGBColorComponents(null);
        float[] result = new float[aComp.length];

        for (int i = 0; i < aComp.length; i++) {
            result[i] = (float) (aComp[i] * (1.0 - alpha) + bComp[i] * alpha);
        }

        return new Color(result[0], result[1], result[2]);
    }
}
