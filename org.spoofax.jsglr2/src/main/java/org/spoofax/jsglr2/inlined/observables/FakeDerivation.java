package org.spoofax.jsglr2.inlined.observables;

import java.util.Arrays;

import org.metaborg.parsetable.productions.IProduction;
import org.metaborg.parsetable.productions.ProductionType;
import org.spoofax.jsglr2.inlined.components.observables.InlinedDerivation;
import org.spoofax.jsglr2.parseforest.IDerivation;

//TODO: improve this glue
public class FakeDerivation implements IDerivation<FakeParseForest> {

    protected final InlinedDerivation internal;

    public FakeDerivation(InlinedDerivation deriv) {
        internal = deriv;
    }

    @Override
    public IProduction production() {
        return internal.production();
    }

    @Override
    public ProductionType productionType() {
        return internal.productionType();
    }

    @Override
    public FakeParseForest[] parseForests() {
        return (FakeParseForest[]) Arrays.stream(internal.parseForests()).map(forest -> new FakeParseForest()).toArray();
    }

}
