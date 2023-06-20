package tripleo.elijah.stages.write_stage.pipeline_impl;

public class SPrintStream implements XPrintStream {
	private final StringBuilder sb = new StringBuilder();

	public String getString() {
		return sb.toString();
	}

	@Override
	public void println(final String aS) {
		sb.append(aS);
		sb.append('\n');
	}
}
