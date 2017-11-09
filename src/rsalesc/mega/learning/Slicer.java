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

package rsalesc.mega.learning;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Roberto Sales on 23/08/17.
 */
public class Slicer {
    private static final String[] adaptiveNames =
            new String[]{"BFT", "LAT_VEL", "ADV_VEL", "ESCAPE", "ESCAPE", "ACCEL", "DECEL", "REVERT", "D10"};

    private static final double[] simpleChances =
            new double[]{0.7, 0.9, 0.4, 0.6, 0.35, 0.15, 0, 0, 0};

    private static final double[] adaptiveChances =
            new double[]{0.75, 0.75, 0.2, 0.5, 0.2, 0.2, 0.25, 0.25, 0.25};

    private static final double[] flattenerChances =
            new double[]{0.6, 0.6, 0.4, 0.6, 0.4, 0.65, 0.4, 0.4, 0.4};

    private static final double[] tickChances =
            new double[]{0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.45, 0.5};

    private static final double[] pmChances =
            new double[]{0.2, 1.0, 0.3, 0, 0, 0.3, 0.7, 0.4, 0.75};


    public static void main(String[] args) {
//        printSlices(
//                generateSliced("simple", 35, 2.0, adaptiveNames, simpleChances),
//                generateSliced("pm", 25, 10.0, adaptiveNames, pmChances),
//                generateSliced("adaptive", 40, 10.0, adaptiveNames, adaptiveChances)
//        );

        printSlices(generateSliced("flattener", 50, Double.POSITIVE_INFINITY, adaptiveNames, flattenerChances));
    }

    private static Sliced generateSliced(String name, int count, double density, String[] names, double[] chances) {
        String[][] slices = new String[count][names.length];

        for(int i = 0; i < count; i++) {
            double proportion = Math.min(-Math.log(1 - (double) i / count) / Math.log(1.5) * density, 1);
//            System.out.println(proportion);

            for(int j = 0; j < names.length; j++) {
                double rnd = Math.random();
                if(rnd > proportion) {
                    slices[i][j] = getName(names, j, 0);
                    continue;
                }

                rnd = (proportion - rnd) / proportion;
                if(rnd > chances[j])
                    slices[i][j] = getName(names, j, 0);
                else
                    slices[i][j] = getName(names, j, (int) (rnd / chances[j] * 2.999999999) + 1);
            }
        }

        Arrays.sort(slices, new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                for(int i = 0; i < o1.length; i++) {
                    if(o1[i] != o2[i])
                        return -o1[i].compareToIgnoreCase(o2[i]);
                }

                return 0;
            }
        });

        return new Sliced(name, slices);
    }

    private static String getName(String[] names, int i, int type) {
        if(type == 0)
            return "EMPTY";
        else if(type == 1)
            return names[i] + "_S";
        else if(type == 2)
            return names[i];
        else return names[i] + "_P";
    }

    static void printSlices(Sliced... sliceds) {
        System.out.println("return new double[][][]{");

        for(int k = 0; k < sliceds.length; k++) {
            System.out.println("\t// " + sliceds[k].name);
            String[][] slices = sliceds[k].slices;
            for (int i = 0; i < slices.length; i++) {
                System.out.print("\t{");
                for (int j = 0; j < slices[i].length; j++) {
                    if (j > 0)
                        System.out.print(", ");
                    System.out.print(slices[i][j]);
                }
                System.out.println("}" + (i + 1 == slices.length && k + 1 == sliceds.length ? "" : ","));
            }
        }

        System.out.println("};");
    }

    private static class Sliced {
        private final String name;
        private final String[][] slices;

        private Sliced(String name, String[][] slices) {
            this.name = name;
            this.slices = slices;
        }
    }
}
