/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import tripleo.elijah.lang.i.ClassStatement;

/**
 * Created 1/5/22 11:27 PM
 */
class DerivedClassInvocation extends ClassInvocation {
	private final ClassInvocation derivation;

	public DerivedClassInvocation(final ClassStatement aClassStatement, final ClassInvocation aClassInvocation) {
		super(aClassStatement, null);
		derivation = aClassInvocation;
	}

	public ClassInvocation getDerivation() {
		return derivation;
	}

	@Override
	public void setForFunctionInvocation(final FunctionInvocation aFunctionInvocation) {
		aFunctionInvocation.setClassInvocation(this);
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
