package org.spoofax.jsglr.client.imploder;

import static java.lang.Math.min;
import static org.spoofax.jsglr.client.imploder.IToken.Kind.*;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.jsglr.client.IKeywordRecognizer;
import org.spoofax.jsglr.client.KeywordRecognizer;
import org.spoofax.terms.SimpleTermVisitor;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Karl Trygve Kalleberg <karltk near strategoxt dot org>
 */
public class Tokenizer extends AbstractTokenizer {

    private static final long serialVersionUID = -7507424974905643146L;

    private static final double EXPECTED_TOKENS_DIVIDER = 1.3;

    private final ArrayList<Token> tokens;

    private IKeywordRecognizer keywords;

    private ISimpleTerm ast;

    /** Start of the next token. */
    private int startOffset;

    /** Line number of the next token. */
    private int line;

    private int offsetAtLineStart;

    /**
     * Creates a new tokenizer for the given file name (if applicable) and contents.
     */
    public Tokenizer(String input, String filename, IKeywordRecognizer keywords) {
        this(input, filename, keywords, true);
    }

    public Tokenizer(String input, String filename, IKeywordRecognizer keywords, boolean startWithReserved) {
        super(input, filename);
        this.keywords = keywords;
        this.tokens = new ArrayList<Token>(); // capacity set in internalMakeToken()
        startOffset = 0;
        line = 0;
        offsetAtLineStart = 0;
        // Ensure there's at least one token
        if(startWithReserved) {
            tokens.add(new Token(this, filename, 0, line, 0, 0, -1, TK_RESERVED));
        }
    }

