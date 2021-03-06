package org.spoofax.jsglr2.inlined;

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
import org.spoofax.jsglr2.messages.Category;
import org.spoofax.jsglr2.messages.Message;
import org.spoofax.jsglr2.parser.ParseException;
import org.spoofax.jsglr2.parser.Position;
import org.spoofax.jsglr2.parser.result.ParseFailureCause;
import org.spoofax.jsglr2.recovery.RecoveryMessage;
import org.spoofax.jsglr2.recovery.RecoveryType;

/**
 * 
 */
final class InlinedParseState {

    Stack<InlinedBacktrackChoicePoint> backtrackChoicePoints = new Stack<>();
    private InlinedRecoveryJob recoveryJob = null;
    private boolean appliedRecovery = false;

    final JSGLR2Request request;

    InlinedInputStack inputStack;
    ParsingMode mode;
    
    /**
     * 
     */
    final InlinedActiveStacks activeStacks;
    /**
     * List of "right-most" stack nodes that can be processed in the current parse loop round
     */
    final InlinedForActorStacks forActorStacks;
    /**
     * List of shift state transitions that need to be processed.
     */
    final ArrayDeque<InlinedForShifterElement> forShifter = new ArrayDeque<>();

    InlinedStackNode acceptingStack;

    protected InlinedParseState(JSGLR2Request request, InlinedInputStack inputStack) {//, StatCounter counter) {
        this.request = request;
        this.inputStack = inputStack;
        this.mode = ParsingMode.Standard;

        this.activeStacks = new InlinedActiveStacks();
        this.forActorStacks = new InlinedForActorStacks();
    }

    boolean isRecovering() {
        return recoveryJob() != null;
    }

    void nextParseRound() throws ParseException {
        if (isRecovering() && recoveryJob.timeout())
            throw new ParseException(
                    new ParseFailureCause(ParseFailureCause.Type.RecoveryTimeout, inputStack.safePosition()),
                    inputStack.safeCharacter());

        int currentOffset = inputStack.offset();

        // Record backtrack choice points per line.
        // If in recovery mode, only record new choice points when parsing after the
        // point that initiated recovery.
        if ((currentOffset == 0 ||
           CharacterClassFactory.isNewLine(inputStack.getChar(currentOffset - 1))) &&
           (!isRecovering() || backtrackChoicePoints.peek().offset() < currentOffset))
            // save point
            backtrackChoicePoints.push(new InlinedBacktrackChoicePoint(inputStack.clone(), activeStacks));

        if (successfulRecovery(request, currentOffset)) {
            //be done with recovery
            recoveryJob = null;
            mode = ParsingMode.Standard;
        }
    }

    void startRecovery(JSGLR2Request request, int offset) {
        recoveryJob = new InlinedRecoveryJob(offset, request.recoveryIterationsQuota, request.recoveryTimeout);
        mode = ParsingMode.Recovery;
    }

    InlinedRecoveryJob recoveryJob() {
        return recoveryJob;
    }

    boolean nextRecoveryIteration() {
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
    
    boolean appliedRecovery() {
        return appliedRecovery;
    }

    void setAppliedRecovery() {
        appliedRecovery = true;
    }

    List<Message> postProcessMessages(Collection<Message> originalMessages, ITokens tokens) {
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
    
    private boolean successfulRecovery(JSGLR2Request request, int currentOffset) {
        return isRecovering() && currentOffset >= recoveryJob().offset + request.succeedingRecoveryOffset;
    }
    
    private void resetToBacktrackChoicePoint(InlinedBacktrackChoicePoint backtrackChoicePoint) {
        this.inputStack = backtrackChoicePoint.inputStack().clone();

        this.activeStacks.clear();

        for (InlinedStackNode activeStack : backtrackChoicePoint.activeStacks())
            this.activeStacks.add(activeStack);
    }
}
