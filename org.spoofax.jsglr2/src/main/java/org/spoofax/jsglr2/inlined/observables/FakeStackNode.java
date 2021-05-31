package org.spoofax.jsglr2.inlined.observables;

import java.util.ArrayList;
import java.util.Collections;

import org.metaborg.parsetable.states.IState;
import org.spoofax.jsglr2.inlined.components.observables.InlinedStackNode;
import org.spoofax.jsglr2.stack.IStackNode;
import org.spoofax.jsglr2.stack.StackLink;
import org.spoofax.jsglr2.util.iterators.SingleElementWithListIterable;

//TODO: improve this glue
public class FakeStackNode implements IStackNode {
    
    InlinedStackNode internal;
    
    public FakeStackNode(InlinedStackNode internal) {
        this.internal = internal;
    }
    
    @Override
    public IState state() {
        return internal.state();
    }
    
    @Override
    public boolean allLinksRejected() {
        return internal.allLinksRejected();
    }
}
