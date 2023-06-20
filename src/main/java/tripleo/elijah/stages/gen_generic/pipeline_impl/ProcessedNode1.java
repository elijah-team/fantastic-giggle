package tripleo.elijah.stages.gen_generic.pipeline_impl;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.i.OS_Module;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaContainerNC;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_generic.GenerateFiles;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.work.WorkManager;

import java.util.Collection;

public class ProcessedNode1 implements ProcessedNode {

	private final EvaNode evaNode;

	public ProcessedNode1(final EvaNode aEvaNode) {
		evaNode = aEvaNode;
	}

	public EvaNode getEvaNode() {
		return evaNode;
	}

	@Override
	public boolean isContainerNode() {
		return evaNode instanceof EvaContainerNC;
	}

	@Override
	public boolean matchModule(final OS_Module aMod) {
		return evaNode.module() == aMod;
	}

	@Override
	public void processClassMap(final GenerateFiles ggc, final GenerateResult gr, final GenerateResultSink aResultSink, final WorkManager wm) {
		final EvaContainerNC nc = (EvaContainerNC) evaNode;

		final @NotNull Collection<EvaNode> gn2 = GenerateFiles.classes_to_list_of_generated_nodes(nc.classMap.values());
		GenerateResult                     gr4 = ggc.generateCode(gn2, wm, aResultSink);
		gr.additional(gr4);
		aResultSink.additional(gr4);
	}

	@Override
	public void processConstructors(final GenerateFiles ggc, final GenerateResult gr, final GenerateResultSink aResultSink, final WorkManager wm) {
		final EvaContainerNC nc = (EvaContainerNC) evaNode;

		if (nc instanceof final EvaClass evaClass) {
			final @NotNull Collection<EvaNode> gn2 = GenerateFiles.constructors_to_list_of_generated_nodes(evaClass.constructors.values());
			GenerateResult                     gr3 = ggc.generateCode(gn2, wm, aResultSink);
			gr.additional(gr3);
			aResultSink.additional(gr3);
		}
/*
		if (nc instanceof final EvaNamespace evaNamespace) {
			final @NotNull Collection<EvaNode> gn2 = GenerateFiles.constructors_to_list_of_generated_nodes(evaNamespace.constructors.values());
			GenerateResult                     gr3 = ggc.generateCode(gn2, wm, aResultSink);
			gr.additional(gr3);
			aResultSink.additional(gr3);
		}
*/
	}

	@Override
	public void processContainer(final GenerateFiles ggc,
								 final GenerateResult gr,
								 final GenerateResultSink aResultSink) {
		final EvaContainerNC nc = (EvaContainerNC) evaNode;

		nc.generateCode(ggc, gr, aResultSink);
	}

	@Override
	public void processFunctions(final GenerateFiles ggc, final GenerateResult gr, final GenerateResultSink aResultSink, final WorkManager wm) {
		final EvaContainerNC nc = (EvaContainerNC) evaNode;

		final @NotNull Collection<EvaNode> gn1 = GenerateFiles.functions_to_list_of_generated_nodes(nc.functionMap.values());
		GenerateResult                     gr2 = ggc.generateCode(gn1, wm, aResultSink);
		gr.additional(gr2);
		aResultSink.additional(gr2);
	}
}
