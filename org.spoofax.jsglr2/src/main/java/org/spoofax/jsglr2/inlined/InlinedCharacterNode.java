package org.spoofax.jsglr2.inlined;

import org.metaborg.parsetable.characterclasses.CharacterClassFactory;
import org.spoofax.jsglr2.parseforest.IParseForest;

/**
 * Represents a terminal in a CFG. It is stored in stack links that were added
 * by shifting in new characters from the input.
 */
final class InlinedCharacterNode implements IParseForest {
    
    /**
     * the unicode code point for the charater to store, or -1 for EOF.
     */
    final int character;

    InlinedCharacterNode(int character) {
        this.character = character;
    }

    @Override
    public int width() {
        return Character.charCount(character);
    }

    @Override
    public String descriptor() {
        return "'" + CharacterClassFactory.intToString(character) + "'";
    }
}
