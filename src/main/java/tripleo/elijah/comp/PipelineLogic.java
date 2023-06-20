/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.i.ICompilationAccess;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.comp.notation.GN_PL_Run2;
import tripleo.elijah.lang.i.OS_Module;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleList;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.logging.ElLog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created 12/30/20 2:14 AM
 */
public class PipelineLogic {
	public final  List<ElLog>         elLogs         = new LinkedList<ElLog>();
	public final  DeducePhase         dp;
	private final List<OS_Module>     __mods_BACKING = new ArrayList<OS_Module>();
	public final  GeneratePhase       generatePhase;
	private final EIT_ModuleList      mods           = new EIT_ModuleList(__mods_BACKING);
	private final IPipelineAccess     pa;
	public final  Observer<OS_Module> om             = new Observer<OS_Module>() {
		@Contract(value = "_ -> fail", pure = true)
		@Override
		public void onSubscribe(Disposable d) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Contract(value = "_ -> fail", pure = true)
		@Override
		public void onError(Throwable e) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void onNext(final @NotNull OS_Module mod) {
			//System.err.printf("7070 %s %d%n", mod.getFileName(), mod.entryPoints.size());

			pa.notate(7059, new GN_PL_Run2(PipelineLogic.this, mod));
		}

		@Override
		public void onComplete() {
			dp.finish();
		}
	};
	private final ElLog.Verbosity     verbosity;

	public PipelineLogic(final IPipelineAccess aPa, final @NotNull ICompilationAccess ca) {
		pa = aPa;

		ca.setPipelineLogic(this);
		verbosity     = ca.testSilence();
		generatePhase = new GeneratePhase(verbosity, pa, this);
		dp            = new DeducePhase(ca, pa, this);
	}

	public void addLog(ElLog aLog) {
		elLogs.add(aLog);
	}

	public void addModule(OS_Module m) {
		mods.add(m);
	}

	@NotNull
	public GenerateFunctions getGenerateFunctions(OS_Module mod) {
		return generatePhase.getGenerateFunctions(mod);
	}

	public ElLog.Verbosity getVerbosity() {
		return verbosity;
	}

	public EIT_ModuleList mods() {
		return mods;
	}

	public void resolveCheck(DeducePhase.@NotNull GeneratedClasses lgc) {
		for (final EvaNode evaNode : lgc) {
			if (evaNode instanceof EvaFunction) {

			} else if (evaNode instanceof EvaClass) {
//				final EvaClass generatedClass = (EvaClass) generatedNode;
//				for (EvaFunction generatedFunction : generatedClass.functionMap.values()) {
//					for (IdentTableEntry identTableEntry : generatedFunction.idte_list) {
//						final IdentIA ia2 = new IdentIA(identTableEntry.getIndex(), generatedFunction);
//						final String s = generatedFunction.getIdentIAPathNormal(ia2);
//						if (identTableEntry/*.isResolved()*/.getStatus() == BaseTableEntry.Status.KNOWN) {
////							GeneratedNode node = identTableEntry.resolved();
////							resolved_nodes.add(node);
//							tripleo.elijah.util.Stupidity.println_out_2("91 Resolved IDENT "+ s);
//						} else {
////							assert identTableEntry.getStatus() == BaseTableEntry.Status.UNKNOWN;
////							identTableEntry.setStatus(BaseTableEntry.Status.UNKNOWN, null);
//							tripleo.elijah.util.Stupidity.println_out_2("92 Unresolved IDENT "+ s);
//						}
//					}
//				}
			} else if (evaNode instanceof EvaNamespace) {
//				final EvaNamespace generatedNamespace = (EvaNamespace) generatedNode;
//				NamespaceStatement namespaceStatement = generatedNamespace.getNamespaceStatement();
//				for (EvaFunction generatedFunction : generatedNamespace.functionMap.values()) {
//					for (IdentTableEntry identTableEntry : generatedFunction.idte_list) {
//						if (identTableEntry.isResolved()) {
//							GeneratedNode node = identTableEntry.resolved();
//							resolved_nodes.add(node);
//						}
//					}
//				}
			}
		}
	}
}

//
//
//
