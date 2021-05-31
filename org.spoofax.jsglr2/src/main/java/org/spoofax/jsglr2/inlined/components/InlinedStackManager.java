package org.spoofax.jsglr2.inlined.components;

import java.util.ArrayList;
import java.util.List;

import org.metaborg.parsetable.states.IState;
import org.spoofax.jsglr2.inlined.observables.FakeDerivation;
import org.spoofax.jsglr2.inlined.observables.FakeParseForest;
import org.spoofax.jsglr2.inlined.observables.FakeParseNode;
import org.spoofax.jsglr2.inlined.observables.FakeParseState;
import org.spoofax.jsglr2.inlined.observables.FakeStackNode;
import org.spoofax.jsglr2.parseforest.ParseForestManager;
import org.spoofax.jsglr2.parser.observing.ParserObserving;
import org.spoofax.jsglr2.stack.StackLink;
import org.spoofax.jsglr2.stack.paths.EmptyStackPath;
import org.spoofax.jsglr2.stack.paths.NonEmptyStackPath;
import org.spoofax.jsglr2.stack.paths.StackPath;

public class InlinedStackManager {
    
    protected final ParserObserving<FakeParseForest, FakeDerivation, FakeParseNode, FakeStackNode, FakeParseState> observing;

    protected InlinedStackManager(
        ParserObserving<FakeParseForest, FakeDerivation, FakeParseNode, FakeStackNode, FakeParseState> observing) {
        this.observing = observing;
    }

    public FakeStackNode createStackNode(IState state) {
        FakeStackNode newStackNode = new FakeStackNode();

        observing.notify(observer -> observer.createStackNode(newStackNode));

        return newStackNode;
    }

    public StackLink<FakeParseForest, FakeStackNode> createStackLink(FakeParseState parseState, FakeStackNode from,
            FakeStackNode to, FakeParseForest parseForest) {
        StackLink<FakeParseForest, FakeStackNode> link = from.addLink(to, parseForest);

        observing.notify(observer -> observer.createStackLink(link));

        return link;
    }

    public void rejectStackLink(StackLink<FakeParseForest, FakeStackNode> link) {
        link.reject();

        observing.notify(observer -> observer.rejectStackLink(link));
    }

    public StackLink<FakeParseForest, FakeStackNode> findDirectLink(FakeStackNode from, FakeStackNode to) {
        for(StackLink<FakeParseForest, FakeStackNode> link : stackLinksOut(from)) {
            if(link.to == to)
                return link;
        }

        return null;
    }

    public List<StackPath<FakeParseForest, FakeStackNode>> findAllPathsOfLength(FakeStackNode stack, int length) {
        List<StackPath<FakeParseForest, FakeStackNode>> paths = new ArrayList<>();

        StackPath<FakeParseForest, FakeStackNode> pathsOrigin = new EmptyStackPath<>(stack);

        findAllPathsOfLength(pathsOrigin, length, paths);

        return paths;
    }

    private void findAllPathsOfLength(StackPath<FakeParseForest, FakeStackNode> path, int length,
        List<StackPath<FakeParseForest, FakeStackNode>> paths) {
        if(length == 0)
            paths.add(path);
        else {
            FakeStackNode lastStackNode = path.head();

            for(StackLink<FakeParseForest, FakeStackNode> linkOut : stackLinksOut(lastStackNode)) {
                if(!linkOut.isRejected()) {
                    StackPath<FakeParseForest, FakeStackNode> extendedPath = new NonEmptyStackPath<>(linkOut, path);

                    findAllPathsOfLength(extendedPath, length - 1, paths);
                }
            }
        }
    }

    protected Iterable<StackLink<FakeParseForest, FakeStackNode>> stackLinksOut(FakeStackNode stack) {
        return null;
    }

    public FakeParseForest[] getParseForests(ParseForestManager<FakeParseForest, ?, ?, ?, ?> parseForestManager,
        StackPath<FakeParseForest, FakeStackNode> pathBegin) {
        FakeParseForest[] res = parseForestManager.parseForestsArray(pathBegin.length);

        if(res != null) {
            StackPath<FakeParseForest, FakeStackNode> path = pathBegin;

            for(int i = 0; i < pathBegin.length; i++) {
                NonEmptyStackPath<FakeParseForest, FakeStackNode> nonEmptyPath =
                    (NonEmptyStackPath<FakeParseForest, FakeStackNode>) path;

                res[i] = nonEmptyPath.link.parseForest;

                path = nonEmptyPath.tail;
            }

            return res;
        }

        return null;
    }

}
