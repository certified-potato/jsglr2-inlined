package org.spoofax.jsglr2.inlined;

import org.spoofax.jsglr2.parser.Position;

interface IInlinedParseNodeVisitor {

    
    void postVisit(InlinedParseNode parseNode, Position startPosition, Position endPosition);

    boolean preVisit(InlinedParseNode parseNode, Position startPosition);
}
