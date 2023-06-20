package tripleo.elijah.lang.impl;

import tripleo.elijah.contexts.ContextInfo;
import tripleo.elijah.lang.i.Context;
import tripleo.elijah.lang.i.LookupResult;
import tripleo.elijah.lang.i.OS_Element;

public class LookupResultImpl implements LookupResult {
	private final Context     context;
	private final OS_Element  element;
	private final ContextInfo importInfo;
	private final int         level;
	private final String      name;

	public LookupResultImpl(final String aName, final OS_Element aElement, final int aLevel, final Context aContext) {
		name            = aName;
		element         = aElement;
		level           = aLevel;
		context         = aContext;
		this.importInfo = null;
	}

	public LookupResultImpl(final String aName, final OS_Element aElement, final int aLevel, final Context aContext, final ContextInfo aImportInfo) {
		name       = aName;
		element    = aElement;
		level      = aLevel;
		context    = aContext;
		importInfo = aImportInfo;
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public OS_Element getElement() {
		return element;
	}

	@Override
	public ContextInfo getImportInfo() {
		return importInfo;
	}

	@Override
	public int getLevel() {
		return 0;
	}

	@Override
	public String getName() {
		return null;
	}
}
