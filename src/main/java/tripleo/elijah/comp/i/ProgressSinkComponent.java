package tripleo.elijah.comp.i;

public enum ProgressSinkComponent {
	CCI {
		@Override
		public boolean isPrintErr(final int aCode, final int aType) {
			return aCode == 131 && aType == -1;
		}

		@Override
		public String printErr(final int aCode, final int aType, final Object[] aParams) {
			return "*** CCI ->> " + aParams[0]; // ci.getName
		}
	};

	public abstract boolean isPrintErr(final int aCode, final int aType);

	public abstract String printErr(final int aCode, final int aType, final Object[] aParams);
}
