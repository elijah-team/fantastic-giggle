package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Operation;
import tripleo.elijah.comp.Stages;
import tripleo.elijah.comp.i.CR_Action;
import tripleo.elijah.comp.i.ICompilationAccess;
import tripleo.elijah.comp.i.IPipelineAccess;

public class CR_RunBetterAction implements CR_Action {
	public enum StageToRuntime {
		;

		public static @NotNull RuntimeProcesses get(final @NotNull IPipelineAccess aPa) {
			final ICompilationAccess ca = aPa.getCompilationEnclosure().getCompilationAccess();
			return get(ca.getStage(), ca, aPa);
		}

		@Contract("_, _, _, _ -> new")
		@NotNull
		public static RuntimeProcesses get(final @NotNull Stages stage,
										   final @NotNull ICompilationAccess ca,
										   final @NotNull IPipelineAccess aPa) {
			final ProcessRecord    processRecord = aPa.getProcessRecord();
			final RuntimeProcesses r             = new RuntimeProcesses(ca, processRecord);

			r.add(stage.getProcess(ca, processRecord));

			return r;
		}
	}

	@Override
	public void attach(final CompilationRunner cr) {

	}

	@Override
	public Operation<Boolean> execute(final CR_State st, final CB_Output aO) {
		try {
			final ICompilationAccess ca = st.ca();

			st.rt = StageToRuntime.get(ca.getCompilation().pa());
			st.rt.run_better();

			return Operation.success(true);
		} catch (final Exception aE) {
			return Operation.failure(aE);
		}
	}

	@Override
	public String name() {
		return "run better";
	}
}
