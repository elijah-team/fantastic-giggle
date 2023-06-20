package tripleo.elijah.stages.deduce.post_bytecode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.*;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.gen_fn.GenType;

class SGTA_RegisterClassInvocation implements setup_GenType_Action {

	private final ClassStatement classStatement;
	private final DeducePhase    phase;

	public SGTA_RegisterClassInvocation(final ClassStatement aClassStatement, final DeducePhase aPhase) {
		classStatement = aClassStatement;
		phase          = aPhase;
	}

	@Override
	public void run(final @NotNull GenType gt, final @NotNull setup_GenType_Action_Arena arena) {
		@Nullable ClassInvocation ci = new ClassInvocation(classStatement, null);
		ci = phase.registerClassInvocation(ci);

		arena.put("ci", ci);
	}
}
