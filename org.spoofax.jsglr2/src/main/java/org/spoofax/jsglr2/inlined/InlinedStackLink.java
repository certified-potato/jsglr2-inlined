package org.spoofax.jsglr2.inlined;

import org.spoofax.jsglr2.parseforest.IParseForest;

public class InlinedStackLink {
    public final InlinedStackNode from; // Farthest away from initial stack node
    public final InlinedStackNode to; // Closest to initial stack node
    public final IParseForest parseForest;
    private boolean isRejected;

    public InlinedStackLink(InlinedStackNode from, InlinedStackNode to, IParseForest parseForest) {
        this.from = from;
        this.to = to;
        this.parseForest = parseForest;
        this.isRejected = false;
    }

    public void reject() {
        this.isRejected = true;
    }

    public boolean isRejected() {
        return this.isRejected;
    }

}
