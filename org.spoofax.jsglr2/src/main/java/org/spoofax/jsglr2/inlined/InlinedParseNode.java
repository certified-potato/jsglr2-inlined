package org.spoofax.jsglr2.inlined;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.metaborg.parsetable.productions.IProduction;
import org.spoofax.jsglr2.parseforest.IParseForest;

class InlinedParseNode implements IParseForest {
    private final int width;
    private final IProduction production;
    private ArrayList<InlinedDerivation> derivations;

    InlinedParseNode(int width, IProduction production, InlinedDerivation firstDerivation) {
        this.width = width;
        this.production = production;
        this.derivations = new ArrayList<>();
        derivations.add(firstDerivation);
    }

    InlinedParseNode(int width, IProduction production) {
        this(width, production, null);
    }

    @Override
    public int width() {
        return width;
    }

    IProduction production() {
        return production;
    }

    void addDerivation(InlinedDerivation derivation) {
        derivations.add(derivation);
    }

    boolean hasDerivations() {
        return !derivations.isEmpty();
    }

    ArrayList<InlinedDerivation> getDerivations() {
        return derivations;
    }

    InlinedDerivation getFirstDerivation() {
        try {
            return derivations.get(0);
        } catch (IndexOutOfBoundsException e) {
            throw new UnsupportedOperationException("Cannot get derivation of skipped parse node");
        }
    }

    boolean isAmbiguous() {
        return derivations.size() > 0;
    }

    void disambiguate(InlinedDerivation derivation) {
        derivations.clear();
        derivations.add(derivation);
    }

    List<InlinedDerivation> getPreferredAvoidedDerivations() {
        if (!isAmbiguous())
            return Collections.singletonList(getFirstDerivation());
        else {
            ArrayList<InlinedDerivation> preferred = null, avoided = null, other = null;

            for (InlinedDerivation derivation : getDerivations()) {
                switch (derivation.productionType()) {
                case PREFER:
                    if (preferred == null)
                        preferred = new ArrayList<>();

                    preferred.add(derivation);
                    break;
                case AVOID:
                    if (avoided == null)
                        avoided = new ArrayList<>();

                    avoided.add(derivation);
                    break;
                default:
                    if (other == null)
                        other = new ArrayList<>();

                    other.add(derivation);
                }
            }

            if (preferred != null && !preferred.isEmpty())
                return preferred;
            else if (other != null && !other.isEmpty())
                return other;
            else
                return avoided;
        }
    }

    @Override
    public String descriptor() {
        return production().lhs().toString();
    }

}
