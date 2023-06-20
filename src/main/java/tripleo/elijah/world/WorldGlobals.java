package tripleo.elijah.world;

import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.*;

public enum WorldGlobals {
	;

	private static final OS_Package _dp = new OS_PackageImpl(null, 0);

	public static OS_Package defaultPackage() {
		return _dp;
	}

}
