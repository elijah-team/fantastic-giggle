package tripleo.elijah.comp.i;

import java.util.List;

public interface ICompilationBus {
	void add(CB_Action aCBAction);

	interface CB_Process {
//		void execute();

		List<CB_Action> steps();
	}

	class COutputString implements OutputString {

		private final String _text;

		public COutputString(final String aText) {
			_text = aText;
		}

		@Override
		public String getText() {
			return _text;
		}
	}

	void add(CB_Process aProcess);

	void inst(ILazyCompilerInstructions aLazyCompilerInstructions);

	void option(CompilationChange aChange);

	List<CB_Process> processes();

	interface CB_Action {
		void execute();

		String name();

		List<OutputString> outputStrings();

	}

	interface OutputString {
		String getText();
	}
}
