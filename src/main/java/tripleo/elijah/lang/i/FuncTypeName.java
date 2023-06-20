package tripleo.elijah.lang.i;

import java.io.File;

public interface FuncTypeName extends TypeName {
	void argList(FormalArgList op);

	void argList(TypeNameList tnl);

	int getColumn();

	int getColumnEnd();

	Context getContext();

	File getFile();

	boolean isNull();

	Type kindOfType();

	void setContext(Context context);

	int getLine();

	void returnValue(TypeName rtn);

	int getLineEnd();

	// @Override
	void type(TypeModifiers typeModifiers);
}
