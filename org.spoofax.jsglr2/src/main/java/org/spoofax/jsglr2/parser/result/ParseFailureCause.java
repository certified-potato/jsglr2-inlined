package org.spoofax.jsglr2.parser.result;

import org.spoofax.jsglr2.messages.Category;
import org.spoofax.jsglr2.messages.Message;
import org.spoofax.jsglr2.parser.Position;

public class ParseFailureCause {

    public enum Type {

        UnexpectedEOF("Unexpected end of input", Category.PARSING),
        UnexpectedInput("Unexpected input", Category.PARSING),
        InvalidStartSymbol("Invalid start symbol", Category.PARSING), Cycle("Cycle in parse forest", Category.CYCLE),
        NonAssoc("Operator is non-associative", Category.NONASSOC),
        NonNested("Operator is non-nested", Category.NONASSOC);

        public final String message;
        public final Category category;

        Type(String message, Category category) {
            this.message = message;
            this.category = category;
        }

    }

    public final Type type;
    public final Position position;

    public ParseFailureCause(Type type, Position position) {
        this.type = type;
        this.position = position;
    }

    public ParseFailureCause(Type type) {
        this(type, null);
    }

    public Message toMessage() {
        return Message.error(type.message, type.category, position);
    }

}
