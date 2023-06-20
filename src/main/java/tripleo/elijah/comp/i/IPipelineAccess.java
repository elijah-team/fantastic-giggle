package tripleo.elijah.comp.i;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.internal.ProcessRecord;
import tripleo.elijah.comp.notation.GN_Notable;
import tripleo.elijah.nextgen.output.NG_OutputItem;
import tripleo.elijah.nextgen.reactive.Reactive;
import tripleo.elijah.nextgen.reactive.Reactive;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_generic.pipeline_impl.GenerateResultSink;
import tripleo.elijah.stages.logging.ElLog;

import java.io.File;
import java.util.List;

public interface IPipelineAccess {
	void _setAccessBus(AccessBus ab);

	void addFunctionStatement(EvaPipeline.FunctionStatement aFunctionStatement);

	void addLog(ElLog aLOG);

	void addOutput(NG_OutputItem aO);

	AccessBus getAccessBus();

	File getBaseDir();

	Compilation getCompilation();

	CompilationClosure getCompilationClosure();

	CompilationEnclosure getCompilationEnclosure();

	List<CompilerInput> getCompilerInput();

	void setCompilerInput(List<CompilerInput> aInputs);

	GenerateResultSink getGenerateResultSink();

	DeducePipeline getDeducePipeline();

	List<NG_OutputItem> getOutputs();

	DeferredObject/* Promise */<PipelineLogic, Void, Void> getPipelineLogicPromise();

	ProcessRecord getProcessRecord();

	WritePipeline getWitePipeline();

	void notate(int provenance, GN_Notable aNotable);

	PipelineLogic pipelineLogic();

	void setBaseDir(File aBaseDir);

	void registerNodeList(DoneCallback<List<EvaNode>> done);

	void setEvaPipeline(@NotNull EvaPipeline agp);

	void setGenerateResultSink(GenerateResultSink aGenerateResultSink);

	void setNodeList(List<EvaNode> aEvaNodeList);

	void setWritePipeline(WritePipeline aWritePipeline);

	default void addReactive(Reactive reactive) {
		getCompilationEnclosure().addReactive(reactive);
	}

	void activeFunction(BaseEvaFunction aEvaFunction);

	void activeClass(EvaClass aEvaClass);

	void activeNamespace(EvaNamespace aEvaNamespace);

	List<EvaNamespace> getActiveNamespaces();

	List<BaseEvaFunction> getActiveFunctions();

	List<EvaClass> getActiveClasses();
}
