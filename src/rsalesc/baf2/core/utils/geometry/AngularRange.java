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

package rsalesc.baf2.core.utils.geometry;

import robocode.util.Utils;

/**
 * Created by Roberto Sales on 16/08/17.
 */
public class AngularRange extends Range {
    private double reference;

    public AngularRange(double reference, double min, double max) {
        super(min, max);
        this.reference = reference;
    }

    public AngularRange(double reference, Range range) {
        super(range.min, range.max);
        this.reference = reference;
    }

    public double getAngle(double offset) {
        return Utils.normalAbsoluteAngle(reference + offset);
    }

    public double getOffset(double angle) {
        return Utils.normalRelativeAngle(angle - reference);
    }

    public double getStartingAngle() {
        return getAngle(min);
    }

    public double getEndingAngle() {
        return getAngle(max);
    }

    public void pushAngle(double angle) {
        push(getOffset(angle));
    }

    public boolean isAngleNearlyContained(double angle) {
        return isNearlyContained(getOffset(angle));
    }

    public AngularRange translate(double newReference) {
        double delta = newReference - reference;
        return new AngularRange(newReference, min - delta, max - delta);
    }

    // TODO: handle cases where translation is absurd
    public AngularRange intersectAngles(AngularRange rhs) {
        AngularRange translated = rhs.translate(reference);
        Range res = intersect(translated);
        return new AngularRange(reference, res.min, res.max);
    }
}
