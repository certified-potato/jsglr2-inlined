package org.spoofax.jsglr2;

import org.metaborg.parsetable.IParseTable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr2.imploder.ImplodeResult;
import org.spoofax.jsglr2.inlined.InlinedImploder;
import org.spoofax.jsglr2.inlined.InlinedParser;
import org.spoofax.jsglr2.parseforest.IParseForest;
import org.spoofax.jsglr2.parser.IParser;
import org.spoofax.jsglr2.parser.observing.IParserObserver;
import org.spoofax.jsglr2.parser.result.ParseFailure;
import org.spoofax.jsglr2.parser.result.ParseResult;
import org.spoofax.jsglr2.parser.result.ParseSuccess;
import org.spoofax.jsglr2.tokens.Tokens;

public class JSGLR2RecoveryInlined implements JSGLR2<IStrategoTerm> {

    InlinedParser parser;
    InlinedImploder imploder = new InlinedImploder();

    public JSGLR2RecoveryInlined(IParseTable table) {
        parser = new InlinedParser(table);
    }

    @Override
    public IParser<?> parser() {
        return parser;
    }

    @Override
    public void attachObserver(@SuppressWarnings("rawtypes") IParserObserver observer) {
        throw new UnsupportedOperationException("This JSGLR2 does not use a IObservableParser!");
    }

    @Override
    public JSGLR2Result<IStrategoTerm> parseResult(JSGLR2Request request) {
        ParseResult<IParseForest> parse = parser.parse(request);
        if (parse instanceof ParseSuccess<?>) {
            ParseSuccess<IParseForest> suc = (ParseSuccess<IParseForest>) parse;
            ImplodeResult<Tokens, Void, IStrategoTerm> implode = imploder.implode(request, suc.parseResult);
            Tokens tokens = implode.intermediateResult();
            suc.messages = parser.postProcessMessages(suc.messages, tokens); //replaces suc.postProcessMessages
            return new JSGLR2Success<IStrategoTerm>(request, (IStrategoTerm) implode.ast(), tokens,
                    implode.isAmbiguous(), suc.messages);
        } else if (parse instanceof ParseFailure<?>) {
            ParseFailure<?> fail = (ParseFailure<?>) parse;
            return new JSGLR2Failure<>(request, fail, fail.messages);
        } else {
            throw new IllegalStateException("result is neither a success or failure");
        }

    }

}
