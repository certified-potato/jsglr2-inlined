package org.spoofax.jsglr2.inlined;

import org.metaborg.parsetable.productions.IProduction;
import org.metaborg.parsetable.productions.ProductionType;
import org.spoofax.jsglr2.parseforest.IParseForest;

class InlinedDerivation {
    final IProduction production;
    final ProductionType productionType;
    final IParseForest[] parseForests;
    
    InlinedDerivation(IProduction production, ProductionType productionType, IParseForest[] parseForests) {
        this.production = production;
        this.productionType = productionType;
        this.parseForests = parseForests;
    }

    IProduction production() {
        return production;
    }

    ProductionType productionType() {
        return productionType;
    }

    IParseForest[] parseForests() {
        return parseForests;
    }
}
