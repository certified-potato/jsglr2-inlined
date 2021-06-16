package org.spoofax.jsglr2.inlined;

import org.spoofax.jsglr2.parser.Position;
import org.spoofax.jsglr2.parser.result.ParseFailureCause;

final class InlinedParseFailureHandler {

    boolean onFailure(InlinedParseState parseState) {
        if (!parseState.isRecovering()) {
            parseState.startRecovery(parseState.request, parseState.inputStack.offset());
            parseState.setAppliedRecovery();
        }
        return parseState.nextRecoveryIteration();
    }

    ParseFailureCause failureCause(InlinedParseState parseState) {
        Position position = parseState.inputStack.safePosition();

        if (parseState.inputStack.offset() < parseState.inputStack.length())
            return new ParseFailureCause(ParseFailureCause.Type.UnexpectedInput, position);
        else
            return new ParseFailureCause(ParseFailureCause.Type.UnexpectedEOF, position);
    }
}
