package org.spoofax.jsglr2.inlined;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.metaborg.parsetable.IParseTable;
import org.metaborg.parsetable.actions.IAction;
import org.metaborg.parsetable.actions.IReduce;
import org.metaborg.parsetable.actions.IShift;
import org.metaborg.parsetable.states.IState;
import org.spoofax.jsglr2.JSGLR2Request;
import org.spoofax.jsglr2.messages.Message;
import org.spoofax.jsglr2.parseforest.IParseForest;
import org.spoofax.jsglr2.parseforest.ParseNodeVisitor;
import org.spoofax.jsglr2.parser.IParser;
import org.spoofax.jsglr2.parser.ParseException;
import org.spoofax.jsglr2.parser.result.ParseFailure;
import org.spoofax.jsglr2.parser.result.ParseFailureCause;
import org.spoofax.jsglr2.parser.result.ParseResult;
import org.spoofax.jsglr2.parser.result.ParseSuccess;
import org.spoofax.jsglr2.tokens.Tokens;
import org.spoofax.terms.util.NotImplementedException;

public class InlinedParser implements IParser<IParseForest> {

    // public final StatCounter observer = new StatCounter();
    protected final IParseTable parseTable;
    protected final InlinedStackManager stackManager;
    protected final InlinedParseForestManager parseForestManager;
    public final InlinedReduceManager reduceManager;
    protected final InlinedParseFailureHandler failureHandler;
    InlinedParseState parseState;

    public InlinedParser(IParseTable table) {
        parseTable = table;
        stackManager = new InlinedStackManager();
        parseForestManager = new InlinedParseForestManager();
        reduceManager = new InlinedReduceManager(table, stackManager, parseForestManager);
        failureHandler = new InlinedParseFailureHandler();
    }

    /**
     * Main parse method.
     * 
     * @param request        The input to parse.
     * @param previousInput  ignored.
     * @param previousResult ignored.
     * @return either a {@link ParseSuccess} or a {@link ParseFailure} representing
     *         the result.
     */
    @Override
    public ParseResult<IParseForest> parse(JSGLR2Request request, String previousInput, IParseForest previousResult) {
        parseState = new InlinedParseState(request, new InlinedInputStack(request.input));

        // observer.parseStart(parseState);

        // start keeping track in which cell of the parse table the parse state is.
        InlinedStackNode initialStackNode = stackManager.createStackNode(parseTable.getStartState());
        parseState.activeStacks.add(initialStackNode);

        boolean recover;

        try {
            do {
                // parse the entire text
                parseLoop(parseState);

                // did the parser find a successful parse?
                if (parseState.acceptingStack == null)
                    // failed to parse, try to recover
                    recover = failureHandler.onFailure(parseState);
                else
                    recover = false;
            } while (recover); // retry parsing until recovery gives up

            // did the parser find a successful parse, even if it had to recover?
            if (parseState.acceptingStack != null) {
                // get the top most non-terminal
                InlinedParseNode topParseNode = (InlinedParseNode) stackManager
                        .findDirectLink(parseState.acceptingStack, initialStackNode).parseForest;

                // if the request asked for it, filter out derivations that don't start with a
                // specific non-terminal symbol
                InlinedParseNode parseForestWithStartSymbol = request.startSymbol != null
                        ? parseForestManager.filterStartSymbol(topParseNode, request.startSymbol, parseState)
                        : topParseNode;

                // the parse was successful, but it did not start with this symbol
                if (topParseNode != null && parseForestWithStartSymbol == null)
                    return failure(new ParseFailureCause(ParseFailureCause.Type.InvalidStartSymbol));
                else
                    // there is a valid parse, but now it needs to be checked for faulty behaviour.
                    return complete(parseState, parseForestWithStartSymbol);
            } else
                // parse failed, retrieve the error from the state.
                return failure(failureHandler.failureCause(parseState));
        } catch (ParseException e) {
            // parse failed, because recovery timed out.
            return failure(e.cause);
        }
    }

