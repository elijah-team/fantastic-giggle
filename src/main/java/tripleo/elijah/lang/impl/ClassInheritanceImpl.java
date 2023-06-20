/*
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
/* Created on Aug 30, 2005 9:01:37 PM
 *
 * $Id$
 *
 */
package tripleo.elijah.lang.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import tripleo.elijah.lang.i.TypeName;

public class ClassInheritanceImpl implements tripleo.elijah.lang.i.ClassInheritance {

	public List<TypeName> tns = new ArrayList<TypeName>();

	/**
	 * Do nothing and wait for addAll or add.
	 * Used by ClassBuilder
	 */
	public ClassInheritanceImpl() {
	}

	@Override
	public void add(final TypeName tn) {
		tns.add(tn);
	}

	@Override
	public void addAll(final Collection<TypeName> tns) {
		this.tns.addAll(tns);
	}

	@Override
	public List<TypeName> tns() {
		return this.tns;
	}

}

//
//
//
