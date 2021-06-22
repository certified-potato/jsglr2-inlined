package org.spoofax.jsglr2.inlined;

import org.metaborg.parsetable.actions.IReduce;
import org.spoofax.jsglr2.parseforest.IParseForest;

public class InlinedRecoveryObserver implements IInlinedObserver {
    
    @Override public void reducer(InlinedParseState parseState, InlinedStackNode activeStack, InlinedStackNode originStack, IReduce reduce,
            IParseForest[] parseNodes, InlinedStackNode gotoStack) {
            if(parseState.isRecovering()) {
                int quota = parseState.recoveryJob().getQuota(activeStack);

                if(reduce.production().isRecovery()) {
                    quota--;

                    parseState.recoveryJob().updateLastRecoveredOffset(gotoStack, parseState.inputStack.offset());
                } else {
                    parseState.recoveryJob().updateLastRecoveredOffset(gotoStack,
                        parseState.recoveryJob().lastRecoveredOffset(activeStack));
                }

                parseState.recoveryJob().updateQuota(gotoStack, quota);
            }
        }

        @Override public void shift(InlinedParseState parseState, InlinedStackNode originStack, InlinedStackNode gotoStack) {
            if(parseState.isRecovering()) {
                int quota = parseState.recoveryJob().getQuota(originStack);
                int lastRecoveredOffset = parseState.recoveryJob().lastRecoveredOffset(originStack);

                parseState.recoveryJob().updateQuota(gotoStack, quota);
                parseState.recoveryJob().updateLastRecoveredOffset(gotoStack, lastRecoveredOffset);
            }
        }
}
