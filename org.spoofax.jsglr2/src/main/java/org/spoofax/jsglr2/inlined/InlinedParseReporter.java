package org.spoofax.jsglr2.inlined;

import java.util.Collection;
import java.util.List;

import org.spoofax.jsglr2.messages.Message;
import org.spoofax.jsglr2.messages.SourceRegion;
import org.spoofax.jsglr2.parseforest.ParseNodeVisiting;
import org.spoofax.jsglr2.parser.Position;
import org.spoofax.jsglr2.recovery.RecoveryMessages;

public class InlinedParseReporter implements IInlinedParseNodeVisitor {

    static void report(InlinedParseState parseState, InlinedParseNode parseForest, InlinedParseForestManager manager, List<Message> messages) {
        if(parseState.appliedRecovery()) {
            InlinedParseReporter visitor =
                new InlinedParseReporter(messages, parseState.inputStack.inputString());

            manager.visit(parseState.request, parseForest, visitor);
        }
    }
    
    private final Collection<Message> messages;
    private final String inputString;

    private InlinedParseReporter(Collection<Message> messages, String inputString) {
        this.messages = messages;
        this.inputString = inputString;
    }

    @Override public boolean preVisit(InlinedParseNode parseNode, Position startPosition) {
        return !isRecovery(parseNode);
    }

    @Override public void postVisit(InlinedParseNode parseNode, Position startPosition, Position endPosition) {
        if(isRecovery(parseNode)) {
            SourceRegion region = ParseNodeVisiting.visitRegion(inputString, startPosition, endPosition);

            messages.add(RecoveryMessages.get(parseNode.production(), region));
        }
    }

    private boolean isRecovery(InlinedParseNode parseNode) {
        return parseNode.production().isRecovery() || parseNode.production().isWater();
    }
}
