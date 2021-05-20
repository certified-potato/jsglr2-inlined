package org.spoofax.jsglr2.inlined;

import java.util.ArrayList;
import java.util.List;

import org.metaborg.parsetable.IParseTable;
import org.metaborg.parsetable.actions.IAction;
import org.metaborg.parsetable.actions.IReduce;
import org.metaborg.parsetable.actions.IShift;
import org.metaborg.parsetable.states.IState;
import org.spoofax.jsglr2.JSGLR2Request;
import org.spoofax.jsglr2.inputstack.InputStack;
import org.spoofax.jsglr2.inputstack.InputStackFactory;
import org.spoofax.jsglr2.messages.Message;
import org.spoofax.jsglr2.parseforest.Disambiguator;
import org.spoofax.jsglr2.parseforest.IDerivation;
import org.spoofax.jsglr2.parseforest.IParseForest;
import org.spoofax.jsglr2.parseforest.IParseNode;
import org.spoofax.jsglr2.parseforest.ParseForestManager;
import org.spoofax.jsglr2.parseforest.ParseForestManagerFactory;
import org.spoofax.jsglr2.parseforest.ParseNodeVisitor;
import org.spoofax.jsglr2.parseforest.hybrid.HybridDerivation;
import org.spoofax.jsglr2.parseforest.hybrid.HybridParseForest;
import org.spoofax.jsglr2.parseforest.hybrid.HybridParseForestManager;
import org.spoofax.jsglr2.parseforest.hybrid.HybridParseNode;
import org.spoofax.jsglr2.parser.AbstractParseState;
import org.spoofax.jsglr2.parser.AmbiguityDetector;
import org.spoofax.jsglr2.parser.CycleDetector;
import org.spoofax.jsglr2.parser.ForShifterElement;
import org.spoofax.jsglr2.parser.IObservableParser;
import org.spoofax.jsglr2.parser.IParseReporter;
import org.spoofax.jsglr2.parser.NonAssocDetector;
import org.spoofax.jsglr2.parser.ParseException;
import org.spoofax.jsglr2.parser.ParseReporterFactory;
import org.spoofax.jsglr2.parser.ParseStateFactory;
import org.spoofax.jsglr2.parser.failure.IParseFailureHandler;
import org.spoofax.jsglr2.parser.failure.ParseFailureHandlerFactory;
import org.spoofax.jsglr2.parser.observing.ParserObserving;
import org.spoofax.jsglr2.parser.result.ParseFailure;
import org.spoofax.jsglr2.parser.result.ParseFailureCause;
import org.spoofax.jsglr2.parser.result.ParseResult;
import org.spoofax.jsglr2.parser.result.ParseSuccess;
import org.spoofax.jsglr2.recovery.RecoveryDisambiguator;
import org.spoofax.jsglr2.recovery.RecoveryParseFailureHandler;
import org.spoofax.jsglr2.recovery.RecoveryParseReporter;
import org.spoofax.jsglr2.recovery.RecoveryParseState;
import org.spoofax.jsglr2.recovery.RecoveryReducerOptimized;
import org.spoofax.jsglr2.reducing.ReduceManager;
import org.spoofax.jsglr2.reducing.ReduceManagerFactory;
import org.spoofax.jsglr2.stack.IStackNode;
import org.spoofax.jsglr2.stack.StackManagerFactory;
import org.spoofax.jsglr2.stack.collections.ActiveStacksFactory;
import org.spoofax.jsglr2.stack.collections.ActiveStacksRepresentation;
import org.spoofax.jsglr2.stack.collections.ForActorStacksFactory;
import org.spoofax.jsglr2.stack.collections.ForActorStacksRepresentation;
import org.spoofax.jsglr2.stack.hybrid.HybridStackManager;
import org.spoofax.jsglr2.stack.hybrid.HybridStackNode;

public class InlinedParser implements IObservableParser<HybridParseForest, HybridDerivation, HybridParseNode, HybridStackNode<HybridParseForest>, RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>>> {

