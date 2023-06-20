/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
/**
 * Created Apr 1, 2019 at 3:21:26 PM
 */
package tripleo.elijah.lang.impl;

import antlr.Token;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.util.NotImplementedException;

import java.io.File;
import java.util.List;

/**
 * @author Tripleo(sb)
 */
public class IdentExpressionImpl implements tripleo.elijah.lang.i.IdentExpression {

	public  Attached   _a;
	private OS_Element _resolvedElement;
	OS_Type _type;
	private Token      text;

	public IdentExpressionImpl(final Token r1) {
		this.text = r1;
		this._a   = new AttachedImpl();
	}

	public IdentExpressionImpl(final Token r1, final Context cur) {
		this.text = r1;
		this._a   = new AttachedImpl();
		setContext(cur);
	}


	public @NotNull List<FormalArgListItem> getArgs() {
		return null;
	}


	@Override
	public int getColumn() {
		return token().getColumn();
	}

	@Override
	public int getColumnEnd() {
		return token().getColumn();
	}

	@Override
	public File getFile() {
		final String filename = token().getFilename();
		if (filename == null)
			return null;
		return new File(filename);
	}

	@Override
	public int getLine() {
		return token().getLine();
	}

	@Override
	public ExpressionKind getKind() {
		return ExpressionKind.IDENT;
	}

	@Override
	public IExpression getLeft() {
		return this;
	}

	@Override
	public int getLineEnd() {
		return token().getLine();
	}

	@Override
	public Context getContext() {
		return _a.getContext();
	}

	@Override
	public OS_Element getParent() {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
//		return null;
	}

	@Override
	public void visitGen(final tripleo.elijah.lang2.ElElementVisitor visit) {
		visit.visitIdentExpression(this);
	}

	@Override
	public OS_Element getResolvedElement() {
		return _resolvedElement;
	}

	@Override
	public OS_Type getType() {
		return _type;
	}

	@Override
	public boolean hasResolvedElement() {
		return _resolvedElement != null;
	}

	public void setArgs(final ExpressionList ael) {

	}

	/**
	 * same as getText()
	 */
	@Override
	public String toString() {
		return getText();
	}

	@Override
	public String getText() {
		return text.getText();
	}

	@Override
	public void setContext(final Context cur) {
		_a.setContext(cur);
	}

	@Override
	public boolean is_simple() {
		return true;
	}

	@Override
	public String repr_() {
		return String.format("IdentExpression(%s)", text.getText());
	}

	// region Locatable

	@Override
	public void setResolvedElement(final OS_Element element) {
		_resolvedElement = element;
	}

	@Override
	public void setKind(final ExpressionKind aIncrement) {
		// log and ignore
		tripleo.elijah.util.Stupidity
				.println_err_2("Trying to set ExpressionType of IdentExpression to " + aIncrement.toString());
	}

	public Token token() {
		return text;
	}

	@Override
	public void setLeft(final IExpression iexpression) {
//		if (iexpression instanceof IdentExpression) {
//			text = ((IdentExpression) iexpression).text;
//		} else {
//			// NOTE was tripleo.elijah.util.Stupidity.println_err_2
		throw new IllegalArgumentException("Trying to set left-side of IdentExpression to " + iexpression.toString());
//		}
	}

	@Override
	public void setType(final OS_Type deducedExpression) {
		_type = deducedExpression;
	}

	// endregion
}

//
//
//
