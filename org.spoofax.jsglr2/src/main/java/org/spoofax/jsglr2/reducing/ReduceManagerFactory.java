package org.spoofax.jsglr2.reducing;

import org.metaborg.parsetable.IParseTable;
import org.spoofax.jsglr2.parseforest.IDerivation;
import org.spoofax.jsglr2.parseforest.IParseForest;
import org.spoofax.jsglr2.parseforest.ParseForestManager;
import org.spoofax.jsglr2.parser.AbstractParseState;
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
    ParseState    extends AbstractParseState<ParseForest, StackNode>,
    StackManager  extends AbstractStackManager<ParseForest, StackNode, ParseState>,
    ReduceManager extends org.spoofax.jsglr2.reducing.ReduceManager<ParseForest, ParseNode, Derivation, StackNode, ParseState>>
//@formatter:on
{

    ReduceManager get(IParseTable parseTable, StackManager stackManager,
        ParseForestManager<ParseForest, ParseNode, Derivation, StackNode, ParseState> parseForestManager);

}
