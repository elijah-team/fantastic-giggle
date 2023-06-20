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
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang2.ElElementVisitor;
import tripleo.elijah.util.NotImplementedException;

public class AliasStatementImpl extends __Access implements AliasStatement {
	// private String name;
	private       OS_Element      _resolvedElement;
	private       AccessNotation  access_note;
	private       El_Category     category;
	private final OS_Element      parent;
	private       IExpression     expr;
	private       IdentExpression nameToken;

	public AliasStatementImpl(final OS_Element aParent) {
		this.parent = aParent;
		if (parent instanceof OS_Container) {
			((OS_Container) parent).add(this);
		} else {
			throw new IllegalStateException("adding AliasStatement to " + aParent.getClass().getName());
		}
	}

	@Override
	public AccessNotation getAccess() {
		return access_note;
	}

	@Override
	public void setAccess(AccessNotation aNotation) {
		access_note = aNotation;
	}

	@Override // OS_Element
	public Context getContext() {
		return getParent().getContext();
	}

	@Override
	public El_Category getCategory() {
		return category;
	}

	@Override // OS_Element
	public OS_Element getParent() {
		return this.parent;
	}

	@Override
	public Qualident getExpression() {
		return (Qualident) expr;
	}

	public void setExpression(final IExpression expr) {
		if (expr.getKind() != ExpressionKind.IDENT && expr.getKind() != ExpressionKind.QIDENT
				&& expr.getKind() != ExpressionKind.DOT_EXP) // TODO need DOT_EXP to QIDENT
		{
			throw new NotImplementedException();
//			tripleo.elijah.util.Stupidity.println_out_2(String.format("[AliasStatement#setExpression] %s %s", expr, expr.getKind()));
		}
		this.expr = expr;
	}

	@Override // OS_Element2
	public String name() {
		return this.nameToken.getText();
	}

	@Override
	public void setExpression(final Qualident aXy) {
		expr = aXy;
	}

	// region ClassItem

	@Override
	public void setCategory(El_Category aCategory) {
		category = aCategory;
	}

	@Override
	public void setName(@NotNull final IdentExpression i1) {
		this.nameToken = i1;
	}

	@Override
	public OS_Element getResolvedElement() {
		return _resolvedElement;
	}

	@Override
	public boolean hasResolvedElement() {
		return _resolvedElement != null;
	}

	@Override
	public void setResolvedElement(final OS_Element element) {
		_resolvedElement = element;
	}

	@Override // OS_Element
	public void visitGen(final ElElementVisitor visit) {
		visit.visitAliasStatement(this);
	}

	// endregion

}

//
//
//
