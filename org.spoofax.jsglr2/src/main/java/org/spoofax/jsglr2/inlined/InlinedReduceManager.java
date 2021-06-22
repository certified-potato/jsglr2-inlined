package org.spoofax.jsglr2.inlined;

import org.metaborg.parsetable.IParseTable;
import org.metaborg.parsetable.actions.IReduce;
import org.metaborg.parsetable.states.IState;
import org.spoofax.jsglr2.parseforest.IParseForest;

public class InlinedReduceManager {

    protected final IParseTable parseTable;
    protected final InlinedStackManager stackManager;
    protected final InlinedParseForestManager parseForestManager;
    protected final InlinedReducer reducer;
    private final InlinedObserving observing;

    public InlinedReduceManager(IParseTable parseTable, InlinedStackManager stackManager,
            InlinedParseForestManager parseForestManager, InlinedObserving observing) {
        this.parseTable = parseTable;
        this.stackManager = stackManager;
        this.parseForestManager = parseForestManager;
        this.reducer = new InlinedReducer(stackManager, parseForestManager);
        this.observing = observing;
    }

    public void doReductions(InlinedParseState parseState, InlinedStackNode activeStack, IReduce reduce) {
        if (ignoreReduceAction(parseState, activeStack, reduce))
            return;

        observing.notify(o -> o.doReductions(parseState, activeStack, reduce));

        doReductionsHelper(parseState, activeStack, reduce, null);
    }

    private void doLimitedReductions(InlinedParseState parseState, InlinedStackNode stack, IReduce reduce,
            InlinedStackLink throughLink) {
        if (ignoreReduceAction(parseState, stack, reduce))
            return;

        observing.notify(o -> o.doLimitedReductions(parseState, stack, reduce, throughLink));

        doReductionsHelper(parseState, stack, reduce, throughLink);
    }

    private boolean ignoreReduceAction(InlinedParseState parseState, InlinedStackNode stack, IReduce reduce) {
        if (reduce.production().isCompletion())
            return true;

        if (!reduce.production().isRecovery())
            return false; // Regular productions can always be used
        if (!parseState.isRecovering())
            return true; // Ignore recovery productions outside recovery mode
        if (parseState.recoveryJob().getQuota(stack) <= 0)
            return true; // Ignore recovery productions after quota exceeded
        if (!(parseState.inputStack.offset() > parseState.recoveryJob().lastRecoveredOffset(stack)))
            return true; // Prevent multiple recover reductions at the same location

        return false;
    }

    protected void doReductionsHelper(InlinedParseState parseState, InlinedStackNode activeStack, IReduce reduce,
            InlinedStackLink throughLink) {
        for (InlinedStackPath path : stackManager.findAllPathsOfLength(activeStack, reduce.arity())) {
            if (throughLink == null || path.contains(throughLink)) {
                InlinedStackNode originStack = path.head();
                IParseForest[] parseNodes = stackManager.getParseForests(parseForestManager, path);

                reducer(parseState, activeStack, originStack, reduce, parseNodes);
            }
        }
    }

    /**
     * Perform a reduction for the given reduce action and parse forests. The reduce
     * action contains which production will be reduced and the parse forests
     * represent the right hand side of this production. The reduced derivation will
     * end up on a stack link from the given stack to a stack with the goto state.
     * The latter can already exist or not and if such an active stack already
     * exists, the link to it can also already exist. Based on the existence of the
     * stack with the goto state and the link to it, different actions are
     * performed.
     */
    protected void reducer(InlinedParseState parseState, InlinedStackNode activeStack, InlinedStackNode originStack,
            IReduce reduce, IParseForest[] parseForests) {
        int gotoId = originStack.state().getGotoId(reduce.production().id());
        IState gotoState = parseTable.getState(gotoId);

        InlinedStackNode gotoStack = parseState.activeStacks.findWithState(gotoState);

        if (gotoStack != null) {
            InlinedStackLink directLink = stackManager.findDirectLink(gotoStack, originStack);

            observing.notify(o -> o.directLinkFound(parseState, directLink));
            
            if (directLink != null) {
                reducer.reducerExistingStackWithDirectLink(parseState, reduce, directLink, parseForests);
            } else {
                InlinedStackLink link = reducer.reducerExistingStackWithoutDirectLink(parseState, reduce, gotoStack,
                        originStack, parseForests);

                for (InlinedStackNode activeStackForLimitedReductions : parseState.activeStacks
                        .forLimitedReductions(parseState.forActorStacks)) {
                    for (IReduce reduceAction : activeStackForLimitedReductions.state()
                            .getApplicableReduceActions(parseState.inputStack, parseState.mode))
                        doLimitedReductions(parseState, activeStackForLimitedReductions, reduceAction, link);
                }
            }
        } else {
            gotoStack = reducer.reducerNoExistingStack(parseState, reduce, originStack, gotoState, parseForests);

            parseState.activeStacks.add(gotoStack);
            parseState.forActorStacks.add(gotoStack);
        }
        
        InlinedStackNode fgs = gotoStack;
        observing.notify(o -> o.reducer(parseState, activeStack, originStack, reduce, parseForests, fgs));
    }
}
