package org.spoofax.jsglr2.inlined;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.metaborg.parsetable.productions.IProduction;
import org.spoofax.jsglr2.parseforest.IParseForest;
import org.spoofax.jsglr2.util.iterators.SingleElementWithListIterable;

public class InlinedParseNode implements IParseForest {
    private final int width;
    private final IProduction production;
    private InlinedDerivation firstDerivation;
    private ArrayList<InlinedDerivation> otherDerivations;

    InlinedParseNode(int width, IProduction production, InlinedDerivation firstDerivation) {
        this.width = width;
        this.production = production;
        this.otherDerivations = null;
        this.firstDerivation = firstDerivation;
    }

    InlinedParseNode(int width, IProduction production) {
        this(width, production, null);
    }

    @Override
    public int width() {
        return width;
    }

    public IProduction production() {
        return production;
    }

    void addDerivation(InlinedDerivation derivation) {
        if(otherDerivations == null)
            otherDerivations = new ArrayList<>();

        otherDerivations.add(derivation);
    }

    boolean hasDerivations() {
        return firstDerivation != null;
    }

    public Iterable<InlinedDerivation> getDerivations() {
        if(firstDerivation == null)
            return Collections.emptyList();
        if(otherDerivations == null)
            return Collections.singleton(firstDerivation);

        return SingleElementWithListIterable.of(firstDerivation, otherDerivations);
    }

    InlinedDerivation getFirstDerivation() {
        if(firstDerivation == null)
            throw new UnsupportedOperationException("Cannot get derivation of skipped parse node");

        return firstDerivation;
    }

    public boolean isAmbiguous() {
        return otherDerivations != null;
    }

    void disambiguate(InlinedDerivation derivation) {
        firstDerivation = derivation;
        otherDerivations = null;
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
