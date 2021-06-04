package org.spoofax.jsglr2;

import org.metaborg.parsetable.IParseTable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr2.imploder.ImplodeResult;
import org.spoofax.jsglr2.imploder.TokenizedStrategoTermImploder;
import org.spoofax.jsglr2.inlined.InlinedParser;
import org.spoofax.jsglr2.parseforest.hybrid.HybridDerivation;
import org.spoofax.jsglr2.parseforest.hybrid.HybridParseForest;
import org.spoofax.jsglr2.parseforest.hybrid.HybridParseNode;
import org.spoofax.jsglr2.parser.IParser;
import org.spoofax.jsglr2.parser.observing.IParserObserver;
import org.spoofax.jsglr2.parser.result.ParseFailure;
import org.spoofax.jsglr2.parser.result.ParseResult;
import org.spoofax.jsglr2.parser.result.ParseSuccess;
import org.spoofax.jsglr2.tokens.Tokens;

public class JSGLR2RecoveryInlined implements JSGLR2<IStrategoTerm> {

    InlinedParser parser;
    TokenizedStrategoTermImploder<HybridParseForest, HybridParseNode, HybridDerivation> imploder = new TokenizedStrategoTermImploder<HybridParseForest, HybridParseNode, HybridDerivation>();

    public JSGLR2RecoveryInlined(IParseTable table) {
        parser = new InlinedParser(table);
    }

    @Override
    public IParser<?> parser() {
        return parser;
    }

    @Override
    public void attachObserver(IParserObserver observer) {
        throw new UnsupportedOperationException("This JSGLR2 does not use a IObservableParser!");
    }

    @Override
    public JSGLR2Result<IStrategoTerm> parseResult(JSGLR2Request request) {
        ParseResult<HybridParseForest> parse = parser.parse(request);
        if (parse instanceof ParseSuccess<?>) {
            ParseSuccess<HybridParseForest> suc = (ParseSuccess<HybridParseForest>) parse;
            ImplodeResult<Tokens, Void, IStrategoTerm> implode = imploder.implode(request, suc.parseResult);
            Tokens tokens = implode.intermediateResult();
            suc.postProcessMessages(tokens);
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
