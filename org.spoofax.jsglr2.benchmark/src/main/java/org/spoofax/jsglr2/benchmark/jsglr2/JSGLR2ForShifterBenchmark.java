package org.spoofax.jsglr2.benchmark.jsglr2;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr2.JSGLR2Variants;
import org.spoofax.jsglr2.JSGLR2Variants.ParseForestConstruction;
import org.spoofax.jsglr2.JSGLR2Variants.ParseForestRepresentation;
import org.spoofax.jsglr2.JSGLR2Variants.Reducing;
import org.spoofax.jsglr2.JSGLR2Variants.StackRepresentation;
import org.spoofax.jsglr2.benchmark.BaseBenchmark;
import org.spoofax.jsglr2.benchmark.BenchmarkParserObserver;
import org.spoofax.jsglr2.parseforest.AbstractParseForest;
import org.spoofax.jsglr2.parser.ForShifterElement;
import org.spoofax.jsglr2.parser.IParser;
import org.spoofax.jsglr2.parser.ParseException;
import org.spoofax.jsglr2.parsetable.IParseTable;
import org.spoofax.jsglr2.parsetable.ParseTableReadException;
import org.spoofax.jsglr2.parsetable.ParseTableReader;
import org.spoofax.jsglr2.stack.AbstractStackNode;
import org.spoofax.jsglr2.testset.Input;
import org.spoofax.jsglr2.testset.TestSet;
import org.spoofax.terms.ParseError;

public abstract class JSGLR2ForShifterBenchmark extends BaseBenchmark {

    IParser<?, ?> parser;
    ForShifterObserver forShifterObserver;

    protected JSGLR2ForShifterBenchmark(TestSet testSet) {
        super(testSet);
    }

    public enum Representation {
        ArrayDequeue, ArrayList, LinkedList, Set
    }

    @Param({ "ArrayDequeue", "ArrayList", "LinkedList", "Set" }) public Representation representation;

    Collection<ForShifterElement<?, ?>> forShifter;

    @Setup public void parserSetup() throws ParseError, ParseTableReadException, IOException,
        InvalidParseTableException, InterruptedException, URISyntaxException {
        IParseTable parseTable = new ParseTableReader().read(testSetReader.getParseTableTerm());

        parser = JSGLR2Variants.getParser(parseTable, ParseForestRepresentation.Basic, ParseForestConstruction.Full,
            StackRepresentation.Basic, Reducing.Basic);

        forShifterObserver = new ForShifterObserver();

        parser.attachObserver(forShifterObserver);

        try {
            for(Input input : inputs)
                parser.parseUnsafe(input.content, input.filename, null);
        } catch(ParseException e) {
            throw new IllegalStateException("setup of benchmark should not fail");
        }

        switch(representation) {
            case ArrayDequeue:
                forShifter = new ArrayDeque<>();

                break;
            case ArrayList:
                forShifter = new ArrayList<>();

                break;
            case LinkedList:
                forShifter = new LinkedList<>();

                break;
            case Set:
                forShifter = new HashSet<>();

                break;
            default:
                break;
        }
    }

    class ParseRound {
        final List<ForShifterElement<?, ?>> forShifterElements = new ArrayList<ForShifterElement<?, ?>>();
    }

    class ForShifterObserver<StackNode extends AbstractStackNode<ParseForest>, ParseForest extends AbstractParseForest>
        extends BenchmarkParserObserver<StackNode, ParseForest> {

        public List<ParseRound> parseRounds = new ArrayList<ParseRound>();

        @Override public void parseCharacter(int character, Iterable<StackNode> activeStacks) {
            parseRounds.add(new ParseRound());
        }

        private ParseRound currentParseRound() {
            return parseRounds.get(parseRounds.size() - 1);
        }

        @Override public void addForShifter(ForShifterElement<StackNode, ParseForest> forShifterElement) {
            currentParseRound().forShifterElements.add(forShifterElement);
        }

    }

    @Benchmark public void benchmark(Blackhole bh) throws ParseException {
        for(ParseRound parseRound : ((ForShifterObserver<?, ?>) forShifterObserver).parseRounds) {
            forShifter.clear();

            for(ForShifterElement<?, ?> forShifterElement : parseRound.forShifterElements)
                forShifter.add(forShifterElement);

            for(ForShifterElement<?, ?> forShifterElement : forShifter)
                bh.consume(forShifterElement);
        }
    }

}
