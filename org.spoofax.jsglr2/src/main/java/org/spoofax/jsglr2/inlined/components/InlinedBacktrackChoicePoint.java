package org.spoofax.jsglr2.inlined.components;

import java.util.ArrayList;

public class InlinedBacktrackChoicePoint {
    
    public final int offset;
    public final InlinedInputStack inputStack;
    public final ArrayList<InlinedStackNode> activeStacks;

    public InlinedBacktrackChoicePoint(InlinedInputStack inputStack, InlinedActiveStacks activeStacks) {
        this.offset = inputStack.offset();
        this.inputStack = inputStack;
        this.activeStacks = new ArrayList<>(activeStacks.activeStacks);
    }

    public int offset() {
        return offset;
    }

    public ArrayList<InlinedStackNode> activeStacks() {
        return activeStacks;
    }

    public InlinedInputStack inputStack() {
        return inputStack;
    }

}
