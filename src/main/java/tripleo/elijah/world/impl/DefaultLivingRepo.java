package tripleo.elijah.world.impl;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.entrypoints.MainClassEntryPoint;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.BaseFunctionDef;
import tripleo.elijah.lang.impl.OS_PackageImpl;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.world.i.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultLivingRepo implements LivingRepo {
	private       int                     _classCode    = 101;
	private       int                     _functionCode = 1001;
	private final Map<String, OS_Package> _packages     = new HashMap<String, OS_Package>();
	List<LivingNode> repo = new ArrayList<>();
	private       int                     _packageCode  = 1;

	@Override
	public LivingClass addClass(final ClassStatement cs) {
		return null;
	}

	@Override
	public DefaultLivingClass addClass(final EvaClass aClass, final @NotNull Add addFlag) {
		switch (addFlag) {
		case NONE -> {
			aClass.setCode(nextClassCode());
		}
		case MAIN_FUNCTION -> {
			throw new IllegalArgumentException("not a function");
		}
		case MAIN_CLASS -> {
			final boolean isMainClass = MainClassEntryPoint.isMainClass(aClass.getKlass());
			if (!isMainClass) {
				throw new IllegalArgumentException("not a main class");
			}
			aClass.setCode(100);
		}
		}

		final DefaultLivingClass living = new DefaultLivingClass(aClass);
		aClass._living = living;

		repo.add(living);

		return living;
	}

	@Override
	public DefaultLivingFunction addFunction(final BaseEvaFunction aFunction, final @NotNull Add addFlag) {
		switch (addFlag) {
		case NONE -> {
			aFunction.setCode(nextFunctionCode());
		}
		case MAIN_FUNCTION -> {
			if (aFunction.getFD() instanceof FunctionDef &&
					MainClassEntryPoint.is_main_function_with_no_args((FunctionDef) aFunction.getFD())) {
				aFunction.setCode(1000);
				//compilation.notifyFunction(code, aFunction);
			} else {
				throw new IllegalArgumentException("not a main function");
			}
		}
		case MAIN_CLASS -> {
			throw new IllegalArgumentException("not a class");
		}
		}

		final DefaultLivingFunction living = new DefaultLivingFunction(aFunction);
		aFunction._living = living;

		return living;
	}

	@Override
	public void addModule(final OS_Module mod, final String aFilename, final Compilation aC) {
		aC.addModule__(mod, aFilename);
	}

	@Override
	public LivingFunction addFunction(final BaseFunctionDef fd) {
		return null;
	}

	@Override
	public DefaultLivingNamespace addNamespace(final EvaNamespace aNamespace, final Add addFlag) {
		switch (addFlag) {
		case NONE -> {
			aNamespace.setCode(nextClassCode());
		}
		case MAIN_FUNCTION -> {
			throw new IllegalArgumentException("not a function");
		}
		case MAIN_CLASS -> {
			throw new IllegalArgumentException("not a main class");
		}
		}

		final DefaultLivingNamespace living = new DefaultLivingNamespace(aNamespace);
		aNamespace._living = living;

		repo.add(living);

		return living;
	}

	@Override
	public LivingPackage addPackage(final OS_Package pk) {
		return null;
	}

	@Override
	public LivingClass getClass(final EvaClass aEvaClass) {
		for (LivingNode livingNode : repo) {
			if (livingNode instanceof final LivingClass livingClass) {
				if (livingClass.evaNode().equals(aEvaClass))
					return livingClass;
			}
		}

		final DefaultLivingClass living = new DefaultLivingClass(aEvaClass);
		//klass._living = living;

		repo.add(living);

		return living;
	}

	@Override
	public LivingNamespace getNamespace(final EvaNamespace aEvaNamespace) {
		for (LivingNode livingNode : repo) {
			if (livingNode instanceof final LivingNamespace livingNamespace) {
				if (livingNamespace.evaNode().equals(aEvaNamespace))
					return livingNamespace;
			}
		}

		final DefaultLivingNamespace living = new DefaultLivingNamespace(aEvaNamespace);
		//klass._living = living;

		repo.add(living);

		return living;
	}

	@Override
	public OS_Package getPackage(final String aPackageName) {
		return _packages.get(aPackageName);
	}

	@Override
	public boolean hasPackage(final String aPackageName) {
		if (aPackageName.equals("C")) {
			int y = 2;
		}
		return _packages.containsKey(aPackageName);
	}

	@Override
	public OS_Package makePackage(final Qualident pkg_name) {
		final String pkg_name_s = pkg_name.toString();
		if (!isPackage(pkg_name_s)) {
			final OS_Package newPackage = new OS_PackageImpl(pkg_name, nextPackageCode());
			_packages.put(pkg_name_s, newPackage);
			return newPackage;
		} else
			return _packages.get(pkg_name_s);
	}

	public boolean isPackage(final String pkg) {
		return _packages.containsKey(pkg);
	}

	@Contract(mutates = "this")
	private int nextPackageCode() {
		int i = _packageCode;
		_packageCode++;
		return i;
	}

	public int nextClassCode() {
		int i = _classCode;
		_classCode++;
		return i;
	}

	public int nextFunctionCode() {
		int i = _functionCode;
		_functionCode++;
		return i;
	}
}
