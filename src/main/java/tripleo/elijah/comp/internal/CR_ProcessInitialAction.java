package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructionsImpl;
import tripleo.elijah.comp.Operation;
import tripleo.elijah.comp.i.CR_Action;

public class CR_ProcessInitialAction implements CR_Action {
	private final CompilerInstructionsImpl ci;
	private       CompilationRunner        compilationRunner;
	private final boolean                  do_out;

	@Contract(pure = true)
	public CR_ProcessInitialAction(final CompilerBeginning beginning) {
		this((CompilerInstructionsImpl) beginning.compilerInstructions(), beginning.cfg().do_out);
	}

	@Contract(pure = true)
	public CR_ProcessInitialAction(final @NotNull CompilerInstructionsImpl aCi,
								   final boolean aDo_out) {
		ci     = aCi;
		do_out = aDo_out;
	}

	@Override
	public void attach(final @NotNull CompilationRunner cr) {
		compilationRunner = cr;
	}

	@Override
	public Operation<Boolean> execute(final @NotNull CR_State st, final CB_Output aO) {
		compilationRunner = st.runner();

		try {
			compilationRunner._compilation.use(ci, do_out);
			return Operation.success(true);
		} catch (final Exception aE) {
			return Operation.failure(aE);
		}
	}

	@Override
	public String name() {
//			"process initial action"
		return "process initial";
	}
}
