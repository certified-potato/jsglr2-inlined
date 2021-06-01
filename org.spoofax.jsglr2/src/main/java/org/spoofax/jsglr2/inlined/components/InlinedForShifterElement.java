package org.spoofax.jsglr2.inlined.components;

import org.metaborg.parsetable.states.IState;

public final class InlinedForShifterElement {
    public final InlinedStackNode stack;
    public final IState state;

    public InlinedForShifterElement(InlinedStackNode stack, IState state) {
        this.stack = stack;
        this.state = state;
    }

}
