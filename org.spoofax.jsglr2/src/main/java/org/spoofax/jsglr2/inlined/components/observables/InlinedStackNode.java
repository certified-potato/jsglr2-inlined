package org.spoofax.jsglr2.inlined.components.observables;

import java.util.ArrayList;
import java.util.Collections;

import org.metaborg.parsetable.states.IState;
import org.spoofax.jsglr2.inlined.components.InlinedStackLink;
import org.spoofax.jsglr2.inlined.observables.FakeStackNode;
import org.spoofax.jsglr2.util.iterators.SingleElementWithListIterable;

public class InlinedStackNode {

    // state depends on the parsetable
    public final IState state;

    private FakeStackNode fake = null;

    private InlinedStackLink firstLink;
    private ArrayList<InlinedStackLink> otherLinks;

    public InlinedStackNode(IState state) {
        this.state = state;
    }

    public FakeStackNode getFake() {
        if (fake == null) {
            fake = new FakeStackNode(this);
        }
        return fake;
    }
    
    public IState state() {
        return state;
    }

    public InlinedStackLink addLink(InlinedStackLink link) {
        if (firstLink == null)
            firstLink = link;
        else {
            if (otherLinks == null)
                otherLinks = new ArrayList<>();

            otherLinks.add(link);
        }

        return link;
    }

    public Iterable<InlinedStackLink> getLinks() {
        if (otherLinks == null) {
            return Collections.singleton(firstLink);
        } else {
            return SingleElementWithListIterable.of(firstLink, otherLinks);
        }
    }

    public boolean allLinksRejected() {
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
