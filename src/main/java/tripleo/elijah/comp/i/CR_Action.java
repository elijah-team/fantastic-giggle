package tripleo.elijah.comp.i;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.internal.CompilationRunner;
import tripleo.elijah.comp.Operation;
import tripleo.elijah.comp.internal.CB_Output;
import tripleo.elijah.comp.internal.CR_State;

public interface CR_Action {
	void attach(@NotNull CompilationRunner cr);

	Operation<Boolean> execute(@NotNull CR_State st, CB_Output aO);

	String name();
}
