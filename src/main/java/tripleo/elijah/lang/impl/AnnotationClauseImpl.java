/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import tripleo.elijah.lang.i.AnnotationPart;

/**
 * Created 8/15/20 6:31 PM
 */
public class AnnotationClauseImpl implements tripleo.elijah.lang.i.AnnotationClause {
	private List<AnnotationPart> aps = new ArrayList<AnnotationPart>();

	@Override
	public void add(final AnnotationPart ap) {
		aps.add(ap);
	}

	@Override
	public Collection<? extends AnnotationPart> aps() {
		return aps;
	}
}

//
//
//
