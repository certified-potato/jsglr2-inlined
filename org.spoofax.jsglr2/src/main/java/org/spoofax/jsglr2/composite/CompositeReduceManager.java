package org.spoofax.jsglr2.composite;

import static org.spoofax.jsglr2.datadependent.DataDependentReduceManager.ignoreByDeepPriorityConflict;
import static org.spoofax.jsglr2.layoutsensitive.LayoutSensitiveReduceManager.ignoreByLayoutConstraint;

import org.metaborg.parsetable.IParseTable;
import org.metaborg.parsetable.actions.IReduce;
import org.spoofax.jsglr2.parseforest.ParseForestManager;
import org.spoofax.jsglr2.parser.AbstractParseState;
import org.spoofax.jsglr2.reducing.ReduceManager;
import org.spoofax.jsglr2.reducing.ReduceManagerFactory;
import org.spoofax.jsglr2.reducing.ReducerFactory;
import org.spoofax.jsglr2.stack.AbstractStackManager;
import org.spoofax.jsglr2.stack.IStackNode;

public class CompositeReduceManager
//@formatter:off
   <StackNode  extends IStackNode,
    ParseState extends AbstractParseState<?, StackNode>>
//@formatter:on
    extends
    ReduceManager<ICompositeParseForest, ICompositeDerivation<ICompositeParseForest>, ICompositeParseNode<ICompositeParseForest, ICompositeDerivation<ICompositeParseForest>>, StackNode, ParseState> {

    private CompositeReduceManager(IParseTable parseTable,
        AbstractStackManager<ICompositeParseForest, ICompositeDerivation<ICompositeParseForest>, ICompositeParseNode<ICompositeParseForest, ICompositeDerivation<ICompositeParseForest>>, StackNode, ParseState> stackManager,
        ParseForestManager<ICompositeParseForest, ICompositeDerivation<ICompositeParseForest>, ICompositeParseNode<ICompositeParseForest, ICompositeDerivation<ICompositeParseForest>>, StackNode, ParseState> parseForestManager,
        ReducerFactory<ICompositeParseForest, ICompositeDerivation<ICompositeParseForest>, ICompositeParseNode<ICompositeParseForest, ICompositeDerivation<ICompositeParseForest>>, StackNode, ParseState> reducerFactory) {
        super(parseTable, stackManager, parseForestManager, reducerFactory);
    }

    public static
    //@formatter:off
       <StackNode_    extends IStackNode,
        ParseState_   extends AbstractParseState<?, StackNode_>,
        StackManager_ extends AbstractStackManager<ICompositeParseForest, ICompositeDerivation<ICompositeParseForest>, ICompositeParseNode<ICompositeParseForest, ICompositeDerivation<ICompositeParseForest>>, StackNode_, ParseState_>>
    //@formatter:on
    ReduceManagerFactory<ICompositeParseForest, ICompositeDerivation<ICompositeParseForest>, ICompositeParseNode<ICompositeParseForest, ICompositeDerivation<ICompositeParseForest>>, StackNode_, ParseState_, StackManager_, CompositeReduceManager<StackNode_, ParseState_>>
        factoryComposite(
            ReducerFactory<ICompositeParseForest, ICompositeDerivation<ICompositeParseForest>, ICompositeParseNode<ICompositeParseForest, ICompositeDerivation<ICompositeParseForest>>, StackNode_, ParseState_> reducerFactory) {
        return (parseTable, stackManager, parseForestManager) -> new CompositeReduceManager<>(parseTable, stackManager,
            parseForestManager, reducerFactory);
    }

    @Override protected boolean ignoreReducePath(StackNode pathBegin, IReduce reduce,
        ICompositeParseForest[] parseNodes) {
        return ignoreByDeepPriorityConflict(reduce, parseNodes) || ignoreByLayoutConstraint(reduce, parseNodes);
    }

}
