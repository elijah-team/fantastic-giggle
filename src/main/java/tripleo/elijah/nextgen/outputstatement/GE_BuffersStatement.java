package tripleo.elijah.nextgen.outputstatement;

import org.jetbrains.annotations.Contract;
import tripleo.elijah.util.Helpers;
import tripleo.util.buffer.Buffer;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class GE_BuffersStatement implements EG_Statement {
	@Override
	public EX_Explanation getExplanation() {
		return new GE_BuffersExplanation(this);
	}

	private final Map.Entry<String, Collection<Buffer>> entry;

	@Contract(pure = true)
	public GE_BuffersStatement(final Map.Entry<String, Collection<Buffer>> aEntry) {
		entry = aEntry;
	}

	private static class GE_BuffersExplanation implements EX_Explanation {
		final         String              message = "buffers to statement";
		private final GE_BuffersStatement st;

		public GE_BuffersExplanation(final GE_BuffersStatement aGEBuffersStatement) {
			st = aGEBuffersStatement;
		}

		public String getText() {
			return message;
		}

		@Override
		public String message() {
			return "GE_BuffersExplanation";
		}
	}

	@Override
	public String getText() {
		return Helpers.String_join("\n\n", entry.getValue()
				.stream()
				//.filter(entry)
				.map(buffer -> buffer.getText())
				.collect(Collectors.toList()));
	}
}
