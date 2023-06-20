package tripleo.elijah.lang.impl;

import tripleo.elijah.lang.i.AccessNotation;
import tripleo.elijah.lang.i.ClassItem;

public abstract class __Access implements ClassItem {
	private AccessNotation _an;

	@Override
	public AccessNotation getAccess() {
		return _an;
	}

	@Override
	public void setAccess(AccessNotation an) {
		_an = an;
	}

}
