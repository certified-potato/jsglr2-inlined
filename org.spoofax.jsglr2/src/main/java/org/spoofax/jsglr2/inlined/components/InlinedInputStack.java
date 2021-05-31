package org.spoofax.jsglr2.inlined.components;

import static org.metaborg.parsetable.characterclasses.ICharacterClass.EOF_INT;
import static org.metaborg.parsetable.characterclasses.ICharacterClass.MAX_CHAR;

import org.spoofax.jsglr2.parser.Position;

public class InlinedInputStack {
    protected final String inputString;
    protected final int inputLength;
    protected int currentOffset = 0;
    int currentChar; // Current ASCII char in range [0, MAX_CHAR] or EOF_INT

    public InlinedInputStack(String inputString) {
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

    public boolean hasNext() {
        return currentOffset <= inputLength;
    }

    public void next() {
        currentOffset += Character.charCount(currentChar);
        currentChar = getChar(currentOffset);
    }

    public int getChar() {
        return currentChar;
    }
    
    public String inputString() {
        return inputString;
    }

    public int offset() {
        return currentOffset;
    }

    public int length() {
        return inputLength;
    }

    public int actionQueryCharacter() {
        if(currentOffset < inputLength)
            return inputString.codePointAt(currentOffset);
        if(currentOffset == inputLength)
            return EOF_INT;
        else
            return -1;
    }

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

    public int getChar(int offset) {
        if(offset < inputLength) {
            int c = inputString.codePointAt(offset);

            if(c > MAX_CHAR)
                throw new IllegalStateException("Character " + c + " not supported");

            return c;
        } else
            return EOF_INT;
    }
    
    public Position safePosition() {
        return Position.atOffset(inputString(), Math.max(Math.min(offset(), length() - 1), 0));
    }

    public Integer safeCharacter() {
        return offset() <= inputString().length() - 1 ? inputString().codePointAt(offset()) : null;
    }
}
