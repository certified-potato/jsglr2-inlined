package org.spoofax.jsglr2.tests.util;

import static java.util.Collections.sort;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr2.JSGLR2;
import org.spoofax.jsglr2.JSGLR2Variants;
import org.spoofax.jsglr2.parser.ParseResult;
import org.spoofax.jsglr2.parser.Parser;
import org.spoofax.jsglr2.parsetable.IParseTable;
import org.spoofax.jsglr2.parsetable.ParseTableReadException;
import org.spoofax.jsglr2.parsetable.ParseTableReader;
import org.spoofax.jsglr2.util.AstUtilities;
import org.spoofax.jsglr2.util.Sdf2Table;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.io.binary.TermReader;

public abstract class BaseTest {

    private TermReader termReader;
    private IStrategoTerm parseTableTerm;
    
	protected AstUtilities astUtilities;
    
    protected BaseTest() {
        TermFactory termFactory = new TermFactory();
        this.termReader = new TermReader(termFactory);
        
        this.astUtilities = new AstUtilities();
    }
    
    @BeforeClass
    public static void setUpNativeSdf2Table() throws URISyntaxException, IOException {
    		Sdf2Table.setupSdf2TableInTargetDir();
    }
    
    public TermReader getTermReader() {
        return termReader;
    }
    
    public void setParseTableTerm(IStrategoTerm parseTableTerm) {
        this.parseTableTerm = parseTableTerm;
    }

    public IStrategoTerm getParseTableTerm() {
        return parseTableTerm;
    }
    
    protected IParseTable getParseTable() {
        try {
            return ParseTableReader.read(getParseTableTerm());
        } catch(ParseTableReadException e) {
            e.printStackTrace();
            
            fail();
            
            return null;
        }
    }

	public void testParseSuccess(String inputString) {
		IParseTable parseTable = getParseTable();
		
		for (JSGLR2Variants.Variant variant : JSGLR2Variants.allVariants()) {
			Parser<?, ?, ?, ?> parser = JSGLR2Variants.getParser(parseTable, variant);
			
	        ParseResult<?> parseResult = parser.parse(inputString);

	        assertEquals("Variant '" + variant.name() + "' failed: ", true, parseResult.isSuccess);
		}
	}

	public void testParseFailure(String inputString) {
		IParseTable parseTable = getParseTable();
		
		for (JSGLR2Variants.Variant variant : JSGLR2Variants.allVariants()) {
			Parser<?, ?, ?, ?> parser = JSGLR2Variants.getParser(parseTable, variant);
			
	        ParseResult<?> parseResult = parser.parse(inputString);

	        assertEquals("Variant '" + variant.name() + "' failed: ", false, parseResult.isSuccess);
		}
	}

    protected IStrategoTerm testSuccess(IParseTable parseTable, JSGLR2Variants.Variant variant, String inputString) {
    		JSGLR2<?, ?, IStrategoTerm> jsglr2 = JSGLR2Variants.getJSGLR2(parseTable, variant);
    		
        ParseResult<?> parseResult = jsglr2.parser.parse(inputString);

        assertEquals("Variant '" + variant.name() + "' failed parsing: ", true, parseResult.isSuccess); // Fail here if parsing failed
        
        IStrategoTerm result = jsglr2.parse(inputString);
        
        assertNotNull("Variant '" + variant.name() + "' failed imploding: ", result); // Fail here if imploding or tokenization failed
        
        return result;
    }
	
	protected void testSuccessByAstString(String inputString, String expectedOutputAstString) {
		testSuccess(inputString, expectedOutputAstString, false);
	}
	
	protected void testSuccessByExpansions(String inputString, String expectedOutputAstString) {
		testSuccess(inputString, expectedOutputAstString, true);
	}
	
	private void testSuccess(String inputString, String expectedOutputAstString, boolean equalityByExpansions) {
		IParseTable parseTable = getParseTable();
		
		for (JSGLR2Variants.Variant variant : JSGLR2Variants.allVariants()) {
			IStrategoTerm actualOutputAst = testSuccess(parseTable, variant, inputString);
			
			if (equalityByExpansions) {
				IStrategoTerm expectedOutputAst = termReader.parseFromString(expectedOutputAstString);

	            assertEqualTermExpansions(expectedOutputAst, actualOutputAst);
			} else {
	            assertEquals(expectedOutputAstString, actualOutputAst.toString());
			}
		}
	}
	
	protected void assertEqualTermExpansions(IStrategoTerm expected, IStrategoTerm actual) {
		List<String> expectedExpansion = toSortedStringList(this.astUtilities.expand(expected));
		List<String> actualExpansion = toSortedStringList(this.astUtilities.expand(actual));
		
		assertEquals(expectedExpansion, actualExpansion);
		
	}
	
	private List<String> toSortedStringList(List<IStrategoTerm> astExpansion) {
		List<String> result = new ArrayList<String>(astExpansion.size());
		
		for (IStrategoTerm ast : astExpansion) {
			result.add(ast.toString());
		}
		
		sort(result);
		
		return result;
	}

    protected String getFileAsString(String filename) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        byte[] encoded = Files.readAllBytes(Paths.get(classLoader.getResource("samples/" + filename).getPath()));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    protected IStrategoTerm getFileAsAST(String filename) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = new FileInputStream(classLoader.getResource("samples/" + filename).getFile());
        return this.termReader.parseFromStream(inputStream);
    }
	
}
