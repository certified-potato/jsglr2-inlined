package org.spoofax.jsglr2.inlined.components;

import org.metaborg.parsetable.productions.IProduction;
import org.metaborg.parsetable.productions.ProductionType;

public class InlinedDerivation {
    public final IProduction production;
    public final ProductionType productionType;
    public final InlinedParseForest[] parseForests;
    
    public InlinedDerivation(IProduction production, ProductionType productionType, InlinedParseForest[] parseForests) {
        this.production = production;
        this.productionType = productionType;
        this.parseForests = parseForests;
    }

    public IProduction production() {
        return production;
    }

    public ProductionType productionType() {
        return productionType;
    }

    public InlinedParseForest[] parseForests() {
        return parseForests;
    }
}
