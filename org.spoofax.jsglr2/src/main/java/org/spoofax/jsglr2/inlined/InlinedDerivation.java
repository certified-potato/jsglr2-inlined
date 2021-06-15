package org.spoofax.jsglr2.inlined;

import org.metaborg.parsetable.productions.IProduction;
import org.metaborg.parsetable.productions.ProductionType;
import org.spoofax.jsglr2.parseforest.IParseForest;

class InlinedDerivation {
    private final IProduction production;
    private final ProductionType productionType;
    private final IParseForest[] parseForests;
    
    InlinedDerivation(IProduction production, ProductionType productionType, IParseForest[] parseForests) {
        this.production = production;
        this.productionType = productionType;
        this.parseForests = parseForests;
    }

    /**
     * The derivation rule, such as "A -> bc" or "OP.PLUS -> x+x"
     */
    IProduction production() {
        return production;
    }

    /**
     * Defines the priority of this rule:
     * PREFER: these derivations have a higher priority than all others. 
     * AVOID: only choose this derivation if there are no other types.
     * NO_TYPE: neutral priority, lower than PREFER, higher than AVOID.
     * REJECT: this derivation is not allowed at all, and should not exist. 
     */
    ProductionType productionType() {
        return productionType;
    }

    /**
     * The nodes that this production decomposes into. If the rule is "A -> Bc", for example, then
     * parseForests returns [ParseNode(B), CharNode('c')].
     */
    IParseForest[] parseForests() {
        return parseForests;
    }
}
