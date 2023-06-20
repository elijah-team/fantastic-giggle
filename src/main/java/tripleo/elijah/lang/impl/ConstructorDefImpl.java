/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.impl;

import org.jetbrains.annotations.Nullable;
import tripleo.elijah.contexts.FunctionContext;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang2.ElElementVisitor;
import tripleo.elijah.util.Helpers;

/**
 * @author Tripleo
 * <p>
 * Created Apr 16, 2020 at 7:34:07 AM
 */
public class ConstructorDefImpl extends BaseFunctionDef implements tripleo.elijah.lang.i.ConstructorDef {
	// TODO override name() ??
	public final static ConstructorDef  defaultVirtualCtor   = new ConstructorDefImpl(null, null, null);
	public final static IdentExpression emptyConstructorName = Helpers.string_to_ident("<>");

	private FunctionModifiers mod;

	private final OS_Element parent;

	private @Nullable TypeName rt;

	public ConstructorDefImpl(final IdentExpression aConstructorName, final _CommonNC aParent, final Context context) {
		parent = (OS_Element) aParent;
		if (parent != null) {
			if (aParent instanceof OS_Container) {
				((OS_Container) parent).add(this);
			} else {
				throw new IllegalStateException("adding FunctionDef to " + aParent.getClass().getName());
			}
			_a.setContext(new FunctionContext(context, this));
		}

		if (aConstructorName != null)
			setName(aConstructorName);
		else
			setName(emptyConstructorName); // hack for Context#lookup
		setSpecies(Species.CTOR);
	}

	@Override
	public void add(final FunctionItem seq) {
		items().add((OS_Element2) seq);
	}

	@Override // OS_Element
	public OS_Element getParent() {
		return parent;
	}

	@Override
	public void postConstruct() {

	}

	@Override
	public @Nullable TypeName returnType() {
		return rt;
	}

	@Override
	public void set(FunctionModifiers mod) {
		this.mod = mod;
	}

	@Override
	public void setAbstract(boolean b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBody(FunctionBody aFunctionBody) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHeader(FunctionHeader aFunctionHeader) {
		setFal(aFunctionHeader.getFal());
//		set(aFunctionHeader.getModifier());
		assert aFunctionHeader.getModifier() == null;
		setName(aFunctionHeader.getName());
//		setReturnType(aFunctionHeader.getReturnType());
		assert aFunctionHeader.getReturnType() == null;
	}

	@Override
	public void setReturnType(TypeName tn) {
		rt = tn;
	}

	@Override
	public void visitGen(ElElementVisitor visit) {
		visit.visitConstructorDef(this);
	}

	@Override
	public String toString() {
		return String.format("<Constructor %s %s %s>", parent, name(), getArgs());
	}

}

//
//
//
