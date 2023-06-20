package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.PipelineMember;
import tripleo.elijah.comp.Stages;
import tripleo.elijah.comp.i.ICompilationAccess;
import tripleo.elijah.comp.notation.GN_WriteLogs;
import tripleo.elijah.stages.deduce.IFunctionMapHook;
import tripleo.elijah.stages.logging.ElLog;

import java.util.List;

public class DefaultCompilationAccess implements ICompilationAccess {
	protected final Compilation                                compilation;

	@Contract(pure = true)
	public DefaultCompilationAccess(final Compilation aCompilation) {
		compilation = aCompilation;
	}

	@Override
	public void addFunctionMapHook(final IFunctionMapHook aFunctionMapHook) {
		compilation.getCompilationEnclosure().getPipelineLogic().dp.addFunctionMapHook(aFunctionMapHook);
	}

	@Override
	public void addPipeline(final PipelineMember pl) {
		compilation.addPipeline(pl);
	}

	@Override
	public List<IFunctionMapHook> functionMapHooks() {
		return compilation.getCompilationEnclosure().getPipelineLogic().dp.functionMapHooks;
	}

	@Override
	public Compilation getCompilation() {
		return compilation;
	}

	@Override
	public Stages getStage() {
		return Stages.O;
	}

	@Override
	public void setPipelineLogic(final PipelineLogic pl) {
		assert compilation.getCompilationEnclosure().getPipelineLogic() == null;
		compilation.getCompilationEnclosure().setPipelineLogic(pl);
	}

	@Override
	@NotNull
	public ElLog.Verbosity testSilence() {
		return compilation.cfg.silent ? ElLog.Verbosity.SILENT : ElLog.Verbosity.VERBOSE;
	}

	@Override
	public void writeLogs() {
		final PipelineLogic pipelineLogic = compilation.getCompilationEnclosure().getPipelineLogic();

		compilation.pa().notate(92, new GN_WriteLogs(this, pipelineLogic.elLogs));
	}
}
