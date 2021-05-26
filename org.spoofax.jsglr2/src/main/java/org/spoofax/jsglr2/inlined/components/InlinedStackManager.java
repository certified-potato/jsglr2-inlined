package org.spoofax.jsglr2.inlined.components;

import java.util.ArrayList;
import java.util.List;

import org.metaborg.parsetable.states.IState;
import org.spoofax.jsglr2.inlined.observables.InlinedFakeDerivation;
import org.spoofax.jsglr2.inlined.observables.InlinedFakeParseForest;
import org.spoofax.jsglr2.inlined.observables.InlinedFakeParseNode;
import org.spoofax.jsglr2.inlined.observables.InlinedFakeParseState;
import org.spoofax.jsglr2.inlined.observables.InlinedFakeStackNode;
import org.spoofax.jsglr2.parseforest.ParseForestManager;
import org.spoofax.jsglr2.parser.observing.ParserObserving;
import org.spoofax.jsglr2.stack.StackLink;
import org.spoofax.jsglr2.stack.paths.EmptyStackPath;
import org.spoofax.jsglr2.stack.paths.NonEmptyStackPath;
import org.spoofax.jsglr2.stack.paths.StackPath;

public class InlinedStackManager {
    
    protected final ParserObserving<InlinedFakeParseForest, InlinedFakeDerivation, InlinedFakeParseNode, InlinedFakeStackNode, InlinedFakeParseState> observing;

    protected InlinedStackManager(
        ParserObserving<InlinedFakeParseForest, InlinedFakeDerivation, InlinedFakeParseNode, InlinedFakeStackNode, InlinedFakeParseState> observing) {
        this.observing = observing;
    }

    public InlinedFakeStackNode createStackNode(IState state) {
        InlinedFakeStackNode newStackNode = new InlinedFakeStackNode();

        observing.notify(observer -> observer.createStackNode(newStackNode));

        return newStackNode;
    }

    public StackLink<InlinedFakeParseForest, InlinedFakeStackNode> createStackLink(InlinedFakeParseState parseState, InlinedFakeStackNode from,
            InlinedFakeStackNode to, InlinedFakeParseForest parseForest) {
        StackLink<InlinedFakeParseForest, InlinedFakeStackNode> link = from.addLink(to, parseForest);

        observing.notify(observer -> observer.createStackLink(link));

        return link;
    }

    public void rejectStackLink(StackLink<InlinedFakeParseForest, InlinedFakeStackNode> link) {
        link.reject();

        observing.notify(observer -> observer.rejectStackLink(link));
    }

    public StackLink<InlinedFakeParseForest, InlinedFakeStackNode> findDirectLink(InlinedFakeStackNode from, InlinedFakeStackNode to) {
        for(StackLink<InlinedFakeParseForest, InlinedFakeStackNode> link : stackLinksOut(from)) {
            if(link.to == to)
                return link;
        }

        return null;
    }

    public List<StackPath<InlinedFakeParseForest, InlinedFakeStackNode>> findAllPathsOfLength(InlinedFakeStackNode stack, int length) {
        List<StackPath<InlinedFakeParseForest, InlinedFakeStackNode>> paths = new ArrayList<>();

        StackPath<InlinedFakeParseForest, InlinedFakeStackNode> pathsOrigin = new EmptyStackPath<>(stack);

        findAllPathsOfLength(pathsOrigin, length, paths);

        return paths;
    }

    private void findAllPathsOfLength(StackPath<InlinedFakeParseForest, InlinedFakeStackNode> path, int length,
        List<StackPath<InlinedFakeParseForest, InlinedFakeStackNode>> paths) {
        if(length == 0)
            paths.add(path);
        else {
            InlinedFakeStackNode lastStackNode = path.head();

            for(StackLink<InlinedFakeParseForest, InlinedFakeStackNode> linkOut : stackLinksOut(lastStackNode)) {
                if(!linkOut.isRejected()) {
                    StackPath<InlinedFakeParseForest, InlinedFakeStackNode> extendedPath = new NonEmptyStackPath<>(linkOut, path);

                    findAllPathsOfLength(extendedPath, length - 1, paths);
                }
            }
        }
    }

    protected Iterable<StackLink<InlinedFakeParseForest, InlinedFakeStackNode>> stackLinksOut(InlinedFakeStackNode stack) {
        return null;
    }

    public InlinedFakeParseForest[] getParseForests(ParseForestManager<InlinedFakeParseForest, ?, ?, ?, ?> parseForestManager,
        StackPath<InlinedFakeParseForest, InlinedFakeStackNode> pathBegin) {
        InlinedFakeParseForest[] res = parseForestManager.parseForestsArray(pathBegin.length);

        if(res != null) {
            StackPath<InlinedFakeParseForest, InlinedFakeStackNode> path = pathBegin;

            for(int i = 0; i < pathBegin.length; i++) {
                NonEmptyStackPath<InlinedFakeParseForest, InlinedFakeStackNode> nonEmptyPath =
                    (NonEmptyStackPath<InlinedFakeParseForest, InlinedFakeStackNode>) path;

                res[i] = nonEmptyPath.link.parseForest;

                path = nonEmptyPath.tail;
            }

            return res;
        }

        return null;
    }

}
