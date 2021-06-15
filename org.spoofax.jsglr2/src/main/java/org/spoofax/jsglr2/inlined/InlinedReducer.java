package org.spoofax.jsglr2.inlined;

import org.metaborg.parsetable.actions.IReduce;
import org.metaborg.parsetable.states.IState;
import org.spoofax.jsglr2.parseforest.IParseForest;

public class InlinedReducer {

    private final InlinedStackManager stackManager;
    private final InlinedParseForestManager parseForestManager;

    InlinedReducer(InlinedStackManager stackManager, InlinedParseForestManager parseForestManager) {
        this.stackManager = stackManager;
        this.parseForestManager = parseForestManager;
    }

    private boolean skipParseNodeCreation(InlinedParseState parseState, IReduce reduce) {
        return reduce.production().isSkippableInParseForest() && !parseState.isRecovering();
    }
    
    /**
     * Reduce the parse forest nodes, while following a state transition that is already present.
     * @param parseState The current state of the parser.
     * @param reduce The reduction rule.
     * @param existingDirectLinkToActiveStateWithGoto The state transition to follow.
     * @param parseForests the nodes that will be reduced into a single parse node.
     */
    void reducerExistingStackWithDirectLink(InlinedParseState parseState, IReduce reduce,
            InlinedStackLink existingDirectLinkToActiveStateWithGoto, IParseForest[] parseForests) {
        // because the link already exists, there is also a node associated with it.
        // this node can never be a character node, because then either the link does not exist yet,
        // or this link is not part of the active stacks anymore.
        InlinedParseNode parseNode = (InlinedParseNode) existingDirectLinkToActiveStateWithGoto.parseForest;

        if (reduce.isRejectProduction())
            //instead of reducing, reject this link, mark it as invalid.
            stackManager.rejectStackLink(existingDirectLinkToActiveStateWithGoto);
        else if (!existingDirectLinkToActiveStateWithGoto.isRejected()
                && !reduce.production().isSkippableInParseForest()) {
            // because the parse node already exists, with its own derivation
            // a new 'alternative' derivation is added to that node. The two derivations are compared,
            // and the best one is is kept.
            InlinedDerivation derivation = parseForestManager.createDerivation(parseState,
                    existingDirectLinkToActiveStateWithGoto.to, reduce.production(), reduce.productionType(),
                    parseForests);
            parseForestManager.addDerivation(parseState, parseNode, derivation);
        }
    }

    InlinedStackLink reducerExistingStackWithoutDirectLink(InlinedParseState parseState, IReduce reduce,
            InlinedStackNode existingActiveStackWithGotoState, InlinedStackNode stack,
            IParseForest[] parseForests) {
        InlinedStackLink newDirectLinkToActiveStateWithGoto;

        if (reduce.isRejectProduction()) {
            newDirectLinkToActiveStateWithGoto = stackManager.createStackLink(parseState,
                    existingActiveStackWithGotoState, stack,
                    parseForestManager.createSkippedNode(parseState, reduce.production(), parseForests));

            stackManager.rejectStackLink(newDirectLinkToActiveStateWithGoto);
        } else {
            InlinedParseNode parseNode = getParseNode(parseState, reduce, stack, parseForests);

            newDirectLinkToActiveStateWithGoto = stackManager.createStackLink(parseState,
                    existingActiveStackWithGotoState, stack, parseNode);
        }

        return newDirectLinkToActiveStateWithGoto;
    }

    InlinedStackNode reducerNoExistingStack(InlinedParseState parseState, IReduce reduce, InlinedStackNode stack,
            IState gotoState, IParseForest[] parseForests) {
        InlinedStackNode newStackWithGotoState = stackManager.createStackNode(gotoState);

        InlinedStackLink link;

        if (reduce.isRejectProduction()) {
            link = stackManager.createStackLink(parseState, newStackWithGotoState, stack,
                    parseForestManager.createSkippedNode(parseState, reduce.production(), parseForests));

            stackManager.rejectStackLink(link);
        } else {
            InlinedParseNode parseNode = getParseNode(parseState, reduce, stack, parseForests);

            stackManager.createStackLink(parseState, newStackWithGotoState, stack, parseNode);
        }

        return newStackWithGotoState;
    }

    private InlinedParseNode getParseNode(InlinedParseState parseState, IReduce reduce, InlinedStackNode stack,
            IParseForest[] parseForests) {
        InlinedParseNode parseNode;

        if (skipParseNodeCreation(parseState, reduce))
            parseNode = parseForestManager.createSkippedNode(parseState, reduce.production(), parseForests);
        else {
            InlinedDerivation derivation = parseForestManager.createDerivation(parseState, stack, reduce.production(),
                    reduce.productionType(), parseForests);
            parseNode = parseForestManager.createParseNode(parseState, stack, reduce.production(), derivation);
        }

        return parseNode;
    }
}
