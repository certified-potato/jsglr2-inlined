package org.spoofax.jsglr2.inlined.components.observables;

import org.metaborg.parsetable.productions.IProduction;
import org.metaborg.parsetable.productions.ProductionType;
import org.spoofax.jsglr2.inlined.observables.FakeDerivation;

public class InlinedDerivation {
    public final IProduction production;
    public final ProductionType productionType;
    public final InlinedParseForest[] parseForests;
    
    private FakeDerivation fake = null;

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
    
    public FakeDerivation getFake() {
        if (fake == null) {
            fake = new FakeDerivation(this);
        }
        return fake;
    }
}