    public final int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        assert isAmbiguous() || this.startOffset >= getInput().length();
        if(this.startOffset == startOffset)
            return;
        this.startOffset = startOffset;
        IToken lastToken = getTokenAtOffset(startOffset);
        this.offsetAtLineStart = lastToken.getStartOffset() - lastToken.getColumn();
        this.line = lastToken.getLine();
    }

    public IToken currentToken() {
        return tokens.size() == 0 ? null : tokens.get(tokens.size() - 1);
    }

    public int getTokenCount() {
        return tokens.size();
    }

    public Token getTokenAt(int i) {
        Token result = tokens.get(i);
        // Disabled: might fail for testing language token sequences
        // (e.g., self-application.spt)
        // assert i == 0 || result.getIndex() == i;
        return result;
    }

    public Token internalGetTokenAt(int i) {
        Token result = tokens.get(i);
        return result;
    }

    protected void removeTokenAt(int i) {
        tokens.remove(i);
    }

    public void setPositions(int line, int startOffset, int offsetAtLineStart) {
        this.line = line;
        this.offsetAtLineStart = offsetAtLineStart;
        this.startOffset = startOffset;
    }

    protected void setKeywordRecognizer(IKeywordRecognizer keywords) {
        this.keywords = keywords;
    }

    public IToken getTokenAtOffset(int offset) {
        assert isAmbiguous() || getTokenCount() < 2
            || internalGetTokenAt(getTokenCount() - 1)
                .getStartOffset() == internalGetTokenAt(getTokenCount() - 2).getEndOffset()
                    + 1 : "Unordered tokens at end of tokenizer";
        Token key = new Token(this, getFilename(), -1, -1, -1, offset, offset - 1, TK_RESERVED);
        int resultIndex = Collections.binarySearch(tokens, key);
        if(resultIndex == -1)
            throw new IndexOutOfBoundsException(
                "No token at offset " + offset + " (binary search returned " + resultIndex + ")");
        if(resultIndex < -1)
            resultIndex = (-resultIndex) - 1;
        if(offset == getInput().length() && tokens.size() > 0)
            return currentToken();
        if(resultIndex >= getTokenCount())
            throw new IndexOutOfBoundsException("No token at offset " + offset);
        return /* resultIndex == -1 ? null : */ internalGetTokenAt(resultIndex);
    }

    public final Token makeToken(int endOffset, IToken.Kind kind, boolean allowEmptyToken) {
        return makeToken(endOffset, kind, allowEmptyToken, null);
    }

    public Token makeToken(int endOffset, IToken.Kind kind, boolean allowEmptyToken, String errorMessage) {
        String input = getInput();
        assert endOffset <= input.length();
        if(!allowEmptyToken && startOffset > endOffset) // empty token
            return null;

        assert endOffset + 1 >= startOffset
            || (kind == TK_RESERVED && startOffset == 0) : "Creating token ending before current start offset";

        int offset;
        Token token = null;
        for(offset = min(startOffset, endOffset); offset < endOffset; offset++) {
            if(input.charAt(offset) == '\n') {
                if(offset - 1 > startOffset)
                    token = internalMakeToken(kind, offset - 1, errorMessage);
                internalMakeToken(TK_LAYOUT, offset, errorMessage); // newline
                line++;
                offsetAtLineStart = startOffset;
            }
        }

        if(token == null || offset <= endOffset) {
            int oldStartOffset = startOffset;
            token = internalMakeToken(kind, offset, errorMessage);
            if(offset >= oldStartOffset && input.charAt(offset) == '\n') {
                line++;
                offsetAtLineStart = startOffset;
            }
            return token;
        } else {
            return token;
        }
    }

    protected Token internalMakeToken(IToken.Kind kind, int endOffset, String errorMessage) {
        if(endOffset >= getInput().length()) { // somebody set up us the bomb
            assert false;
            endOffset = getInput().length() - 1; // move 'zig'
        }
        Token result =
            new Token(this, getFilename(), tokens.size(), line, startOffset - offsetAtLineStart, startOffset, endOffset, kind);
        if(errorMessage != null)
            result.setError(errorMessage);
        if(tokens.size() == 5)
            tokens.ensureCapacity((int) (getInput().length() / EXPECTED_TOKENS_DIVIDER));
        tokens.add(result);
        startOffset = endOffset + 1;
        return result;
    }

    /**
     * Reassigns (i.e., steals) an existing tokenizer to this tokenizer.
     */
    public void reassignToken(Token token) {
        assert token.getTokenizer() != this;
        assert token.getEndOffset() <= getInput().length();
        token.setTokenizer(this);
        token.setIndex(tokens.size());
        tokens.add(token);
        startOffset = token.getEndOffset() + 1;
    }

    @Override protected void setErrorMessage(IToken leftToken, IToken rightToken, String message) {
        assert leftToken.getTokenizer() == this && rightToken.getTokenizer() == this;
        for(int i = leftToken.getIndex(), max = rightToken.getIndex(); i <= max; i++) {
            tokens.get(i).setError(message);
        }
    }

    public void tryMakeSkippedRegionToken(int offset) {
        char inputChar = getInput().charAt(offset);

        boolean isInputKeywordChar = KeywordRecognizer.isPotentialKeywordChar(inputChar);
        if(isAtPotentialKeywordEnd(offset, isInputKeywordChar)) {
            if(keywords.isKeyword(toString(startOffset, offset - 1))) {
                makeToken(offset - 1, TK_ERROR_KEYWORD, false, ERROR_SKIPPED_REGION);
            } else {
                makeToken(offset - 1, TK_ERROR, false, ERROR_SKIPPED_REGION);
            }
        }
        if(isAtPotentialKeywordStart(offset, isInputKeywordChar)) {
            if(keywords.isKeyword(toString(startOffset, offset))) {
                makeToken(offset, TK_ERROR_KEYWORD, false, ERROR_SKIPPED_REGION);
            } else {
                makeToken(offset, TK_ERROR, false, ERROR_SKIPPED_REGION);
            }
        }

        setSyntaxCorrect(false);
    }

    private boolean isAtPotentialKeywordEnd(int offset, boolean isInputKeywordChar) {
        if(offset >= 1 && offset > startOffset) {
            char prevChar = getInput().charAt(offset - 1);
            return (isInputKeywordChar && !isKeywordChar(prevChar)) || (!isInputKeywordChar && isKeywordChar(prevChar));
        }
        return false;
    }

    private boolean isAtPotentialKeywordStart(int offset, boolean isInputKeywordChar) {
        // Eduardo: check whether reaching the end of file
        if(isInputKeywordChar && offset + 1 == getInput().length())
            return true;
        if(offset + 1 < getInput().length()) {
            char nextChar = getInput().charAt(offset + 1);
            if((isInputKeywordChar && !isKeywordChar(nextChar)) || (!isInputKeywordChar && isKeywordChar(nextChar))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether the given character could possibly be part of a keyword (as opposed to an operator).
     */
    private boolean isKeywordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    @Override public String toString() {
        String input = getInput();
        StringBuilder result = new StringBuilder();
        result.append('[');
        for(IToken token : tokens) {
            int offset = token.getStartOffset();
            result.append(input, offset, token.getEndOffset() + 1);
            result.append(',');
        }
        if(tokens.size() > 0)
            result.deleteCharAt(result.length() - 1);
        result.append(']');
        return result.toString();
    }

    protected IKeywordRecognizer getKeywordRecognizer() {
        return keywords;
    }

    @Override public Iterator<IToken> iterator() {
        return new FilteredTokenIterator(allTokens());
    }

    /**
     * This iterator filters out:
     * <ul>
     * <li>Tokens coming from ambiguous regions in the AST (only the first derivation is kept)
     * <li>Empty tokens
     * </ul>
     */
    public static class FilteredTokenIterator implements Iterator<IToken> {
        final Iterator<IToken> allTokensIterator;
        IToken next = null;
        int offset = -1;

        public FilteredTokenIterator(Iterable<IToken> allTokens) {
            allTokensIterator = allTokens.iterator();
            calculateNext();
        }

        @Override public boolean hasNext() {
            return next != null;
        }

        @Override public IToken next() {
            if(!hasNext())
                throw new NoSuchElementException();

            IToken res = next;
            calculateNext();
            return res;
        }

        private void calculateNext() {
            while(allTokensIterator.hasNext()) {
                next = allTokensIterator.next();
                if(next.getKind() != TK_RESERVED && next.getKind() != TK_EOF
                    && next.getStartOffset() == next.getEndOffset() + 1)
                    continue; // Skip empty tokens (that are not the start or end token)
                if(next.getStartOffset() > offset) {
                    offset = next.getEndOffset();
                    return;
                }
            }
            next = null;
        }
    }

    @Override public Iterable<IToken> allTokens() {
        @SuppressWarnings("unchecked") // covariance
        Iterable<IToken> result = (Iterable<IToken>) (Iterable<?>) Collections.unmodifiableList(tokens);
        return result;
    }

    public void setAst(ISimpleTerm ast) {
        this.ast = ast;
    }

    public void initAstNodeBinding() {
        if(ast == null)
            return;
        int tokenIndex = getLeftToken(ast).getIndex();
        int endTokenIndex = getRightToken(ast).getIndex();
        bindAstNode(ast, tokenIndex, endTokenIndex);
    }

    private void bindAstNode(ISimpleTerm node, int tokenIndex, int endTokenIndex) {
        assert getTokenizer(node) == this;
        int tokenCount = getTokenCount();

        // Set ast node for spaces between children and recursively for children
        Iterator<ISimpleTerm> iterator = SimpleTermVisitor.tryGetListIterator(node);
        for(int i = 0, max = node.getSubtermCount(); i < max; i++) {
            ISimpleTerm child = iterator == null ? node.getSubterm(i) : iterator.next();

            int childStart = getLeftToken(child).getIndex();
            int childEnd = getRightToken(child).getIndex();

            while(tokenIndex < childStart && tokenIndex < tokenCount) {
                Token token = getTokenAt(tokenIndex++);
                token.setAstNode(node);
            }

            bindAstNode(child, childStart, childEnd);
            tokenIndex = childEnd + 1;
        }

        // Set ast node for tokens after children
        while(tokenIndex <= endTokenIndex && tokenIndex < tokenCount) {
            Token token = getTokenAt(tokenIndex++);
            token.setAstNode(node);
        }
    }

}
