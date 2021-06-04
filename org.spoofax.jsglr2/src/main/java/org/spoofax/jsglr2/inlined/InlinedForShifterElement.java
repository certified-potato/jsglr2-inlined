package org.spoofax.jsglr2.inlined;

import org.metaborg.parsetable.states.IState;

final class InlinedForShifterElement {
    final InlinedStackNode stack;
    final IState state;

    InlinedForShifterElement(InlinedStackNode stack, IState state) {
        this.stack = stack;
        this.state = state;
    }

}
