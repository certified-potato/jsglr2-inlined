package org.spoofax.jsglr2.measure.parsing;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.metaborg.parsetable.IParseTable;
import org.metaborg.parsetable.ParseTableReadException;
import org.metaborg.parsetable.ParseTableReader;
import org.spoofax.jsglr2.JSGLR2Request;
import org.spoofax.jsglr2.measure.CSV;
import org.spoofax.jsglr2.measure.Config;
import org.spoofax.jsglr2.measure.MeasureTestSetWithParseTableReader;
import org.spoofax.jsglr2.measure.Measurements;
import org.spoofax.jsglr2.parseforest.IDerivation;
import org.spoofax.jsglr2.parseforest.IParseForest;
import org.spoofax.jsglr2.parseforest.IParseNode;
import org.spoofax.jsglr2.parseforest.ParseForestConstruction;
import org.spoofax.jsglr2.parseforest.ParseForestRepresentation;
import org.spoofax.jsglr2.parser.AbstractParseState;
import org.spoofax.jsglr2.parser.IObservableParser;
import org.spoofax.jsglr2.parser.ParserVariant;
import org.spoofax.jsglr2.reducing.Reducing;
import org.spoofax.jsglr2.stack.IStackNode;
import org.spoofax.jsglr2.stack.StackRepresentation;
import org.spoofax.jsglr2.stack.collections.ActiveStacksRepresentation;
import org.spoofax.jsglr2.stack.collections.ForActorStacksRepresentation;
import org.spoofax.jsglr2.testset.TestSetWithParseTable;
import org.spoofax.jsglr2.testset.testinput.StringInput;

public class ParsingMeasurements extends Measurements<String, StringInput> {

    public static final ParserVariant variantRecovery =
    //@formatter:off
        new ParserVariant(
            ActiveStacksRepresentation.ArrayList,
            ForActorStacksRepresentation.ArrayDeque,
            ParseForestRepresentation.Hybrid,
            ParseForestConstruction.Full,
            StackRepresentation.Hybrid,
            Reducing.Basic,
            false
        );
        //@formatter:on

    public ParsingMeasurements(TestSetWithParseTable<String, StringInput> testSet) {
        super(testSet);
    }

    @Override public void measure(Config<String, StringInput> config) throws ParseTableReadException, IOException {
        CSV<ParsingMeasurement> output = new CSV<>(ParsingMeasurement.values());

        IParseTable parseTable = new ParseTableReader().read(testSetReader.getParseTableTerm());

        //@formatter:off
        output.addRows(measure("recovery",     variantRecovery,      parseTable, new StandardParserMeasureObserver<>()));
        output.addRows(measureInlined("recoveryInlined", parseTable));
        //@formatter:on

        output.write(config.prefix(testSet) + "parsing.csv");
    }

    private List<Map<ParsingMeasurement, String>> measureInlined(String name, IParseTable parseTable) throws IOException {
    	return testSetReader.getInputBatches().map(inputBatch -> {
            return toOutputInlined(name, inputBatch);
        }).collect(Collectors.toList());
	}
    
