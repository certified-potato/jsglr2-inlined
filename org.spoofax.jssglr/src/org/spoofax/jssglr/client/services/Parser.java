package org.spoofax.jssglr.client.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.spoofax.jsglr.client.imploder.AbstractTokenizer.findLeftMostTokenOnSameLine;
import static org.spoofax.jsglr.client.imploder.AbstractTokenizer.findRightMostTokenOnSameLine;

import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr.client.ParseException;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.jsglr.client.RegionRecovery;
import org.spoofax.jsglr.client.SGLR;
import org.spoofax.jsglr.client.imploder.Token;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.TermTreeFactory;
import org.spoofax.jsglr.client.imploder.TreeBuilder;
import org.spoofax.jsglr.shared.BadTokenException;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.jsglr.shared.TokenExpectedException;
import org.spoofax.terms.TermFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

/**
 * A parser for a specific editor instance.
 */
public class Parser {

	// TODO: get list of incremental sorts from some place
	private final String[] HACK_DEFAULT_INCREMENTAL_SORTS =
		{ "RuleDec", "SDec",
		  "MethodDec", "ClassBodyDec", "ClassMemberDec", "ConstrDec", "FieldDec" };

	private static final int LARGE_REGION_SIZE = 8;

	private static final String LARGE_REGION_START =
		"Region could not be parsed because of subsequent syntax error(s) indicated below";

	private final ITermFactory af;
	private ParseTable parseTable;
	private TreeBuilder treeBuilder;
	private SGLR sglr;
	private Set<String> incrementalSorts;
	private IStrategoTerm lastResult;

	public Parser() {
		af = new TermFactory();

		incrementalSorts = new HashSet<String>();
		for (String s : HACK_DEFAULT_INCREMENTAL_SORTS)
			incrementalSorts.add(s);
	}

	public JavaScriptObject asyncInitializeFromURL(String parseTableURL) {
		final RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, parseTableURL);
		try {
			builder.sendRequest( null,  new RequestCallback() {
				@Override
				public void onError(Request request, Throwable exception)
				{
					GWT.log( "error", exception );
				}
				@Override
				public void onResponseReceived(Request request, Response response) {
					if(response.getStatusCode() == 200 || response.getStatusCode() == 304) {
						initialize(response.getText());
					}
				}
			});
		} catch (final RequestException e) {
			GWT.log( "error", e);
		}

