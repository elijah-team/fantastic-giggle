package tripleo.elijah.comp.notation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.lang.i.OS_Module;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;

import java.util.ArrayList;
import java.util.List;

public class GN_PL_Run2 implements GN_Notable {
	private final @NotNull OS_Module     mod;
	private final          PipelineLogic pipelineLogic;

	@Contract(pure = true)
	public GN_PL_Run2(final PipelineLogic aPipelineLogic, final @NotNull OS_Module aMod) {
		pipelineLogic = aPipelineLogic;
		mod           = aMod;
	}

	@Override
	public void run() {
		final GenerateFunctions gfm         = pipelineLogic.getGenerateFunctions(mod);
		final DeducePhase       deducePhase = pipelineLogic.dp;

		final DefaultClassGenerator dcg = new DefaultClassGenerator(deducePhase);
		gfm.generateFromEntryPoints(mod.entryPoints(), dcg);

//		WorkManager wm = new WorkManager();
//		WorkList wl = new WorkList();

		DeducePhase.@NotNull GeneratedClasses lgc            = deducePhase.generatedClasses;
		List<EvaNode>                         resolved_nodes = new ArrayList<EvaNode>();

		//assert lgc.copy().size() >0;

		final ICodeRegistrar cr = dcg.getCodeRegistrar();

		for (final EvaNode evaNode : lgc) {
			if (!(evaNode instanceof final GNCoded coded)) {
				throw new IllegalStateException("node must be coded");
			}

			switch (coded.getRole()) {
			case FUNCTION: {
//				EvaFunction generatedFunction = (EvaFunction) generatedNode;
//				if (coded.getCode() == 0)
//					coded.setCode(mod.getCompilation().nextFunctionCode());

				cr.registerFunction1((BaseEvaFunction) evaNode);
				break;
			}
			case CLASS: {
				final EvaClass evaClass = (EvaClass) evaNode;

				//assert (evaClass.getCode() != 0);
				if (evaClass.getCode() == 0) {
					cr.registerClass1(evaClass);
				}

//					if (generatedClass.getCode() == 0)
//						generatedClass.setCode(mod.getCompilation().nextClassCode());
				for (EvaClass evaClass2 : evaClass.classMap.values()) {
					if (evaClass2.getCode() == 0) {
						//evaClass2.setCode(mod.getCompilation().nextClassCode());
						cr.registerClass1(evaClass2);
					}
				}
				for (EvaFunction generatedFunction : evaClass.functionMap.values()) {
					for (IdentTableEntry identTableEntry : generatedFunction.idte_list) {
						if (identTableEntry.isResolved()) {
							EvaNode node = identTableEntry.resolvedType();
							resolved_nodes.add(node);
						}
					}
				}
				break;
			}
			case NAMESPACE: {
				final EvaNamespace evaNamespace = (EvaNamespace) evaNode;
				if (coded.getCode() == 0) {
					//coded.setCode(mod.getCompilation().nextClassCode());
					cr.registerNamespace(evaNamespace);
					pipelineLogic.generatePhase.codeRegistrarP(acr -> acr.registerNamespace(evaNamespace));
				}
				for (EvaClass evaClass3 : evaNamespace.classMap.values()) {
					if (evaClass3.getCode() == 0) {
						//evaClass.setCode(mod.getCompilation().nextClassCode());
						cr.registerClass1(evaClass3);
					}
				}
				for (EvaFunction generatedFunction : evaNamespace.functionMap.values()) {
					for (IdentTableEntry identTableEntry : generatedFunction.idte_list) {
						if (identTableEntry.isResolved()) {
							EvaNode node = identTableEntry.resolvedType();
							resolved_nodes.add(node);
						}
					}
				}
				break;
			}
			default:
				throw new IllegalStateException("Unexpected value: " + coded.getRole());
			}
		}

		for (final EvaNode evaNode : resolved_nodes) {
			if (!(evaNode instanceof GNCoded)) {
				throw new IllegalStateException("node is not coded");
			}

			final GNCoded coded = (GNCoded) evaNode;

			if (coded.getCode() == 0) {
				switch (coded.getRole()) {
				case FUNCTION:
					cr.registerFunction1((BaseEvaFunction) coded);
					break;
				case NAMESPACE:
					cr.registerNamespace((EvaNamespace) coded);
					break;
				case CLASS:
					cr.registerClass1((EvaClass) coded);
					break;
				default:
					throw new IllegalStateException("Invalid coded role");
				}
			}
		}

		deducePhase.deduceModule(mod, lgc, pipelineLogic.getVerbosity());

		pipelineLogic.resolveCheck(lgc);

//		for (final GeneratedNode gn : lgf) {
//			if (gn instanceof EvaFunction) {
//				EvaFunction gf = (EvaFunction) gn;
//				System.out.println("----------------------------------------------------------");
//				System.out.println(gf.name());
//				System.out.println("----------------------------------------------------------");
//				EvaFunction.printTables(gf);
//				System.out.println("----------------------------------------------------------");
//			}
//		}

	}
}
