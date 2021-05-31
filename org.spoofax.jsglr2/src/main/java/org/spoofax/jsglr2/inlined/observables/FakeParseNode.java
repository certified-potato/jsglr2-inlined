package org.spoofax.jsglr2.inlined.observables;

import java.util.ArrayList;

import org.metaborg.parsetable.productions.IProduction;
import org.spoofax.jsglr2.inlined.components.observables.InlinedDerivation;
import org.spoofax.jsglr2.inlined.components.observables.InlinedParseNode;
import org.spoofax.jsglr2.parseforest.IParseNode;

//TODO: improve this glue
public class FakeParseNode implements IParseNode<FakeParseForest, FakeDerivation> {

    protected final InlinedParseNode internal;

    public FakeParseNode(InlinedParseNode internal) {
        this.internal = internal;
    }

    @Override
    public int width() {
        return internal.width();
    }

    @Override
    public IProduction production() {
        return internal.production();
    }

    @Override
    public void addDerivation(FakeDerivation derivation) {
        internal.addDerivation(derivation.internal);
    }

    @Override
    public boolean hasDerivations() {
        return internal.hasDerivations();
    }

    @Override
    public Iterable<FakeDerivation> getDerivations() {
        ArrayList<InlinedDerivation> orig = internal.getDerivations();
        ArrayList<FakeDerivation> list = new ArrayList<>(orig.size());
        orig.forEach(el -> list.add(el.getFake()));
        return list;
    }

    @Override
    public FakeDerivation getFirstDerivation() {
        return internal.getFirstDerivation().getFake();
    }

    @Override
    public boolean isAmbiguous() {
        return internal.isAmbiguous();
    }

    @Override
    public void disambiguate(FakeDerivation derivation) {
        internal.addDerivation(derivation.internal);
    }

}
