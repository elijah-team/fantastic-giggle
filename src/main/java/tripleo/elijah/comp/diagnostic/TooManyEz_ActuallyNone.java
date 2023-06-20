package tripleo.elijah.comp.diagnostic;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.diagnostic.Locatable;

import java.io.PrintStream;
import java.util.List;

public class TooManyEz_ActuallyNone implements Diagnostic {
	final String message = "No .ez files found.";

	@Override
	public String code() {
		return "9999";
	}

	@Override
	public @NotNull Locatable primary() {
		return null;
	}

	@Override
	public void report(PrintStream stream) {
		stream.println(String.format("%s %s", code(), message));
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
