/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import org.apache.commons.lang3.tuple.Pair;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.AccessBus.AB_GenerateResultListener;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.stages.gen_c.CDependencyRef;
import tripleo.elijah.stages.gen_c.OutputFileC;
import tripleo.elijah.stages.gen_generic.*;
import tripleo.elijah.stages.generate.ElSystem;
import tripleo.elijah.stages.generate.OutputStrategy;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.stages.write_stage.pipeline_impl.*;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;
import tripleo.util.buffer.TextBuffer;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static tripleo.elijah.util.Helpers.List_of;

/**
 * Created 8/21/21 10:19 PM
 */
public class WritePipeline implements PipelineMember, Consumer<Supplier<GenerateResult>>, AB_GenerateResultListener {
	public final  DeferredObject<GenerateResult, Void, Void>                               prom = new DeferredObject<>();

	private final CompletedItemsHandler                                                    cih;
	private final DoubleLatch<GenerateResult>                                              latch;
	private       HashMap<WP_Indiviual_Step, Pair<WP_Flow.FlowStatus, Operation<Boolean>>> ops;
	public final WritePipelineSharedState st;

	@Override
	public void accept(final @NotNull Supplier<GenerateResult> aGenerateResultSupplier) {
		final GenerateResult gr = aGenerateResultSupplier.get();
		int                  y  = 2;
	}

	public WritePipeline(final @NotNull IPipelineAccess pa) {
		st = new WritePipelineSharedState(pa);

		// computed
		st.file_prefix = st.base_dir; //new File("COMP", st.c.getCompilationNumberString());

		// created
		latch = new DoubleLatch<GenerateResult>(gr -> {
			st.setGr(gr);

			final WP_Indiviual_Step wpis_go = new WPIS_GenerateOutputs();
			final WP_Indiviual_Step wpis_mk = new WPIS_MakeOutputDirectory();
			final WP_Indiviual_Step wpis_wi = new WPIS_WriteInputs(this);
			final WP_Indiviual_Step wpis_wf = new WPIS_WriteFiles(this);
			final WP_Indiviual_Step wpis_wb = new WPIS_WriteBuffers(this);
			final WP_Indiviual_Step wpis_ot = new WPIS_WriteOutputTree();

			// TODO: Do something with op, like set in {@code pa} to proceed to next pipeline
			final WP_Flow f = new WP_Flow(this, List_of(wpis_go, wpis_mk, wpis_wi, wpis_wf, wpis_wb, wpis_ot));
			// TODO WP_FlowMember?
			// TODO each IndividualStep may return an op?
			//  - with type or Boolean?
			//  - are we modeing effects here?
			ops = f.act();
		});

		// state
		st.mmb         = ArrayListMultimap.create();
		st.lsp_outputs = ArrayListMultimap.create();
		st.grs         = pa.getGenerateResultSink();

		// ??
		st.sys = new ElSystem(false, st.c, this::createOutputStratgy);

		cih = new CompletedItemsHandler(st);

		pa.getAccessBus().subscribe_GenerateResult(this::gr_slot);
		pa.getAccessBus().subscribe_GenerateResult(prom::resolve);

		pa.setWritePipeline(this);

		st.outputs = pa.getOutputs();
	}

	OutputStrategy createOutputStratgy() {
		final OutputStrategy os = new OutputStrategy();
		os.per(OutputStrategy.Per.PER_CLASS); // TODO this needs to be configured per lsp

		return os;
	}

	public Operation<String> append_hash(TextBuffer outputBuffer, String aFilename) {
		final @NotNull Operation<String> hh2 = Helpers.getHashForFilename(aFilename);

		if (hh2.mode() == Mode.SUCCESS) {
			final String hh = hh2.success();

			assert hh != null;

			// TODO EG_Statement here

			outputBuffer.append(hh);
			outputBuffer.append(" ");
			outputBuffer.append_ln(aFilename);
		}

		return hh2;
	}

