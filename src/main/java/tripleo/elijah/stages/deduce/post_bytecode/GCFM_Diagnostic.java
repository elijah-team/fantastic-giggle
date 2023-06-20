package tripleo.elijah.stages.deduce.post_bytecode;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.diagnostic.Locatable;

import java.io.PrintStream;
import java.util.List;

public interface GCFM_Diagnostic extends Diagnostic {
	static  GCFM_Diagnostic forThis(final String aMessage, final String aCode, final Severity aSeverity) {
		return new GCFM_Diagnostic() {
			@Override
			public String _message() {
				return aMessage;
			}

			@Override
			public String code() {
				return aCode;
			}

			@Override
			public @NotNull Locatable primary() {
				return null;
			}

			@Override
			public void report(final PrintStream stream) {
				stream.printf("%s %s%n", code(), _message());
			}

			@Override
			public @NotNull List<Locatable> secondary() {
				return null;
			}

			@Override
			public Severity severity() {
				return aSeverity;
			}
		};
	}
	String _message();
}
