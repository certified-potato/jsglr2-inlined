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

    //public final StatCounter observer = new StatCounter();
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

    @Override
    public ParseResult<IParseForest> parse(JSGLR2Request request, String previousInput, IParseForest previousResult) {
        parseState = new InlinedParseState(request, new InlinedInputStack(request.input));

        //observer.parseStart(parseState);

        InlinedStackNode initialStackNode = stackManager.createStackNode(parseTable.getStartState());

        parseState.activeStacks.add(initialStackNode);

        boolean recover;

        try {
            do {
                parseLoop(parseState);

                if (parseState.acceptingStack == null)
                    recover = failureHandler.onFailure(parseState);
                else
                    recover = false;
            } while (recover);

            if (parseState.acceptingStack != null) {
                InlinedParseNode parseForest = (InlinedParseNode) stackManager.findDirectLink(parseState.acceptingStack,
                        initialStackNode).parseForest;

                InlinedParseNode parseForestWithStartSymbol = request.startSymbol != null
                        ? parseForestManager.filterStartSymbol(parseForest, request.startSymbol, parseState)
                        : parseForest;

                if (parseForest != null && parseForestWithStartSymbol == null)
                    return failure(new ParseFailureCause(ParseFailureCause.Type.InvalidStartSymbol));
                else
                    return complete(parseState, parseForestWithStartSymbol);
            } else
                return failure(failureHandler.failureCause(parseState));
        } catch (ParseException e) {
            return failure(e.cause);
        }
    }

    @Override
    public void visit(ParseSuccess<?> success, ParseNodeVisitor<?, ?, ?> visitor) {
        throw new NotImplementedException("This parser uses its own parse nodes");
    }

    protected ParseResult<IParseForest> complete(InlinedParseState parseState, InlinedParseNode parseForest) {
        List<Message> messages = new ArrayList<>();
        InlinedCycleDetector cycleDetector = new InlinedCycleDetector(messages);

        parseForestManager.visit(parseState.request, parseForest, cycleDetector);

        if (cycleDetector.cycleDetected()) {
            return failure(cycleDetector.failureCause);
        } else {
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

            //observer.success();

            return success;
        }
    }

    protected ParseFailure<IParseForest> failure(ParseFailureCause failureCause) {
        return new ParseFailure<>(null, failureCause);
    }

    protected void parseLoop(InlinedParseState parseState) throws ParseException {
        while (parseState.inputStack.hasNext() && !parseState.activeStacks.isEmpty()) {
            parseCharacter(parseState);
            if (!parseState.activeStacks.isEmpty())
                parseState.inputStack.next();
        }
    }

    protected void parseCharacter(InlinedParseState parseState) throws ParseException {
        parseState.nextParseRound();

        parseState.activeStacks.addAllTo(parseState.forActorStacks);

        processForActorStacks(parseState);

        shifter(parseState);
    }

    protected void processForActorStacks(InlinedParseState parseState) {
        while (parseState.forActorStacks.nonEmpty()) {
            InlinedStackNode stack = parseState.forActorStacks.remove();

            if (!stack.allLinksRejected())
                actor(stack, parseState);
        }
    }

    protected void actor(InlinedStackNode stack, InlinedParseState parseState) {
        //observer.actor();
        stack.state().getApplicableActions(parseState.inputStack, parseState.mode);

        for (IAction action : stack.state().getApplicableActions(parseState.inputStack, parseState.mode))
            actor(stack, parseState, action);
    }

    protected void actor(InlinedStackNode stack, InlinedParseState parseState, IAction action) {
        switch (action.actionType()) {
        case SHIFT:
            IShift shiftAction = (IShift) action;
            IState shiftState = parseTable.getState(shiftAction.shiftStateId());

            addForShifter(parseState, stack, shiftState);

            break;
        case REDUCE:
        case REDUCE_LOOKAHEAD: // Lookahead is checked while retrieving applicable actions from the state
            IReduce reduceAction = (IReduce) action;

            reduceManager.doReductions(/* observing, */ parseState, stack, reduceAction);

            break;
        case ACCEPT:
            parseState.acceptingStack = stack;

            break;
        }
    }

    protected void shifter(InlinedParseState parseState) {
        parseState.activeStacks.clear();

        InlinedCharacterNode characterNode = getNodeToShift(parseState);

        for (InlinedForShifterElement forShifterElement : parseState.forShifter) {
            InlinedStackNode gotoStack = parseState.activeStacks.findWithState(forShifterElement.state);

            if (gotoStack != null) {
                stackManager.createStackLink(parseState, gotoStack, forShifterElement.stack, characterNode);
            } else {
                gotoStack = stackManager.createStackNode(forShifterElement.state);

                stackManager.createStackLink(parseState, gotoStack, forShifterElement.stack, characterNode);

                parseState.activeStacks.add(gotoStack);
            }

            // from RecoveryObserver
            if (parseState.isRecovering()) {
                int quota = parseState.recoveryJob().getQuota(forShifterElement.stack);
                int lastRecoveredOffset = parseState.recoveryJob().lastRecoveredOffset(forShifterElement.stack);

                parseState.recoveryJob().updateQuota(gotoStack, quota);
                parseState.recoveryJob().updateLastRecoveredOffset(gotoStack, lastRecoveredOffset);
            }
        }

        parseState.forShifter.clear();
    }

    protected InlinedCharacterNode getNodeToShift(InlinedParseState parseState) {
        return parseForestManager.createCharacterNode(parseState);
    }

    protected void addForShifter(InlinedParseState parseState, InlinedStackNode stack, IState shiftState) {
        parseState.forShifter.add(new InlinedForShifterElement(stack, shiftState));
    }

    public Collection<Message> postProcessMessages(Collection<Message> messages, Tokens tokens) {
        return parseState.postProcessMessages(messages, tokens);        
    }
}
