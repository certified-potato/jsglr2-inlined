package org.spoofax.jsglr2.inlined.components;

import static org.spoofax.jsglr2.parseforest.IParseForest.sumWidth;

import java.util.ArrayList;
import java.util.List;

import org.metaborg.parsetable.productions.IProduction;
import org.metaborg.parsetable.productions.ProductionType;
import org.spoofax.jsglr2.inlined.observables.InlinedFakeDerivation;
import org.spoofax.jsglr2.inlined.observables.InlinedFakeParseForest;
import org.spoofax.jsglr2.inlined.observables.InlinedFakeParseNode;
import org.spoofax.jsglr2.inlined.observables.InlinedFakeParseState;
import org.spoofax.jsglr2.inlined.observables.InlinedFakeStackNode;
import org.spoofax.jsglr2.parseforest.Disambiguator;
import org.spoofax.jsglr2.parseforest.ParseForestManagerFactory;
import org.spoofax.jsglr2.parseforest.hybrid.HybridCharacterNode;
import org.spoofax.jsglr2.parseforest.hybrid.HybridDerivation;
import org.spoofax.jsglr2.parseforest.hybrid.HybridParseForest;
import org.spoofax.jsglr2.parseforest.hybrid.HybridParseForestManager;
import org.spoofax.jsglr2.parseforest.hybrid.HybridParseNode;
import org.spoofax.jsglr2.parser.AbstractParseState;
import org.spoofax.jsglr2.parser.observing.ParserObserving;
import org.spoofax.jsglr2.recovery.RecoveryDisambiguator;
import org.spoofax.jsglr2.stack.IStackNode;

public class InlinedParseForestManager {
    
    protected final ParserObserving<InlinedFakeParseForest, InlinedFakeDerivation, InlinedFakeParseNode, InlinedFakeStackNode, InlinedFakeParseState> observing;
    protected final RecoveryDisambiguator<InlinedFakeParseForest, InlinedFakeDerivation, InlinedFakeParseNode, InlinedFakeStackNode, InlinedFakeParseState> disambiguator;
    
    public InlinedParseForestManager(
            ParserObserving<HybridParseForest, HybridDerivation, HybridParseNode, InlinedFakeStackNode, InlinedFakeParseState> observing,
            RecoveryDisambiguator<HybridParseForest, HybridDerivation, HybridParseNode, InlinedFakeStackNode, InlinedFakeParseState> disambiguator) {
        
        }

        public static
    //@formatter:off
       <StackNode_   extends IStackNode,
        ParseState_  extends AbstractParseState<?, StackNode_>>
    //@formatter:on
        ParseForestManagerFactory<HybridParseForest, HybridDerivation, HybridParseNode, StackNode_, ParseState_> factory() {
            return HybridParseForestManager::new;
        }
        
        public HybridParseNode createParseNode(InlinedFakeParseState parseState, IStackNode stack, IProduction production,
            HybridDerivation firstDerivation) {
            HybridParseNode parseNode =
                new HybridParseNode(sumWidth(firstDerivation.parseForests()), production, firstDerivation);

            observing.notify(observer -> observer.createParseNode(parseNode, production));
            observing.notify(observer -> observer.addDerivation(parseNode, firstDerivation));

            return parseNode;
        }

        public HybridDerivation createDerivation(InlinedFakeParseState parseState, IStackNode stack, IProduction production,
            ProductionType productionType, HybridParseForest[] parseForests) {
            HybridDerivation derivation = new HybridDerivation(production, productionType, parseForests);

            observing.notify(observer -> observer.createDerivation(derivation, production, parseForests));

            return derivation;
        }

        public void addDerivation(InlinedFakeParseState parseState, HybridParseNode parseNode, HybridDerivation derivation) {
            observing.notify(observer -> observer.addDerivation(parseNode, derivation));

            parseNode.addDerivation(derivation);

            if(disambiguator != null)
                disambiguator.disambiguate(parseState, parseNode);
        }

        public HybridParseNode createSkippedNode(InlinedFakeParseState parseState, IProduction production,
            HybridParseForest[] parseForests) {
            return new HybridParseNode(sumWidth(parseForests), production);
        }

        public HybridCharacterNode createCharacterNode(InlinedFakeParseState parseState) {
            HybridCharacterNode characterNode = new HybridCharacterNode(parseState.inputStack.getChar());

            observing.notify(observer -> observer.createCharacterNode(characterNode, characterNode.character));

            return characterNode;
        }

        public HybridParseForest[] parseForestsArray(int length) {
            return new HybridParseForest[length];
        }

        protected HybridParseNode filteredTopParseNode(HybridParseNode parseNode,
            List<HybridDerivation> derivations) {
            HybridParseNode topParseNode =
                new HybridParseNode(parseNode.width(), parseNode.production(), derivations.get(0));

            for(int i = 1; i < derivations.size(); i++)
                topParseNode.addDerivation(derivations.get(i));

            return topParseNode;
        }
        
        public InlinedFakeParseForest filterStartSymbol(InlinedFakeParseState parseForest, String startSymbol, InlinedFakeParseState parseState) {
            InlinedFakeParseNode topNode = (ParseNode) parseForest;
            List<Derivation> derivationsWithStartSymbol = new ArrayList<>();

            for(Derivation derivation : topNode.getDerivations()) {
                String derivationStartSymbol = derivation.production().startSymbolSort();

                if(derivationStartSymbol != null && derivationStartSymbol.equals(startSymbol))
                    derivationsWithStartSymbol.add(derivation);
            }

            if(derivationsWithStartSymbol.isEmpty())
                return null;
            else
                return (ParseForest) filteredTopParseNode(topNode, derivationsWithStartSymbol);
        }
}
