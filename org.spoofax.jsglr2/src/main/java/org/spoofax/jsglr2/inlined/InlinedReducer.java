package org.spoofax.jsglr2.inlined;

import org.metaborg.parsetable.actions.IReduce;
import org.metaborg.parsetable.states.IState;
import org.spoofax.jsglr2.parseforest.IParseForest;

public class InlinedReducer {

    private final InlinedStackManager stackManager;
    private final InlinedParseForestManager parseForestManager;

    public InlinedReducer(InlinedStackManager stackManager, InlinedParseForestManager parseForestManager) {
        this.stackManager = stackManager;
        this.parseForestManager = parseForestManager;
    }

    protected boolean skipParseNodeCreation(InlinedParseState parseState, IReduce reduce) {
        return reduce.production().isSkippableInParseForest() && !parseState.isRecovering();
    }

    public void reducerExistingStackWithDirectLink(InlinedParseState parseState, IReduce reduce,
            InlinedStackLink existingDirectLinkToActiveStateWithGoto, IParseForest[] parseForests) {
        InlinedParseNode parseNode = (InlinedParseNode) existingDirectLinkToActiveStateWithGoto.parseForest;

        if (reduce.isRejectProduction())
            stackManager.rejectStackLink(existingDirectLinkToActiveStateWithGoto);
        else if (!existingDirectLinkToActiveStateWithGoto.isRejected()
                && !reduce.production().isSkippableInParseForest()) {
            InlinedDerivation derivation = parseForestManager.createDerivation(parseState,
                    existingDirectLinkToActiveStateWithGoto.to, reduce.production(), reduce.productionType(),
                    parseForests);
            parseForestManager.addDerivation(parseState, parseNode, derivation);
        }
    }

    public InlinedStackLink reducerExistingStackWithoutDirectLink(InlinedParseState parseState, IReduce reduce,
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

    public InlinedStackNode reducerNoExistingStack(InlinedParseState parseState, IReduce reduce, InlinedStackNode stack,
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
