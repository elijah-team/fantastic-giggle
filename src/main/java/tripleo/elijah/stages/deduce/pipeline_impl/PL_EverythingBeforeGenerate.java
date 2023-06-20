package tripleo.elijah.stages.deduce.pipeline_impl;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.*;

class PL_EverythingBeforeGenerate implements PipelineLogicRunnable {
	@Override
	public void run(final @NotNull PipelineLogic pipelineLogic) {
		//assert lgc.size() == 0;

		for (final OS_Module mod : pipelineLogic.mods().getMods()) {
			pipelineLogic.om.onNext(mod);
		}

		pipelineLogic.om.onComplete();
	}
}
