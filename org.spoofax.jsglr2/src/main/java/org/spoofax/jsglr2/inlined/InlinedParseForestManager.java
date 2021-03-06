package org.spoofax.jsglr2.inlined;

import static org.spoofax.jsglr2.parseforest.IParseForest.sumWidth;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.metaborg.parsetable.productions.IProduction;
import org.metaborg.parsetable.productions.ProductionType;
import org.spoofax.jsglr2.JSGLR2Request;
import org.spoofax.jsglr2.parseforest.ICharacterNode;
import org.spoofax.jsglr2.parseforest.IParseForest;
import org.spoofax.jsglr2.parser.Position;

final class InlinedParseForestManager {

//    final StatCounter observer;
    final InlinedDisambugator disambiguator = new InlinedDisambugator();

//    InlinedParseForestManager(StatCounter observer) {
//        this.observer = observer;
//    }

    InlinedParseNode createParseNode(InlinedParseState parseState, InlinedStackNode stack, IProduction production,
            InlinedDerivation firstDerivation) {
        InlinedParseNode parseNode = new InlinedParseNode(sumWidth(firstDerivation.parseForests()), production,
                firstDerivation);

        // observer.createParseNode(parseNode);

        return parseNode;
    }

    InlinedDerivation createDerivation(InlinedParseState parseState, InlinedStackNode stack, IProduction production,
            ProductionType productionType, IParseForest[] parseForests) {
        InlinedDerivation derivation = new InlinedDerivation(production, productionType, parseForests);

        return derivation;
    }

    void addDerivation(InlinedParseState parseState, InlinedParseNode parseNode, InlinedDerivation derivation) {
        parseNode.addDerivation(derivation);

        if (disambiguator != null)
            disambiguator.disambiguate(parseState, parseNode);
    }

    InlinedParseNode createSkippedNode(InlinedParseState parseState, IProduction production,
            IParseForest[] parseForests) {
        return new InlinedParseNode(sumWidth(parseForests), production);
    }

    InlinedCharacterNode createCharacterNode(InlinedParseState parseState) {
        InlinedCharacterNode characterNode = new InlinedCharacterNode(parseState.inputStack.getChar());

        // observer.createCharacterNode();

        return characterNode;
    }

    InlinedParseNode filteredTopParseNode(InlinedParseNode parseNode, List<InlinedDerivation> derivations) {
        InlinedParseNode topParseNode = new InlinedParseNode(parseNode.width(), parseNode.production(),
                derivations.get(0));

        for (int i = 1; i < derivations.size(); i++)
            topParseNode.addDerivation(derivations.get(i));

        return topParseNode;
    }

    /**
     * Filter out derivations from the parse node that don't have a specific
     * left-hand-side (non-)terminal symbol
     * 
     * @param topNode     The node to filter out of.
     * @param startSymbol The name of the non-terminal to filter for.
     * @param parseState  The current state of the parse.
     * @return A new parse node that only has derivations that have the specific
     *         symbol on the left hand side. If none exist, it returns null
     *         otherwise.
     */
    InlinedParseNode filterStartSymbol(InlinedParseNode topNode, String startSymbol, InlinedParseState parseState) {
        List<InlinedDerivation> derivationsWithStartSymbol = new ArrayList<>();

        for (InlinedDerivation derivation : topNode.getDerivations()) {
            String derivationStartSymbol = derivation.production().startSymbolSort();

            if (derivationStartSymbol != null && derivationStartSymbol.equals(startSymbol))
                derivationsWithStartSymbol.add(derivation);
        }

        if (derivationsWithStartSymbol.isEmpty())
            return null;
        else
            return filteredTopParseNode(topNode, derivationsWithStartSymbol);
    }

    void visit(JSGLR2Request request, IParseForest root, IInlinedParseNodeVisitor visitor) {
        Stack<Position> positionStack = new Stack<>(); // Start positions of parse nodes
        Stack<Object> inputStack = new Stack<>(); // Pending parse nodes and derivations
        Stack<Visit> outputStack = new Stack<>(); // Parse node and derivations with remaining children

        Position pivotPosition = Position.START_POSITION;
        positionStack.push(Position.START_POSITION);
        inputStack.push(root);
        outputStack.push(new Visit());

        while (!inputStack.isEmpty() || !outputStack.isEmpty()) {
            if (!outputStack.isEmpty() && outputStack.peek().done()) { // Finish derivation
                outputStack.pop();

                if (outputStack.isEmpty())
                    break;

                outputStack.peek().remainingChildren--;

                if (!outputStack.isEmpty() && outputStack.peek().done()) { // Visit parse node
                    InlinedParseNode parseNode = outputStack.pop().parseNode;

                    visitor.postVisit(parseNode, positionStack.pop(), pivotPosition);

                    outputStack.peek().remainingChildren--;
                }
            } else if (inputStack.peek() instanceof InlinedDerivation) { // Consume derivation
                InlinedDerivation derivation = (InlinedDerivation) inputStack.pop();

                outputStack.push(new Visit(derivation));

                IParseForest[] children = derivation.parseForests();

                for (int i = children.length - 1; i >= 0; i--)
                    inputStack.push(children[i]);

                pivotPosition = positionStack.peek();
            } else if (inputStack.peek() instanceof ICharacterNode) { // Consume character node
                pivotPosition = pivotPosition.step(request.input, ((ICharacterNode) inputStack.pop()).width());

                outputStack.peek().remainingChildren--;
            } else if (inputStack.peek() instanceof InlinedParseNode) { // Consume (skipped) parse node
                InlinedParseNode parseNode = (InlinedParseNode) inputStack.pop();
                positionStack.push(pivotPosition);

                boolean visitChildren = visitor.preVisit(parseNode, pivotPosition);

                if (visitChildren && parseNode.hasDerivations()) { // Parse node with derivation(s)
                    int derivations = 0;

                    for (InlinedDerivation derivation : parseNode.getDerivations()) {
                        inputStack.push(derivation);
                        derivations++;

// You only ignore other derivations during testing
//                        if (derivations >= 1 && !visitor.visitAmbiguousDerivations())
//                            break;
                    }

                    outputStack.push(new Visit(derivations, parseNode));
                } else { // Skipped parse node (without derivations)
                    pivotPosition = pivotPosition.step(request.input, parseNode.width());

                    visitor.postVisit(parseNode, positionStack.pop(), pivotPosition);

                    outputStack.peek().remainingChildren--;
                }
            }
        }
    }

    class Visit {
        int remainingChildren;
        InlinedParseNode parseNode;

        Visit() {
            this.remainingChildren = 1;
            this.parseNode = null;
        }

        Visit(InlinedDerivation derivation) {
            this.remainingChildren = derivation.parseForests().length;
            this.parseNode = null;
        }

        Visit(int remainingChildren, InlinedParseNode parseNode) {
            this.remainingChildren = remainingChildren;
            this.parseNode = parseNode;
        }

        boolean done() {
            return remainingChildren == 0;
        }
    }

}
