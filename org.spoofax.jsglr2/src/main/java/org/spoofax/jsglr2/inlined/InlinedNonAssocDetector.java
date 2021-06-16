package org.spoofax.jsglr2.inlined;

import java.util.Collection;

import org.spoofax.jsglr2.messages.Category;
import org.spoofax.jsglr2.messages.Message;
import org.spoofax.jsglr2.parseforest.IParseForest;
import org.spoofax.jsglr2.parser.Position;
import org.spoofax.jsglr2.parser.result.ParseFailureCause;

final class InlinedNonAssocDetector implements IInlinedParseNodeVisitor {

    private final Collection<Message> messages;

    private ParseFailureCause.Type failure = null;

    InlinedNonAssocDetector(Collection<Message> messages) {
        this.messages = messages;
    }

    @Override public boolean preVisit(InlinedParseNode parseNode, Position startPosition) {
        if(hasNonAssoc(parseNode)) {
            // Because we return false here, the children of this parseNode are not visited,
            // and the postVisit method will be called on the same parseNode directly after this method returns.
            // Therefore, we can temporarily store the failure type in a local field
            // to avoid having to call the hasNonAssoc method twice. Ugly, but works! /shrug
            failure = ParseFailureCause.Type.NonAssoc;
            return false;
        } else if(hasNonNested(parseNode)) {
            failure = ParseFailureCause.Type.NonNested;
            return false;
        } else {
            return parseNode.production().isContextFree();
        }
    }


    @Override public void postVisit(InlinedParseNode parseNode, Position startPosition, Position endPosition) {
        if(failure != null) {
            messages.add(new Message(failure.message, Category.NON_ASSOC, startPosition, endPosition));
            failure = null;
        }
    }

    private boolean hasNonAssoc(InlinedParseNode parseNode) {
        for(InlinedDerivation derivation : parseNode.getDerivations()) {
            IParseForest[] children = derivation.parseForests();
            if(children.length == 0)
                continue;
            IParseForest firstChild = children[0];
            if(firstChild instanceof InlinedParseNode
                && derivation.production().isNonAssocWith(((InlinedParseNode) firstChild).production()))
                return true;
        }
        return false;
    }

    private boolean hasNonNested(InlinedParseNode parseNode) {
        for(InlinedDerivation derivation : parseNode.getDerivations()) {
            IParseForest[] children = derivation.parseForests();
            if(children.length == 0)
                continue;
            IParseForest lastChild = children[children.length - 1];
            if(lastChild instanceof InlinedParseNode
                && derivation.production().isNonNestedWith(((InlinedParseNode) lastChild).production()))
                return true;
        }
        return false;
    }


}
