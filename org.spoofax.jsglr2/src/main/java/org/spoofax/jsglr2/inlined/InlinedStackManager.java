package org.spoofax.jsglr2.inlined;

import java.util.ArrayList;
import java.util.List;

import org.metaborg.parsetable.states.IState;
import org.spoofax.jsglr2.parseforest.IParseForest;

public class InlinedStackManager {

    protected final InlinedObserver observing;

    public InlinedStackManager(InlinedObserver observing) {
        this.observing = observing;
    }

    public InlinedStackNode createStackNode(IState state) {
        InlinedStackNode newStackNode = new InlinedStackNode(state);

        // observing.notify(observer -> observer.createStackNode(newStackNode));

        return newStackNode;
    }

    public InlinedStackLink createStackLink(InlinedParseState parseState, InlinedStackNode from, InlinedStackNode to,
            IParseForest parseForest) {
        InlinedStackLink link = from.addLink(to, parseForest);

        // observing.notify(observer -> observer.createStackLink(link));

        return link;
    }

    public void rejectStackLink(InlinedStackLink link) {
        link.reject();

        // observing.notify(observer -> observer.rejectStackLink(link));
    }

    public InlinedStackLink findDirectLink(InlinedStackNode from, InlinedStackNode to) {
        for (InlinedStackLink link : from.getLinks()) {
            if (link.to == to)
                return link;
        }

        return null;
    }

    public List<InlinedStackPath> findAllPathsOfLength(InlinedStackNode stack, int length) {
        List<InlinedStackPath> paths = new ArrayList<>();

        InlinedStackPath pathsOrigin = new InlinedStackPath.Empty(stack);

        findAllPathsOfLength(pathsOrigin, length, paths);

        return paths;
    }

    private void findAllPathsOfLength(InlinedStackPath path, int length, List<InlinedStackPath> paths) {
        if (length == 0)
            paths.add(path);
        else {
            InlinedStackNode lastStackNode = path.head();

            for (InlinedStackLink linkOut : lastStackNode.getLinks()) {
                if (!linkOut.isRejected()) {
                    InlinedStackPath extendedPath = new InlinedStackPath.NonEmpty(linkOut, path);

                    findAllPathsOfLength(extendedPath, length - 1, paths);
                }
            }
        }
    }

    public IParseForest[] getParseForests(InlinedParseForestManager parseForestManager, InlinedStackPath pathBegin) {
        IParseForest[] res = new IParseForest[pathBegin.length];

        InlinedStackPath path = pathBegin;

        for (int i = 0; i < pathBegin.length; i++) {
            InlinedStackPath.NonEmpty nonEmptyPath = (InlinedStackPath.NonEmpty) path;

            res[i] = nonEmptyPath.link.parseForest;

            path = nonEmptyPath.tail;
        }

        return res;

    }

}
