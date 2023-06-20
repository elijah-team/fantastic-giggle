package tripleo.elijah.comp.impl;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.i.CompilationChange;

public class CC_SetDoOut implements CompilationChange {
	private final boolean flag;

	public CC_SetDoOut(final boolean aB) {
		flag = aB;
	}

	@Override
	public void apply(final Compilation c) {
		c.cfg.do_out = flag;
	}
}