    protected final ParserObserving<HybridParseForest, HybridDerivation, HybridParseNode, HybridStackNode<HybridParseForest>, RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>>> observing;
    protected final InputStackFactory<InputStack> inputStackFactory;
    protected final ParseStateFactory<HybridParseForest, HybridDerivation, HybridParseNode, InputStack, HybridStackNode<HybridParseForest>, RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>>> parseStateFactory;
    protected final IParseTable parseTable;
    protected final HybridStackManager<HybridParseForest, HybridDerivation, HybridParseNode, RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>>> stackManager;
    protected final HybridParseForestManager<HybridStackNode<HybridParseForest>, RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>>> parseForestManager;
    public final ReduceManager<HybridParseForest, HybridDerivation, HybridParseNode, HybridStackNode<HybridParseForest>, InputStack, RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>>> reduceManager;
    protected final IParseFailureHandler<HybridParseForest, HybridStackNode<HybridParseForest>, RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>>> failureHandler;
    protected final IParseReporter<HybridParseForest, HybridDerivation, HybridParseNode, HybridStackNode<HybridParseForest>, InputStack, RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>>> reporter;
    
    
    public InlinedParser(IParseTable table) {
        this.observing = new ParserObserving<>();
        this.inputStackFactory = InputStack::new;
        this.parseStateFactory = RecoveryParseState.factory(
                new ActiveStacksFactory(ActiveStacksRepresentation.standard()),
                new ForActorStacksFactory(ForActorStacksRepresentation.standard()));
        this.parseTable = table; 
        this.stackManager = new HybridStackManager<>(observing);
        this.parseForestManager = new HybridParseForestManager<>(observing, new RecoveryDisambiguator<>());
        this.reduceManager = new ReduceManager<>(table, stackManager, parseForestManager, RecoveryReducerOptimized.factoryRecoveryOptimized());
        ParseFailureHandlerFactory<HybridParseForest, HybridDerivation, HybridParseNode, HybridStackNode<HybridParseForest>, RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>>> rpfhf = RecoveryParseFailureHandler.factory();
        this.failureHandler = rpfhf.get(observing);
        ParseReporterFactory<HybridParseForest, HybridDerivation, HybridParseNode, HybridStackNode<HybridParseForest>, InputStack, RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>>> rf = RecoveryParseReporter.factory();
        this.reporter = rf.get(parseForestManager);   
    }
    
    @Override public ParseResult<HybridParseForest> parse(JSGLR2Request request, String previousInput,
        HybridParseForest previousResult) {
        RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>> parseState = getParseState(request, previousInput, previousResult);

        observing.notify(observer -> observer.parseStart(parseState));

        HybridStackNode<HybridParseForest> initialStackNode = stackManager.createInitialStackNode(parseTable.getStartState());

        parseState.activeStacks.add(initialStackNode);

        boolean recover;

        try {
            do {
                parseLoop(parseState);

                if(parseState.acceptingStack == null)
                    recover = failureHandler.onFailure(parseState);
                else
                    recover = false;
            } while(recover);

            if(parseState.acceptingStack != null) {
                HybridParseForest parseForest =
                    stackManager.findDirectLink(parseState.acceptingStack, initialStackNode).parseForest;

                HybridParseForest parseForestWithStartSymbol = request.startSymbol != null
                    ? parseForestManager.filterStartSymbol(parseForest, request.startSymbol, parseState) : parseForest;

                if(parseForest != null && parseForestWithStartSymbol == null)
                    return failure(parseState, new ParseFailureCause(ParseFailureCause.Type.InvalidStartSymbol));
                else
                    return complete(parseState, parseForestWithStartSymbol);
            } else
                return failure(parseState, failureHandler.failureCause(parseState));
        } catch(ParseException e) {
            return failure(parseState, e.cause);
        }
    }

    @Override public void visit(ParseSuccess<?> success, ParseNodeVisitor<?, ?, ?> visitor) {
        parseForestManager.visit(success.parseState.request, (HybridParseForest) success.parseResult,
            (ParseNodeVisitor<HybridParseForest, HybridDerivation, HybridParseNode>) visitor);
    }

    protected RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>> getParseState(JSGLR2Request request, String previousInput, HybridParseForest previousResult) {
        return parseStateFactory.get(request, inputStackFactory.get(request.input), observing);
    }

    protected ParseResult<HybridParseForest> complete(RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>> parseState, HybridParseForest parseForest) {
        List<Message> messages = new ArrayList<>();
        CycleDetector<HybridParseForest, HybridDerivation, HybridParseNode> cycleDetector = new CycleDetector<>(messages);

        parseForestManager.visit(parseState.request, parseForest, cycleDetector);

        if(cycleDetector.cycleDetected()) {
            return failure(new ParseFailure<>(parseState, cycleDetector.failureCause));
        } else {
            reporter.report(parseState, parseForest, messages);

            // Generate errors for non-assoc or non-nested productions that are used associatively
            parseForestManager.visit(parseState.request, parseForest, new NonAssocDetector<>(messages));

            if(parseState.request.reportAmbiguities) {
                // Generate warnings for ambiguous parse nodes
                parseForestManager.visit(parseState.request, parseForest,
                    new AmbiguityDetector<>(parseState.inputStack.inputString(), messages));
            }

            ParseSuccess<HybridParseForest> success = new ParseSuccess<>(parseState, parseForest, messages);

            observing.notify(observer -> observer.success(success));

            return success;
        }
    }

