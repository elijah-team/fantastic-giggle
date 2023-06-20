package tripleo.elijah.comp.impl;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.i.CompilationChange;

public class CC_SetShowTree implements CompilationChange {
	private final boolean flag;

	public CC_SetShowTree(final boolean aB) {
		flag = aB;
	}

	@Override
	public void apply(final Compilation c) {
		c.cfg.showTree = flag;
	}
}
