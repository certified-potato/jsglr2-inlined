package org.spoofax.jsglr2.inlined.observables;

import org.metaborg.parsetable.productions.IProduction;
import org.spoofax.jsglr2.parseforest.IParseNode;

public class InlinedFakeParseNode implements IParseNode<InlinedFakeParseForest, InlinedFakeDerivation> {

    @Override
    public int width() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public IProduction production() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addDerivation(InlinedFakeDerivation derivation) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean hasDerivations() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterable<InlinedFakeDerivation> getDerivations() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InlinedFakeDerivation getFirstDerivation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAmbiguous() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void disambiguate(InlinedFakeDerivation derivation) {
        // TODO Auto-generated method stub
        
    }

}
