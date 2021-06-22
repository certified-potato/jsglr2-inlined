package org.spoofax.jsglr2.measure.parsing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.metaborg.parsetable.actions.IAction;
import org.metaborg.parsetable.actions.IReduce;
import org.metaborg.parsetable.productions.IProduction;
import org.spoofax.jsglr2.inlined.IInlinedObserver;
import org.spoofax.jsglr2.inlined.InlinedParseNode;
import org.spoofax.jsglr2.inlined.InlinedParseState;
import org.spoofax.jsglr2.inlined.InlinedStackLink;
import org.spoofax.jsglr2.inlined.InlinedStackNode;
import org.spoofax.jsglr2.parseforest.IParseForest;
import org.spoofax.jsglr2.parser.result.ParseFailure;
import org.spoofax.jsglr2.parser.result.ParseSuccess;

public class InlinedMeasureObserver implements IInlinedObserver {
	
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

    long stackNodeLinkCount(InlinedStackNode stackNode) {
        long linksOutCount = 0;

        for(InlinedStackLink link : stackNode.getLinks())
            linksOutCount++;

        return linksOutCount;
    }

    @Override public void parseStart(InlinedParseState parseState) {
        length += parseState.inputStack.inputString().length();
    }

    @Override public void createStackNode(InlinedStackNode stack) {
        stackNodes_.add(stack);
    }

    @Override public void createStackLink(InlinedStackLink link) {
        stackLinks_.add(link);
    }

    @Override public void rejectStackLink(InlinedStackLink link) {
        stackLinksRejected++;
    }

    @Override public void actor(InlinedStackNode stack, InlinedParseState parseState, Iterable<IAction> applicableActions) {
        actors++;
    }

    @Override public void doReductions(InlinedParseState parseState, InlinedStackNode stack, IReduce reduce) {
        doReductions++;
    }

    @Override public void doLimitedReductions(InlinedParseState parseState, InlinedStackNode stack, IReduce reduce,
        InlinedStackLink throughLink) {
        doLimitedReductions++;
    }

    @Override public void reducer(InlinedParseState parseState, InlinedStackNode activeStack, InlinedStackNode originStack, IReduce reduce,
        IParseForest[] parseNodes, InlinedStackNode gotoStack) {
        reducers++;
    }

    @Override public void createParseNode(InlinedParseNode parseNode, IProduction production) {
        if(parseNode.production() != null) // Do not record temporary parse nodes created by the incremental parser
            parseNodes_.add(parseNode);
    }

    @Override public void createCharacterNode(IParseForest characterNode, int character) {
        characterNodes++;
    }

    @Override public void success(ParseSuccess<IParseForest> success) {
        for(InlinedStackNode stackNode : stackNodes_) {
            stackNodes++;

            if(stackNodeLinkCount(stackNode) == 1)
                stackNodesSingleLink++;
        }

        for(InlinedStackLink stackLink : stackLinks_) {
            stackLinks++;

            if(stackLink.isRejected())
                stackLinksRejected++;
        }

        for(InlinedParseNode parseNode : parseNodes_) {
            parseNodes++;

            boolean ambiguous = parseNode.isAmbiguous();

            if(ambiguous)
                parseNodesAmbiguous++;

            switch(parseNode.production().concreteSyntaxContext()) {
                case ContextFree:
                    parseNodesContextFree++;

                    if(ambiguous)
                        parseNodesContextFreeAmbiguous++;
                    break;
                case Lexical:
                    parseNodesLexical++;

                    if(ambiguous)
                        parseNodesLexicalAmbiguous++;
                    break;
                case Layout:
                    parseNodesLayout++;

                    if(ambiguous)
                        parseNodesLayoutAmbiguous++;
                    break;
                case Literal:
                    parseNodesLiteral++;

                    if(ambiguous)
                        parseNodesLiteralAmbiguous++;
                    break;
            }

            int derivationCount = 0;

            for(Object derivation : parseNode.getDerivations())
                derivationCount++;

            if(derivationCount == 1)
                parseNodesSingleDerivation++;
        }

        stackNodes_.clear();
        stackLinks_.clear();
        parseNodes_.clear();
    }

    @Override public void failure(ParseFailure<IParseForest> failure) {
        throw new IllegalStateException("Parsing failed on " + failure.parseState.request.fileName);
    }

}
