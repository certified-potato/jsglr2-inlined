package org.spoofax.jsglr2.inlined;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.metaborg.parsetable.productions.IProduction;
import org.metaborg.parsetable.symbols.IMetaVarSymbol;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr2.JSGLR2Request;
import org.spoofax.jsglr2.imploder.ImplodeResult;
import org.spoofax.jsglr2.imploder.treefactory.TokenizedTermTreeFactory;
import org.spoofax.jsglr2.parseforest.IParseForest;
import org.spoofax.jsglr2.parseforest.IParseNode;
import org.spoofax.jsglr2.parser.Position;
import org.spoofax.jsglr2.tokens.Tokens;

public class InlinedImploder {
    final TokenizedTermTreeFactory treeFactory = new TokenizedTermTreeFactory();

    public ImplodeResult<Tokens, Void, IStrategoTerm> implode(JSGLR2Request request, IParseForest parseForest) {
        Tokens tokens = new Tokens(request.input, request.fileName);
        tokens.makeStartToken();

        Position position = Position.START_POSITION;

        SubTree tree = implodeParseNode(parseForest, tokens, position);

        tokens.makeEndToken(tree.endPosition);

        tokenTreeBinding(tokens.startToken(), tree.tree);
        tokenTreeBinding(tokens.endToken(), tree.tree);

        return new ImplodeResult<>(tokens, null, tree.tree, tree.containsAmbiguity);
    }

    static class SubTree {

        IStrategoTerm tree;
        Position endPosition;
        IToken leftToken, rightToken;
        boolean containsAmbiguity;

        SubTree(IStrategoTerm tree, Position endPosition, IToken leftToken, IToken rightToken,
                boolean containsAmbiguity) {
            this.tree = tree;
            this.endPosition = endPosition;
            this.leftToken = leftToken;
            this.rightToken = rightToken;
            this.containsAmbiguity = containsAmbiguity;
        }

    }

    SubTree implodeParseNode(IParseForest parseForest, Tokens tokens, Position startPosition) {

        if (parseForest instanceof InlinedCharacterNode) {
            int width = parseForest.width();
            Position endPosition = startPosition.step(tokens.getInput(), width);
            IToken token = tokens.makeToken(startPosition, endPosition, null);
            IStrategoTerm tree = createCharacterTerm(((InlinedCharacterNode) parseForest).character, token);
            return new SubTree(tree, endPosition, token, token, false);
        }

        InlinedParseNode parseNode = implodeInjection((InlinedParseNode) parseForest);

        IProduction production = parseNode.production();

        if (production.isContextFree() && !production.isSkippableInParseForest()) {
            List<InlinedDerivation> filteredDerivations = applyDisambiguationFilters(parseNode);

            if (filteredDerivations.size() > 1) {
                List<IStrategoTerm> trees = new ArrayList<>(filteredDerivations.size());
                SubTree result = null;

                if (production.isList()) {
                    for (List<IParseForest> derivationParseForests : implodeAmbiguousLists(filteredDerivations)) {
                        if (result == null) {
                            result = implodeListDerivation(tokens, production, derivationParseForests, startPosition);

                            trees.add(result.tree);
                        } else
                            trees.add(implodeListDerivation(tokens, production, derivationParseForests,
                                    startPosition).tree);
                    }
                } else {
                    for (InlinedDerivation derivation : filteredDerivations) {
                        if (result == null) {
                            result = implodeDerivation(tokens, derivation, startPosition);

                            trees.add(result.tree);
                        } else
                            trees.add(implodeDerivation(tokens, derivation, startPosition).tree);
                    }
                }

                result.tree = treeFactory.createAmb(trees, result.leftToken, result.rightToken);
                result.containsAmbiguity = true;

                return result;
            } else
                return implodeDerivation(tokens, filteredDerivations.get(0), startPosition);
        } else {
            int width = parseNode.width();

            Position endPosition = startPosition.step(tokens.getInput(), width);

            IToken token = width > 0 || production.isLexical()
                    ? tokens.makeToken(startPosition, endPosition, production)
                    : null;

            IStrategoTerm tree;

            if (production.isLayout() || production.isLiteral()) {
                tree = null;
            } else if (production.isLexical()) {
                tree = createLexicalTerm(production, tokens.toString(startPosition.offset, endPosition.offset), token);
            } else {
                throw new RuntimeException("invalid term type");
            }

            return new SubTree(tree, endPosition, token, token, false);
        }
    }

