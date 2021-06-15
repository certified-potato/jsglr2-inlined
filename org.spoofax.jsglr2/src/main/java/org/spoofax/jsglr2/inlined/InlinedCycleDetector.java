package org.spoofax.jsglr2.inlined;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.spoofax.jsglr2.messages.Message;
import org.spoofax.jsglr2.parser.Position;
import org.spoofax.jsglr2.parser.result.ParseFailureCause;

public class InlinedCycleDetector implements IInlinedParseNodeVisitor {
    private Collection<Message> messages;
    private ArrayList<InlinedParseNode> spine = new ArrayList<>();
    ParseFailureCause failureCause = null;

    InlinedCycleDetector(Collection<Message> messages) {
        this.messages = messages;
    }

    @Override
    public boolean preVisit(InlinedParseNode parseNode, Position startPosition) {
        if(spine.contains(parseNode)) {
            failureCause =
                new ParseFailureCause(ParseFailureCause.Type.Cycle, startPosition, cycleDescription(parseNode));

            messages.add(failureCause.toMessage());

            return false;
        } else {
            spine.add(parseNode);

            return parseNode.production().isContextFree();
        }
    }

    boolean cycleDetected() {
        return failureCause != null;
    }

    private String cycleDescription(InlinedParseNode parseNode) {
        int cycleStartIndex = spine.size() - 1;

        while(spine.get(cycleStartIndex) != parseNode)
            cycleStartIndex--;

        List<InlinedParseNode> cycle = spine.subList(cycleStartIndex, spine.size());

        cycle.add(parseNode);

        return cycle.stream().map(InlinedParseNode::descriptor).collect(Collectors.joining(" -> "));
    }

    @Override
    public void postVisit(InlinedParseNode parseNode, Position startPosition, Position endPosition) {
        spine.remove(parseNode);
    }
}
