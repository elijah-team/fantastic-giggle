package tripleo.elijah.comp.diagnostic;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.diagnostic.Locatable;
import tripleo.elijah.lang.i.OS_Module;
import tripleo.elijah.nextgen.query.Operation2;

import java.io.PrintStream;
import java.util.List;

public class UnknownExceptionDiagnostic implements Diagnostic {
	private final Operation2<OS_Module> m;

	public UnknownExceptionDiagnostic(final Operation2<OS_Module> aM) {
		m = aM;
	}

	@Override
	public String code() {
		return "9002";
	}

	@Override
	public @NotNull Locatable primary() {
		return null;
	}

	@Override
	public void report(final PrintStream stream) {
		stream.printf("%s Some error %s%n", code(), m.failure());
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
