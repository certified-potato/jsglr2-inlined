package org.spoofax.jsglr2.inlined;

import java.util.ArrayList;

class InlinedBacktrackChoicePoint {
    
    final int offset;
    final InlinedInputStack inputStack;
    final ArrayList<InlinedStackNode> activeStacks;

    InlinedBacktrackChoicePoint(InlinedInputStack inputStack, InlinedActiveStacks activeStacks) {
        this.offset = inputStack.offset();
        this.inputStack = inputStack;
        this.activeStacks = new ArrayList<>(activeStacks.activeStacks);
    }

    int offset() {
        return offset;
    }

    ArrayList<InlinedStackNode> activeStacks() {
        return activeStacks;
    }

    InlinedInputStack inputStack() {
        return inputStack;
    }

}
