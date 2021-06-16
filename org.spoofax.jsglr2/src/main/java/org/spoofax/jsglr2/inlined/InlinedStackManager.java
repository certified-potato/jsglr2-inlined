package org.spoofax.jsglr2.inlined;

import java.util.ArrayList;
import java.util.List;

import org.metaborg.parsetable.states.IState;
import org.spoofax.jsglr2.parseforest.IParseForest;

final class InlinedStackManager {

//    protected final StatCounter observer;
//
//    public InlinedStackManager(StatCounter counter) {
//        this.observer = observer;
//    }

    InlinedStackNode createStackNode(IState state) {
        InlinedStackNode newStackNode = new InlinedStackNode(state);

        //observer.createStackNode(newStackNode);

        return newStackNode;
    }

    InlinedStackLink createStackLink(InlinedParseState parseState, InlinedStackNode from, InlinedStackNode to,
            IParseForest parseForest) {
        InlinedStackLink link = from.addLink(to, parseForest);

        //observer.createStackLink(link);

        return link;
    }

    void rejectStackLink(InlinedStackLink link) {
        link.reject();

        //observer.rejectStackLink();
    }

    InlinedStackLink findDirectLink(InlinedStackNode from, InlinedStackNode to) {
        for (InlinedStackLink link : from.getLinks()) {
            if (link.to == to)
                return link;
        }

        return null;
    }

    List<InlinedStackPath> findAllPathsOfLength(InlinedStackNode stack, int length) {
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

    IParseForest[] getParseForests(InlinedParseForestManager parseForestManager, InlinedStackPath pathBegin) {
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
