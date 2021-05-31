package org.spoofax.jsglr2.inlined.observables;

import org.spoofax.jsglr2.JSGLR2Request;
import org.spoofax.jsglr2.inputstack.InputStack;
import org.spoofax.jsglr2.parser.AbstractParseState;
import org.spoofax.jsglr2.stack.collections.IActiveStacks;
import org.spoofax.jsglr2.stack.collections.IForActorStacks;

//TODO: improve this glue
public class FakeParseState extends AbstractParseState<InputStack, FakeStackNode> {

    protected FakeParseState(JSGLR2Request request, InputStack inputStack,
            IActiveStacks<FakeStackNode> activeStacks, IForActorStacks<FakeStackNode> forActorStacks) {
        super(request, inputStack, activeStacks, forActorStacks);
        // TODO Auto-generated constructor stub
    }

}
