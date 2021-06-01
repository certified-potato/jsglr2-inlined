package org.spoofax.jsglr2.inlined.components;

import org.spoofax.jsglr2.parseforest.IParseForest;

public class InlinedParseForest implements IParseForest {
    
    
    public static int sumWidth(InlinedParseForest... parseForests) {
        int width = 0;
        for(InlinedParseForest parseForest : parseForests) {
            width += parseForest.width();
        }
        return width;
    }

    @Override
    public int width() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String descriptor() {
        // TODO Auto-generated method stub
        return null;
    }
}
