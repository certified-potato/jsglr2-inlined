package org.spoofax.jsglr2.inlined;

import org.metaborg.parsetable.IParseTable;
import org.metaborg.parsetable.actions.IReduce;
import org.metaborg.parsetable.states.IState;
import org.spoofax.jsglr2.parseforest.IParseForest;

/**
 * Contains the reduction method.
 */
public class InlinedReduceManager {

    private final IParseTable parseTable;
    private final InlinedStackManager stackManager;
    private final InlinedParseForestManager parseForestManager;
    private final InlinedReducer reducer;
    //private final StatCounter observer;

    InlinedReduceManager(IParseTable parseTable, InlinedStackManager stackManager,
            InlinedParseForestManager parseForestManager) { //, StatCounter observer) {
        this.parseTable = parseTable;
        this.stackManager = stackManager;
        this.parseForestManager = parseForestManager;
        this.reducer = new InlinedReducer(stackManager, parseForestManager);
        //this.observer = observer;
    }
    
    /**
     * Main entry point for the reduce part: walk back along the stack, and try to apply reduction rule. 
     * @param parseState The current state of the parser.
     * @param activeStack Which stack node to do the reduction on.
     * @param reduce The reduction rule to apply.
     */
    void doReductions(InlinedParseState parseState, InlinedStackNode activeStack, IReduce reduce) {
        //not all reduction actions need to actually do anything. Check that first.
        if (ignoreReduceAction(parseState, activeStack, reduce))
            return;

        //observer.doReductions();
        //do the reduction via all links of this stack node
        doReductionsHelper(parseState, activeStack, reduce, null);
    }
    
    /**
     * Like {@linkplain doReductions}, but this method only reduces via a specific link, rather than all of them.
     * @param parseState The current state of the parser.
     * @param activeStack Which stack node to do the reduction on.
     * @param reduce The reduction rule to apply.
     * @param throughLink Via which link of {@linkplain activeStack}} to reduce.
     */
    private void doLimitedReductions(InlinedParseState parseState, InlinedStackNode stack, IReduce reduce,
            InlinedStackLink throughLink) {
        //not all reduction actions need to actually do anything. Check that first.
        if (ignoreReduceAction(parseState, stack, reduce))
            return;

        //observer.doLimitedReductions();

        doReductionsHelper(parseState, stack, reduce, throughLink);
    }
    
    /**
     * Check whether the reduction rule actually needs to be applied.
     */
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

    /**
     * Perform reductions through all the paths of the stack graph.
     * @param parseState The current state of the parser.
     * @param activeStack The stack node to start the reductions from
     * @param reduce the production rule (e.g. "A -> bc") to reduce by
     * @param throughLink if not null, only reduce through paths that contain this link
     */
    private void doReductionsHelper(InlinedParseState parseState, InlinedStackNode activeStack, IReduce reduce,
            InlinedStackLink throughLink) {
        for (InlinedStackPath path : stackManager.findAllPathsOfLength(activeStack, reduce.arity())) {
            if (throughLink == null || path.contains(throughLink)) {
                //get the stack node where the reduction stops.
                InlinedStackNode originStack = path.head();
                //grab the parse forest associated with the path
                IParseForest[] parseNodes = stackManager.getParseForests(parseForestManager, path);
                
                //reduce
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
     * @param parseState The current state of the parser.
     * @param activeStack The stack node to reduce the reductions from.
     * @param originStack The stack node the reduction should end up with.
     * @param reduce The reduction rule.
     * @param The parse forest associated with the current path, that needs to be updated.
     */
    private void reducer(InlinedParseState parseState, InlinedStackNode activeStack, InlinedStackNode originStack,
            IReduce reduce, IParseForest[] parseForests) {
        //check whether the state that will be reduced into, is already contained in the active stack.
        int gotoId = originStack.state().getGotoId(reduce.production().id());
        IState gotoState = parseTable.getState(gotoId);
        InlinedStackNode gotoStack = parseState.activeStacks.findWithState(gotoState);

        if (gotoStack != null) {
            InlinedStackLink directLink = stackManager.findDirectLink(gotoStack, originStack);

            //observer.reducers++;
            
            if (directLink != null) {
                //the goto state is indeed there, and there is even a link already!
                //
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

        //from recoveryObserver
        if(parseState.isRecovering()) {
            int quota = parseState.recoveryJob().getQuota(activeStack);

            if(reduce.production().isRecovery()) {
                quota--;

                parseState.recoveryJob().updateLastRecoveredOffset(gotoStack, parseState.inputStack.offset());
            } else {
                parseState.recoveryJob().updateLastRecoveredOffset(gotoStack,
                    parseState.recoveryJob().lastRecoveredOffset(activeStack));
            }

            parseState.recoveryJob().updateQuota(gotoStack, quota);
        }
    }
}