    protected ParseFailure<HybridParseForest> failure(RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>> parseState, ParseFailureCause failureCause) {
        return failure(new ParseFailure<>(parseState, failureCause));
    }

    protected ParseFailure<HybridParseForest> failure(ParseFailure<HybridParseForest> failure) {
        observing.notify(observer -> observer.failure(failure));

        return failure;
    }

    protected void parseLoop(RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>> parseState) throws ParseException {
        while(parseState.inputStack.hasNext() && !parseState.activeStacks.isEmpty()) {
            parseCharacter(parseState);
            parseState.inputStack.consumed();

            if(!parseState.activeStacks.isEmpty())
                parseState.inputStack.next();
        }
    }

    protected void parseCharacter(RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>> parseState) throws ParseException {
        parseState.nextParseRound(observing);

        parseState.activeStacks.addAllTo(parseState.forActorStacks);

        observing.notify(observer -> observer.forActorStacks(parseState.forActorStacks));

        processForActorStacks(parseState);

        shifter(parseState);
    }

    protected void processForActorStacks(RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>> parseState) {
        while(parseState.forActorStacks.nonEmpty()) {
            HybridStackNode<HybridParseForest> stack = parseState.forActorStacks.remove();

            observing.notify(observer -> observer.handleForActorStack(stack, parseState.forActorStacks));

            if(!stack.allLinksRejected())
                actor(stack, parseState);
            else
                observing.notify(observer -> observer.skipRejectedStack(stack));
        }
    }

    protected void actor(HybridStackNode<HybridParseForest> stack, RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>> parseState) {
        observing.notify(observer -> observer.actor(stack, parseState,
            stack.state().getApplicableActions(parseState.inputStack, parseState.mode)));

        for(IAction action : stack.state().getApplicableActions(parseState.inputStack, parseState.mode))
            actor(stack, parseState, action);
    }

    protected void actor(HybridStackNode<HybridParseForest> stack, RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>> parseState, IAction action) {
        switch(action.actionType()) {
            case SHIFT:
                IShift shiftAction = (IShift) action;
                IState shiftState = parseTable.getState(shiftAction.shiftStateId());

                addForShifter(parseState, stack, shiftState);

                break;
            case REDUCE:
            case REDUCE_LOOKAHEAD: // Lookahead is checked while retrieving applicable actions from the state
                IReduce reduceAction = (IReduce) action;

                reduceManager.doReductions(observing, parseState, stack, reduceAction);

                break;
            case ACCEPT:
                parseState.acceptingStack = stack;

                observing.notify(observer -> observer.accept(stack));

                break;
        }
    }

    protected void shifter(RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>> parseState) {
        parseState.activeStacks.clear();

        HybridParseForest characterNode = getNodeToShift(parseState);

        observing.notify(observer -> observer.shifter(characterNode, parseState.forShifter));

        for(ForShifterElement<HybridStackNode<HybridParseForest>> forShifterElement : parseState.forShifter) {
            HybridStackNode<HybridParseForest> gotoStack = parseState.activeStacks.findWithState(forShifterElement.state);

            if(gotoStack != null) {
                stackManager.createStackLink(parseState, gotoStack, forShifterElement.stack, characterNode);
            } else {
                gotoStack = stackManager.createStackNode(forShifterElement.state);

                stackManager.createStackLink(parseState, gotoStack, forShifterElement.stack, characterNode);

                parseState.activeStacks.add(gotoStack);
            }

            HybridStackNode<HybridParseForest> finalGotoStack = gotoStack;
            observing.notify(observer -> observer.shift(parseState, forShifterElement.stack, finalGotoStack));
        }

        parseState.forShifter.clear();
    }

    protected HybridParseForest getNodeToShift(RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>> parseState) {
        return parseForestManager.createCharacterNode(parseState);
    }

    protected void addForShifter(RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>> parseState, HybridStackNode<HybridParseForest> stack, IState shiftState) {
        ForShifterElement<HybridStackNode<HybridParseForest>> forShifterElement = new ForShifterElement<>(stack, shiftState);

        observing.notify(observer -> observer.addForShifter(forShifterElement));

        parseState.forShifter.add(forShifterElement);
    }

    @Override public ParserObserving<HybridParseForest, HybridDerivation, HybridParseNode, HybridStackNode<HybridParseForest>, RecoveryParseState<InputStack, HybridStackNode<HybridParseForest>>> observing() {
        return observing;
    }

}
