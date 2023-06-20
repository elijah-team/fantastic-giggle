package tripleo.elijah.stages.gen_c;

interface GCR_Rule {
	public static GCR_Rule withMessage(String message) {
		return new GCR_Rule() {
			@Override
			public String text() {
				return message;
			}
		};
	}

	String text();
}
