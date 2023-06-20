/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.impl;

import tripleo.elijah.contexts.NamespaceContext;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang2.ElElementVisitor;
import tripleo.elijah.util.NotImplementedException;

/**
 * @author Tripleo(sb)
 * <p>
 * Created Apr 2, 2019 at 11:08:12 AM
 */
public class NamespaceStatementImpl extends _CommonNC
		implements Documentable, ClassItem, tripleo.elijah.lang.i.NamespaceStatement {

	private       NamespaceTypes _kind;
	private final OS_Element     parent;

	public NamespaceStatementImpl(final OS_Element aElement, final Context context) {
		parent = aElement; // setParent
		if (aElement instanceof OS_Module) {
			final OS_Module module = (OS_Module) aElement;
			//
			this.setPackageName(module.pullPackageName());
			_packageName.addElement(this);
			module.add(this);
		} else if (aElement instanceof OS_Container) {
			((OS_Container) aElement).add(this);
		} else {
			throw new IllegalStateException(String.format("Cant add NamespaceStatement to %s", aElement));
		}
		setContext(new NamespaceContext(context, this));
	}

	@Override // OS_Container
	public void add(final OS_Element anElement) {
		if (anElement instanceof ClassItem)
			items.add((ClassItem) anElement);
		else
			tripleo.elijah.util.Stupidity
					.println_err_2(String.format("[NamespaceStatement#add] not a ClassItem: %s", anElement));
	}

	@Override
	public FunctionDef funcDef() {
		return new FunctionDefImpl(this, getContext());
	}

	@Override // OS_Element
	public Context getContext() {
		return _a.getContext();
	}

	@Override
	public NamespaceTypes getKind() {
		return _kind;
	}

	@Override // OS_Element
	public OS_Element getParent() {
		return parent;
	}

	@Override // OS_Element
	public void visitGen(final ElElementVisitor visit) {
		visit.visitNamespaceStatement(this);
	}

	@Override
	public void postConstruct() {
		if (nameToken == null || nameToken.getText().equals("")) {
			setType(NamespaceTypes.MODULE);
		} else if (nameToken.getText().equals("_")) {
			setType(NamespaceTypes.PRIVATE);
		} else if (nameToken.getText().equals("__package__")) {
			setType(NamespaceTypes.PACKAGE);
		} else {
			setType(NamespaceTypes.NAMED);
		}
	}

	public void setContext(final NamespaceContext ctx) {
		_a.setContext(ctx);
	}

	@Override
	public StatementClosure statementClosure() {
		return new AbstractStatementClosure(new AbstractScope2(this) {
			@Override
			public void add(final StatementItem aItem) {
				NamespaceStatementImpl.this.add((OS_Element) aItem);
			}

			@Override
			public StatementClosure statementClosure() {
				throw new NotImplementedException();
//				return null;
			}

			@Override
			public void statementWrapper(final IExpression aExpr) {
				throw new NotImplementedException();
			}

		});
	}

	@Override
	public void setType(final NamespaceTypes aType) {
		_kind = aType;
	}

	@Override
	public TypeAliasStatement typeAlias() {
		throw new NotImplementedException();
	}

	@Override
	public ProgramClosure XXX() {
		return new ProgramClosureImpl() {
		};
	}

	@Override
	public InvariantStatement invariantStatement() {
		throw new NotImplementedException();
	}

	@Override
	public void setCategory(El_Category aCategory) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		return String.format("<Namespace %s `%s'>", getPackageName().getName(), getName());
	}


	// region ClassItem

	// endregion

}

//
//
//
