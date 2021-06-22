package org.spoofax.jsglr2.inlined;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

class InlinedForActorStacks {
    
    private final InlinedObserving observing;
    
    final ArrayDeque<InlinedStackNode> forActor = new ArrayDeque<>();
    final Queue<InlinedStackNode> forActorDelayed;

    InlinedForActorStacks(InlinedObserving observing) {
        this.observing = observing;
        
        // TODO: implement priority (see P9707 Section 8.4)
        Comparator<InlinedStackNode> stackNodePriorityComparator = (InlinedStackNode stackNode1,
                InlinedStackNode stackNode2) -> 0;

        this.forActorDelayed = new PriorityQueue<>(stackNodePriorityComparator);
    }

    void add(InlinedStackNode stack) {
        observing.notify(observer -> observer.addForActorStack(stack));
        
        if (stack.state().isRejectable()) {
            forActorDelayed.add(stack);
        }
        else {
            forActor.add(stack);
        }
    }

    boolean contains(InlinedStackNode stack) {
        return forActor.contains(stack) || forActorDelayed.contains(stack);
    }

    boolean nonEmpty() {
        return !forActor.isEmpty() || !forActorDelayed.isEmpty();
    }

    InlinedStackNode remove() {
        // First return all actors in forActor
        if (!forActor.isEmpty())
            return forActor.remove();

        // Then return actors from forActorDelayed
        return forActorDelayed.remove();
    }
}
