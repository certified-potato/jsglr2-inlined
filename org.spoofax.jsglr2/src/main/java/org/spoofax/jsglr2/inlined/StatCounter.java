package org.spoofax.jsglr2.inlined;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mimics ParserMeasureObserver, for the measurements run
 */
public class StatCounter {

    long length = 0;

    private Set<InlinedStackNode> stackNodes_ = new HashSet<>();
    private Set<InlinedStackLink> stackLinks_ = new HashSet<>();

    long stackNodes = 0;
    long stackNodesSingleLink = 0;
    long stackLinks = 0;
    long stackLinksRejected = 0;

    long actors = 0;

    long doReductions = 0;
    long doLimitedReductions = 0;

    long doReductionsLR = 0;
    long doReductionsDeterministicGLR = 0;
    long doReductionsNonDeterministicGLR = 0;

    long reducers = 0;
    long reducersElkhound = 0;

    long deterministicDepthResets = 0;

    private List<InlinedParseNode> parseNodes_ = new ArrayList<>();

    long parseNodes = 0;
    long parseNodesAmbiguous = 0;
    long parseNodesContextFree = 0;
    long parseNodesContextFreeAmbiguous = 0;
    long parseNodesLexical = 0;
    long parseNodesLexicalAmbiguous = 0;
    long parseNodesLiteral = 0;
    long parseNodesLiteralAmbiguous = 0;
    long parseNodesLayout = 0;
    long parseNodesLayoutAmbiguous = 0;
    long parseNodesSingleDerivation = 0;
    long characterNodes = 0;

    private long stackNodeLinkCount(InlinedStackNode stackNode) {
        long linksOutCount = 0;

        for (InlinedStackLink link : stackNode.getLinks())
            linksOutCount++;

        return linksOutCount;
    }

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

            if (stackNodeLinkCount(stackNode) == 1)
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
            if (parseNode.getDerivations().size() == 1)
                parseNodesSingleDerivation++;
        }

        stackNodes_.clear();
        stackLinks_.clear();
        parseNodes_.clear();
    }
}
