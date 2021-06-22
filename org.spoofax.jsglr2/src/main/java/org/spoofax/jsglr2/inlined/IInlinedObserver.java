package org.spoofax.jsglr2.inlined;

import java.util.Queue;

import org.metaborg.parsetable.actions.IAction;
import org.metaborg.parsetable.actions.IReduce;
import org.metaborg.parsetable.productions.IProduction;
import org.metaborg.parsetable.states.IState;
import org.spoofax.jsglr2.parseforest.IParseForest;
import org.spoofax.jsglr2.parser.result.ParseFailure;
import org.spoofax.jsglr2.parser.result.ParseSuccess;

public interface IInlinedObserver {
    default void parseStart(InlinedParseState parseState) {
    }

    default void parseRound(InlinedParseState parseState, Iterable<InlinedStackNode> activeStacks) {
    }

    default void addActiveStack(InlinedStackNode stack) {
    }

    default void addForActorStack(InlinedStackNode stack) {
    }

    default void findActiveStackWithState(IState state) {
    }

    default void createStackNode(InlinedStackNode stack) {
    }

    default void createStackLink(InlinedStackLink link) {
    }

    default void rejectStackLink(InlinedStackLink link) {
    }

    default void forActorStacks(InlinedForActorStacks forActorStacks) {
    }

    default void handleForActorStack(InlinedStackNode stack, InlinedForActorStacks forActorStacks) {
    }

    default void actor(InlinedStackNode stack, InlinedParseState parseState, Iterable<IAction> applicableActions) {
    }

    default void skipRejectedStack(InlinedStackNode stack) {
    }

    default void addForShifter(InlinedForShifterElement forShifterElement) {
    }

    default void doReductions(InlinedParseState parseState, InlinedStackNode stack, IReduce reduce) {
    }

    default void doLimitedReductions(InlinedParseState parseState, InlinedStackNode stack, IReduce reduce,
        InlinedStackLink link) {
    }

    default void reducer(InlinedParseState parseState, InlinedStackNode activeStack, InlinedStackNode originStack, IReduce reduce,
        IParseForest[] parseNodes, InlinedStackNode gotoStack) {
    }

    default void directLinkFound(InlinedParseState parseState, InlinedStackLink directLink) {
    }

    default void accept(InlinedStackNode acceptingStack) {
    }

    default void createParseNode(InlinedParseNode parseNode, IProduction production) {
    }

    default void createDerivation(InlinedDerivation derivationNode, IProduction production, IParseForest[] parseNodes) {
    }

    default void createCharacterNode(IParseForest characterNode, int character) {
    }

    default void addDerivation(InlinedParseNode parseNode, InlinedDerivation derivation) {
    }

    default void shifter(IParseForest termNode, Queue<InlinedForShifterElement> forShifter) {
    }

    default void shift(InlinedParseState parseState, InlinedStackNode originStack, InlinedStackNode gotoStack) {
    }

    default void recoveryBacktrackChoicePoint(int index, InlinedBacktrackChoicePoint choicePoint) {
    }

    default void startRecovery(InlinedParseState parseState) {
    }

    default void recoveryIteration(InlinedParseState parseState) {
    }

    default void endRecovery(InlinedParseState parseState) {
    }

    default void remark(String remark) {
    }

    default void success(ParseSuccess<IParseForest> success) {
    }

    default void failure(ParseFailure<IParseForest> failure) {
    }
}
