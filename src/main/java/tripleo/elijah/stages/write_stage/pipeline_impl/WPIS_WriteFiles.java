package tripleo.elijah.stages.write_stage.pipeline_impl;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Operation;
import tripleo.elijah.comp.WritePipeline;
import tripleo.elijah.nextgen.outputtree.EOT_OutputFile;
import tripleo.elijah.stages.gen_generic.DoubleLatch;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultItem;
import tripleo.util.buffer.Buffer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WPIS_WriteFiles implements WP_Indiviual_Step {
	private final DoubleLatch<Triple<GenerateResult, @NotNull WritePipelineSharedState, @NotNull WP_State_Control>> dl;
	private       Operation<Boolean>                                                                                op;
	private final WritePipeline                                                                                     writePipeline;

	@Contract(pure = true)
	public WPIS_WriteFiles(final @NotNull WritePipeline aWritePipeline) {
		writePipeline = aWritePipeline;
		dl            = new DoubleLatch<Triple<GenerateResult, @NotNull WritePipelineSharedState, @NotNull WP_State_Control>>((t -> {
			hasGenerateResult(t.getLeft(), t.getMiddle(), t.getRight());
		}));
	}

	@Override
	public void act(final @NotNull WritePipelineSharedState st, final WP_State_Control sc) {
		// 4. write files

		//final List<GenerateResultItem> generateResultItems = st.getGr().results();

		// TODO make GenerateResult Stateful 06/16
		writePipeline.prom.then((final GenerateResult result) -> {
			// README call hasGenerateResult when latch set
			dl.notifyData(Triple.of(result, st, sc));
		});

		dl.notifyLatch(true);
	}

	private void hasGenerateResult(final @NotNull GenerateResult result,
								   final @NotNull WritePipelineSharedState st,
								   final @NotNull WP_State_Control sc) {
		final List<EOT_OutputFile> leof = new ArrayList<>();

		result.observe(new io.reactivex.rxjava3.core.Observer<GenerateResultItem>() {
			@Override
			public void onSubscribe(@NonNull final Disposable d) {
			}

			@Override
			public void onError(@NonNull final Throwable e) {
			}

			@Override
			public void onNext(@NonNull final GenerateResultItem aGenerateResultItem) {
				final String s = aGenerateResultItem.output();

				final Collection<Buffer> vs = st.mmb.get(s);

				final EOT_OutputFile eof = EOT_OutputFile.bufferSetToOutputFile(s, vs, st.c, aGenerateResultItem.node().module());
				leof.add(eof);
			}

			@Override
			public void onComplete() {
				st.c.getOutputTree().addAll(leof);

				final File fn1 = st.base_dir;

				System.err.println("mkdirs 77-79 " + fn1);
				fn1.mkdirs();

				op = Operation.success(true);
			}
		});

//		op = Operation.success(true); // README see #onComplete
	}
}
