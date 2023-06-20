package tripleo.elijah.comp.diagnostic;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.diagnostic.Locatable;

import java.io.PrintStream;
import java.util.List;

public class TooManyEz_BeSpecific implements Diagnostic {
	final String message = "Too many .ez files, be specific.";

	@Override
	public String code() {
		return "9997";
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
