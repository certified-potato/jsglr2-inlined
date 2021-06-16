package org.spoofax.jsglr2.inlined;

import static org.metaborg.parsetable.characterclasses.ICharacterClass.EOF_INT;
import static org.metaborg.parsetable.characterclasses.ICharacterClass.MAX_CHAR;

import org.metaborg.parsetable.query.IActionQuery;
import org.spoofax.jsglr2.parser.Position;

/**
 * Keeps track which character the parser is currently parsing, along the input text
 */
final class InlinedInputStack implements IActionQuery {
    private final String inputString;
    private final int inputLength;
    private int currentOffset = 0;
    int currentChar; // Current ASCII char in range [0, MAX_CHAR] or EOF_INT

    InlinedInputStack(String inputString) {
        this.inputString = inputString;
        this.inputLength = inputString.length();
        currentChar = getChar(currentOffset);
    }

    public InlinedInputStack clone() {
        InlinedInputStack clone = new InlinedInputStack(inputString);
        clone.currentChar = currentChar;
        clone.currentOffset = currentOffset;
        return clone;
    }

    boolean hasNext() {
        return currentOffset <= inputLength;
    }

    void next() {
        currentOffset += Character.charCount(currentChar);
        currentChar = getChar(currentOffset);
    }

    int getChar() {
        return currentChar;
    }
    
    String inputString() {
        return inputString;
    }

    int offset() {
        return currentOffset;
    }

    int length() {
        return inputLength;
    }

    @Override
    public int actionQueryCharacter() {
        if(currentOffset < inputLength)
            return inputString.codePointAt(currentOffset);
        if(currentOffset == inputLength)
            return EOF_INT;
        else
            return -1;
    }

    @Override
    public int[] actionQueryLookahead(int length) {
        int[] res = new int[length];
        int nextOffset = currentOffset + Character.charCount(getChar(currentOffset));
        for(int i = 0; i < length; i++) {
            if(nextOffset >= inputLength) {
                int[] resShort = new int[i];
                System.arraycopy(res, 0, resShort, 0, i);
                return resShort;
            }
            res[i] = inputString.codePointAt(nextOffset);
            nextOffset += Character.charCount(res[i]);
        }
        return res;
    }

    int getChar(int offset) {
        if(offset < inputLength) {
            int c = inputString.codePointAt(offset);

            if(c > MAX_CHAR)
                throw new IllegalStateException("Character " + c + " not supported");

            return c;
        } else
            return EOF_INT;
    }
    
    Position safePosition() {
        return Position.atOffset(inputString(), Math.max(Math.min(offset(), length() - 1), 0));
    }

    Integer safeCharacter() {
        return offset() <= inputString().length() - 1 ? inputString().codePointAt(offset()) : null;
    }
}
