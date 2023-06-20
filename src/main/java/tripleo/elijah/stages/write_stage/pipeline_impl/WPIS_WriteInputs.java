package tripleo.elijah.stages.write_stage.pipeline_impl;

import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Operation;
import tripleo.elijah.comp.WritePipeline;
import tripleo.elijah.nextgen.outputstatement.EG_Naming;
import tripleo.elijah.nextgen.outputstatement.EG_SequenceStatement;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.nextgen.outputtree.EOT_OutputFile;
import tripleo.elijah.nextgen.outputtree.EOT_OutputTree;
import tripleo.elijah.nextgen.outputtree.EOT_OutputType;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.util.Helpers;
import tripleo.util.buffer.DefaultBuffer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tripleo.elijah.util.Helpers.List_of;

public class WPIS_WriteInputs implements WP_Indiviual_Step {
	private final WritePipeline writePipeline;

	private final Map<String, Operation<String>> ops = new HashMap<>();

	public enum XSRC {LIB, NULL, PREL, SRC}

	@Contract(pure = true)
	public WPIS_WriteInputs(final WritePipeline aWritePipeline) {
		writePipeline = aWritePipeline;
	}

	@Override
	public void act(final @NotNull WritePipelineSharedState st, final WP_State_Control sc) {
		// 3. write inputs
		// TODO ... 1/ output(s) per input and 2/ exceptions ... and 3/ plan
		//  "plan", effects; input(s), output(s)
		// TODO flag?
		try {
			final String        fn1           = new File(st.file_prefix, "inputs.txt").toString();
			final DefaultBuffer buf           = new DefaultBuffer("");

			final List<File>    recordedreads = st.c.getIO().recordedreads;

			for (final File file : recordedreads) {
				final String fn = file.toString();

				final Operation<String> op = writePipeline.append_hash(buf, fn);

				ops.put(fn, op);

				if (op.mode() == Mode.FAILURE) {
					break;
				}
			}

			String s = buf.getText();

			if (false) {
				Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fn1, true)));
				w.write(s);
				w.close();
			}

			final @NotNull EOT_OutputTree ot = st.c.getOutputTree();

			final List<Triple<String,XSRC,String>> yys = new ArrayList<>();

			{
				for (final File file : recordedreads) {
					final String fn = file.toString();

					final @NotNull Operation<String> op2 = Helpers.getHashForFilename(fn);

					if (op2.mode() == Mode.SUCCESS) {
						final String hh = op2.success();


						XSRC x;


						assert hh != null;

						// TODO EG_Statement here

						if (fn.equals("lib_elijjah/lib-c/Prelude.elijjah")) {
							x = XSRC.PREL;
						} else if (fn.startsWith("lib_elijjah/")) {
							x = XSRC.LIB;
						} else if (fn.startsWith("test/")) {
							x = XSRC.SRC;
						} else {
							throw new Error();
						}

						final Triple<String, XSRC, String> yy = Triple.of(hh, x, fn);
						yys.add(yy);
					}
				}
			}

			final EG_SequenceStatement seq = new EG_SequenceStatement(
					new EG_Naming("<<WPIS_WriteInputs>>"),
					List_of(
							EG_Statement.of(s, ()->"<<WPIS_WriteInputs>> >> statement")
						   )
			);

			final EOT_OutputFile off = new EOT_OutputFile(st.c, List_of(), fn1, EOT_OutputType.INPUTS, seq);

			off.x = yys;

			ot.add(off);
		} catch (IOException aE) {
			//throw new RuntimeException(aE);
			sc.exception(aE);
		}
	}
}