    /**
     * In other parsers, this method lets you to traverse and inspect the parse
     * tree. But it is non-functional here.
     */
    @Override
    public void visit(ParseSuccess<?> success, ParseNodeVisitor<?, ?, ?> visitor) {
        throw new NotImplementedException("This parser uses its own parse nodes");
    }

    /**
     * Do post-processing on the parse forest, by checking on errors
     * 
     * @param parseState  The state of the parser.
     * @param parseForest The top/root parse node.
     * @return Whether the parse was indeed successful, or whether there is
     *         something wrong with the parse forest.
     */
    private ParseResult<IParseForest> complete(InlinedParseState parseState, InlinedParseNode parseForest) {
        List<Message> messages = new ArrayList<>();
        InlinedCycleDetector cycleDetector = new InlinedCycleDetector(messages);

        // check whether the parse forest has cycles (shouldn't ever happen, error means
        // something is wrong with the parser or parse table itself)
        parseForestManager.visit(parseState.request, parseForest, cycleDetector);

        if (cycleDetector.cycleDetected()) {
            return failure(cycleDetector.failureCause);
        } else {
            // if recovery was applied, then add the error messages for the unexpected or
            // missing parts.
            InlinedParseReporter.report(parseState, parseForest, parseForestManager, messages);

            // Generate errors for non-assoc or non-nested productions that are used
            // associatively
            parseForestManager.visit(parseState.request, parseForest, new InlinedNonAssocDetector(messages));

            if (parseState.request.reportAmbiguities) {
                // Generate warnings for ambiguous parse nodes
                parseForestManager.visit(parseState.request, parseForest,
                        new InlinedAmbiguityDetector(parseState.inputStack.inputString(), messages));
            }

            ParseSuccess<IParseForest> success = new ParseSuccess<>(null, parseForest, messages);

            // observer.success();

            return success;
        }
    }

    private ParseFailure<IParseForest> failure(ParseFailureCause failureCause) {
        return new ParseFailure<>(null, failureCause);
    }

    /**
     * Run the parsing, from start, till end.
     * 
     * @param parseState The current state of the parser.
     * @throws ParseException The parser is in recovery mode, and recovery has timed
     *                        out.
     */
    private void parseLoop(InlinedParseState parseState) throws ParseException {
        // while there is anything left to parse
        while (parseState.inputStack.hasNext() && !parseState.activeStacks.isEmpty()) {
            parseCharacter(parseState);
            if (!parseState.activeStacks.isEmpty())
                parseState.inputStack.next();
        }
    }

    /**
     * Add the next character from the input text to the stack, and decide how to
     * handle it.
     * 
     * @param parseState the current state of the parser.
     * @throws ParseException The parser is in recovery mode, and recovery has timed
     *                        out.
     */
    private void parseCharacter(InlinedParseState parseState) throws ParseException {
        // create a backtrack point for the recovery mechanism
        parseState.nextParseRound();

        // add the character to all parallel stacks
        parseState.activeStacks.addAllTo(parseState.forActorStacks);

        while (parseState.forActorStacks.nonEmpty()) {
            // grab a stack node from the stacks to be processed
            InlinedStackNode stack = parseState.forActorStacks.remove();

            // and process it if it has not been rejected
            if (!stack.allLinksRejected())
                actor(stack, parseState);
        }

        // finally, shift in the new character into the stack.
        shifter(parseState);
    }

    /**
     * Read the stack and apply a rule on in from the parse table
     * 
     * @param stack      The characters that have been read so far, and how they
     *                   were parsed.
     * @param parseState The current state of the parse.
     */
    private void actor(InlinedStackNode stack, InlinedParseState parseState) {
        // observer.actor();
        stack.state().getApplicableActions(parseState.inputStack, parseState.mode);

        // if the grammar is ambiguous, then there can be multiple actions:
        // therefore, do them in all in parallel.
        for (IAction action : stack.state().getApplicableActions(parseState.inputStack, parseState.mode))
            actor(stack, parseState, action);
    }

