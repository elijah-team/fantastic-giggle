package tripleo.elijah.comp.i;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.internal.CR_State;

public interface CD_CompilationRunnerStart extends CompilerDriven {

	void start(@NotNull CompilerInstructions aCompilerInstructions,
			   @NotNull CR_State crState);
}
