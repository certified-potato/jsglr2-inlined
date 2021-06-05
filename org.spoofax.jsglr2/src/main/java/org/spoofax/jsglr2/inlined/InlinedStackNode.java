package org.spoofax.jsglr2.inlined;

import java.util.ArrayList;
import java.util.Collections;

import org.metaborg.parsetable.states.IState;
import org.spoofax.jsglr2.parseforest.IParseForest;
import org.spoofax.jsglr2.util.iterators.SingleElementWithListIterable;

public class InlinedStackNode {

    // state depends on the parsetable
    public final IState state;

    private InlinedStackLink firstLink;
    private ArrayList<InlinedStackLink> otherLinks;

    InlinedStackNode(IState state) {
        this.state = state;
    }
    
    IState state() {
        return state;
    }

    InlinedStackLink addLink(InlinedStackLink link) {
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
    
    int getLinksSize() {
        if (otherLinks == null) {
            return 1;
        } else {
            return 1 + otherLinks.size();
        }
    }
    

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
