package tripleo.elijah.comp;

import tripleo.elijah.comp.i.CompilationFlow;
import tripleo.elijah.comp.impl.DefaultCompilationFlow;
import tripleo.elijah.comp.internal.CompilationImpl;
import tripleo.elijah.contexts.ModuleContext;
import tripleo.elijah.lang.i.OS_Module;
import tripleo.elijah.lang.impl.OS_ModuleImpl;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.nextgen.query.Operation2;

public class ModuleBuilder {
	private       boolean   _addToCompilation = false;
	private       String    _fn               = null;
	//		private final Compilation compilation;
	private final OS_Module mod;

	public ModuleBuilder(Compilation aCompilation) {
//			compilation = aCompilation;
		mod = new OS_ModuleImpl();
		mod.setParent(aCompilation);
	}

	public ModuleBuilder addToCompilation() {
		_addToCompilation = true;
		return this;
	}

	public OS_Module build() {
		if (_addToCompilation) {
			if (_fn == null) throw new IllegalStateException("Filename not set in ModuleBuilder");
			mod.getCompilation().world().addModule(mod, _fn, mod.getCompilation());
			//mod.getCompilation().addModule(mod, _fn);
		}
		return mod;
	}

	public ModuleBuilder setContext() {
		final ModuleContext mctx = new ModuleContext(mod);
		mod.setContext(mctx);
		return this;
	}

	public ModuleBuilder withFileName(String aFn) {
		_fn = aFn;
		mod.setFileName(aFn);
		return this;
	}

	public ModuleBuilder withPrelude(String aPrelude) {
		final Operation2<OS_Module>[] p = new Operation2[]{null};

		if (false) {
			final CompilationFlow.CF_FindPrelude cffp = new CompilationFlow.CF_FindPrelude((pp) -> p[0] = pp);
			final DefaultCompilationFlow         flow = new DefaultCompilationFlow();
			flow.add(cffp);

			flow.run((CompilationImpl) mod.getCompilation());
		} else {
			p[0] = mod.getCompilation().findPrelude(aPrelude);
		}

		assert p[0].mode() == Mode.SUCCESS;

		mod.setPrelude(p[0].success());

		return this;
	}
}
