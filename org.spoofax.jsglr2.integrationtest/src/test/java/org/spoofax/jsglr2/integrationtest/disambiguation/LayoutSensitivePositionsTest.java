package org.spoofax.jsglr2.integrationtest.disambiguation;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.spoofax.jsglr2.integrationtest.BaseTestWithLayoutSensitiveSdf3ParseTables;
import org.spoofax.terms.ParseError;

public class LayoutSensitivePositionsTest extends BaseTestWithLayoutSensitiveSdf3ParseTables {

    public LayoutSensitivePositionsTest() {
        super("layout-sensitive-positions.sdf3");
    }

    @TestFactory public Stream<DynamicTest> literalFirstLine1Col1() throws ParseError {
        //@formatter:off
        return concat(
            testLayoutSensitiveSuccessByExpansions("l", "LiteralFirstLine1Col1()", "LiteralFirstLine1Col1"),
            testLayoutSensitiveParseFiltered(" l", "LiteralFirstLine1Col1"),
            testLayoutSensitiveParseFiltered("\nl", "LiteralFirstLine1Col1"),
            testLayoutSensitiveParseFiltered("\n\rl", "LiteralFirstLine1Col1"),
            testLayoutSensitiveParseFiltered(" \nl", "LiteralFirstLine1Col1"),
            testLayoutSensitiveParseFiltered(" \n\rl", "LiteralFirstLine1Col1"),
            testLayoutSensitiveParseFiltered("\n l", "LiteralFirstLine1Col1"),
            testLayoutSensitiveParseFiltered("\n\r l", "LiteralFirstLine1Col1")
        );
        //@formatter:on
    }

    @TestFactory public Stream<DynamicTest> literalFirstLine1Col2() throws ParseError {
        //@formatter:off
        return concat(
            testLayoutSensitiveParseFiltered("l", "LiteralFirstLine1Col2"),
            testLayoutSensitiveSuccessByExpansions(" l", "LiteralFirstLine1Col2()", "LiteralFirstLine1Col2"),
            testLayoutSensitiveParseFiltered("\nl", "LiteralFirstLine1Col2"),
            testLayoutSensitiveParseFiltered("\n\rl", "LiteralFirstLine1Col2"),
            testLayoutSensitiveParseFiltered(" \nl", "LiteralFirstLine1Col2"),
            testLayoutSensitiveParseFiltered(" \n\rl", "LiteralFirstLine1Col2"),
            testLayoutSensitiveParseFiltered("\n l", "LiteralFirstLine1Col2"),
            testLayoutSensitiveParseFiltered("\n\r l", "LiteralFirstLine1Col2")
        );
        //@formatter:on
    }

    @TestFactory public Stream<DynamicTest> literalFirstLine2Col1() throws ParseError {
        //@formatter:off
        return concat(
            testLayoutSensitiveParseFiltered("l", "LiteralFirstLine2Col1"),
            testLayoutSensitiveParseFiltered(" l", "LiteralFirstLine2Col1"),
            testLayoutSensitiveSuccessByExpansions("\nl", "LiteralFirstLine2Col1()", "LiteralFirstLine2Col1"),
            testLayoutSensitiveSuccessByExpansions("\n\rl", "LiteralFirstLine2Col1()", "LiteralFirstLine2Col1"),
            testLayoutSensitiveSuccessByExpansions(" \nl", "LiteralFirstLine2Col1()", "LiteralFirstLine2Col1"),
            testLayoutSensitiveSuccessByExpansions(" \n\rl", "LiteralFirstLine2Col1()", "LiteralFirstLine2Col1"),
            testLayoutSensitiveParseFiltered("\n l", "LiteralFirstLine2Col1"),
            testLayoutSensitiveParseFiltered("\n\r l", "LiteralFirstLine2Col1")
        );
        //@formatter:on
    }

    @TestFactory public Stream<DynamicTest> literalFirstLine2Col2() throws ParseError {
        //@formatter:off
        return concat(
            testLayoutSensitiveParseFiltered("l", "LiteralFirstLine2Col2"),
            testLayoutSensitiveParseFiltered(" l", "LiteralFirstLine2Col2"),
            testLayoutSensitiveParseFiltered("\nl", "LiteralFirstLine2Col2"),
            testLayoutSensitiveParseFiltered("\n\rl", "LiteralFirstLine2Col2"),
            testLayoutSensitiveParseFiltered(" \nl", "LiteralFirstLine2Col2"),
            testLayoutSensitiveParseFiltered(" \n\rl", "LiteralFirstLine2Col2"),
            testLayoutSensitiveSuccessByExpansions("\n l", "LiteralFirstLine2Col2()", "LiteralFirstLine2Col2"),
            testLayoutSensitiveSuccessByExpansions("\n\r l", "LiteralFirstLine2Col2()", "LiteralFirstLine2Col2")
        );
        //@formatter:on
    }

