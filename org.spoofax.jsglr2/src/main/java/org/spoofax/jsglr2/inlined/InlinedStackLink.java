package org.spoofax.jsglr2.inlined;

import org.spoofax.jsglr2.parseforest.IParseForest;

/**
 * Link between two stack nodes. Whereas the stack nodes store the parse table state,
 * the links store the parse forest: when the {@code from} node was added via a shift, then
 * the {@code parseForest} is a simple character node. When the {@code from} node was added via reduction, then
 * the {@code parseForest} is a parse node representing a 'confirmed' reduction.
 */
final class InlinedStackLink {
    
    // [init] <-- [to] <--this-- [from]
    final InlinedStackNode from; // Farthest away from initial stack node
    final InlinedStackNode to; // Closest to initial stack node
    final IParseForest parseForest;
    private boolean isRejected;

    InlinedStackLink(InlinedStackNode from, InlinedStackNode to, IParseForest parseForest) {
        this.from = from;
        this.to = to;
        this.parseForest = parseForest;
        this.isRejected = false;
    }

    /**
     * Reject think link: as far as the parser should be concerned, this link does not exist at all.
     */
    void reject() {
        this.isRejected = true;
    }

    boolean isRejected() {
        return this.isRejected;
    }

}
