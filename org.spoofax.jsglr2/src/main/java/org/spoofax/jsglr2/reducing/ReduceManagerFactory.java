package org.spoofax.jsglr2.reducing;

import org.metaborg.parsetable.IParseTable;
import org.spoofax.jsglr2.datadependent.DataDependentReduceManager;
import org.spoofax.jsglr2.elkhound.AbstractElkhoundStackNode;
import org.spoofax.jsglr2.elkhound.ElkhoundReduceManager;
import org.spoofax.jsglr2.elkhound.ElkhoundStackManager;
import org.spoofax.jsglr2.incremental.IIncrementalParse;
import org.spoofax.jsglr2.incremental.IncrementalReduceManager;
import org.spoofax.jsglr2.incremental.parseforest.IncrementalParseForest;
import org.spoofax.jsglr2.layoutsensitive.LayoutSensitiveParseForest;
import org.spoofax.jsglr2.layoutsensitive.LayoutSensitiveReduceManager;
import org.spoofax.jsglr2.parseforest.IDerivation;
import org.spoofax.jsglr2.parseforest.IParseForest;
import org.spoofax.jsglr2.parseforest.ParseForestManager;
import org.spoofax.jsglr2.parser.AbstractParse;
import org.spoofax.jsglr2.parser.IParseState;
import org.spoofax.jsglr2.parser.ParserVariant;
import org.spoofax.jsglr2.stack.AbstractStackManager;
import org.spoofax.jsglr2.stack.IStackNode;

// Implementation note: in theory, this factory is an unnecessary bypass for the constructors of ReduceManagers.
// It is unnecessary, because the factory is only used once after it has been created and passed to the Parser.
// However, using this factory prevents the duplication of the new StackManager in JSGLR2Variants.getParser.
// This duplication would either mean
// - having to extract the new StackManager as a variable and having to explicitly write all type parameters, or
// - constructing two new StackManagers instead.
// Having a ReduceManagerFactory was preferred over these other two options.
public interface ReduceManagerFactory
//@formatter:off
   <ParseForest   extends IParseForest,
    ParseNode     extends ParseForest,
    Derivation    extends IDerivation<ParseForest>,
    StackNode     extends IStackNode,
    ParseState    extends IParseState<ParseForest, StackNode>,
    Parse         extends AbstractParse<ParseForest, StackNode, ParseState>,
    StackManager  extends AbstractStackManager<ParseForest, StackNode, ParseState, Parse>,
    ReduceManager extends org.spoofax.jsglr2.reducing.ReduceManager<ParseForest, ParseNode, Derivation, StackNode, ParseState, Parse>>
