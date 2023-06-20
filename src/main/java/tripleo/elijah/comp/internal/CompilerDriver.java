package tripleo.elijah.comp.internal;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.Operation;
import tripleo.elijah.comp.i.CompilerDriven;
import tripleo.elijah.comp.i.ICompilationBus;

import java.util.HashMap;
import java.util.Map;

public class CompilerDriver {
	private final ICompilationBus                  cb;
	private final Map<DriverToken, CompilerDriven> defaults = new HashMap<>();
	private final Map<DriverToken, CompilerDriven> drivens  = new HashMap<>();

	private /*static*/ boolean initialized;

	public CompilerDriver(final CompilationBus aCompilationBus) {
		cb = aCompilationBus;

		if (!initialized) {
			defaults.put(Compilation.CompilationAlways.Tokens.COMPILATION_RUNNER_START, new CD_CompilationRunnerStart_1());
			defaults.put(Compilation.CompilationAlways.Tokens.COMPILATION_RUNNER_FIND_STDLIB2, new CD_FindStdLibImpl());
			initialized = true;
		}
	}

	public Operation<CompilerDriven> get(final DriverToken aToken) {
		final Operation<CompilerDriven> o;

		if (drivens.containsKey(aToken)) {
			o = Operation.success(drivens.get(aToken));
		} else {
			if (defaults.containsKey(aToken)) {
				final CompilerDriven x = defaults.get(aToken);
				o = Operation.success(x);
			} else {
				o = Operation.failure(new Exception("Compiler Driven get failure"));
			}
		}

		return o;
	}
}
