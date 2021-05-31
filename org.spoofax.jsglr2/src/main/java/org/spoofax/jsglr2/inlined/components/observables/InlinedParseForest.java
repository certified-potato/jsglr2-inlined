package org.spoofax.jsglr2.inlined.components.observables;

public class InlinedParseForest {
    public static int sumWidth(InlinedParseForest... parseForests) {
        int width = 0;
        for(InlinedParseForest parseForest : parseForests) {
            width += parseForest.width();
        }
        return width;
    }
}