    /**
     * Apply an action from the parse table upon the current stack
     * 
     * @param stack      The characters that have been read so far, and how they
     *                   were parsed.
     * @param parseState The current state of the parse.
     * @param action     what action to apply.
     */
    private void actor(InlinedStackNode stack, InlinedParseState parseState, IAction action) {
        switch (action.actionType()) {
        case SHIFT:
            // nothing to reduce, just record it and follow the respective shift transition
            // in the parse table.
            IShift shiftAction = (IShift) action;
            IState shiftState = parseTable.getState(shiftAction.shiftStateId());

            addForShifter(parseState, stack, shiftState);

            break;
        case REDUCE:
        case REDUCE_LOOKAHEAD: // Lookahead is checked while retrieving applicable actions from the state
            // a reduction is possible, do it.
            IReduce reduceAction = (IReduce) action;
            reduceManager.doReductions(/* observing, */ parseState, stack, reduceAction);

            break;
        case ACCEPT:
            // text parsing is done.
            parseState.acceptingStack = stack;

            break;
        }
    }
    
    /**
     * Shift in the next character after the current state.
     * @param parseState The state of the parser.
     */
    private void shifter(InlinedParseState parseState) {
        //clear out the old stacks, because they won't be leaves anymore.
        parseState.activeStacks.clear();
        
        //create a character node for the next character.
        InlinedCharacterNode characterNode = getNodeToShift(parseState);
        
        // if the actor() actions contained shifts, check the parse table what state (table cell/entry) to shift to
        for (InlinedForShifterElement forShifterElement : parseState.forShifter) {
            //get the new state to shift into
            InlinedStackNode gotoStack = parseState.activeStacks.findWithState(forShifterElement.state);
            
            if (gotoStack != null) {
                //the the next stack node already exists, let that stack node split into the old one.
                // example:
                // [(+ 'x' 'x')] <- ['*']
                // ['x' '+' 'x'] <-/  <== add this link
                stackManager.createStackLink(parseState, gotoStack, forShifterElement.stack, characterNode);
            } else {
                //the new state did not exist in the stack yet, create it
                //example: ['x'] { <- ['+']} <== add the link and plus node 
                gotoStack = stackManager.createStackNode(forShifterElement.state);
                
                stackManager.createStackLink(parseState, gotoStack, forShifterElement.stack, characterNode);
                
                //add the new state to the active stacks, so that if there are other shift states
                // that would lead to this stack node, it would be shared.
                parseState.activeStacks.add(gotoStack);
            }

            // from RecoveryObserver
            if (parseState.isRecovering()) {
                //TODO: figure out what this is for, exactly
                // I guess this is to figure out whether this part of the parse is safe, or whether it would cause an error?
                int quota = parseState.recoveryJob().getQuota(forShifterElement.stack);
                int lastRecoveredOffset = parseState.recoveryJob().lastRecoveredOffset(forShifterElement.stack);

                parseState.recoveryJob().updateQuota(gotoStack, quota);
                parseState.recoveryJob().updateLastRecoveredOffset(gotoStack, lastRecoveredOffset);
            }
        }
        
        // be done with the shift states for the next round.
        parseState.forShifter.clear();
    }

    private InlinedCharacterNode getNodeToShift(InlinedParseState parseState) {
        return parseForestManager.createCharacterNode(parseState);
    }

    private void addForShifter(InlinedParseState parseState, InlinedStackNode stack, IState shiftState) {
        parseState.forShifter.add(new InlinedForShifterElement(stack, shiftState));
    }

    public Collection<Message> postProcessMessages(Collection<Message> messages, Tokens tokens) {
        return parseState.postProcessMessages(messages, tokens);
    }
}
