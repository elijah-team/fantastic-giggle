package tripleo.elijah.stages.write_stage.pipeline_impl;

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.WritePipeline;
import tripleo.elijah.nextgen.outputstatement.EG_SingleStatement;
import tripleo.elijah.nextgen.outputtree.EOT_OutputFile;
import tripleo.elijah.nextgen.outputtree.EOT_OutputType;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.Old_GenerateResultItem;
import tripleo.elijah.stages.write_stage.functionality.f301.WriteBufferText;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static tripleo.elijah.util.Helpers.List_of;

public class WPIS_WriteBuffers implements WP_Indiviual_Step {
	private final WritePipeline writePipeline;

	@Contract(pure = true)
	public WPIS_WriteBuffers(final WritePipeline aWritePipeline) {
		writePipeline = aWritePipeline;
	}

	@Override
	public void act(final @NotNull WritePipelineSharedState st, final WP_State_Control sc) {
		// 5. write buffers
		// TODO flag?
		try {
			System.out.println("77-34 " + st.file_prefix);
			st.file_prefix.mkdirs();

			debug_buffers(st, sc);
		} catch (FileNotFoundException aE) {
			sc.exception(aE);
		}
	}

	private void debug_buffers(final @NotNull WritePipelineSharedState st, final WP_State_Control aSc) throws FileNotFoundException {
		// TODO can/should this fail??

		final List<Old_GenerateResultItem> generateResultItems1 = st.getGr().results();

		var or = st.c.paths().outputRoot();

		final File file = or.subFile("buffers.txt").toFile();

		writePipeline.prom.then(new DoneCallback<GenerateResult>() {
			@Override
			public void onDone(final GenerateResult result) {
				WriteBufferText wbt = new WriteBufferText(st, aSc);
				//wbt.setFile(file);
				wbt.setResult(result);
				wbt.run();
			}
		});

		final File file1 = st.c.paths().outputRoot().subFile("buffers.txt").toFile();

		final EOT_OutputFile off1 = new EOT_OutputFile(st.c, List_of(), // !!
													   file1.toString(),
													   EOT_OutputType.BUFFERS,
													   new EG_SingleStatement("<<>>"));
		st.c.getOutputTree().add(off1);
	}
}