	public Consumer<Supplier<GenerateResult>> consumer() {
		if (false) {
			return new Consumer<Supplier<GenerateResult>>() {
				@Override
				public void accept(final Supplier<GenerateResult> aGenerateResultSupplier) {
					// final GenerateResult gr = aGenerateResultSupplier.get();
				}
			};
		}

		return (x) -> {
		};
	}

	@Override
	public void gr_slot(final @NotNull GenerateResult gr1) {
		Objects.requireNonNull(gr1);
		latch.notifyData(gr1);
		gr1.subscribeCompletedItems(cih.observer());
	}

	@Override
	public void run() throws Exception {
		latch.notifyLatch(true);
	}

	private static class CompletedItemsHandler {
		// README debugging purposes
		private final List<GenerateResultItem>                 abs  = new ArrayList<>();
		private final Multimap<Dependency, GenerateResultItem> gris = ArrayListMultimap.create();
		private final ElLog                                    LOG;
		private final WritePipelineSharedState                 sharedState;
		private       Observer<GenerateResultItem>             observer;

		public CompletedItemsHandler(final WritePipelineSharedState aSharedState) {
			sharedState = aSharedState;

			final ElLog.Verbosity verbosity = sharedState.c.cfg.silent ? ElLog.Verbosity.SILENT : ElLog.Verbosity.VERBOSE;

			LOG = new ElLog("(WRITE-PIPELINE)", verbosity, "(write-pipeline)");

			sharedState.pa.addLog(LOG);
		}

		@Contract(mutates = "this")
		public Observer<GenerateResultItem> observer() {
			if (observer == null) {
				observer = new Observer<GenerateResultItem>() {
					@Override
					public void onSubscribe(@NonNull Disposable d) {
					}

					@Override
					public void onNext(@NonNull GenerateResultItem ab) {
						addItem(ab);
					}

					@Override
					public void onError(@NonNull Throwable e) {
					}

					@Override
					public void onComplete() {
						completeSequence();
					}
				};
			}

			return observer;
		}

		public void completeSequence() {
			final @NotNull GenerateResult generateResult = sharedState.getGr();

			generateResult.outputFiles((final Map<String, OutputFileC> outputFiles) -> {
				System.err.println("252252"); // 06/16
			});
		}

		public void addItem(final @NotNull GenerateResultItem ab) {
			NotImplementedException.raise();

			// README debugging purposes
			abs.add(ab);

			LOG.info("-----------=-----------=-----------=-----------=-----------=-----------");
			LOG.info("GenerateResultItem >> " + ab.jsonString());
			LOG.info("abs.size >> " + abs.size());

			final Dependency dependency = ab.getDependency();

			LOG.info("ab.getDependency >> " + dependency.jsonString());

			// README debugging purposes
			final DependencyRef dependencyRef = dependency.getRef();

			LOG.info("dependencyRef >> " + (dependencyRef != null ? dependencyRef.jsonString() : "null"));

			if (dependencyRef == null) {
				gris.put(dependency, ab);
			} else {
				final String output = ((CDependencyRef) dependencyRef).getHeaderFile();

				LOG.info("CDependencyRef.getHeaderFile >> " + output);

				sharedState.mmb.put(output, ab.buffer());
				sharedState.lsp_outputs.put(ab.lsp().getInstructions(), output);
				for (GenerateResultItem generateResultItem : gris.get(dependency)) {
					final String output1 = generateResultItem.output();
					sharedState.mmb.put(output1, generateResultItem.buffer());
					sharedState.lsp_outputs.put(generateResultItem.lsp().getInstructions(), output1);
				}

				//for (Map.Entry<Dependency, Collection<GenerateResultItem>> entry : gris.asMap().entrySet()) {
				//	System.out.println(entry.getKey().jsonString());
				//	System.out.println(entry.getValue());
				//}

				if (gris.containsKey(dependency))
					System.out.println("*** 235 yes");
				else
					System.out.println("*** 235 no");

				gris.removeAll(dependency);
			}

			LOG.info("-----------=-----------=-----------=-----------=-----------=-----------");
		}
	}
}

//
//
//
