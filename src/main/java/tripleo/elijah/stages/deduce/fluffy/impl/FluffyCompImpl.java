package tripleo.elijah.stages.deduce.fluffy.impl;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.internal.CompilationImpl;
import tripleo.elijah.entrypoints.MainClassEntryPoint;
import tripleo.elijah.lang.i.ClassItem;
import tripleo.elijah.lang.i.FunctionDef;
import tripleo.elijah.lang.i.OS_Module;
import tripleo.elijah.stages.deduce.fluffy.i.FluffyComp;
import tripleo.elijah.stages.deduce.fluffy.i.FluffyModule;

import java.util.HashMap;
import java.util.Map;

public class FluffyCompImpl implements FluffyComp {

	private final CompilationImpl _comp;

	public static boolean isMainClassEntryPoint(@NotNull final ClassItem input) {
		final FunctionDef fd = (FunctionDef) input;
		return MainClassEntryPoint.is_main_function_with_no_args(fd);
	}

	private final Map<OS_Module, FluffyModule> fluffyModuleMap = new HashMap<>();

	public FluffyCompImpl(final CompilationImpl aComp) {
		_comp = aComp;
	}

	@Override
	public FluffyModule module(final OS_Module aModule) {
		if (fluffyModuleMap.containsKey(aModule)) {
			return fluffyModuleMap.get(aModule);
		}

		final FluffyModuleImpl fluffyModule = new FluffyModuleImpl(aModule, _comp);

		fluffyModuleMap.put(aModule, fluffyModule);
//		fluffyModule.

		return fluffyModule;
	}
}
