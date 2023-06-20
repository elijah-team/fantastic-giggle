/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.impl;

import antlr.Token;
import tripleo.elijah.lang.i.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 12/27/20 8:50 AM
 */
public abstract class AbstractScope2 implements Scope {
	private final OS_Element _Parent;

	private List<String> docstrings;

	protected AbstractScope2(OS_Element aParent) {
		_Parent = aParent;
	}

	@Override
	public void addDocString(Token s1) {
		if (docstrings == null)
			docstrings = new ArrayList<String>();
		docstrings.add(s1.getText());
	}

//	@Override
//	public StatementClosure statementClosure() {
//	}

	@Override
	public BlockStatement blockStatement() {
		return new BlockStatementImpl(this);
	}

	@Override
	public OS_Element getElement() {
		return _Parent;
	}

	@Override
	public OS_Element getParent() {
		return _Parent;
	}

	@Override
	public InvariantStatement invariantStatement() {
		return new InvariantStatement();
	}

	@Override
	public void statementWrapper(IExpression aExpr) {
		// TODO is getParent.getContext right?
		add(new StatementWrapperImpl(aExpr, getParent().getContext(), getParent()));
	}

	@Override
	public TypeAliasStatement typeAlias() {
		return new TypeAliasStatementImpl(getParent());
	}
}

//
//
//
