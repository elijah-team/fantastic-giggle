package tripleo.elijah.comp.nextgen;

public class CP_OutputPath implements CP_Path {
	@Override
	public CP_SubFile subFile(final String aFile) { // s ;)
		return new CP_SubFile(this, aFile);
	}
}
