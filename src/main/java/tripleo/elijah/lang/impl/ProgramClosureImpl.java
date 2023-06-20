/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.impl;

import tripleo.elijah.lang.i.AliasStatement;
import tripleo.elijah.lang.i.ClassStatement;
import tripleo.elijah.lang.i.Context;
import tripleo.elijah.lang.i.NamespaceStatement;
import tripleo.elijah.lang.i.OS_Element;

public abstract class ProgramClosureImpl implements tripleo.elijah.lang.i.ProgramClosure {
	@Override
	public AliasStatement aliasStatement(final OS_Element aParent) {
		final AliasStatement aliasStatement = new AliasStatementImpl(aParent);
		return aliasStatement;
	}

//	public ImportStatement importStatement(OS_Element aParent) {
//		final ImportStatement importStatement = new ImportStatement(aParent);
//		return importStatement;
//	}

	@Override
	public ClassStatement classStatement(final OS_Element aParent, Context ctx) {
		final ClassStatement classStatement = new ClassStatementImpl(aParent, ctx);
		return classStatement;
	}

	@Override
	public NamespaceStatement namespaceStatement(final OS_Element aParent, Context ctx) {
		final NamespaceStatement namespaceStatement = new NamespaceStatementImpl(aParent, ctx);
		return namespaceStatement;
	}

}

//
//
//
