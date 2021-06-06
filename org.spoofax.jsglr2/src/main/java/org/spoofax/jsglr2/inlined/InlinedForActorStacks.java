package org.spoofax.jsglr2.inlined;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

class InlinedForActorStacks {
    
    private final StatCounter counter;
    
    final ArrayDeque<InlinedStackNode> forActor = new ArrayDeque<>();
    final Queue<InlinedStackNode> forActorDelayed;

    InlinedForActorStacks(StatCounter counter) {
        this.counter = counter;
        
        // TODO: implement priority (see P9707 Section 8.4)
        Comparator<InlinedStackNode> stackNodePriorityComparator = (InlinedStackNode stackNode1,
                InlinedStackNode stackNode2) -> 0;

        this.forActorDelayed = new PriorityQueue<>(stackNodePriorityComparator);
    }

    void add(InlinedStackNode stack) {
        if (stack.state().isRejectable()) {
            forActorDelayed.add(stack);
            counter.forActorDelayedAdds++;
        }
        else {
            forActor.add(stack);
            counter.forActorAdds++;
        }
        counter.forActorMaxSize = Math.max(counter.forActorMaxSize, forActor.size());
        counter.forActorDelayedMaxSize = Math.max(counter.forActorDelayedMaxSize, forActorDelayed.size());
    }

    boolean contains(InlinedStackNode stack) {
        counter.forActorContainsChecks++;
        return forActor.contains(stack) || forActorDelayed.contains(stack);
    }

    boolean nonEmpty() {
        counter.forActorNonEmptyChecks++;
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
