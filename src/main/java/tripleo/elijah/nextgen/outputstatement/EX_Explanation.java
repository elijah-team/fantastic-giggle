package tripleo.elijah.nextgen.outputstatement;

public interface EX_Explanation {
	static EX_Explanation withMessage(final String message) {
		return new EX_Explanation() {
			@Override
			public String message() {
				return message;
			}
		};
	}

	String message();
}