    SubTree implodeDerivation(Tokens tokens, InlinedDerivation derivation, Position startPosition) {
        IProduction production = derivation.production();

        if (!production.isContextFree())
            throw new RuntimeException("non context free imploding not supported");

        List<IStrategoTerm> childASTs = new ArrayList<>();
        List<IToken> unboundTokens = new ArrayList<>();

        SubTree subTree = implodeChildParseNodes(tokens, childASTs, Arrays.asList(derivation.parseForests()),
                derivation.production(), unboundTokens, startPosition);

        subTree.tree = createContextFreeTerm(derivation.production(), childASTs, subTree.leftToken, subTree.rightToken);

        for (IToken token : unboundTokens)
            tokenTreeBinding(token, subTree.tree);

        return subTree;
    }

    SubTree implodeListDerivation(Tokens tokens, IProduction production, List<IParseForest> childParseForests,
            Position startPosition) {
        List<IStrategoTerm> childASTs = new ArrayList<>();
        List<IToken> unboundTokens = new ArrayList<>();

        SubTree subTree = implodeChildParseNodes(tokens, childASTs, childParseForests, production, unboundTokens,
                startPosition);

        subTree.tree = createContextFreeTerm(production, childASTs, subTree.leftToken, subTree.rightToken);

        for (IToken token : unboundTokens)
            tokenTreeBinding(token, subTree.tree);

        return subTree;
    }

    SubTree implodeChildParseNodes(Tokens tokens, List<IStrategoTerm> childASTs,
            Iterable<IParseForest> childParseForests, IProduction production, List<IToken> unboundTokens,
            Position startPosition) {
        SubTree result = new SubTree(null, startPosition, null, null, false);

        Position pivotPosition = startPosition;

        for (IParseForest childParseForest : childParseForests) {
            InlinedParseNode childParseNode = childParseForest instanceof InlinedParseNode
                    ? (InlinedParseNode) childParseForest
                    : null;
            IProduction childProduction = childParseNode != null ? childParseNode.production() : null;

            SubTree subTree;

            if (production.isList() && childProduction != null && (
            // @formatter:off
            // Constraints for flattening nested lists productions:
            childProduction.isList() && // The subtree is a list
                    childProduction.constructor() == null && // The subtree has no constructor
                    childParseNode.getPreferredAvoidedDerivations().size() <= 1 && // The subtree is not ambiguous
                    !production.isLexical() // Not in lexical context; otherwise just implode as lexical token
            // @formatter:on
            )) {
                // Make sure lists are flattened
                subTree = implodeChildParseNodes(tokens, childASTs,
                        Arrays.asList(childParseNode.getFirstDerivation().parseForests()), childProduction,
                        unboundTokens, pivotPosition);
            } else {
                subTree = implodeParseNode(childParseForest, tokens, pivotPosition);

                if (subTree.tree != null)
                    childASTs.add(subTree.tree);

                // Collect tokens that are not bound to a tree such that they can later be bound
                // to the resulting
                // parent tree
                if (subTree.tree == null) {
                    if (subTree.leftToken != null)
                        unboundTokens.add(subTree.leftToken);

                    // Make sure that if subTree.leftToken == subTree.rightToken it is not
                    // considered twice
                    if (subTree.rightToken != null && subTree.rightToken != subTree.leftToken)
                        unboundTokens.add(subTree.rightToken);
                }
            }

            // Set the parent tree left and right token from the outermost non-layout left
            // and right child tokens
            if (childProduction != null && !childProduction.isLayout()
                    // Also do this for character nodes
                    || childParseNode == null) {
                if (result.leftToken == null)
                    result.leftToken = subTree.leftToken;

                if (subTree.rightToken != null) {
                    result.rightToken = subTree.rightToken;
                }
            }

            pivotPosition = subTree.endPosition;
            result.containsAmbiguity |= subTree.containsAmbiguity;
        }

        // If is no token, this means that this AST has no characters in the input.
        // In this case, create an empty token to associate with this AST node.
        if (result.leftToken == null) {
            assert result.rightToken == null;
            result.leftToken = result.rightToken = tokens.makeToken(startPosition, pivotPosition, production);
            unboundTokens.add(result.leftToken);
        }

        result.endPosition = pivotPosition;

        return result;
    }

