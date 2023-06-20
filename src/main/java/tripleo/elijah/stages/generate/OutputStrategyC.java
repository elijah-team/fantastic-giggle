/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.generate;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.ci.LibraryStatementPart;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.Old_GenerateResult;

import java.io.File;

/**
 * Created 1/13/21 5:54 AM
 */
public class OutputStrategyC {
	private final OutputStrategy outputStrategy;

	public OutputStrategyC(OutputStrategy outputStrategy) {
		this.outputStrategy = outputStrategy;
	}

	public void appendExtension(Old_GenerateResult.TY aTy, StringBuilder aSb) {
		switch (aTy) {
		case IMPL:
			aSb.append(".c");
			break;
		case PRIVATE_HEADER:
			aSb.append("_Priv.h");
		case HEADER:
			aSb.append(".h");
			break;
		}
	}

	private OS_Package findPackage(OS_Element e) {
		while (e != null) {
			e = e.getParent();
			if (e.getContext().getParent() == e.getContext())
				e = null;
			else {
				@NotNull ElObjectType t = DecideElObjectType.getElObjectType(e);
				switch (t) {
				case NAMESPACE:
					if (((NamespaceStatement) e).getPackageName() != null)
						return ((NamespaceStatement) e).getPackageName();
					break;
				case CLASS:
					if (((ClassStatement) e).getPackageName() != null)
						return ((ClassStatement) e).getPackageName();
					break;
				case FUNCTION:
					continue;
				default:
					// datatype, enum, alias
					continue;
				}
			}
		}
		return null;
	}

	public String nameForClass(@NotNull EvaClass aEvaClass, GenerateResult.TY aTy) {
		if (aEvaClass.module().isPrelude()) {
			// We are dealing with the Prelude
			StringBuilder sb = new StringBuilder();
			sb.append("/Prelude/");
			sb.append("Prelude");
			appendExtension(aTy, sb);
			return sb.toString();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("/");
		final LibraryStatementPart lsp = aEvaClass.module().getLsp();
		if (lsp == null)
			sb.append("______________");
		else
//			sb.append(generatedClass.module.lsp.getName());
			sb.append(lsp.getInstructions().getName());
		sb.append("/");
		OS_Package pkg = aEvaClass.getKlass().getPackageName();
		if (pkg != OS_Package.default_package) {
			if (pkg == null)
				pkg = findPackage(aEvaClass.getKlass());
			sb.append(pkg.getName());
			sb.append("/");
		}
		switch (outputStrategy.per()) {
		case PER_CLASS: {
			if (aEvaClass.isGeneric())
				sb.append(aEvaClass.getNumberedName());
			else
				sb.append(aEvaClass.getName());
		}
		break;
		case PER_MODULE: {
//					mod = generatedClass.getKlass().getContext().module();
			OS_Module mod = aEvaClass.module();
			File      f   = new File(mod.getFileName());
			String    ff  = f.getName();
			int       y   = 2;
			ff = strip_elijah_extension(ff);
			sb.append(ff);
//					sb.append('/');
		}
		break;
		case PER_PACKAGE: {
			final OS_Package pkg2 = aEvaClass.getKlass().getPackageName();
			String           pkgName;
			if (pkg2 != OS_Package.default_package) {
				pkgName = "$default_package";
			} else
				pkgName = pkg2.getName();
			sb.append(pkgName);
//					sb.append('/');
		}
		break;
		case PER_PROGRAM: {
			CompilerInstructions xx = lsp.getInstructions();
			String               n  = xx.getName();
			sb.append(n);
//					sb.append('/');
		}
		break;
		default:
			throw new IllegalStateException("Unexpected value: " + outputStrategy.per());
		}
		appendExtension(aTy, sb);
		return sb.toString();
	}

	public String nameForConstructor(EvaConstructor aEvaConstructor, Old_GenerateResult.TY aTy) {
		EvaNode c = aEvaConstructor.getGenClass();
		if (c == null) c = aEvaConstructor.getParent(); // TODO fixme
		if (c instanceof EvaClass)
			return nameForClass((EvaClass) c, aTy);
		else if (c instanceof EvaNamespace)
			return nameForNamespace((EvaNamespace) c, aTy);
		return null;
	}

	public String nameForFunction(EvaFunction generatedFunction, Old_GenerateResult.TY aTy) {
		EvaNode c = generatedFunction.getGenClass();
		if (c == null) c = generatedFunction.getParent(); // TODO fixme
		if (c instanceof EvaClass)
			return nameForClass((EvaClass) c, aTy);
		else if (c instanceof EvaNamespace)
			return nameForNamespace((EvaNamespace) c, aTy);
		return null;
	}

	public String nameForNamespace(EvaNamespace generatedNamespace, Old_GenerateResult.TY aTy) {
		if (generatedNamespace.module().isPrelude()) {
			// We are dealing with the Prelude
			StringBuilder sb = new StringBuilder();
			sb.append("/Prelude/");
			sb.append("Prelude");
			appendExtension(aTy, sb);
			return sb.toString();
		}
		String filename;
		if (generatedNamespace.getNamespaceStatement().getKind() == NamespaceTypes.MODULE) {
			final String moduleFileName = generatedNamespace.module().getFileName();
			File         moduleFile     = new File(moduleFileName);
			filename = moduleFile.getName();
			filename = strip_elijah_extension(filename);
		} else
			filename = generatedNamespace.getName();
		StringBuilder sb = new StringBuilder();
		sb.append("/");
		final LibraryStatementPart lsp = generatedNamespace.module().getLsp();
		if (lsp == null)
			sb.append("___________________");
		else
			sb.append(lsp.getInstructions().getName());
		sb.append("/");
		OS_Package pkg = generatedNamespace.getNamespaceStatement().getPackageName();
		if (pkg != OS_Package.default_package) {
			if (pkg == null)
				pkg = findPackage(generatedNamespace.getNamespaceStatement());
			sb.append(pkg.getName());
			sb.append("/");
		}
		sb.append(filename);
		appendExtension(aTy, sb);
		return sb.toString();
	}

	String strip_elijah_extension(String aFilename) {
		if (aFilename.endsWith(".elijah")) {
			aFilename = aFilename.substring(0, aFilename.length() - 7);
		} else if (aFilename.endsWith(".elijjah")) {
			aFilename = aFilename.substring(0, aFilename.length() - 8);
		}
		return aFilename;
	}

}

//
//
//
