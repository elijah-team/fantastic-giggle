/*  -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp.internal;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.i.*;
import tripleo.elijah.comp.notation.GN_Notable;
import tripleo.elijah.nextgen.output.NG_OutputItem;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_generic.pipeline_impl.GenerateResultSink;
import tripleo.elijah.stages.logging.ElLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CR_State {
	public ICompilationBus.CB_Action cur;
	public ProcessRecord pr;
	public RuntimeProcesses rt;
	public boolean started;
	ICompilationAccess ca;
	private CompilationRunner compilationRunner;

	@Contract(pure = true)
	public CR_State(ICompilationAccess aCa) {
		ca = aCa;
		ca.getCompilation().set_pa(new ProcessRecord_PipelineAccess()); // FIXME 05/28
		pr = new ProcessRecordImpl(ca);
	}

	public ICompilationAccess ca() {
		return ca;
	}

	public CompilationRunner runner() {
		return compilationRunner;
	}

	public void setRunner(CompilationRunner aCompilationRunner) {
		compilationRunner = aCompilationRunner;
	}

	public interface PipelinePlugin {
		PipelineMember instance(final @NotNull AccessBus ab0);

		String name();
	}

	public static class DeducePipelinePlugin implements PipelinePlugin {
		@Override
		public PipelineMember instance(final @NotNull AccessBus ab0) {
			return new DeducePipeline(ab0.getPipelineAccess());
		}

		@Override
		public String name() {
			return "DeducePipeline";
		}
	}

	public static class EvaPipelinePlugin implements PipelinePlugin {
		@Override
		public PipelineMember instance(final @NotNull AccessBus ab0) {
			return new EvaPipeline(ab0.getPipelineAccess());
		}

		@Override
		public String name() {
			return "EvaPipeline";
		}
	}

	private static class ProcessRecordImpl implements ProcessRecord {
		//private final DeducePipeline                             dpl;
		private final ICompilationAccess ca;
		private final IPipelineAccess    pa;
		private final PipelineLogic      pipelineLogic;
		private       AccessBus          ab;

		public ProcessRecordImpl(final @NotNull ICompilationAccess ca0) {
			ca = ca0;

			//ca.getCompilation().getCompilationEnclosure().getAccessBusPromise()
			//		.then(iab->ab=iab);
			ca.getCompilation().getCompilationEnclosure().getAccessBusPromise().then((final @NotNull AccessBus iab) -> {
				ab = iab;
			});

			pa = ca.getCompilation().get_pa();

			pipelineLogic = new PipelineLogic(pa, ca);
			//dpl           = new DeducePipeline(pa);

			//ca.getCompilation().getCompilationEnclosure().getAccessBusPromise().then((final @NotNull AccessBus iab) -> {
			//	ab  = iab;
			//	env = ab.env();
			//});
		}

		@Contract(pure = true)
		@Override
		public AccessBus ab() {
			return ab;
		}

		@Contract(pure = true)
		@Override
		public ICompilationAccess ca() {
			return ca;
		}

		@Contract(pure = true)
		@Override
		public IPipelineAccess pa() {
			return pa;
		}

		@Contract(pure = true)
		@Override
		public PipelineLogic pipelineLogic() {
			return pipelineLogic;
		}

		@Override
		public void writeLogs() {
			ca.getCompilation().cfg.stage.writeLogs(ca);
		}
	}

	public static class WriteMesonPipelinePlugin implements PipelinePlugin {
		@Override
		public PipelineMember instance(final @NotNull AccessBus ab0) {
			return new WriteMesonPipeline(ab0.getPipelineAccess());
		}

		@Override
		public String name() {
			return "WriteMesonPipeline";
		}
	}

	public static class WritePipelinePlugin implements PipelinePlugin {
		@Override
		public PipelineMember instance(final @NotNull AccessBus ab0) {
			return new WritePipeline(ab0.getPipelineAccess());
		}

		@Override
		public String name() {
			return "WritePipeline";
		}
	}

	class ProcessRecord_PipelineAccess implements IPipelineAccess {
		private final DeferredObject<EvaPipeline, Void, Void>   EvaPipelinePromise = new DeferredObject<>();
		private final DeferredObject<List<EvaNode>, Void, Void> nlp                = new DeferredObject<>();
		private final List<NG_OutputItem> outputs = new ArrayList<NG_OutputItem>();
		private       AccessBus                                 _ab;
		private       File                                      _base_dir;
		private       WritePipeline                             _wpl;
		private       GenerateResultSink                        grs;
		private       List<CompilerInput>                       inp;

		private final DeferredObject<PipelineLogic, Void, Void> ppl = new DeferredObject<>();

		@Override
		public void _setAccessBus(final AccessBus ab) {
			_ab = ab;
		}

		@Override
		public void addFunctionStatement(final EvaPipeline.FunctionStatement aFunctionStatement) {
			EvaPipelinePromise.then(gp -> {
				gp.addFunctionStatement(aFunctionStatement);
			});
		}

		@Override
		public void addLog(final ElLog aLOG) {
			getCompilationEnclosure().getPipelineLogic().addLog(aLOG);
		}

		@Override
		public void addOutput(final NG_OutputItem aOutput) {
			this.outputs.add(aOutput);
		}

		@Override
		public AccessBus getAccessBus() {
			return _ab;
		}

		@Override
		public File getBaseDir() {
			return _base_dir;
		}

		@Override
		public Compilation getCompilation() {
			return ca.getCompilation();
		}

		@Override
		public CompilationClosure getCompilationClosure() {
			return getCompilation().getCompilationClosure();
		}

		@Override
		public CompilationEnclosure getCompilationEnclosure() {
			return getCompilation().getCompilationEnclosure();
		}

		@Override
		public List<CompilerInput> getCompilerInput() {
			return getCompilationEnclosure().getCompilerInput();
		}

		@Override
		public GenerateResultSink getGenerateResultSink() {
			return grs;
		}

		@Override
		public DeducePipeline getDeducePipeline() {
			//	return getProcessRecord().dpl();
			throw new Error("237 dpl");
		}

		@Override
		public List<NG_OutputItem> getOutputs() {
			return outputs;
		}

		@Override
		public DeferredObject<PipelineLogic, Void, Void> getPipelineLogicPromise() {
			return ppl;
		}

		@Override
		public ProcessRecord getProcessRecord() {
			return pr;
		}

		@Override
		public WritePipeline getWitePipeline() {
			return _wpl;
		}

		@Override
		public void notate(final int provenance, final @NotNull GN_Notable aNotable) {
			aNotable.run();
		}

		@Override
		public PipelineLogic pipelineLogic() {
			return getProcessRecord().pipelineLogic();
		}

		@Override
		public void setBaseDir(final File aBaseDir) {
			_base_dir = aBaseDir;
		}

		@Override
		public void registerNodeList(final DoneCallback<List<EvaNode>> done) {
			nlp.then(done);
		}

		@Override
		public void setCompilerInput(final List<CompilerInput> aInputs) {
			getCompilationEnclosure().setCompilerInput(aInputs);
		}

		@Override
		public void setEvaPipeline(final @NotNull EvaPipeline agp) {
			EvaPipelinePromise.resolve(agp);
		}

		@Override
		public void setGenerateResultSink(final GenerateResultSink aGenerateResultSink) {
			grs = aGenerateResultSink;
		}

		@Override
		public void setNodeList(final List<EvaNode> aEvaNodeList) {
			nlp/*;)*/.resolve(aEvaNodeList);
		}

		@Override
		public void setWritePipeline(final WritePipeline aWritePipeline) {
			_wpl = aWritePipeline;
		}

		List<EvaNamespace> activeNamespaces = new ArrayList<>();
		List<BaseEvaFunction> activeFunctions = new ArrayList<BaseEvaFunction>();
		private List<EvaClass> activeClasses = new ArrayList<>();

		@Override
		public void activeFunction(final BaseEvaFunction aEvaFunction) {
			activeFunctions.add(aEvaFunction);
		}

		@Override
		public void activeClass(final EvaClass aEvaClass) {
			activeClasses.add(aEvaClass);
		}

		@Override
		public void activeNamespace(final EvaNamespace aEvaNamespace) {
			activeNamespaces.add(aEvaNamespace);
		}

		@Override
		public List<EvaNamespace> getActiveNamespaces() {
			return activeNamespaces;
		}

		@Override
		public List<BaseEvaFunction> getActiveFunctions() {
			return activeFunctions;
		}

		@Override
		public List<EvaClass> getActiveClasses() {
			return activeClasses;
		}
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
