package tripleo.elijah.stages.gen_c;

import tripleo.elijah.ci.LibraryStatementPart;
import tripleo.elijah.nextgen.reactive.Reactivable;
import tripleo.elijah.nextgen.reactive.ReactiveDimension;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.util.buffer.Buffer;

import java.util.List;

public class GCFM implements Reactivable {

	private       Buffer          buf;
	private       Buffer          bufHdr;
	private final BaseEvaFunction gf;
	private final GenerateResult  gr;

	public GCFM(final List<Generate_Code_For_Method.C2C_Result> aRs, final BaseEvaFunction aGf, final GenerateResult aGr) {
		gf = aGf;
		gr = aGr;

		for (Generate_Code_For_Method.C2C_Result r : aRs) {
			// TODO store a Map<TY, Buffer/*GRI??*/> in rs
			switch (r.ty()) {
			case HEADER -> buf = r.getBuffer();
			case IMPL -> bufHdr = r.getBuffer();
			default -> throw new IllegalStateException();
			}
		}

	}

	@Override
	public void respondTo(final ReactiveDimension aDimension) {
		if (aDimension instanceof GenerateC) {
			final LibraryStatementPart lsp = gf.module().getLsp();

			gr.addFunction(gf, buf, GenerateResult.TY.IMPL, lsp);
			gr.addFunction(gf, bufHdr, GenerateResult.TY.HEADER, lsp);
		}
	}
}
