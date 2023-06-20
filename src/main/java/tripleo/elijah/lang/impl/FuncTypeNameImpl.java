package tripleo.elijah.lang.impl;

import tripleo.elijah.lang.i.*;
import tripleo.elijah.util.NotImplementedException;

import java.io.File;

/**
 * Created 8/16/20 2:16 AM
 */
public class FuncTypeNameImpl implements tripleo.elijah.lang.i.FuncTypeName {
	private       TypeNameList  _arglist     = null/* new TypeNameList() */;
	private final Context       _ctx;
	private       TypeModifiers _modifiers;
	private       TypeName      _returnValue = null /* new RegularTypeNameImpl() */; // TODO warning

	public FuncTypeNameImpl(final Context cur) {
		_ctx = cur;
	}

	@Override
	public void argList(final FormalArgList op) {
		TypeNameList tnl = new TypeNameListImpl();
		for (FormalArgListItem fali : op.falis()) {
			final TypeName tn = fali.typeName();
			if (tn != null)
				tnl.add(tn);
			else {
//				tnl.add(TypeName.Undeclared(fali)); // TODO implement me
			}
		}
		argList(tnl);
	}

	@Override
	public void argList(final TypeNameList tnl) {
		_arglist = tnl;
	}

	@Override
	public int getColumn() {
		return -1;
	}

	@Override
	public int getColumnEnd() {
		return -1;
	}

	@Override
	public Context getContext() {
		return _ctx;
	}

	@Override
	public File getFile() {
		return null;
	}

	@Override
	public int getLine() {
		return -1;
	}

	@Override
	public boolean isNull() {
		return _arglist == null && _returnValue == null;
	}

	@Override
	public Type kindOfType() {
		return Type.FUNCTION;
	}

	@Override
	public void setContext(final Context context) {
		throw new NotImplementedException();
	}

	@Override
	public void returnValue(final TypeName rtn) {
		_returnValue = rtn;
	}

	@Override
	public int getLineEnd() {
		return -1;
	}

	// @Override
	@Override
	public void type(final TypeModifiers typeModifiers) {
		_modifiers = typeModifiers;
	}
}

//
//
//