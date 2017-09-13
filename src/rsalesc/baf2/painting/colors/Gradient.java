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

import rsalesc.baf2.core.utils.R;

import java.awt.*;
import java.util.Arrays;

/**
 * Created by Roberto Sales on 24/07/17.
 */
public class Gradient {
    private final GradientColor[] colors;

    public Gradient(GradientColor[] colors) {
        GradientColor[] tmpColors = Arrays.copyOf(colors, colors.length);
        Arrays.sort(tmpColors);
        this.colors = new GradientColor[colors.length + 2];
        this.colors[0] = new GradientColor(tmpColors[0].color, 0);
        this.colors[this.colors.length - 1] = new GradientColor(tmpColors[tmpColors.length - 1].color, 1);
        for (int i = 0; i < tmpColors.length; i++)
            this.colors[i + 1] = tmpColors[i];
    }

    public Color evaluate(double x) {
        x = R.constrain(0, x, 1);
        GradientColor color = colors[0];
        for (int i = 1; i < colors.length; i++) {
            if (x < colors[i].x) {
                if (R.isNear(colors[i].x, color.x))
                    return colors[i].color;

                return ColorUtils.interpolateRGB(color.color,
                        colors[i].color,
                        (x - color.x) / (colors[i].x - color.x));
            } else {
                color = colors[i];
            }
        }

        return color.color;
    }

    public static class GradientColor implements Comparable {
        private double x;
        private Color color;

        public GradientColor(Color color, double x) {
            this.x = x;
            this.color = color;
        }


        @Override
        public int compareTo(Object o) {
            if (!(o instanceof GradientColor))
                throw new ClassCastException("Gradient object expected");

            return (int) Math.signum(this.x - ((GradientColor) o).x);
        }
    }
}
