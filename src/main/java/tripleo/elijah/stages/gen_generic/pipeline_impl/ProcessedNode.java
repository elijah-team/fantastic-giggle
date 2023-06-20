package tripleo.elijah.stages.gen_generic.pipeline_impl;

import tripleo.elijah.lang.i.OS_Module;
import tripleo.elijah.stages.gen_generic.GenerateFiles;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.work.WorkManager;

public interface ProcessedNode {
	boolean isContainerNode();

	boolean matchModule(OS_Module aMod);

	void processClassMap(GenerateFiles ggc,
						 GenerateResult gr,
						 GenerateResultSink aResultSink, final WorkManager wm);

	void processConstructors(GenerateFiles ggc,
							 GenerateResult gr,
							 GenerateResultSink aResultSink, final WorkManager wm);

	void processContainer(GenerateFiles ggc,
						  GenerateResult gr,
						  GenerateResultSink aResultSink);

	void processFunctions(GenerateFiles ggc,
						  GenerateResult gr,
						  GenerateResultSink aResultSink, final WorkManager wm);
}
