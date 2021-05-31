package org.spoofax.jsglr2.inlined.components.observables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.metaborg.parsetable.productions.IProduction;
import org.spoofax.jsglr2.inlined.observables.FakeParseNode;

public class InlinedParseNode {
    private final int width;
    private final IProduction production;
    private ArrayList<InlinedDerivation> derivations;
    
    private FakeParseNode fake = null;

    public InlinedParseNode(int width, IProduction production, InlinedDerivation firstDerivation) {
        this.width = width;
        this.production = production;
        this.derivations = new ArrayList<>();
        derivations.add(firstDerivation);
    }

    public InlinedParseNode(int width, IProduction production) {
        this(width, production, null);
    }

    public int width() {
        return width;
    }

    public IProduction production() {
        return production;
    }

    public void addDerivation(InlinedDerivation derivation) {
        derivations.add(derivation);
    }

    public boolean hasDerivations() {
        return !derivations.isEmpty();
    }

    public ArrayList<InlinedDerivation> getDerivations() {
        return derivations;
    }

    public InlinedDerivation getFirstDerivation() {            
        try {
        return derivations.get(0);
        } catch (IndexOutOfBoundsException e) {
            throw new UnsupportedOperationException("Cannot get derivation of skipped parse node");
        }
    }

    public boolean isAmbiguous() {
        return derivations.size() > 0;
    }

    public void disambiguate(InlinedDerivation derivation) {
        derivations.clear();
        derivations.add(derivation);
    }
    
    public List<InlinedDerivation> getPreferredAvoidedDerivations() {
        if(!isAmbiguous())
            return Collections.singletonList(getFirstDerivation());
        else {
            ArrayList<InlinedDerivation> preferred = null, avoided = null, other = null;

            for(InlinedDerivation derivation : getDerivations()) {
                switch(derivation.productionType()) {
                    case PREFER:
                        if(preferred == null)
                            preferred = new ArrayList<>();

                        preferred.add(derivation);
                        break;
                    case AVOID:
                        if(avoided == null)
                            avoided = new ArrayList<>();

                        avoided.add(derivation);
                        break;
                    default:
                        if(other == null)
                            other = new ArrayList<>();

                        other.add(derivation);
                }
            }

            if(preferred != null && !preferred.isEmpty())
                return preferred;
            else if(other != null && !other.isEmpty())
                return other;
            else
                return avoided;
        }
    }
    
    public String descriptor() {
        return production().lhs().toString();
    }
    
    public FakeParseNode getFake() {
        if (fake == null) {
            fake = new FakeParseNode(this);
        }
        return fake;
    }
}
