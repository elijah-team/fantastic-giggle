package tripleo.elijah.nextgen.small;

public class ES_Symbol implements ES_Item {
	private final String s;

	public ES_Symbol(final String aS) {
		s = aS;
	}

	public String getText() {
		return s;
	}
}
