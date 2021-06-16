package org.spoofax.jsglr2.inlined;

import java.util.ArrayList;
import java.util.Collections;

import org.metaborg.parsetable.states.IState;
import org.spoofax.jsglr2.parseforest.IParseForest;
import org.spoofax.jsglr2.util.iterators.SingleElementWithListIterable;

/**
 * Represents an entry in the stack: how much has the parser parsed already, and how it parsed the text.
 */
final class InlinedStackNode {

    /**
     * The state (cell, entry) of the parse table.
     * It states how to react to the next character.
     * I.E. whether to shift, reduce, or accept.
     */
    private final IState state;

    /**
     * A link down the stack, towards text start.
     */
    private InlinedStackLink firstLink;
    /**
     * Alternative links in case of ambigous parse.
     */
    private ArrayList<InlinedStackLink> otherLinks;

    InlinedStackNode(IState state) {
        this.state = state;
    }
    
    IState state() {
        return state;
    }

    private InlinedStackLink addLink(InlinedStackLink link) {
        if (firstLink == null)
            firstLink = link;
        else {
            if (otherLinks == null)
                otherLinks = new ArrayList<>();

            otherLinks.add(link);
        }

        return link;
    }
    
    InlinedStackLink addLink(InlinedStackNode parent, IParseForest parseNode) {
        InlinedStackLink link = new InlinedStackLink(this, parent, parseNode);

        return addLink(link);
    }

    Iterable<InlinedStackLink> getLinks() {
        if (otherLinks == null) {
            return Collections.singleton(firstLink);
        } else {
            return SingleElementWithListIterable.of(firstLink, otherLinks);
        }
    }
     
    /**
     * @return figure whether all links have been rejected, that is their associated production rules are of type REJECT
     * If this node has no links (because it is the start state), then this function returns false.
     */
    boolean allLinksRejected() {
        if (firstLink == null || !firstLink.isRejected())
            return false;

        if (otherLinks == null)
            return true;

        for (InlinedStackLink link : otherLinks) {
            if (!link.isRejected())
                return false;
        }

        return true;
    }

}
