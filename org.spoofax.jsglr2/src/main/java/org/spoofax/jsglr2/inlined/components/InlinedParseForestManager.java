package org.spoofax.jsglr2.inlined.components;

import java.util.ArrayList;
import java.util.List;

import org.metaborg.parsetable.productions.IProduction;
import org.metaborg.parsetable.productions.ProductionType;
import org.spoofax.jsglr2.inlined.InlinedObserver;
import org.spoofax.jsglr2.parseforest.hybrid.HybridCharacterNode;
import org.spoofax.jsglr2.parseforest.hybrid.HybridParseForest;
import org.spoofax.jsglr2.stack.IStackNode;

public class InlinedParseForestManager {

    protected final InlinedObserver observing;
    protected final InlinedDisambugator disambiguator;

    public InlinedParseForestManager(
            InlinedObserver observing,
            InlinedDisambugator disambiguator) {
        
        this.observing = observing;
        this.disambiguator = disambiguator;

    }

    public InlinedParseNode createParseNode(InlinedParseState parseState, IStackNode stack, IProduction production,
            InlinedDerivation firstDerivation) {
        InlinedParseNode parseNode = new InlinedParseNode(InlinedParseForest.sumWidth(firstDerivation.parseForests()), production,
                firstDerivation);

        //observing.notify(observer -> observer.createParseNode(parseNode.getFake(), production));
        //observing.notify(observer -> observer.addDerivation(parseNode, firstDerivation));

        return parseNode;
    }

    public InlinedDerivation createDerivation(InlinedParseState parseState, IStackNode stack, IProduction production,
            ProductionType productionType, InlinedParseForest[] parseForests) {
        InlinedDerivation derivation = new InlinedDerivation(production, productionType, parseForests);

        //observing.notify(observer -> observer.createDerivation(derivation, production, parseForests));

        return derivation;
    }

    public void addDerivation(InlinedParseState parseState, InlinedParseNode parseNode, InlinedDerivation derivation) {
        //observing.notify(observer -> observer.addDerivation(parseNode, derivation));

        parseNode.addDerivation(derivation);

        if (disambiguator != null)
            disambiguator.disambiguate(parseState, parseNode);
    }

    public InlinedParseNode createSkippedNode(InlinedParseState parseState, IProduction production,
            InlinedParseForest[] parseForests) {
        return new InlinedParseNode(InlinedParseForest.sumWidth(parseForests), production);
    }

    public HybridCharacterNode createCharacterNode(InlinedParseState parseState) {
        HybridCharacterNode characterNode = new HybridCharacterNode(parseState.inputStack.getChar());

        //observing.notify(observer -> observer.createCharacterNode(characterNode, characterNode.character));

        return characterNode;
    }

    public HybridParseForest[] parseForestsArray(int length) {
        return new HybridParseForest[length];
    }

    protected InlinedParseNode filteredTopParseNode(InlinedParseNode parseNode, List<InlinedDerivation> derivations) {
        InlinedParseNode topParseNode = new InlinedParseNode(parseNode.width(), parseNode.production(),
                derivations.get(0));

        for (int i = 1; i < derivations.size(); i++)
            topParseNode.addDerivation(derivations.get(i));

        return topParseNode;
    }

    public InlinedParseNode filterStartSymbol(InlinedParseNode topNode, String startSymbol,
            InlinedParseState parseState) {
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
}
