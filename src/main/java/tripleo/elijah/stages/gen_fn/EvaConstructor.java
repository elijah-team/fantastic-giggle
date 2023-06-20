/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.i.ClassStatement;
import tripleo.elijah.lang.i.ConstructorDef;
import tripleo.elijah.lang.i.FunctionDef;
import tripleo.elijah.lang.i.OS_Module;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.FunctionInvocation;

/**
 * Created 6/27/21 9:45 AM
 */
public class EvaConstructor extends BaseEvaFunction {
	public final @Nullable ConstructorDef cd;

	public EvaConstructor(final @Nullable ConstructorDef aConstructorDef) {
		cd = aConstructorDef;
	}

	@Override
	public @NotNull FunctionDef getFD() {
		if (cd == null) throw new IllegalStateException("No function");
		return cd;
	}

	//
	// region toString
	//

	@Override
	public VariableTableEntry getSelf() {
		if (getFD().getParent() instanceof ClassStatement)
			return getVarTableEntry(0);
		else
			return null;
	}

	@Override
	public String identityString() {
		return String.valueOf(cd);
	}

	// endregion

	@Override
	public OS_Module module() {
		return cd.getContext().module();
	}

	public String name() {
		if (cd == null)
			throw new IllegalArgumentException("null cd");
		return cd.name();
	}

	public void setFunctionInvocation(FunctionInvocation fi) {
		GenType genType = new GenType();
		genType.ci       = fi.getClassInvocation(); // TODO will fail on namespace constructors; next line too
		genType.resolved = (((ClassInvocation) genType.ci).getKlass()).getOS_Type();
		genType.node     = this;
		typeDeferred().resolve(genType);
	}

	@Override
	public String toString() {
		return String.format("<GeneratedConstructor %s>", cd);
	}


}

//
//
//
