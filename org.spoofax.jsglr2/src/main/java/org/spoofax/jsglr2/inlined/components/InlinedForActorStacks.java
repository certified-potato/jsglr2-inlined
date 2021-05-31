package org.spoofax.jsglr2.inlined.components;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

import org.spoofax.jsglr2.inlined.components.observables.InlinedStackNode;
import org.spoofax.jsglr2.inlined.observables.FakeDerivation;
import org.spoofax.jsglr2.inlined.observables.FakeParseForest;
import org.spoofax.jsglr2.inlined.observables.FakeParseNode;
import org.spoofax.jsglr2.inlined.observables.FakeParseState;
import org.spoofax.jsglr2.inlined.observables.FakeStackNode;
import org.spoofax.jsglr2.parser.observing.ParserObserving;

import com.google.common.collect.Iterables;

public class InlinedForActorStacks {
    
    protected final ArrayDeque<InlinedStackNode> forActor = new ArrayDeque<>();
    
    
    private final ParserObserving<FakeParseForest, FakeDerivation, FakeParseNode, FakeStackNode, FakeParseState> observing;
    protected final Queue<InlinedStackNode> forActorDelayed;

    public InlinedForActorStacks(ParserObserving<FakeParseForest, FakeDerivation, FakeParseNode, FakeStackNode, FakeParseState> observing) {
        this.observing = observing;

        // TODO: implement priority (see P9707 Section 8.4)
        Comparator<InlinedStackNode> stackNodePriorityComparator = (InlinedStackNode stackNode1, InlinedStackNode stackNode2) -> 0;
        
        this.forActorDelayed = new PriorityQueue<>(stackNodePriorityComparator);
    }

    public void add(InlinedStackNode stack) {
        observing.notify(observer -> observer.addForActorStack(stack.getFake()));

        if(stack.state().isRejectable())
            forActorDelayed.add(stack);
        else
            forActorAdd(stack);
    }

    public boolean contains(InlinedStackNode stack) {
        return forActorContains(stack) || forActorDelayed.contains(stack);
    }

    public boolean nonEmpty() {
        return forActorNonEmpty() || !forActorDelayed.isEmpty();
    }

    public InlinedStackNode remove() {
        // First return all actors in forActor
        if(forActorNonEmpty())
            return forActorRemove();

        // Then return actors from forActorDelayed
        return forActorDelayed.remove();
    }

    public Iterator<InlinedStackNode> iterator() {
        return Iterables.concat(forActorIterable(), forActorDelayed).iterator();
    }
    
    protected void forActorAdd(InlinedStackNode stack) {
        forActor.add(stack);
    }

    protected boolean forActorContains(InlinedStackNode stack) {
        return forActor.contains(stack);
    }

    protected boolean forActorNonEmpty() {
        return !forActor.isEmpty();
    }

    protected InlinedStackNode forActorRemove() {
        return forActor.remove();
    }

    protected Iterable<InlinedStackNode> forActorIterable() {
        return forActor;
    }
    
}
