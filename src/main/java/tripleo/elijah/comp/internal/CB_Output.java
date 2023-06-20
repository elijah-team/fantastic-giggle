package tripleo.elijah.comp.internal;

import tripleo.elijah.comp.i.ICompilationBus;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class CB_Output {
	private final List<ICompilationBus.OutputString> x = new ArrayList<>();

	public List<ICompilationBus.OutputString> get() {
		return x;
	}

	public void logProgress(final int number, final String text) {
		if (number == 130) return;

//		System.err.println
		print(MessageFormat.format("{0} {1}", number, text));
	}

	void print(final String s) {
		x.add(() -> s);
	}
}