	private
//@formatter:off
   <ParseForest extends IParseForest,
    Derivation  extends IDerivation<ParseForest>,
    ParseNode   extends IParseNode<ParseForest, Derivation>,
    StackNode   extends IStackNode,
    ParseState  extends AbstractParseState<?, StackNode>>
//@formatter:on
    List<Map<ParsingMeasurement, String>> measure(String name, ParserVariant variant, IParseTable parseTable,
        ParserMeasureObserver<ParseForest, Derivation, ParseNode, StackNode, ParseState> measureObserver)
        throws IOException {
        return testSetReader.getInputBatches().map(inputBatch -> {
            MeasureActiveStacksFactory measureActiveStacksFactory = new MeasureActiveStacksFactory();
            MeasureForActorStacksFactory measureForActorStacksFactory = new MeasureForActorStacksFactory();

            @SuppressWarnings("unchecked") IObservableParser<ParseForest, Derivation, ParseNode, StackNode, ParseState> parser =
                (IObservableParser<ParseForest, Derivation, ParseNode, StackNode, ParseState>) variant
                    .getParser(parseTable, measureActiveStacksFactory, measureForActorStacksFactory);

            parser.observing().attachObserver(measureObserver);

            try {
                for(StringInput input : inputBatch.inputs)
                    parser.parse(new JSGLR2Request(input.content, input.fileName, null), null, null);
            } catch(Exception e) {
                throw new IllegalStateException("Recovery variant measurement failed: "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
            }

            return toOutput(name, inputBatch, measureActiveStacksFactory, measureForActorStacksFactory,
                measureObserver);
        }).collect(Collectors.toList());
    }

    private
//@formatter:off
   <ParseForest extends IParseForest,
    Derivation  extends IDerivation<ParseForest>,
    ParseNode   extends IParseNode<ParseForest, Derivation>,
    StackNode   extends IStackNode,
    ParseState  extends AbstractParseState<?, StackNode>>
//@formatter:on
    Map<ParsingMeasurement, String> toOutput(String name,
        MeasureTestSetWithParseTableReader<String, StringInput>.InputBatch inputBatch,
        MeasureActiveStacksFactory measureActiveStacksFactory,
        MeasureForActorStacksFactory measureForActorStacksFactory,
        ParserMeasureObserver<ParseForest, Derivation, ParseNode, StackNode, ParseState> measureObserver) {
        return Arrays.stream(ParsingMeasurement.values()).collect(Collectors.toMap(Function.identity(), measurement -> {
            switch(measurement) {
                case name:
                    return name;
                case size:
                    return "" + inputBatch.size;
                case characters:
                    return "" + measureObserver.length;
                case activeStacksAdds:
                    return "" + (measureActiveStacksFactory.measureActiveStacks != null
                        ? measureActiveStacksFactory.measureActiveStacks.adds : 0);
                case activeStacksMaxSize:
                    return "" + (measureActiveStacksFactory.measureActiveStacks != null
                        ? measureActiveStacksFactory.measureActiveStacks.maxSize : 0);
                case activeStacksIsSingleChecks:
                    return "" + (measureActiveStacksFactory.measureActiveStacks != null
                        ? measureActiveStacksFactory.measureActiveStacks.iSingleChecks : 0);
                case activeStacksIsEmptyChecks:
                    return "" + (measureActiveStacksFactory.measureActiveStacks != null
                        ? measureActiveStacksFactory.measureActiveStacks.isEmptyChecks : 0);
                case activeStacksFindsWithState:
                    return "" + (measureActiveStacksFactory.measureActiveStacks != null
                        ? measureActiveStacksFactory.measureActiveStacks.findsWithState : 0);
                case activeStacksForLimitedReductions:
                    return "" + (measureActiveStacksFactory.measureActiveStacks != null
                        ? measureActiveStacksFactory.measureActiveStacks.forLimitedReductions : 0);
                case activeStacksAddAllTo:
                    return "" + (measureActiveStacksFactory.measureActiveStacks != null
                        ? measureActiveStacksFactory.measureActiveStacks.addAllTo : 0);
                case activeStacksClears:
                    return "" + (measureActiveStacksFactory.measureActiveStacks != null
                        ? measureActiveStacksFactory.measureActiveStacks.clears : 0);
                case activeStacksIterators:
                    return "" + (measureActiveStacksFactory.measureActiveStacks != null
                        ? measureActiveStacksFactory.measureActiveStacks.iterators : 0);
                case forActorAdds:
                    return "" + (measureForActorStacksFactory.measureForActorStacks != null
                        ? measureForActorStacksFactory.measureForActorStacks.forActorAdds : 0);
                case forActorDelayedAdds:
                    return "" + (measureForActorStacksFactory.measureForActorStacks != null
                        ? measureForActorStacksFactory.measureForActorStacks.forActorDelayedAdds : 0);
                case forActorMaxSize:
                    return "" + (measureForActorStacksFactory.measureForActorStacks != null
                        ? measureForActorStacksFactory.measureForActorStacks.forActorMaxSize : 0);
                case forActorDelayedMaxSize:
                    return "" + (measureForActorStacksFactory.measureForActorStacks != null
                        ? measureForActorStacksFactory.measureForActorStacks.forActorDelayedMaxSize : 0);
                case forActorContainsChecks:
                    return "" + (measureForActorStacksFactory.measureForActorStacks != null
                        ? measureForActorStacksFactory.measureForActorStacks.containsChecks : 0);
                case forActorNonEmptyChecks:
                    return "" + (measureForActorStacksFactory.measureForActorStacks != null
                        ? measureForActorStacksFactory.measureForActorStacks.nonEmptyChecks : 0);
                case stackNodes:
                    return "" + measureObserver.stackNodes;
                case stackNodesSingleLink:
                    return "" + measureObserver.stackNodesSingleLink;
                case stackLinks:
                    return "" + measureObserver.stackLinks;
                case stackLinksRejected:
                    return "" + measureObserver.stackLinksRejected;
                case deterministicDepthResets:
                    return "" + measureObserver.deterministicDepthResets;
                case parseNodes:
                    return "" + measureObserver.parseNodes;
                case parseNodesAmbiguous:
                    return "" + measureObserver.parseNodesAmbiguous;
                case parseNodesContextFree:
                    return "" + measureObserver.parseNodesContextFree;
                case parseNodesContextFreeAmbiguous:
                    return "" + measureObserver.parseNodesContextFreeAmbiguous;
                case parseNodesLexical:
                    return "" + measureObserver.parseNodesLexical;
                case parseNodesLexicalAmbiguous:
                    return "" + measureObserver.parseNodesLexicalAmbiguous;
                case parseNodesLayout:
                    return "" + measureObserver.parseNodesLayout;
                case parseNodesLayoutAmbiguous:
                    return "" + measureObserver.parseNodesLayoutAmbiguous;
                case parseNodesLiteral:
                    return "" + measureObserver.parseNodesLiteral;
                case parseNodesLiteralAmbiguous:
                    return "" + measureObserver.parseNodesLiteralAmbiguous;
                case parseNodesSingleDerivation:
                    return "" + measureObserver.parseNodesSingleDerivation;
                case characterNodes:
                    return "" + measureObserver.characterNodes;
                case actors:
                    return "" + measureObserver.actors;
                case doReductions:
                    return "" + measureObserver.doReductions;
                case doLimitedReductions:
                    return "" + measureObserver.doLimitedReductions;
                case doReductionsLR:
                    return "" + measureObserver.doReductionsLR;
                case doReductionsDeterministicGLR:
                    return "" + measureObserver.doReductionsDeterministicGLR;
                case doReductionsNonDeterministicGLR:
                    return "" + measureObserver.doReductionsNonDeterministicGLR;
                case reducers:
                    return "" + measureObserver.reducers;
                case reducersElkhound:
                    return "" + measureObserver.reducersElkhound;
                default:
                    return "";
            }
        }));
    }
    
    Map<ParsingMeasurement, String> toOutputInlined(String name,
            MeasureTestSetWithParseTableReader<String, StringInput>.InputBatch inputBatch) {
            return Arrays.stream(ParsingMeasurement.values()).collect(Collectors.toMap(Function.identity(), measurement -> {
                switch(measurement) {
                    case name:
                        return name;
                    case size:
                        return "" + inputBatch.size;
                    case characters:
                        return "";
                    case activeStacksAdds:
                        return "";
                    case activeStacksMaxSize:
                        return "";
                    case activeStacksIsSingleChecks:
                        return "";
                    case activeStacksIsEmptyChecks:
                        return "";
                    case activeStacksFindsWithState:
                    	return "";
                    case activeStacksForLimitedReductions:
                    	return "";
                    case activeStacksAddAllTo:
                    	return "";
                    case activeStacksClears:
                    	return "";
                    case activeStacksIterators:
                    	return "";
                    case forActorAdds:
                    	return "";
                    case forActorDelayedAdds:
                    	return "";
                    case forActorMaxSize:
                    	return "";
                    case forActorDelayedMaxSize:
                    	return "";
                    case forActorContainsChecks:
                    	return "";
                    case forActorNonEmptyChecks:
                        return "";
                    case stackNodes:
                        return "";
                    case stackNodesSingleLink:
                        return "";
                    case stackLinks:
                        return "";
                    case stackLinksRejected:
                        return "";
                    case deterministicDepthResets:
                        return "";
                    case parseNodes:
                        return "";
                    case parseNodesAmbiguous:
                        return "";
                    case parseNodesContextFree:
                        return "";
                    case parseNodesContextFreeAmbiguous:
                        return "";
                    case parseNodesLexical:
                        return "";
                    case parseNodesLexicalAmbiguous:
                        return "";
                    case parseNodesLayout:
                        return "";
                    case parseNodesLayoutAmbiguous:
                        return "";
                    case parseNodesLiteral:
                        return "";
                    case parseNodesLiteralAmbiguous:
                        return "";
                    case parseNodesSingleDerivation:
                        return "";
                    case characterNodes:
                        return "";
                    case actors:
                        return "";
                    case doReductions:
                        return "";
                    case doLimitedReductions:
                        return "";
                    case doReductionsLR:
                        return "";
                    case doReductionsDeterministicGLR:
                        return "";
                    case doReductionsNonDeterministicGLR:
                        return "";
                    case reducers:
                        return "";
                    case reducersElkhound:
                        return "";
                    default:
                        return "";
                }
            }));
        }

}
