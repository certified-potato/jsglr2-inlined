package org.spoofax.jsglr2.inlined.observables;

import org.spoofax.jsglr2.JSGLR2Request;
import org.spoofax.jsglr2.inputstack.InputStack;
import org.spoofax.jsglr2.parser.AbstractParseState;
import org.spoofax.jsglr2.stack.collections.IActiveStacks;
import org.spoofax.jsglr2.stack.collections.IForActorStacks;

public class InlinedFakeParseState extends AbstractParseState<InputStack, InlinedFakeStackNode> {

    protected InlinedFakeParseState(JSGLR2Request request, InputStack inputStack,
            IActiveStacks<InlinedFakeStackNode> activeStacks, IForActorStacks<InlinedFakeStackNode> forActorStacks) {
        super(request, inputStack, activeStacks, forActorStacks);
        // TODO Auto-generated constructor stub
    }

}
