package org.spoofax.jsglr2.inlined;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.metaborg.parsetable.states.IState;

class InlinedActiveStacks{
    ArrayList<InlinedStackNode> activeStacks;
    
    InlinedActiveStacks() {
        this.activeStacks = new ArrayList<>();
    }

    void add(InlinedStackNode stack) {
        activeStacks.add(stack);
    }

    boolean isSingle() {
        return activeStacks.size() == 1;
    }

    InlinedStackNode getSingle() {
        return activeStacks.get(0);
    }

    boolean isEmpty() {
        return activeStacks.isEmpty();
    }

    boolean isMultiple() {
        return activeStacks.size() > 1;
    }

    InlinedStackNode findWithState(IState state) {
        for(InlinedStackNode stack : activeStacks)
            if(stack.state().id() == state.id())
                return stack;

        return null;
    }

    Iterable<InlinedStackNode> forLimitedReductions(InlinedForActorStacks forActorStacks) {
        return () -> new Iterator<InlinedStackNode>() {

            int index = 0;

            // Save the number of active stacks to prevent the for loop from processing active stacks that are added
            // by doLimitedReductions. We can safely limit the loop by the current number of stacks since new stack are
            // added at the end.
            final int currentSize = activeStacks.size();

            @Override public boolean hasNext() {
                // skip non-applicable actions
                while(index < currentSize && !(!activeStacks.get(index).allLinksRejected()
                    && !forActorStacks.contains(activeStacks.get(index)))) {
                    index++;
                }
                return index < currentSize;
            }

            @Override public InlinedStackNode next() {
                if(!hasNext()) {
                    throw new NoSuchElementException();
                }
                return activeStacks.get(index++);
            }

        };
    }

    void addAllTo(InlinedForActorStacks other) {
        for(InlinedStackNode stack : activeStacks)
            other.add(stack);
    }

    void clear() {
        activeStacks.clear();
    }

    Iterator<InlinedStackNode> iterator() {
        return activeStacks.iterator();
    }
}
