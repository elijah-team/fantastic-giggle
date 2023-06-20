package tripleo.elijah.stages.deduce.pipeline_impl;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.i.CompilationEnclosure;
import tripleo.elijah.comp.i.IPipelineAccess;

import java.util.ArrayList;
import java.util.List;

public class DeducePipelineImpl {
	private final IPipelineAccess             pa;
	private final List<PipelineLogicRunnable> plrs = new ArrayList<>();

	public DeducePipelineImpl(final @NotNull IPipelineAccess pa0) {
		pa = pa0;

		final Compilation c = pa.getCompilation();

		addRunnable(new PL_AddModules(c.modules));

		//for (final OS_Module module : c.modules) {
		//	addRunnable(new PL_AddModule(module));
		//}

		addRunnable(new PL_EverythingBeforeGenerate());
		addRunnable(new PL_SaveGeneratedClasses(pa));
	}

	private void addRunnable(final PipelineLogicRunnable plr) {
		plrs.add(plr);
	}

	public void run() {
		final Compilation          c                    = pa.getCompilation();
		final CompilationEnclosure compilationEnclosure = c.getCompilationEnclosure();
		final PipelineLogic        pipelineLogic        = compilationEnclosure.getPipelineLogic();

		assert pipelineLogic != null;

		for (final PipelineLogicRunnable plr : plrs) {
			plr.run(pipelineLogic);
		}
	}
}
