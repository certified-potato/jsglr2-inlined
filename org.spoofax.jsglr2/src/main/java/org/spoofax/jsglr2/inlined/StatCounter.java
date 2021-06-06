package org.spoofax.jsglr2.inlined;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mimics ParserMeasureObserver, for the measurements run
 */
public class StatCounter {

    public long length = 0;

    private Set<InlinedStackNode> stackNodes_ = new HashSet<>();
    private Set<InlinedStackLink> stackLinks_ = new HashSet<>();

    public long stackNodes = 0;
    public long stackNodesSingleLink = 0;
    public long stackLinks = 0;
    public long stackLinksRejected = 0;

    public long actors = 0;

    public long doReductions = 0;
    public long doLimitedReductions = 0;

    public long doReductionsLR = 0;
    public long doReductionsDeterministicGLR = 0;
    public long doReductionsNonDeterministicGLR = 0;

    public long reducers = 0;
    public long reducersElkhound = 0;

    public long deterministicDepthResets = 0;

    private List<InlinedParseNode> parseNodes_ = new ArrayList<>();

    public long parseNodes = 0;
    public long parseNodesAmbiguous = 0;
    public long parseNodesContextFree = 0;
    public long parseNodesContextFreeAmbiguous = 0;
    public long parseNodesLexical = 0;
    public long parseNodesLexicalAmbiguous = 0;
    public long parseNodesLiteral = 0;
    public long parseNodesLiteralAmbiguous = 0;
    public long parseNodesLayout = 0;
    public long parseNodesLayoutAmbiguous = 0;
    public long parseNodesSingleDerivation = 0;
    public long characterNodes = 0;

    public long activeStacksAdds = 0;
    public long activeStacksMaxSize = 0;
    public long activeStacksIsEmptyChecks = 0;
    public long activeStacksFindsWithState = 0;
    public long activeStacksForLimitedReductions = 0;
    public long activeStacksAddAllTo = 0;
    public long activeStacksClears = 0;
    public long forActorAdds = 0;
    public long forActorDelayedAdds = 0;
    public long forActorMaxSize = 0;
    public long forActorDelayedMaxSize = 0;
    public long forActorContainsChecks = 0;
    public long forActorNonEmptyChecks = 0;

    public void parseStart(InlinedParseState parseState) {
        length += parseState.inputStack.inputString().length();
    }

    public void createStackNode(InlinedStackNode stack) {
        stackNodes_.add(stack);
    }

    public void createStackLink(InlinedStackLink link) {
        stackLinks_.add(link);
    }

    public void rejectStackLink() {
        stackLinksRejected++;
    }

    public void actor() {
        actors++;
    }

    public void doReductions() {
        doReductions++;
    }

    public void doLimitedReductions() {
        doLimitedReductions++;
    }

    public void reducer() {
        reducers++;
    }

    public void createParseNode(InlinedParseNode parseNode) {
        if (parseNode.production() != null) // Do not record temporary parse nodes created by the incremental parser
            parseNodes_.add(parseNode);
    }

    public void createCharacterNode() {
        characterNodes++;
    }

    public void success() {
        for (InlinedStackNode stackNode : stackNodes_) {
            stackNodes++;

            if (stackNode.getLinksSize() == 1)
                stackNodesSingleLink++;
        }

        for (InlinedStackLink stackLink : stackLinks_) {
            stackLinks++;

            if (stackLink.isRejected())
                stackLinksRejected++;
        }

        for (InlinedParseNode parseNode : parseNodes_) {
            parseNodes++;

            boolean ambiguous = parseNode.isAmbiguous();

            if (ambiguous)
                parseNodesAmbiguous++;

            switch (parseNode.production().concreteSyntaxContext()) {
            case ContextFree:
                parseNodesContextFree++;

                if (ambiguous)
                    parseNodesContextFreeAmbiguous++;
                break;
            case Lexical:
                parseNodesLexical++;

                if (ambiguous)
                    parseNodesLexicalAmbiguous++;
                break;
            case Layout:
                parseNodesLayout++;

                if (ambiguous)
                    parseNodesLayoutAmbiguous++;
                break;
            case Literal:
                parseNodesLiteral++;

                if (ambiguous)
                    parseNodesLiteralAmbiguous++;
                break;
            }
            int i = 0;
            for(InlinedDerivation d : parseNode.getDerivations())
                i++;
            if(i == 1)
                parseNodesSingleDerivation++;
        }

        stackNodes_.clear();
        stackLinks_.clear();
        parseNodes_.clear();
    }
}