//@formatter:on
{
    ReduceManager get(IParseTable parseTable, StackManager stackManager,
        ParseForestManager<ParseForest, ParseNode, Derivation, Parse> parseForestManager);

    static
    //@formatter:off
       <ParseForest  extends IParseForest,
        ParseNode    extends ParseForest,
        Derivation   extends IDerivation<ParseForest>,
        StackNode    extends IStackNode,
        ParseState   extends IParseState<ParseForest, StackNode>,
        Parse        extends AbstractParse<ParseForest, StackNode, ParseState>,
        StackManager extends AbstractStackManager<ParseForest, StackNode, ParseState, Parse>>
    //@formatter:on
    ReduceManagerFactory<ParseForest, ParseNode, Derivation, StackNode, ParseState, Parse, StackManager, org.spoofax.jsglr2.reducing.ReduceManager<ParseForest, ParseNode, Derivation, StackNode, ParseState, Parse>>
        reduceManagerFactory(ParserVariant parserVariant) {
        return (parseTable, stackManager, parseForestManager) -> new org.spoofax.jsglr2.reducing.ReduceManager<>(
            parseTable, stackManager, parseForestManager, parserVariant.parseForestConstruction);
    }

    static
    //@formatter:off
       <ParseForest  extends IParseForest,
        ParseNode    extends ParseForest,
        Derivation   extends IDerivation<ParseForest>,
        StackNode    extends AbstractElkhoundStackNode<ParseForest>,
        ParseState   extends IParseState<ParseForest, StackNode>,
        Parse        extends AbstractParse<ParseForest, StackNode, ParseState>,
        StackManager extends ElkhoundStackManager<ParseForest, StackNode, ParseState, Parse>>
    //@formatter:on
    ReduceManagerFactory<ParseForest, ParseNode, Derivation, StackNode, ParseState, Parse, StackManager, ElkhoundReduceManager<ParseForest, ParseNode, Derivation, StackNode, ParseState, Parse>>
        elkhoundReduceManagerFactory(ParserVariant parserVariant) {
        return (parseTable, stackManager, parseForestManager) -> new ElkhoundReduceManager<>(parseTable, stackManager,
            parseForestManager, parserVariant.parseForestConstruction);
    }

    static
    //@formatter:off
       <ParseForest  extends IParseForest,
        ParseNode    extends ParseForest,
        Derivation   extends IDerivation<ParseForest>,
        StackNode    extends IStackNode,
        ParseState   extends IParseState<ParseForest, StackNode>,
        Parse        extends AbstractParse<ParseForest, StackNode, ParseState>,
        StackManager extends AbstractStackManager<ParseForest, StackNode, ParseState, Parse>>
    //@formatter:on
    ReduceManagerFactory<ParseForest, ParseNode, Derivation, StackNode, ParseState, Parse, StackManager, DataDependentReduceManager<ParseForest, ParseNode, Derivation, StackNode, ParseState, Parse>>
        dataDependentReduceManagerFactory(ParserVariant parserVariant) {
        return (parseTable, stackManager, parseForestManager) -> new DataDependentReduceManager<>(parseTable,
            stackManager, parseForestManager, parserVariant.parseForestConstruction);
    }

    static
    //@formatter:off
       <ParseForest  extends LayoutSensitiveParseForest,
        ParseNode    extends ParseForest,
        Derivation   extends IDerivation<ParseForest>,
        StackNode    extends IStackNode,
        ParseState   extends IParseState<ParseForest, StackNode>,
        Parse        extends AbstractParse<ParseForest, StackNode, ParseState>,
        StackManager extends AbstractStackManager<ParseForest, StackNode, ParseState, Parse>>
    //@formatter:on
    ReduceManagerFactory<ParseForest, ParseNode, Derivation, StackNode, ParseState, Parse, StackManager, LayoutSensitiveReduceManager<ParseForest, ParseNode, Derivation, StackNode, ParseState, Parse>>
        layoutSensitiveReduceManagerFactory(ParserVariant parserVariant) {
        return (parseTable, stackManager, parseForestManager) -> new LayoutSensitiveReduceManager<>(parseTable,
            stackManager, parseForestManager, parserVariant.parseForestConstruction);
    }

    static
    //@formatter:off
       <ParseForest  extends IncrementalParseForest,
        ParseNode    extends ParseForest,
        Derivation   extends IDerivation<ParseForest>,
        StackNode    extends IStackNode,
        ParseState   extends IParseState<ParseForest, StackNode>,
        Parse        extends AbstractParse<ParseForest, StackNode, ParseState> & IIncrementalParse,
        StackManager extends AbstractStackManager<ParseForest, StackNode, ParseState, Parse>>
    //@formatter:on
    ReduceManagerFactory<ParseForest, ParseNode, Derivation, StackNode, ParseState, Parse, StackManager, IncrementalReduceManager<ParseForest, ParseNode, Derivation, StackNode, ParseState, Parse>>
        incrementalReduceManagerFactory(ParserVariant parserVariant) {
        return (parseTable, stackManager, parseForestManager) -> new IncrementalReduceManager<>(parseTable,
            stackManager, parseForestManager, parserVariant.parseForestConstruction);
    }

}
