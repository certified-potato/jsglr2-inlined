package org.spoofax.jsglr2.inlined;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

import com.google.common.collect.Iterables;

class InlinedForActorStacks {
    private final InlinedObserver observing;
    final ArrayDeque<InlinedStackNode> forActor = new ArrayDeque<>();
    final Queue<InlinedStackNode> forActorDelayed;

    InlinedForActorStacks(InlinedObserver observing) {
        this.observing = observing;

        // TODO: implement priority (see P9707 Section 8.4)
        Comparator<InlinedStackNode> stackNodePriorityComparator = (InlinedStackNode stackNode1,
                InlinedStackNode stackNode2) -> 0;

        this.forActorDelayed = new PriorityQueue<>(stackNodePriorityComparator);
    }

    void add(InlinedStackNode stack) {
        // observing.notify(observer -> observer.addForActorStack(stack.getFake()));

        if (stack.state().isRejectable())
            forActorDelayed.add(stack);
        else
            forActorAdd(stack);
    }

    boolean contains(InlinedStackNode stack) {
        return forActorContains(stack) || forActorDelayed.contains(stack);
    }

    boolean nonEmpty() {
        return forActorNonEmpty() || !forActorDelayed.isEmpty();
    }

    InlinedStackNode remove() {
        // First return all actors in forActor
        if (forActorNonEmpty())
            return forActorRemove();

        // Then return actors from forActorDelayed
        return forActorDelayed.remove();
    }

    Iterator<InlinedStackNode> iterator() {
        return Iterables.concat(forActorIterable(), forActorDelayed).iterator();
    }

    void forActorAdd(InlinedStackNode stack) {
        forActor.add(stack);
    }

    boolean forActorContains(InlinedStackNode stack) {
        return forActor.contains(stack);
    }

    boolean forActorNonEmpty() {
        return !forActor.isEmpty();
    }

    InlinedStackNode forActorRemove() {
        return forActor.remove();
    }

    Iterable<InlinedStackNode> forActorIterable() {
        return forActor;
    }

}
