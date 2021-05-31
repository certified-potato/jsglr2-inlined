package org.spoofax.jsglr2.inlined.observables;

import org.spoofax.jsglr2.inlined.components.observables.InlinedParseForest;
import org.spoofax.jsglr2.parseforest.IParseForest;

//TODO: improve this glue
public class FakeParseForest implements IParseForest {

    InlinedParseForest internal;
    
    @Override
    public int width() {
        return internal.width();
    }

    @Override
    public String descriptor() {
        return internal.descriptor();
    }

}
