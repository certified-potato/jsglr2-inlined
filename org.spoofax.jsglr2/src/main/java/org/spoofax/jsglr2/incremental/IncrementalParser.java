package org.spoofax.jsglr2.incremental;


import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.metaborg.parsetable.IParseTable;
import org.metaborg.parsetable.IProduction;
import org.metaborg.parsetable.actions.IAction;
import org.spoofax.jsglr2.incremental.actions.GotoShift;
import org.spoofax.jsglr2.incremental.parseforest.IncrementalCharacterNode;
import org.spoofax.jsglr2.incremental.parseforest.IncrementalDerivation;
import org.spoofax.jsglr2.incremental.parseforest.IncrementalParseForest;
import org.spoofax.jsglr2.incremental.parseforest.IncrementalParseNode;
import org.spoofax.jsglr2.parseforest.ParseForestManager;
import org.spoofax.jsglr2.parser.ParseFactory;
import org.spoofax.jsglr2.parser.Parser;
import org.spoofax.jsglr2.parser.result.ParseResult;
import org.spoofax.jsglr2.reducing.ReduceManager;
import org.spoofax.jsglr2.stack.AbstractStackManager;
import org.spoofax.jsglr2.stack.IStackNode;
import org.spoofax.jsglr2.stack.collections.IActiveStacks;
import org.spoofax.jsglr2.stack.collections.IActiveStacksFactory;
import org.spoofax.jsglr2.stack.collections.IForActorStacks;
import org.spoofax.jsglr2.stack.collections.IForActorStacksFactory;

public class IncrementalParser
// @formatter:off
   <ParseNode extends IncrementalParseNode,
    Derivation extends IncrementalDerivation,
    StackNode extends IStackNode,
    Parse extends IncrementalParse<StackNode>>
// @formatter:on
    extends Parser<IncrementalParseForest, ParseNode, Derivation, StackNode, Parse> {

    private final IncrementalParseFactory<StackNode, Parse> incrementalParseFactory;

    public IncrementalParser(ParseFactory<IncrementalParseForest, StackNode, Parse> parseFactory,
        IncrementalParseFactory<StackNode, Parse> incrementalParseFactory, IParseTable parseTable,
        IActiveStacksFactory activeStacksFactory, IForActorStacksFactory forActorStacksFactory,
        AbstractStackManager<IncrementalParseForest, StackNode, Parse> stackManager,
        ParseForestManager<IncrementalParseForest, ParseNode, Derivation> parseForestManager,
        ReduceManager<IncrementalParseForest, ParseNode, Derivation, StackNode, Parse> reduceManager) {

        super(parseFactory, parseTable, activeStacksFactory, forActorStacksFactory, stackManager, parseForestManager,
            reduceManager);
        this.incrementalParseFactory = incrementalParseFactory;
    }

    public ParseResult<IncrementalParseForest> incrementalParse(List<EditorUpdate> editorUpdates,
        IncrementalParseForest previousVersion, String filename, String startSymbol) {

        IActiveStacks<StackNode> activeStacks = activeStacksFactory.get(observing);
        IForActorStacks<StackNode> forActorStacks = forActorStacksFactory.get(observing);

        Parse parse = incrementalParseFactory.get(editorUpdates, previousVersion, filename, activeStacks,
            forActorStacks, observing);

        return parseInternal(startSymbol, parse);
    }

    @Override protected void actor(StackNode stack, Parse parse) {
        parse.state = stack.state();

        Collection<IAction> actions;
        while((actions = getActions(stack, parse, parse.reducerLookAhead)).size() == 0
            && parse.reducerLookAhead instanceof IncrementalParseNode)
            parse.reducerLookAhead = parse.reducerLookAhead.leftBreakdown();

        if(actions.size() > 1)
            parse.multipleStates = true;

        Collection<IAction> finalActions = actions;
        observing.notify(observer -> observer.actor(stack, parse, finalActions));

        for(IAction action : actions)
            actor(stack, parse, action);
        // TODO case Accept, if reducerLookAhead != EOF then abort parsing and return error? (should never happen)
    }

    private Collection<IAction> getActions(StackNode stack, Parse parse, IncrementalParseForest lookAhead) {
        if(lookAhead.isTerminal()) {
            LinkedList<IAction> actions = new LinkedList<>();
            stack.state().getApplicableActions(parse).forEach(actions::add);
            return actions;
        } else {
            IProduction production = ((IncrementalParseNode) lookAhead).getFirstDerivation().production();
            try {
                return Collections.singletonList(new GotoShift(stack.state().getGotoId(production.id())));
            } catch(NullPointerException e) { // Can be thrown inside getGotoId or because production == null
                return Collections.emptyList();
            }
        }
    }

    @Override protected IncrementalParseForest getCharacterNodeToShift(Parse parse) {
        parse.multipleStates = parse.forShifter.size() > 1;

        if(parse.forShifter.size() == 0) // This should only happen when the parser has already accepted or has failed
            return IncrementalCharacterNode.EOF_NODE;

        int forShifterState = parse.forShifter.peek().state.id();

        while(!parse.shiftLookAhead.isTerminal()) {
            if(!parse.multipleStates
                && forShifterState == ((IncrementalParseNode) parse.shiftLookAhead).getFirstDerivation().state.id())
                break;
            parse.shiftLookAhead = parse.shiftLookAhead.leftBreakdown();
        }

        return parse.shiftLookAhead;
    }
}
