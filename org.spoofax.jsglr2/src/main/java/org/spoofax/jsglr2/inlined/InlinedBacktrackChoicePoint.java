package org.spoofax.jsglr2.inlined;

import java.util.ArrayList;

final class InlinedBacktrackChoicePoint {
    
    private final int offset;
    private final InlinedInputStack inputStack;
    private final ArrayList<InlinedStackNode> activeStacks;

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
