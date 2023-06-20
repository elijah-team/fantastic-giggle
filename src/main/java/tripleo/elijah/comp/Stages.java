package tripleo.elijah.comp;

import org.jetbrains.annotations.Contract;
import tripleo.elijah.comp.i.ICompilationAccess;
import tripleo.elijah.comp.i.RuntimeProcess;
import tripleo.elijah.comp.internal.OStageProcess;
import tripleo.elijah.comp.internal.ProcessRecord;
import tripleo.elijah.util.NotImplementedException;

public enum Stages {
	D("D") {
		@Override
		public RuntimeProcess getProcess(final ICompilationAccess aCa, final ProcessRecord aPr) {
			return new DStageProcess(aCa, aPr);
		}

		@Override
		public void writeLogs(final ICompilationAccess aCompilationAccess) {
			aCompilationAccess.writeLogs();
		}
	},
	E("E") {
		@Override
		public RuntimeProcess getProcess(final ICompilationAccess aCa, final ProcessRecord aPr) {
			return new EmptyProcess(aCa, aPr);
		}

		@Override
		public void writeLogs(final ICompilationAccess aCompilationAccess) {
			NotImplementedException.raise();
		}
	},
	O("O") {
		@Override
		public RuntimeProcess getProcess(final ICompilationAccess aCa, final ProcessRecord aPr) {
			return new OStageProcess(aCa, aPr);
		}

		@Override
		public void writeLogs(final ICompilationAccess aCompilationAccess) {
			aCompilationAccess.writeLogs();
		}
	},  // Output,  // ??
	S("S") {
		@Override
		public RuntimeProcess getProcess(final ICompilationAccess aCa, final ProcessRecord aPr) {
			throw new NotImplementedException();
		}

		@Override
		public void writeLogs(final ICompilationAccess aCompilationAccess) {
			aCompilationAccess.writeLogs();
		}
	};

	private final String s;

	@Contract(pure = true)
	Stages(final String aO) {
		s = aO;
	}

	public abstract RuntimeProcess getProcess(final ICompilationAccess aCa, final ProcessRecord aPr);

	public abstract void writeLogs(final ICompilationAccess aCompilationAccess);
}
