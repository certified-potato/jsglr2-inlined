package org.spoofax.jsglr2.parser;

import org.spoofax.jsglr2.stack.AbstractStackNode;
import org.spoofax.jsglr2.states.IState;

public final class ForShifterElement<StackNode extends AbstractStackNode<ParseForest>, ParseForest> {

    public final StackNode stack;
    public final IState state;

    public ForShifterElement(StackNode stack, IState state) {
        this.stack = stack;
        this.state = state;
    }

}
