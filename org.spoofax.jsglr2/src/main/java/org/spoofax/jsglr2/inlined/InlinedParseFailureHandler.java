package org.spoofax.jsglr2.inlined;

import org.spoofax.jsglr2.parser.Position;
import org.spoofax.jsglr2.parser.result.ParseFailureCause;

class InlinedParseFailureHandler {
    
    private final InlinedObserving observing;
    
    public InlinedParseFailureHandler(InlinedObserving observing) {
        this.observing = observing;
    }
    
    boolean onFailure(InlinedParseState parseState) {
        if (!parseState.isRecovering()) {
            parseState.startRecovery(parseState.request, parseState.inputStack.offset());
            parseState.setAppliedRecovery();
            observing.notify(observer -> observer.startRecovery(parseState));
        }
        boolean hasNextIteration = parseState.nextRecoveryIteration();

        if(hasNextIteration)
            observing.notify(o -> o.recoveryIteration(parseState));

        return hasNextIteration;
    }

    ParseFailureCause failureCause(InlinedParseState parseState) {
        Position position = parseState.inputStack.safePosition();

        if (parseState.inputStack.offset() < parseState.inputStack.length())
            return new ParseFailureCause(ParseFailureCause.Type.UnexpectedInput, position);
        else
            return new ParseFailureCause(ParseFailureCause.Type.UnexpectedEOF, position);
    }
}
