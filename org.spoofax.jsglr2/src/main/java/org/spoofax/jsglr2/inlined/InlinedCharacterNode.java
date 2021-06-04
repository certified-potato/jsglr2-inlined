package org.spoofax.jsglr2.inlined;

import org.metaborg.parsetable.characterclasses.CharacterClassFactory;
import org.spoofax.jsglr2.parseforest.IParseForest;

class InlinedCharacterNode implements IParseForest {
    
    final int character;

    public InlinedCharacterNode(int character) {
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
