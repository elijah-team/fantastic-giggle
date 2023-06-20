package tripleo.elijah.comp.internal;

public interface DriverToken {
	static DriverToken makeToken(String s) {
		return new DriverToken() {
			@Override
			public String asString() {
				return s;
			}
		};
	}

	String asString();
}
