package org.spoofax.jsglr2.inlined.components;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.metaborg.parsetable.characterclasses.CharacterClassFactory;
import org.metaborg.parsetable.query.ParsingMode;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokens;
import org.spoofax.jsglr2.JSGLR2Request;
import org.spoofax.jsglr2.inlined.InlinedObserver;
import org.spoofax.jsglr2.messages.Category;
import org.spoofax.jsglr2.messages.Message;
import org.spoofax.jsglr2.parser.ParseException;
import org.spoofax.jsglr2.parser.Position;
import org.spoofax.jsglr2.parser.result.ParseFailureCause;
import org.spoofax.jsglr2.recovery.RecoveryMessage;
import org.spoofax.jsglr2.recovery.RecoveryType;

public class InlinedParseState {

    Stack<InlinedBacktrackChoicePoint> backtrackChoicePoints = new Stack<>();
    private InlinedRecoveryJob recoveryJob = null;
    private boolean appliedRecovery = false;
    private InlinedObserver observing;

    final public JSGLR2Request request;

    public InlinedInputStack inputStack;
    public ParsingMode mode;

    public final InlinedActiveStacks activeStacks;
    public final InlinedForActorStacks forActorStacks;
    public final ArrayDeque<InlinedForShifterElement> forShifter = new ArrayDeque<>();

    public InlinedStackNode acceptingStack;

    protected InlinedParseState(JSGLR2Request request, InlinedInputStack inputStack, InlinedObserver observing) {
        this.request = request;
        this.inputStack = inputStack;
        this.observing = observing;
        this.mode = ParsingMode.Standard;

        this.activeStacks = new InlinedActiveStacks(observing);
        this.forActorStacks = new InlinedForActorStacks(observing);
    }

    public InlinedBacktrackChoicePoint createBacktrackChoicePoint() {
        return new InlinedBacktrackChoicePoint(inputStack.clone(), activeStacks);
    }

    public boolean isRecovering() {
        return recoveryJob() != null;
    }

    public boolean successfulRecovery(JSGLR2Request request, int currentOffset) {
        return isRecovering() && currentOffset >= recoveryJob().offset + request.succeedingRecoveryOffset;
    }

    public void nextParseRound() throws ParseException {
        // observing.notify(observer -> observer.parseRound(this.getFake(),
        // activeStacks));
        if (isRecovering() && recoveryJob.timeout())
            throw new ParseException(
                    new ParseFailureCause(ParseFailureCause.Type.RecoveryTimeout, inputStack.safePosition()),
                    inputStack.safeCharacter());

        int currentOffset = inputStack.offset();

        // Record backtrack choice points per line.
        // If in recovery mode, only record new choice points when parsing after the
        // point that initiated recovery.
        if ((currentOffset == 0 || CharacterClassFactory.isNewLine(inputStack.getChar(currentOffset - 1)))
                && (!isRecovering() || lastBacktrackChoicePoint().offset() < currentOffset)) {
            InlinedBacktrackChoicePoint choicePoint = saveBacktrackChoicePoint();

            // observing.notify( observer ->
            // observer.recoveryBacktrackChoicePoint(backtrackChoicePoints().size() - 1,
            // choicePoint));

            // TODO: insert recovery mechanism
        }

        if (successfulRecovery(request, currentOffset)) {
            endRecovery();

            // observing.notify(observer -> observer.endRecovery(this.getFake()));
        }
    }

    public Stack<InlinedBacktrackChoicePoint> backtrackChoicePoints() {
        return backtrackChoicePoints;
    }

    public void startRecovery(JSGLR2Request request, int offset) {
        recoveryJob = new InlinedRecoveryJob(offset, request.recoveryIterationsQuota, request.recoveryTimeout);
        mode = ParsingMode.Recovery;
    }

    public void endRecovery() {
        recoveryJob = null;
        mode = ParsingMode.Standard;
    }

    public InlinedRecoveryJob recoveryJob() {
        return recoveryJob;
    }

    public boolean nextRecoveryIteration() {
        if (recoveryJob().hasNextIteration()) {
            int iteration = recoveryJob().nextIteration();

            for (int i = iteration; i > 0 && backtrackChoicePoints.size() > 1; i--)
                backtrackChoicePoints.pop();

            resetToBacktrackChoicePoint(backtrackChoicePoints.peek());

            recoveryJob.initQuota(activeStacks);

            return true;
        } else
            return false;
    }

    @SuppressWarnings("unchecked")
    protected void resetToBacktrackChoicePoint(InlinedBacktrackChoicePoint backtrackChoicePoint) {
        this.inputStack = backtrackChoicePoint.inputStack().clone();

        this.activeStacks.clear();

        for (InlinedStackNode activeStack : backtrackChoicePoint.activeStacks())
            this.activeStacks.add(activeStack);
    }

    public boolean appliedRecovery() {
        return appliedRecovery;
    }

    public void setAppliedRecovery() {
        appliedRecovery = true;
    }

    public List<Message> postProcessMessages(Collection<Message> originalMessages, ITokens tokens) {
        List<Message> messages = new ArrayList<>();

        for (Message originalMessage : originalMessages) {
            Message message = originalMessage;

            // Move recovery insertion messages in layout to start of layout
            if (originalMessage.category == Category.RECOVERY
                    && ((RecoveryMessage) message).recoveryType == RecoveryType.INSERTION
                    && originalMessage.region != null) {
                IToken token = tokens.getTokenAtOffset(originalMessage.region.startOffset);
                IToken precedingToken = token != null ? token.getTokenBefore() : null;

                if (precedingToken != null && precedingToken.getKind() == IToken.Kind.TK_LAYOUT) {
                    Position position = Position.atStartOfToken(precedingToken);

                    boolean positionAtNewLine = CharacterClassFactory
                            .isNewLine(inputStack.inputString().codePointAt(position.offset));

                    if (positionAtNewLine && position.offset > 0) {
                        Position previousPosition = position.previous(inputStack.inputString());

                        boolean previousPositionAtNewLine = CharacterClassFactory
                                .isNewLine(inputStack.inputString().codePointAt(previousPosition.offset));

                        if (!previousPositionAtNewLine)
                            position = previousPosition;
                    }

                    message = message.atPosition(position);
                }
            }

            messages.add(message);
        }

        return messages;
    }

    public InlinedBacktrackChoicePoint saveBacktrackChoicePoint() {
        return backtrackChoicePoints().push(createBacktrackChoicePoint());
    }

    public InlinedBacktrackChoicePoint lastBacktrackChoicePoint() {
        return backtrackChoicePoints().peek();
    }

}