		return exposeParser(this);
	}

	private void initialize(String tableContents) {
		// TODO: share table across multiple Parser instances
		IStrategoTerm tableTerm = af.parseFromString(tableContents);
		try {
			parseTable = new ParseTable(tableTerm, af);
			TermTreeFactory factory = new TermTreeFactory(af);
			treeBuilder = new TreeBuilder(factory);
			sglr = new SGLR(treeBuilder, parseTable);
			sglr.setUseStructureRecovery(true);
//			sglr = new IncrementalSGLR<IStrategoTerm>(parser, C_STYLE, factory, incrementalSorts);
		} catch (InvalidParseTableException e) {
			GWT.log("error", e);
		}
	}

	public boolean isReady() {
		return sglr != null;
	}

	private native JavaScriptObject exposeParser (Parser parser) /*-{
		var self = this;
		var parser = {};
		parser.parse = function (text) {
			return self.@org.spoofax.jssglr.client.services.Parser::parse(Ljava/lang/String;)(text);
		};
		parser.parseAndTokenize = function (lineCount, text) {
			return self.@org.spoofax.jssglr.client.services.Parser::parseAndTokenize(ILjava/lang/String;)(lineCount, text);
		};
		parser.isReady = function() {
			return self.@org.spoofax.jssglr.client.services.Parser::isReady()();
		};
		return parser;
	}-*/;

	private int findRightMostWithSameError(IToken token, String prefix) {
		String expectedError = token.getError();
		ITokenizer tokenizer = token.getTokenizer();
		int i = token.getIndex();
		for (int max = tokenizer.getTokenCount(); i + 1 < max; i++) {
			String error = tokenizer.getTokenAt(i + 1).getError();
			if (error != expectedError
					&& (error == null || prefix == null || !error.startsWith(prefix)))
				break;
		}
		return i;
	}

	private void reportErrorNearOffset(JsArray<JavaScriptObject> jserrors, ITokenizer tokenizer, int offset, String message) {
		IToken errorToken = tokenizer.getErrorTokenOrAdjunct(offset);
		reportErrorAtTokens(jserrors, errorToken, errorToken, message);
	}

	private void reportErrorAtTokens(JsArray<JavaScriptObject> jserrors, final IToken left, final IToken right, String message) {
		if (left.getStartOffset() > right.getEndOffset()) {
			reportErrorNearOffset(jserrors, left.getTokenizer(), left.getStartOffset(), message);
		} else {
			jserrors.push(createWarningToken(left.getLine() - 1, left.getColumn(), message, false));
		}
	}

	private void reportWarningAtTokens(JsArray<JavaScriptObject> jserrors, final IToken left, final IToken right, final String message) {
		jserrors.push(createWarningToken(left.getLine() - 1, left.getColumn(), message, true));
	}

	private static List<BadTokenException> getCollectedErrorsInRegion(SGLR parser, IToken left, IToken right, boolean alsoOutside) {
		List<BadTokenException> results = new ArrayList<BadTokenException>();
		int line = left.getLine();
		int endLine = right.getLine() + (alsoOutside ? RegionRecovery.NR_OF_LINES_TILL_SUCCESS : 0);
		for (BadTokenException e : parser.getCollectedErrors()) {
			if (e.getLineNumber() >= line && e.getLineNumber() <= endLine)
				results.add(e);
		}
		return results;
	}

	private static IToken findNextNonEmptyToken(IToken token) {
		ITokenizer tokenizer = token.getTokenizer();
		IToken result = null;
		for (int i = token.getIndex(), max = tokenizer.getTokenCount(); i < max; i++) {
			result = tokenizer.getTokenAt(i);
			if (result.getLength() != 0 && !Token.isWhiteSpace(result)) break;
		}
		return result;
	}

	private void reportBadToken(JsArray<JavaScriptObject> jserrors, ITokenizer tokenizer, BadTokenException exception) {
		String message;
		if (exception.isEOFToken() || tokenizer.getTokenCount() <= 1) {
			message = exception.getShortMessage();
		} else {
			IToken token = tokenizer.getTokenAtOffset(exception.getOffset());
			token = findNextNonEmptyToken(token);
			message = ITokenizer.ERROR_WATER_PREFIX + ": " + token.toString().trim();
		}
		reportErrorNearOffset(jserrors, tokenizer, exception.getOffset(), message);
	}


	private void reportSkippedRegion(JsArray<JavaScriptObject> jserrors, SGLR parser, IToken left, IToken right) {
		// Find a parse failure(s) in the given token range
		int line = left.getLine();
		int reportedLine = -1;
		for (BadTokenException e : getCollectedErrorsInRegion(parser, left, right, true)) {
			reportBadToken(jserrors, left.getTokenizer(), e);
			if (reportedLine == -1)
				reportedLine = e.getLineNumber();
		}

		if (reportedLine == -1) {
			// Report entire region
			reportErrorAtTokens(jserrors, left, right, ITokenizer.ERROR_SKIPPED_REGION);
		} else if (reportedLine - line >= LARGE_REGION_SIZE) {
			// Warn at start of region
			reportErrorAtTokens(jserrors, findLeftMostTokenOnSameLine(left),
					findRightMostTokenOnSameLine(left), LARGE_REGION_START);
		}
	}

	@SuppressWarnings("unchecked")
	public JavaScriptObject parseAndTokenize(int lines, String text) {
		final JsArray<JavaScriptObject>[] attrs = new JsArray[lines];
		for(int i = 0; i < lines; i++) {
			attrs[i] = (JsArray<JavaScriptObject>) JavaScriptObject.createArray();
		}
		final ISimpleTerm o = parse(text);
		JsArray<JavaScriptObject> jserrors = (JsArray<JavaScriptObject>) JavaScriptObject.createArray();
		if(o == null) {
			return makeParseResult(makeJsArray(attrs), jserrors);
		}
		final IToken t = ImploderAttachment.get(o).getLeftToken();
		if(t == null) {
			return makeParseResult(makeJsArray(attrs), jserrors);
		}
		final ITokenizer tok = t.getTokenizer();

		for(int i = 0; i < tok.getTokenCount(); i++) {
			final IToken x = tok.getTokenAt(i);
			int line = x.getLine() - 1;
			//debugToken(x);

			final int start = x.getColumn();
			final int end = x.getEndOffset() - x.getStartOffset() + start + 1;
			final String tokentype = convertTokenType(x.getKind());
			attrs[line].push(createBespinToken(x.toString(), tokentype, start, end, x.getLine()));
		}

		// https://svn.strategoxt.org/repos/StrategoXT/spoofax-imp/trunk/org.strategoxt.imp.runtime/src/org/strategoxt/imp/runtime/parser/ParseErrorHandler.java
		for(int i = 0; i < tok.getTokenCount(); i++) {
			final IToken x = tok.getTokenAt(i);
			final String error = x.getError();
			if (error != null) {
				if (error == ITokenizer.ERROR_SKIPPED_REGION) {
					i = findRightMostWithSameError(x, null);
					reportSkippedRegion(jserrors, sglr, x, tok.getTokenAt(i));
				} else if (error.startsWith(ITokenizer.ERROR_WARNING_PREFIX)) {
					i = findRightMostWithSameError(x, null);
					reportWarningAtTokens(jserrors, x, tok.getTokenAt(i), error);
				} else if (error.startsWith(ITokenizer.ERROR_WATER_PREFIX)) {
					i = findRightMostWithSameError(x, ITokenizer.ERROR_WATER_PREFIX);
					reportErrorAtTokens(jserrors, x, tok.getTokenAt(i), error);
				} else {
					i = findRightMostWithSameError(x, null);
					// UNDONE: won't work for multi-token errors (as seen in SugarJ)
					reportErrorAtTokens(jserrors, x, tok.getTokenAt(i), error);
				}
			}
		}

		return makeParseResult(makeJsArray(attrs), jserrors);
	}

	public native static void debug(String message) /*-{
		$self.sender.emit("log", message);
	}-*/;

	public native static JavaScriptObject createWarningToken(int row, int column, String text, boolean isWarning) /*-{
		return {
			row: row,
			column: column,
			text: text,
			type: isWarning ? "warning" : "error"
		};
	}-*/;

	public native static JavaScriptObject createBespinToken(String value, String tokentype, int startColumn, int endColumn, int lineNumber) /*-{
		return {
	            type: tokentype,
				value: value,
	            start: startColumn,
	            end: endColumn,
	            line: lineNumber
		       };
	}-*/;

	@SuppressWarnings("unchecked")
	private JsArray<JavaScriptObject> makeJsArray(JsArray<JavaScriptObject>[] attrs) {
		JsArray<JavaScriptObject> r = (JsArray<JavaScriptObject>) JavaScriptObject.createArray();
		for(JavaScriptObject o : attrs)
			r.push(o);
		return r;
	}

	private native static JavaScriptObject makeParseResult(
			JsArray<JavaScriptObject> tokens, JsArray<JavaScriptObject> errors) /*-{
		return {
			tokens: tokens,
			errors: errors
		};
	}-*/;

	private void debugToken(final IToken x) {
		System.out.println("line  = " + x.getLine());
		System.out.println("start = " + x.getColumn());
		System.out.println("end   = " + (x.getColumn() + x.getEndOffset() - x.getStartOffset() + 1));
		System.out.println("kind  = " + x.getKind());
		System.out.println("tag   = " + convertTokenType(x.getKind()));
		System.out.println("tok   = \"" + x.toString() + "\"");
	}

	private String convertTokenType(int kind) {
		switch(kind) {
		case IToken.TK_LAYOUT: return "comment";
		case IToken.TK_NUMBER: return "constant.numeric";
		case IToken.TK_OPERATOR: return "keyword.operator";
		case IToken.TK_KEYWORD: return "keyword";
		case IToken.TK_STRING: return "string";
		default: return "plain";
		}
	}

	public IStrategoTerm parse(String text) {
		try {
			IStrategoTerm result;
//			try {
//				result = sglr.parseIncremental(text, null, null);
//			} catch (IncrementalSGLRException e) {
				result = (IStrategoTerm)sglr.parse(text, null, null);
//			}
			System.out.println(result);
			return result;
		} catch (final TokenExpectedException e) {
			e.printStackTrace();
		} catch (final BadTokenException e) {
			e.printStackTrace();
		} catch (final ParseException e) {
			e.printStackTrace();
		} catch (final SGLRException e) {
			e.printStackTrace();
		}
		return null;
	}
}