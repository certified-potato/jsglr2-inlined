package org.spoofax.jsglr2.parser;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;

import org.spoofax.jsglr2.actions.IAction;
import org.spoofax.jsglr2.actions.IReduce;
import org.spoofax.jsglr2.characters.ICharacters;
import org.spoofax.jsglr2.parseforest.AbstractParseForest;
import org.spoofax.jsglr2.parsetable.IProduction;
import org.spoofax.jsglr2.stack.AbstractStackNode;
import org.spoofax.jsglr2.stack.StackLink;
import org.spoofax.jsglr2.stack.elkhound.AbstractElkhoundStackNode;
import org.spoofax.jsglr2.states.IState;

public class ParserVisualisationObserver<StackNode extends AbstractStackNode<ParseForest>, ParseForest extends AbstractParseForest>
    implements IParserObserver<StackNode, ParseForest> {

    List<String> jsonTrace = new ArrayList<String>();

    @Override public void parseStart(Parse<StackNode, ParseForest> parse) {
        trace("{\"action\":\"start\",\"inputString\":\"" + parse.inputString + "\"}");
    }

    @Override public void parseCharacter(Parse<StackNode, ParseForest> parse, Iterable<StackNode> activeStacks) {
        trace("{\"action\":\"parseCharacter\",\"character\":\"" + ICharacters.intToString(parse.currentChar)
            + "\",\"activeStacks\":" + stackQueueToString(activeStacks) + "}");
    }

    @Override public void addActiveStack(StackNode stack) {
    }

    @Override public void addForActorStack(StackNode stack) {
    }

    @Override public void findActiveStackWithState(IState state) {
    }

    @Override public void createStackNode(StackNode stack) {
        trace("{\"action\":\"createStackNode\",\"stackNumber\":" + stack.stackNumber + ",\"stateNumber\":"
            + stack.state.stateNumber() + "}");
    }

    @Override public void createStackLink(StackLink<StackNode, ParseForest> link) {
        trace("{\"action\":\"createStackLink\",\"linkNumber\":" + link.linkNumber + ",\"fromStack\":"
            + link.from.stackNumber + ",\"toStack\":" + link.to.stackNumber + ",\"parseNode\":"
            + link.parseForest.nodeNumber + ",\"descriptor\":\"" + escape(link.parseForest.descriptor()) + "\"}");
    }

    @Override public void resetDeterministicDepth(AbstractElkhoundStackNode<ParseForest> stack) {
    }

    @Override public void rejectStackLink(StackLink<StackNode, ParseForest> link) {
        trace("{\"action\":\"rejectStackLink\",\"linkNumber\":" + link.linkNumber + "}");
    }

    @Override public void forActorStacks(IForActorStacks<StackNode> forActorStacks) {
        trace("{\"action\":\"forActorStacks\",\"forActor\":" + forActorStacks + "}");
    }

    @Override public void handleForActorStack(StackNode stack, IForActorStacks<StackNode> forActorStacks) {
    }

    @Override public void actor(StackNode stack, Parse<StackNode, ParseForest> parse,
        Iterable<IAction> applicableActions) {
        trace("{\"action\":\"actor\",\"stackNumber\":" + stack.stackNumber + "}");
    }

    @Override public void skipRejectedStack(StackNode stack) {
        trace("{\"action\":\"skipRejectedStack\",\"stackNumber\":" + stack.stackNumber + "}");
    }

    @Override public void addForShifter(ForShifterElement<StackNode, ParseForest> forShifterElement) {
        trace("{\"action\":\"addForShifter\",\"stack\":" + forShifterElement.stack.stackNumber + ", \"state\":"
            + forShifterElement.state.stateNumber() + "}");
    }

    @Override public void doReductions(Parse<StackNode, ParseForest> parse, StackNode stack, IReduce reduce) {
    }

    @Override public void doLimitedReductions(Parse<StackNode, ParseForest> parse, StackNode stack, IReduce reduce,
        StackLink<StackNode, ParseForest> link) {
    }

    @Override public void reducer(StackNode stack, IReduce reduce, ParseForest[] parseNodes,
        StackNode activeStackWithGotoState) {
        trace("{\"action\":\"reduce\",\"parseNodes\":" + parseForestListToString(parseNodes)
            + ",\"activeStackWithGotoState\":"
            + (activeStackWithGotoState != null ? activeStackWithGotoState.stackNumber : -1) + "}");
    }

    @Override public void reducerElkhound(StackNode stack, IReduce reduce, ParseForest[] parseNodes) {
        trace("{\"action\":\"reduce\",\"parseNodes\":" + parseForestListToString(parseNodes)
            + ",\"activeStackWithGotoState\":-1}");
    }

    @Override public void directLinkFound(Parse<StackNode, ParseForest> parse,
        StackLink<StackNode, ParseForest> directLink) {
        trace("{\"action\":\"directLinkFound\",\"linkNumber\":" + (directLink != null ? directLink.linkNumber : -1)
            + "}");
    }

    @Override public void accept(StackNode acceptingStack) {
        trace("{\"action\":\"acceptStackNode\",\"stackNumber\":" + acceptingStack.stackNumber + "}");
    }

    @Override public void createParseNode(ParseForest parseNode, IProduction production) {
        trace("{\"action\":\"createParseNode\",\"nodeNumber\":" + parseNode.nodeNumber + ",\"production\":"
            + production.productionNumber() + ",\"term\":\"" + escape(production.descriptor()) + "\"}");
    }

    @Override public void createDerivation(int nodeNumber, IProduction production, ParseForest[] parseNodes) {
        trace("{\"action\":\"createDerivation\",\"nodeNumber\":" + nodeNumber + ",\"production\":"
            + production.productionNumber() + ",\"term\":\"" + escape(production.descriptor()) + "\",\"subTrees\":"
            + parseForestListToString(parseNodes) + "}");
    }

    @Override public void createCharacterNode(ParseForest parseNode, int character) {
        trace("{\"action\":\"createCharacterNode\",\"nodeNumber\":" + parseNode.nodeNumber + ",\"character\":\""
            + ICharacters.intToString(character) + "\"" + ",\"startPosition\":" + parseNode.startPosition.offset
            + ",\"endPosition\":" + parseNode.endPosition.offset + "}");
    }

    @Override public void addDerivation(AbstractParseForest parseNode) {
        trace("{\"action\":\"addDerivation\",\"parseNode\":" + parseNode.nodeNumber + "}");
    }

    @Override public void shifter(ParseForest termNode, Queue<ForShifterElement<StackNode, ParseForest>> forShifter) {
        trace("{\"action\":\"shifter\",\"characterNode\":" + termNode.nodeNumber + ",\"elements\":"
            + forShifterQueueToString(forShifter) + "}");
    }

    @Override public void remark(String remark) {
        trace("{\"action\":\"remark\",\"remark\":\"" + remark + "\"}");
    }

    @Override public void success(ParseSuccess<StackNode, ParseForest, ?> success) {
        trace("{\"action\":\"success\"}");
    }

    @Override public void failure(ParseFailure<StackNode, ParseForest, ?> failure) {
        trace("{\"action\":\"failure\"}");
    }

    private void trace(String json) {
        jsonTrace.add(json);
    }

    public String toJson() {
        String res = "";

        for(String action : jsonTrace) {
            if(res.isEmpty())
                res += "\n\t" + action;
            else
                res += ",\n\t" + action;
        }

        return "[" + res + "\n]";
    }

    public void toJsonFile(String filename) throws FileNotFoundException {
        try(PrintWriter out = new PrintWriter(filename)) {
            out.println(toJson());
        }
    }

    private String escape(String string) {
        return string.replaceAll("\"", Matcher.quoteReplacement("\\\""));
    }

}
