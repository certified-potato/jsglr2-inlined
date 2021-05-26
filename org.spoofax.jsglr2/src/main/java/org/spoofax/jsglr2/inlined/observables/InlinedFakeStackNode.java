package org.spoofax.jsglr2.inlined.observables;

import java.util.ArrayList;
import java.util.Collections;

import org.metaborg.parsetable.states.IState;
import org.spoofax.jsglr2.stack.IStackNode;
import org.spoofax.jsglr2.stack.StackLink;
import org.spoofax.jsglr2.stack.hybrid.HybridStackNode;
import org.spoofax.jsglr2.util.iterators.SingleElementWithListIterable;

public class InlinedFakeStackNode implements IStackNode {
    
    private StackLink<InlinedFakeParseForest, InlinedFakeStackNode> firstLink;
    private ArrayList<StackLink<InlinedFakeParseForest, InlinedFakeStackNode>> otherLinks;
    
    @Override
    public IState state() {
        //TODO
        return null;
    }
    
    @Override
    public boolean allLinksRejected() {
        //TODO
        return false;
    }

    public StackLink<InlinedFakeParseForest, InlinedFakeStackNode> addLink(InlinedFakeStackNode to,
            InlinedFakeParseForest parseForest) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public Iterable<StackLink<InlinedFakeParseForest, InlinedFakeStackNode>> getLinks() {
        if(otherLinks == null) {
            return Collections.singleton(firstLink);
        } else {
            return SingleElementWithListIterable.of(firstLink, otherLinks);
        }
    }
}
