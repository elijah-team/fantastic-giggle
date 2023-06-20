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
import tripleo.elijah.lang.i.FormalArgListItem;

import java.util.ArrayList;
import java.util.List;

// Referenced classes of package pak2:
//			FormalArgListItem

public class FormalArgListImpl implements tripleo.elijah.lang.i.FormalArgList {

	public List<FormalArgListItem> falis = new ArrayList<FormalArgListItem>();

	@Override
	public List<FormalArgListItem> falis() {
		return falis;
	}

	@Override
	public @NotNull List<FormalArgListItem> items() {
		return falis;
	}

	@Override
	public FormalArgListItem next() {
		final FormalArgListItem fali = new FormalArgListItemImpl();
		falis.add(fali);
		return fali;
	}

	@Override
	public String toString() {
		return falis.toString();
	}
}

//
//
//
