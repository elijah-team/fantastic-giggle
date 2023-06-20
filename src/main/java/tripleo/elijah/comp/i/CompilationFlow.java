package tripleo.elijah.comp.i;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.internal.CompilationImpl;
import tripleo.elijah.lang.i.OS_Module;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.nextgen.query.Operation2;

import java.util.function.Consumer;

public interface CompilationFlow {
	//static CompilationFlowMember findPrelude() {
	//	return new CF_FindPrelude(aCopm);
	//}

	public static CompilationFlowMember deduceModuleWithClasses() {
		return new CompilationFlowMember() {
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {

			}
		};
	}

	static CompilationFlowMember findMainClass() {
		return new CompilationFlowMember() {
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {

			}
		};
	}

	public static CompilationFlowMember finishModule() {
		return new CompilationFlowMember() {
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {

			}
		};
	}

	public static CompilationFlowMember genFromEntrypoints() {
		return new CompilationFlowMember() {
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {

			}
		};
	}

	public static CompilationFlowMember getClasses() {
		return new CompilationFlowMember() {
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {


			}
		};
	}

	public static CompilationFlowMember parseElijah() {
		return new CompilationFlowMember() {
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {
				int y = 2;
			}
		};
	}

	public static CompilationFlowMember runFunctionMapHooks() {
		return new CompilationFlowMember() {
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {

			}
		};
	}

	void add(CompilationFlowMember aFlowMember);

	public static CompilationFlowMember returnErrorCount() {
		return new CompilationFlowMember() {
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {

			}
		};
	}

	void run(CompilationImpl aCompilation);

	interface CompilationFlowMember {
		public void doIt(Compilation cc, final CompilationFlow flow);
	}

	class CF_FindPrelude implements CompilationFlowMember {
		private final Consumer<Operation2<OS_Module>> copm;

		public CF_FindPrelude(final Consumer<Operation2<OS_Module>> aCopm) {
			copm = aCopm;
		}

		@Override
		public void doIt(final Compilation cc, final CompilationFlow flow) {
			final Operation2<OS_Module> prl = cc.findPrelude(Compilation.CompilationAlways.defaultPrelude());
			assert (prl.mode() == Mode.SUCCESS);

			copm.accept(prl);
		}
	}
}
