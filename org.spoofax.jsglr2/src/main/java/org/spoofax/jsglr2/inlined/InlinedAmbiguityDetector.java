package org.spoofax.jsglr2.inlined;

import java.util.Collection;

import org.spoofax.jsglr2.messages.Category;
import org.spoofax.jsglr2.messages.Message;
import org.spoofax.jsglr2.messages.SourceRegion;
import org.spoofax.jsglr2.parseforest.ParseNodeVisiting;
import org.spoofax.jsglr2.parser.Position;

final class InlinedAmbiguityDetector implements IInlinedParseNodeVisitor {
    private final String inputString;
    private final Collection<Message> messages;

    InlinedAmbiguityDetector(String inputString, Collection<Message> messages) {
        this.inputString = inputString;
        this.messages = messages;
    }

    @Override
    public void postVisit(InlinedParseNode parseNode, Position startPosition, Position endPosition) {
        if(parseNode.isAmbiguous()) {
            String message;

            if(parseNode.production().isContextFree() && parseNode.getPreferredAvoidedDerivations().size() == 1)
                return;

            switch(parseNode.production().concreteSyntaxContext()) {
                case Lexical:
                case Literal:
                    message = "Lexical ambiguity";
                    break;
                case Layout:
                    message = "Layout ambiguity";
                    break;
                case ContextFree:
                default:
                    message = "Ambiguity";
                    break;
            }

            SourceRegion region = ParseNodeVisiting.visitRegion(inputString, startPosition, endPosition);

            messages.add(new Message(message, Category.AMBIGUITY, region));
        }
    }

    @Override
    public boolean preVisit(InlinedParseNode parseNode, Position startPosition) {
        return true;
    }
}
