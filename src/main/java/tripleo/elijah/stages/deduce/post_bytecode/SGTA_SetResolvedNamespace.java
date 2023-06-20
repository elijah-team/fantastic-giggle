package tripleo.elijah.stages.deduce.post_bytecode;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.*;
import tripleo.elijah.stages.gen_fn.GenType;

class SGTA_SetResolvedNamespace implements setup_GenType_Action {
	private final NamespaceStatement namespaceStatement;

	@Contract(pure = true)
	public SGTA_SetResolvedNamespace(final NamespaceStatement aNamespaceStatement) {
		namespaceStatement = aNamespaceStatement;
	}

	@Override
	public void run(final @NotNull GenType gt, final @NotNull setup_GenType_Action_Arena arena) {
		gt.resolvedn = namespaceStatement;
	}
}
