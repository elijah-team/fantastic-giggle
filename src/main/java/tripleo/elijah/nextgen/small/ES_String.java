package tripleo.elijah.nextgen.small;

public class ES_String implements ES_Item {
	private final String s;

	public ES_String(final String aS) {
		s = aS;
	}

	public String getText() {
		return s;
	}
}
