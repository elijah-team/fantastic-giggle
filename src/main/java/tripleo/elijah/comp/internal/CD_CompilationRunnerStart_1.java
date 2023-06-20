package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.i.CD_CompilationRunnerStart;
import tripleo.elijah.comp.i.CR_Action;
import tripleo.elijah.comp.i.IPipelineAccess;

import java.util.List;

import static tripleo.elijah.util.Helpers.List_of;

public class CD_CompilationRunnerStart_1 implements CD_CompilationRunnerStart {

	@Override
	public void start(final @NotNull CompilerInstructions aCompilerInstructions,
					  final @NotNull CR_State crState) {
		final @NotNull CompilationRunner             cr        = crState.runner();
		final @NotNull IPipelineAccess               pa        = crState.ca.getCompilation().getCompilationEnclosure().getPipelineAccess();
		final @NotNull Compilation.CompilationConfig cfg       = crState.ca.getCompilation().cfg;

		final CompilerBeginning                      beginning = new CompilerBeginning(cr._compilation, aCompilerInstructions, pa.getCompilerInput(), cr.progressSink, cfg);

		start(crState, beginning);
	}

	//@Override
	public void start(final @NotNull CR_State crState,
					  final @NotNull CompilerBeginning beginning) {
		if (crState.started) {
			return;
		} else {
			crState.started = true;
		}

		final CB_Output out = new CB_Output();

		final CR_FindCIs              f1 = new CR_FindCIs(beginning);
		final CR_ProcessInitialAction f2 = new CR_ProcessInitialAction(beginning);
		final CR_AlmostComplete       f3 = new CR_AlmostComplete();
		final CR_RunBetterAction      f4 = new CR_RunBetterAction();

		final @NotNull List<CR_Action> crActionList = List_of(f1, f2, f3, f4);

		for (final CR_Action each : crActionList) {
			each.attach(crState.runner());
			each.execute(crState, out);
		}
	}
}
