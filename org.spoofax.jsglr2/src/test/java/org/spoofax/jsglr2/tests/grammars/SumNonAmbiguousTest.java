package org.spoofax.jsglr2.tests.grammars;

import java.io.IOException;

import org.junit.Test;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr2.parsetable.ParseTableReadException;
import org.spoofax.jsglr2.tests.util.BaseTestWithJSGLR1;
import org.spoofax.jsglr2.util.WithGrammar;
import org.spoofax.terms.ParseError;

public class SumNonAmbiguousTest extends BaseTestWithJSGLR1 implements WithGrammar {
	
	public SumNonAmbiguousTest() throws ParseError, ParseTableReadException, IOException, InvalidParseTableException, InterruptedException {
		setupParseTableFromDefFile("sum-nonambiguous");
	}

    @Test
    public void one() throws ParseError, ParseTableReadException, IOException {
    		testParseSuccessByJSGLR("x");
    }

    @Test
    public void two() throws ParseError, ParseTableReadException, IOException {
    		testParseSuccessByJSGLR("x+x");
    }

    @Test
    public void three() throws ParseError, ParseTableReadException, IOException {
    		testParseSuccessByJSGLR("x+x+x");
    }
  
}