    @TestFactory public Stream<DynamicTest> lexicalFirstLine1Col1() throws ParseError {
        //@formatter:off
        return concat(
            testLayoutSensitiveSuccessByExpansions("x", "LexicalFirstLine1Col1(\"x\")", "LexicalFirstLine1Col1"),
            testLayoutSensitiveParseFiltered(" x", "LexicalFirstLine1Col1"),
            testLayoutSensitiveParseFiltered("\nx", "LexicalFirstLine1Col1"),
            testLayoutSensitiveParseFiltered("\n\rx", "LexicalFirstLine1Col1"),
            testLayoutSensitiveParseFiltered(" \nx", "LexicalFirstLine1Col1"),
            testLayoutSensitiveParseFiltered(" \n\rx", "LexicalFirstLine1Col1"),
            testLayoutSensitiveParseFiltered("\n x", "LexicalFirstLine1Col1"),
            testLayoutSensitiveParseFiltered("\n\r x", "LexicalFirstLine1Col1")
        );
        //@formatter:on
    }

    @TestFactory public Stream<DynamicTest> lexicalFirstLine1Col2() throws ParseError {
        //@formatter:off
        return concat(
            testLayoutSensitiveParseFiltered("x", "LexicalFirstLine1Col2"),
            testLayoutSensitiveSuccessByExpansions(" x", "LexicalFirstLine1Col2(\"x\")", "LexicalFirstLine1Col2"),
            testLayoutSensitiveParseFiltered("\nx", "LexicalFirstLine1Col2"),
            testLayoutSensitiveParseFiltered("\n\rx", "LexicalFirstLine1Col2"),
            testLayoutSensitiveParseFiltered(" \nx", "LexicalFirstLine1Col2"),
            testLayoutSensitiveParseFiltered(" \n\rx", "LexicalFirstLine1Col2"),
            testLayoutSensitiveParseFiltered("\n x", "LexicalFirstLine1Col2"),
            testLayoutSensitiveParseFiltered("\n\r x", "LexicalFirstLine1Col2")
        );
        //@formatter:on
    }

    @TestFactory public Stream<DynamicTest> lexicalFirstLine2Col1() throws ParseError {
        //@formatter:off
        return concat(
            testLayoutSensitiveParseFiltered("x", "LexicalFirstLine2Col1"),
            testLayoutSensitiveParseFiltered(" x", "LexicalFirstLine2Col1"),
            testLayoutSensitiveSuccessByExpansions("\nx", "LexicalFirstLine2Col1(\"x\")", "LexicalFirstLine2Col1"),
            testLayoutSensitiveSuccessByExpansions("\n\rx", "LexicalFirstLine2Col1(\"x\")", "LexicalFirstLine2Col1"),
            testLayoutSensitiveSuccessByExpansions(" \nx", "LexicalFirstLine2Col1(\"x\")", "LexicalFirstLine2Col1"),
            testLayoutSensitiveSuccessByExpansions(" \n\rx", "LexicalFirstLine2Col1(\"x\")", "LexicalFirstLine2Col1"),
            testLayoutSensitiveParseFiltered("\n x", "LexicalFirstLine2Col1"),
            testLayoutSensitiveParseFiltered("\n\r x", "LexicalFirstLine2Col1")
        );
        //@formatter:on
    }

    @TestFactory public Stream<DynamicTest> lexicalFirstLine2Col2() throws ParseError {
        //@formatter:off
        return concat(
            testLayoutSensitiveParseFiltered("x", "LexicalFirstLine2Col2"),
            testLayoutSensitiveParseFiltered(" x", "LexicalFirstLine2Col2"),
            testLayoutSensitiveParseFiltered("\nx", "LexicalFirstLine2Col2"),
            testLayoutSensitiveParseFiltered("\n\rx", "LexicalFirstLine2Col2"),
            testLayoutSensitiveParseFiltered(" \nx", "LexicalFirstLine2Col2"),
            testLayoutSensitiveParseFiltered(" \n\rx", "LexicalFirstLine2Col2"),
            testLayoutSensitiveSuccessByExpansions("\n x", "LexicalFirstLine2Col2(\"x\")", "LexicalFirstLine2Col2"),
            testLayoutSensitiveSuccessByExpansions("\n\r x", "LexicalFirstLine2Col2(\"x\")", "LexicalFirstLine2Col2")
        );
        //@formatter:on
    }

}
