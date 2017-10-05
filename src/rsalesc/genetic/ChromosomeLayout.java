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

package rsalesc.genetic;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Roberto Sales on 30/09/17.
 */
public class ChromosomeLayout implements Serializable {
    private static final long serialVersionUID = 1777777711231L;
    protected HashMap<ConcreteGene, Integer> concreteGeneOffset = new HashMap<>();
    private int totalLength = 0;

    public int getBitLength() {
        return totalLength;
    }

    public boolean compatibleWith(ChromosomeSequence sequence) {
        return sequence.size() == this.getBitLength();
    }

    public boolean compatibleWith(ChromosomeLayout layout) {
        return this.getBitLength() == layout.getBitLength();
    }

    public void addGene(Gene gene) {
        if(gene instanceof CompoundGene) {
            for(Gene childGene : ((CompoundGene) gene).getGenes())
                addGene(childGene);
        } else {
            ConcreteGene concreteGene = (ConcreteGene) gene;
            if(concreteGeneOffset.containsKey(gene))
                throw new IllegalStateException("you're adding a duplicate instance of a gene. you must clone it" +
                        "if you want to add another.");
            else {
                concreteGeneOffset.put(concreteGene, totalLength);
                totalLength += concreteGene.getBitLength();
            }
        }
    }

    public Set<ConcreteGene> getConcreteGenes() {
        return concreteGeneOffset.keySet();
    }

    public int getConcreteGeneOffset(ConcreteGene gene) {
        if(!concreteGeneOffset.containsKey(gene))
            throw new IllegalStateException();

        return concreteGeneOffset.get(gene);
    }

    public Set<Map.Entry<ConcreteGene, Integer>> getConcreteGeneEntrySet() {
        return concreteGeneOffset.entrySet();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ChromosomeLayout)
            return concreteGeneOffset.equals(((ChromosomeLayout) obj).concreteGeneOffset);
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getConcreteGeneEntrySet().toArray());
    }
}
