package tripleo.elijah.stages.gen_c;

import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.*;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.gen_fn.*;

class GI_FunctionDef implements GenerateC_Item {
	private final FunctionDef _e;
	private       EvaNode     _evaNaode;
	private final GI_Repo     _repo;

	public GI_FunctionDef(final FunctionDef aE, final GI_Repo aGIRepo) {
		_e    = aE;
		_repo = aGIRepo;
	}

	EvaNode _re_is_FunctionDef(final @Nullable ProcTableEntry pte, final EvaClass a_cheat, final IdentTableEntry ite) {
		EvaNode resolved = null;
		if (pte != null) {
			final FunctionInvocation fi = pte.getFunctionInvocation();
			if (fi != null) {
				final BaseEvaFunction gen = fi.getGenerated();
				if (gen != null)
					resolved = gen;
			}
		}
		if (resolved == null) {
			final EvaNode resolved1 = ite.resolvedType();
			if (resolved1 instanceof EvaFunction)
				resolved = resolved1;
			else if (resolved1 instanceof EvaClass) {
				resolved = resolved1;

				// FIXME Bar#quux is not being resolves as a BGF in Hier

//								FunctionInvocation fi = pte.getFunctionInvocation();
//								fi.setClassInvocation();
			}
		}

		if (resolved == null) {
			resolved = a_cheat;
		}

		return resolved;
	}

	@Override
	public EvaNode getEvaNode() {
		return _evaNaode;
	}

	@Override
	public void setEvaNode(final EvaNode aEvaNode) {
		_evaNaode = aEvaNode;
	}


}
