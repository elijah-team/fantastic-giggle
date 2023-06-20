/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.impl;

import tripleo.elijah.lang.i.Context;
import tripleo.elijah.lang.i.IExpression;
import tripleo.elijah.lang.i.OS_Element;
import tripleo.elijah.lang.i.StatementItem;
import tripleo.elijah.lang2.ElElementVisitor;
import tripleo.elijah.util.NotImplementedException;

public class YieldExpressionImpl extends BasicBinaryExpressionImpl
		implements OS_Element, StatementItem, tripleo.elijah.lang.i.YieldExpression {

	public YieldExpressionImpl(final IExpression aExpr) {
		// TODO Auto-generated constructor stub
		throw new NotImplementedException();
	}

	@Override
	public Context getContext() {
		throw new NotImplementedException();
//		return null;
	}

	@Override
	public OS_Element getParent() {
		throw new NotImplementedException();
//		return null;
	}

	@Override
	public void visitGen(ElElementVisitor visit) {
		visit.visitYield(this);
	}
}

//
//
//
