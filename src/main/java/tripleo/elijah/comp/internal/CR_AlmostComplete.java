package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Operation;
import tripleo.elijah.comp.i.CR_Action;

public class CR_AlmostComplete implements CR_Action {
	private CompilationRunner compilationRunner;

	@Override
	public void attach(final @NotNull CompilationRunner cr) {
		compilationRunner = cr;
	}

	@Override
	public Operation<Boolean> execute(final CR_State st, final CB_Output aO) {
		compilationRunner.cis.almostComplete();
		return Operation.success(true);
	}

	@Override
	public String name() {
		return "cis almostComplete";
	}
}
