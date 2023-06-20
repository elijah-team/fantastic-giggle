package tripleo.elijah.lang.imports;

import tripleo.elijah.contexts.ImportContext;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.QualidentListImpl;
import tripleo.elijah.util.NotImplementedException;

import java.util.List;

/**
 * Created 8/7/20 2:10 AM
 */
public class NormalImportStatement extends _BaseImportStatement {
	private final QualidentList importList = new QualidentListImpl();
	private       Context       _ctx;
	final         OS_Element    parent;

	public NormalImportStatement(final OS_Element aParent) {
		parent = aParent;
		if (parent instanceof OS_Container) {
			((OS_Container) parent).add(this);
		} else
			throw new NotImplementedException();
	}

	public void addNormalPart(final Qualident aQualident) {
		importList.add(aQualident);
	}

	@Override
	public Context getContext() {
		return parent.getContext();
	}

	@Override
	public OS_Element getParent() {
		return parent;
	}

	public Context myContext() {
		assert _ctx != null;
		return _ctx;
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Qualident> parts() {
		return importList.parts();
	}

	@Override
	public void setContext(final ImportContext ctx) {
		_ctx = ctx;
	}

}

//
//
//
