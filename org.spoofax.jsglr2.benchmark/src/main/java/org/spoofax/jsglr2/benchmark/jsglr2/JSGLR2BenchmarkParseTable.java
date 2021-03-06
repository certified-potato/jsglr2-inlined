package org.spoofax.jsglr2.benchmark.jsglr2;

import org.metaborg.parsetable.ParseTableVariant;
import org.metaborg.parsetable.query.ActionsForCharacterRepresentation;
import org.metaborg.parsetable.query.ProductionToGotoRepresentation;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.infra.Blackhole;
import org.spoofax.jsglr2.integration.IntegrationVariant;
import org.spoofax.jsglr2.parseforest.IParseForest;
import org.spoofax.jsglr2.parseforest.ParseForestConstruction;
import org.spoofax.jsglr2.parseforest.ParseForestRepresentation;
import org.spoofax.jsglr2.parser.IParser;
import org.spoofax.jsglr2.parser.ParseException;
import org.spoofax.jsglr2.parser.ParserVariant;
import org.spoofax.jsglr2.reducing.Reducing;
import org.spoofax.jsglr2.stack.StackRepresentation;
import org.spoofax.jsglr2.stack.collections.ActiveStacksRepresentation;
import org.spoofax.jsglr2.stack.collections.ForActorStacksRepresentation;
import org.spoofax.jsglr2.testset.testinput.StringInput;

public abstract class JSGLR2BenchmarkParseTable extends JSGLR2Benchmark<String, StringInput> {

    @Param({ "false" }) public boolean implode;

    @Param({ "Separated", "DisjointSorted" }) ActionsForCharacterRepresentation actionsForCharacterRepresentation;

    @Param({ "ForLoop", "JavaHashMap" }) ProductionToGotoRepresentation productionToGotoRepresentation;

    @Param({ "ArrayList" }) public ActiveStacksRepresentation activeStacksRepresentation;

    @Param({ "ArrayDeque" }) public ForActorStacksRepresentation forActorStacksRepresentation;

    @Param({ "Basic" }) public ParseForestRepresentation parseForestRepresentation;

    @Param({ "Full" }) public ParseForestConstruction parseForestConstruction;

    @Param({ "Basic" }) public StackRepresentation stackRepresentation;

    @Param({ "Basic" }) public Reducing reducing;

    @Override protected IntegrationVariant variant() {
        IntegrationVariant variant = new IntegrationVariant(
            new ParseTableVariant(actionsForCharacterRepresentation, productionToGotoRepresentation),
            new ParserVariant(activeStacksRepresentation, forActorStacksRepresentation, parseForestRepresentation,
                parseForestConstruction, stackRepresentation, reducing, false),
            imploderVariant, tokenizerVariant);
        System.out.println("JSGLR2 PT Var: " + variant.name());
        if(variant.equals(new IntegrationVariant(new ParseTableVariant(ActionsForCharacterRepresentation.DisjointSorted,
            ProductionToGotoRepresentation.JavaHashMap), naiveParserVariant, imploderVariant, tokenizerVariant)))
            throw new IllegalStateException("naive variant is only benchmarked once");
        else
            return variant;
    }

    @Override protected boolean implode() {
        return implode;
    }

    @Override protected Object action(Blackhole bh, StringInput input) throws ParseException {
        return ((IParser<IParseForest>) jsglr2.parser()).parseUnsafe(input.content, null);
    }

}
