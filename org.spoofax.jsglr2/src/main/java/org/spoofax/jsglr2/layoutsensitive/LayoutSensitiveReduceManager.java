package org.spoofax.jsglr2.layoutsensitive;

import org.metaborg.parsetable.IParseTable;
import org.metaborg.parsetable.actions.IReduce;
import org.metaborg.sdf2table.grammar.LayoutConstraintAttribute;
import org.metaborg.sdf2table.parsetable.ParseTableProduction;
import org.spoofax.jsglr2.parseforest.ParseForestConstruction;
import org.spoofax.jsglr2.parseforest.ParseForestManager;
import org.spoofax.jsglr2.parser.AbstractParseState;
import org.spoofax.jsglr2.parser.ParserVariant;
import org.spoofax.jsglr2.parser.observing.ParserObserving;
import org.spoofax.jsglr2.reducing.ReduceManager;
import org.spoofax.jsglr2.reducing.ReduceManagerFactory;
import org.spoofax.jsglr2.stack.AbstractStackManager;
import org.spoofax.jsglr2.stack.IStackNode;
import org.spoofax.jsglr2.stack.StackLink;
import org.spoofax.jsglr2.stack.paths.StackPath;

public class LayoutSensitiveReduceManager
//@formatter:off
   <StackNode   extends IStackNode,
    ParseState  extends AbstractParseState<ILayoutSensitiveParseForest, StackNode>>
//@formatter:on
    extends
    ReduceManager<ILayoutSensitiveParseForest, ILayoutSensitiveDerivation<ILayoutSensitiveParseForest>, ILayoutSensitiveParseNode<ILayoutSensitiveParseForest, ILayoutSensitiveDerivation<ILayoutSensitiveParseForest>>, StackNode, ParseState> {

    public LayoutSensitiveReduceManager(IParseTable parseTable,
        AbstractStackManager<ILayoutSensitiveParseForest, ILayoutSensitiveDerivation<ILayoutSensitiveParseForest>, ILayoutSensitiveParseNode<ILayoutSensitiveParseForest, ILayoutSensitiveDerivation<ILayoutSensitiveParseForest>>, StackNode, ParseState> stackManager,
        ParseForestManager<ILayoutSensitiveParseForest, ILayoutSensitiveDerivation<ILayoutSensitiveParseForest>, ILayoutSensitiveParseNode<ILayoutSensitiveParseForest, ILayoutSensitiveDerivation<ILayoutSensitiveParseForest>>, StackNode, ParseState> parseForestManager,
        ParseForestConstruction parseForestConstruction) {
        super(parseTable, stackManager, parseForestManager, parseForestConstruction);
    }

    public static
    //@formatter:off
       <StackNode_    extends IStackNode,
        ParseState_   extends AbstractParseState<ILayoutSensitiveParseForest, StackNode_>,
        StackManager_ extends AbstractStackManager<ILayoutSensitiveParseForest, ILayoutSensitiveDerivation<ILayoutSensitiveParseForest>, ILayoutSensitiveParseNode<ILayoutSensitiveParseForest, ILayoutSensitiveDerivation<ILayoutSensitiveParseForest>>, StackNode_, ParseState_>>
    //@formatter:on
    ReduceManagerFactory<ILayoutSensitiveParseForest, ILayoutSensitiveDerivation<ILayoutSensitiveParseForest>, ILayoutSensitiveParseNode<ILayoutSensitiveParseForest, ILayoutSensitiveDerivation<ILayoutSensitiveParseForest>>, StackNode_, ParseState_, StackManager_, LayoutSensitiveReduceManager<StackNode_, ParseState_>>
        factoryLayoutSensitive(ParserVariant parserVariant) {
        return (parseTable, stackManager, parseForestManager) -> new LayoutSensitiveReduceManager<>(parseTable,
            stackManager, parseForestManager, parserVariant.parseForestConstruction);
    }

    @Override protected void doReductionsHelper(
        ParserObserving<ILayoutSensitiveParseForest, ILayoutSensitiveDerivation<ILayoutSensitiveParseForest>, ILayoutSensitiveParseNode<ILayoutSensitiveParseForest, ILayoutSensitiveDerivation<ILayoutSensitiveParseForest>>, StackNode, ParseState> observing,
        ParseState parseState, StackNode stack, IReduce reduce,
        StackLink<ILayoutSensitiveParseForest, StackNode> throughLink) {
        pathsLoop: for(StackPath<ILayoutSensitiveParseForest, StackNode> path : stackManager.findAllPathsOfLength(stack,
            reduce.arity())) {
            if(throughLink == null || path.contains(throughLink)) {
                StackNode pathBegin = path.head();
                ILayoutSensitiveParseForest[] parseNodes = stackManager.getParseForests(parseForestManager, path);

                if(reduce.production() instanceof ParseTableProduction) {
                    ParseTableProduction sdf2tableProduction = (ParseTableProduction) reduce.production();

                    for(LayoutConstraintAttribute lca : sdf2tableProduction.getLayoutConstraints()) {
                        // Skip the reduction if the constraint evaluates to false
                        if(!LayoutConstraintEvaluator.evaluate(lca.getLayoutConstraint(), parseNodes).orElse(true))
                            continue pathsLoop;
                    }
                }

                reducer(observing, parseState, pathBegin, reduce, parseNodes);
            }
        }
    }

}
