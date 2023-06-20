/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.i.Context;
import tripleo.elijah.lang.i.ExpressionList;
import tripleo.elijah.lang.i.IExpression;
import tripleo.elijah.lang.i.OS_Element;
import tripleo.elijah.lang2.ElElementVisitor;

/*
 * Created on Sep 1, 2005 6:47:16 PM
 *
 * $Id$
 *
 */
public class ConstructStatementImpl implements tripleo.elijah.lang.i.ConstructStatement {
	private final ExpressionList _args;
	private final IExpression    _expr;
	private final String         constructorName;
	//	private OS_Type _type;
	private final Context        context;
	private final OS_Element     parent;

	public ConstructStatementImpl(@NotNull final OS_Element aParent, @NotNull final Context aContext,
								  @NotNull final IExpression aExpr, @Nullable final String aConstructorName,
								  @Nullable final ExpressionList aExpressionList) {
		parent          = aParent;
		context         = aContext;
		_expr           = aExpr;
		constructorName = aConstructorName;
		_args           = aExpressionList;
	}

//	@Override
//	public boolean is_simple() {
//		return false;
//	}
//
//	@Override
//	public void setType(final OS_Type deducedExpression) {
//		_type = deducedExpression;
//	}
//
//	@Override
//	public OS_Type getType() {
//		return _type;
//	}

	@Override
	public ExpressionList getArgs() {
		return _args;
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public IExpression getExpr() {
		return _expr;
	}

	@Override
	public OS_Element getParent() {
		return parent;
	}

	@Override
	public void visitGen(ElElementVisitor visit) {
		visit.visitConstructStatement(this);
	}
}

//
//
//
