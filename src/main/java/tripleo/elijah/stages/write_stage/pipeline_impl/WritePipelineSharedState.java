package tripleo.elijah.stages.write_stage.pipeline_impl;

import com.google.common.collect.Multimap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.functionality.f203.F203;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.nextgen.output.NG_OutputItem;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.pipeline_impl.GenerateResultSink;
import tripleo.elijah.stages.generate.ElSystem;
import tripleo.util.buffer.Buffer;

import java.io.File;
import java.util.List;

/**
 * Really a record, but state is not all set at once
 */
public final class WritePipelineSharedState {
	public final File                                   base_dir;
	public       Compilation                            c;
	public       File                                   file_prefix;
	public       IPipelineAccess                        pa;
	public       GenerateResultSink                     grs;
	public       Multimap<CompilerInstructions, String> lsp_outputs;
	public       Multimap<String, Buffer>               mmb;
	public       List<NG_OutputItem>                    outputs;
	public       ElSystem                               sys;
	private      GenerateResult                         gr;

	public WritePipelineSharedState(final @NotNull IPipelineAccess pa0) {
		pa = pa0;
		c  = pa0.getCompilation();
		//
		base_dir = new F203(c.getErrSink(), c).chooseDirectory();

		pa.setBaseDir(base_dir);
	}

	@Contract(pure = true)
	public GenerateResult getGr() {
		return gr;
	}

	@Contract(mutates = "this")
	public void setGr(final @NotNull GenerateResult aGr) {
		gr = aGr;
	}
}
