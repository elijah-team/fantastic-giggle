package tripleo.elijah.comp.diagnostic;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.diagnostic.Locatable;

import java.io.PrintStream;
import java.util.List;

public class ExceptionDiagnostic implements Diagnostic {
	private final Exception e;

	public ExceptionDiagnostic(final Exception aE) {
		e = aE;
	}

	@Override
	public String code() {
		return "9003";
	}

	@Override
	public @NotNull Locatable primary() {
		return null;
	}

	@Override
	public void report(final PrintStream stream) {
		stream.println(code() + " Some exception " + e);
	}

	@Override
	public @NotNull List<Locatable> secondary() {
		return null;
	}

	@Override
	public Severity severity() {
		return Severity.ERROR;
	}
}
