package org.spoofax.jsglr2.inlined.components;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.metaborg.parsetable.states.IState;
import org.spoofax.jsglr2.inlined.components.observables.InlinedStackNode;
import org.spoofax.jsglr2.inlined.observables.FakeDerivation;
import org.spoofax.jsglr2.inlined.observables.FakeParseForest;
import org.spoofax.jsglr2.inlined.observables.FakeParseNode;
import org.spoofax.jsglr2.inlined.observables.FakeParseState;
import org.spoofax.jsglr2.inlined.observables.FakeStackNode;
import org.spoofax.jsglr2.parser.observing.ParserObserving;
import org.spoofax.jsglr2.stack.collections.ActiveStacksArrayList;
import org.spoofax.jsglr2.stack.collections.IActiveStacks;
import org.spoofax.jsglr2.stack.collections.IForActorStacks;

public class InlinedActiveStacks implements IActiveStacks<FakeStackNode> {
    protected ParserObserving<FakeParseForest, FakeDerivation, FakeParseNode, FakeStackNode, FakeParseState> observing;
    protected ArrayList<InlinedStackNode> activeStacks;
    
    public InlinedActiveStacks(ParserObserving<FakeParseForest, FakeDerivation, FakeParseNode, FakeStackNode, FakeParseState> observing) {
        this.observing = observing;
        this.activeStacks = new ArrayList<>();
    }

    public void add(InlinedStackNode stack) {
        observing.notify(observer -> observer.addActiveStack(stack.getFake()));

        activeStacks.add(stack);
    }

    public boolean isSingle() {
        return activeStacks.size() == 1;
    }

    public InlinedStackNode getSingle() {
        return activeStacks.get(0);
    }

    public boolean isEmpty() {
        return activeStacks.isEmpty();
    }

    public boolean isMultiple() {
        return activeStacks.size() > 1;
    }

    public InlinedStackNode findWithState(IState state) {
        observing.notify(observer -> observer.findActiveStackWithState(state));

        for(InlinedStackNode stack : activeStacks)
            if(stack.state().id() == state.id())
                return stack;

        return null;
    }

    public Iterable<InlinedStackNode> forLimitedReductions(InlinedForActorStacks forActorStacks) {
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

    public void addAllTo(InlinedForActorStacks other) {
        for(InlinedStackNode stack : activeStacks)
            other.add(stack);
    }

    public void clear() {
        activeStacks.clear();
    }

    public Iterator<InlinedStackNode> iterator() {
        return activeStacks.iterator();
    }
    
    @Override
    public void add(FakeStackNode stack) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public FakeStackNode getSingle() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FakeStackNode findWithState(IState state) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<FakeStackNode> forLimitedReductions(IForActorStacks<FakeStackNode> forActorStacks) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addAllTo(IForActorStacks<FakeStackNode> forActorStacks) {
        // TODO Auto-generated method stub
        
    }
}
