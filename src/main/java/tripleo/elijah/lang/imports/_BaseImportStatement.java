/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.imports;

import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.*;

/**
 * Created 3/26/21 4:55 AM
 */
public abstract class _BaseImportStatement implements ImportStatement {
	// region ClassItem

	protected AccessNotation _access;
	private   AccessNotation access_note;
	private   El_Category    category;

	@Override
	public AccessNotation getAccess() {
		return access_note;
	}

	@Override
	public El_Category getCategory() {
		return category;
	}

	@Override
	public void setAccess(final AccessNotation aNotation) {
		_access = aNotation;
	}

	@Override
	public void setAccess(final AccessNotationImpl aNotation) {
		access_note = aNotation;
	}

	@Override
	public void setCategory(final El_Category aCategory) {
		category = aCategory;
	}

	// endregion

}

//
//
//