    IStrategoTerm createContextFreeTerm(IProduction production, List<IStrategoTerm> childASTs, IToken leftToken,
            IToken rightToken) {
        String constructor = production.constructor();

        if (constructor != null)
            return treeFactory.createNonTerminal(production.lhs(), constructor, childASTs, leftToken, rightToken);
        else if (production.isOptional())
            return treeFactory.createOptional(production.lhs(), childASTs, leftToken, rightToken);
        else if (production.isList())
            return treeFactory.createList(childASTs, leftToken, rightToken);
        else if (childASTs.size() == 1)
            return treeFactory.createInjection(production.lhs(), childASTs.get(0), production.isBracket());
        else
            return treeFactory.createTuple(childASTs, leftToken, rightToken);
    }

    IStrategoTerm createLexicalTerm(IProduction production, String lexicalString, IToken lexicalToken) {
        IStrategoTerm lexicalTerm;

        if (production.lhs() instanceof IMetaVarSymbol)
            lexicalTerm = treeFactory.createMetaVar((IMetaVarSymbol) production.lhs(), lexicalString, lexicalToken);
        else
            lexicalTerm = treeFactory.createStringTerminal(production.lhs(), lexicalString, lexicalToken);

        if (lexicalToken != null) // Can be null, e.g. for empty string lexicals
            tokenTreeBinding(lexicalToken, lexicalTerm);

        return lexicalTerm;
    }

    IStrategoTerm createCharacterTerm(int character, IToken lexicalToken) {
        IStrategoTerm term = treeFactory.createCharacterTerminal(character, lexicalToken);

        tokenTreeBinding(lexicalToken, term);

        return term;
    }

    void tokenTreeBinding(IToken token, IStrategoTerm term) {
        token.setAstNode(term);
    }

    protected List<List<IParseForest>> implodeAmbiguousLists(List<InlinedDerivation> derivations) {
        List<List<IParseForest>> alternatives = new ArrayList<>();

        for (InlinedDerivation derivation : derivations) {
            IParseForest[] children = derivation.parseForests();
            if (children.length == 0) {
                alternatives.add(Collections.emptyList());
            } else if (children.length == 1) {
                alternatives.add(Collections.singletonList(children[0]));
            } else {
                List<IParseForest> subTrees = Arrays.asList(children);

                InlinedParseNode head = (InlinedParseNode) children[0];

                if (head.production().isList() && head.getPreferredAvoidedDerivations().size() > 1) {
                    List<IParseForest> tail = subTrees.subList(1, subTrees.size());

                    List<List<IParseForest>> headExpansions = implodeAmbiguousLists(
                            head.getPreferredAvoidedDerivations());

                    for (List<IParseForest> headExpansion : headExpansions) {
                        List<IParseForest> headExpansionWithTail = new ArrayList<>(headExpansion);
                        headExpansionWithTail.addAll(tail);
                        alternatives.add(headExpansionWithTail);
                    }
                } else {
                    alternatives.add(subTrees);
                }
            }
        }

        return alternatives;
    }

    InlinedParseNode implodeInjection(InlinedParseNode parseNode) {
        for (InlinedDerivation derivation : parseNode.getDerivations()) {
            if (derivation.parseForests().length == 1 && (derivation.parseForests()[0] instanceof IParseNode)) {
                InlinedParseNode injectedParseNode = (InlinedParseNode) derivation.parseForests()[0];

                // Meta variables are injected:
                // https://github.com/metaborg/strategoxt/blob/master/strategoxt/stratego-libraries/sglr/lib/stratego/asfix/implode/injection.str#L68-L69
                if (injectedParseNode.production().lhs() instanceof IMetaVarSymbol) {
                    return injectedParseNode;
                }
            }
        }

        return parseNode;
    }

    List<InlinedDerivation> applyDisambiguationFilters(InlinedParseNode parseNode) {
        if (!parseNode.isAmbiguous())
            return Collections.singletonList(parseNode.getFirstDerivation());

        return parseNode.getPreferredAvoidedDerivations();
    }

}
