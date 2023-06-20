package tripleo.elijah.stages.write_stage.pipeline_impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.nextgen.inputtree.EIT_Input;
import tripleo.elijah.nextgen.output.*;
import tripleo.elijah.nextgen.outputstatement.EG_Naming;
import tripleo.elijah.nextgen.outputstatement.EG_SequenceStatement;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.nextgen.outputstatement.EX_Explanation;
import tripleo.elijah.nextgen.outputtree.EOT_OutputFile;
import tripleo.elijah.nextgen.outputtree.EOT_OutputType;
import tripleo.elijah.stages.garish.GarishClass;
import tripleo.elijah.stages.garish.GarishNamespace;
import tripleo.elijah.stages.gen_c.GenC_FileGen;
import tripleo.elijah.stages.gen_c.GenerateC;
import tripleo.elijah.stages.gen_c.Generate_Code_For_Method;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_generic.GenerateFiles;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.pipeline_impl.GenerateResultSink;
import tripleo.elijah.stages.generate.OutputStrategy;
import tripleo.elijah.stages.generate.OutputStrategyC;
import tripleo.elijah.work.WorkList;
import tripleo.elijah.work.WorkManager;
import tripleo.elijah.world.i.LivingClass;
import tripleo.elijah.world.i.LivingNamespace;
import tripleo.util.buffer.Buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static tripleo.elijah.util.Helpers.List_of;

public class WPIS_GenerateOutputs implements WP_Indiviual_Step {
	private final WPIS_GenerateOutputs_Behavior_PrintDBLString printDBLString;
	private List<NG_OutputRequest> ors = new ArrayList<>();

	private void addOutputItems(final @NotNull WritePipelineSharedState st) {
		final Multimap<String, EG_Statement> mfss = ArrayListMultimap.create();
		var                                  cot  = st.c.getOutputTree();

		// README separate output requests into file requests
		for (NG_OutputRequest or : ors) {
			mfss.put(or.fileName(), or.statement());
		}

		for (Map.Entry<String, Collection<EG_Statement>> entry : mfss.asMap().entrySet()) {
			var seq = new EG_SequenceStatement(new EG_Naming("combined-file"), entry.getValue().stream().toList());
			// TODO inputs (dependencies?)
			var off = new EOT_OutputFile(st.c, List_of(), entry.getKey(), EOT_OutputType.SOURCES2, seq);
			cot.add(off);
		}
	}

	@FunctionalInterface
	public interface WPIS_GenerateOutputs_Behavior_PrintDBLString {
		void print(String sps);
	}

	@Contract(pure = true)
	public WPIS_GenerateOutputs() {
		// 1. GenerateOutputs with ElSystem
		printDBLString = new Default_WPIS_GenerateOutputs_Behavior_PrintDBLString();
	}

	@Contract(pure = true)
	public WPIS_GenerateOutputs(final @NotNull WPIS_GenerateOutputs.WPIS_GenerateOutputs_Behavior_PrintDBLString aPrintDBLString) {
		// 1. GenerateOutputs with ElSystem
		printDBLString = aPrintDBLString;
	}

	boolean _b;

	@Override
	public void act(final @NotNull WritePipelineSharedState st, final WP_State_Control sc) {
		if (_b) throw new Error();
		Preconditions.checkState(st.getGr() != null);
		Preconditions.checkState(st.sys != null);

		GenerateResult result = st.getGr();

		final SPrintStream sps = new SPrintStream();

		DebugBuffersLogic.debug_buffers_logic(result, sps);

		printDBLString.print(sps.getString());

		new Default_WPIS_GenerateOutputs_Behavior_PrintDBLString().print(sps.getString());

		st.sys.generateOutputs(result);

		//ngGenerate(st);

		var cs = st.pa.getActiveClasses();
		var ns = st.pa.getActiveNamespaces();
		var fs = st.pa.getActiveFunctions();

		{
			final OutputStrategy  osg             = st.sys.outputStrategyCreator.get();
			final OutputStrategyC outputStrategyC = new OutputStrategyC(osg);

			List<NG_OutputItem>    itms = new ArrayList<>();
			List<NG_OutputRequest> ors1 = new ArrayList<>();

			{
				for (EvaClass c : cs) {
					var mod = c.module();

					final Compilation compilation = mod.getCompilation();

					var errSink   = compilation.getErrSink();
					var verbosity = compilation.getCompilationEnclosure().getCompilationAccess().testSilence();
					var ce        = compilation.getCompilationEnclosure();

					var gc = new GenerateC(mod, errSink, verbosity, ce);

					var oc = new NG_OutputClass();
					oc.setClass(compilation._repo.getClass(c).getGarish(), gc);
					itms.add(oc);
				}
				for (BaseEvaFunction f : fs) {
					var mod = f.module();

					final Compilation compilation = mod.getCompilation();

					var errSink   = compilation.getErrSink();
					var verbosity = compilation.getCompilationEnclosure().getCompilationAccess().testSilence();
					var ce        = compilation.getCompilationEnclosure();

					var gc = new GenerateC(mod, errSink, verbosity, ce);

					var of = new NG_OutputFunction();

					var fileGen = new GenC_FileGen(new MyGenerateResultSink(of),
												   result,
												   new WorkManager(),
												   new WorkList(),
												   gc);

					gc.generateCodeForMethod(fileGen, f);

					//of.setFunction(f, gc, List_of());
					itms.add(of);
				}
				for (EvaNamespace n : ns) {
					var mod = n.module();

					final Compilation compilation = mod.getCompilation();

					var errSink   = compilation.getErrSink();
					var verbosity = compilation.getCompilationEnclosure().getCompilationAccess().testSilence();
					var ce        = compilation.getCompilationEnclosure();

					var gc = new GenerateC(mod, errSink, verbosity, ce);

					var on = new NG_OutputNamespace();
					on.setNamespace(compilation._repo.getNamespace(n).getGarish(), gc);
					itms.add(on);
				}
			}

			{
				for (NG_OutputItem o : itms) {
					var oxs = o.getOutputs();
					for (Pair<GenerateResult.TY, String> ox : oxs) {
						GenerateResult.TY oxt = ox.getLeft();
						String            oxb = ox.getRight();

						var s = o.outName(outputStrategyC, oxt);

						var or = new NG_OutputRequest(s, EG_Statement.of(oxb, EX_Explanation.withMessage("NG output class")));
						ors1.add(or);
					}
				}
			}

			{
				final Multimap<String, EG_Statement> mfss = ArrayListMultimap.create();
				var                                  cot  = st.c.getOutputTree();

				// README combine output requests into file requests
				for (NG_OutputRequest or : ors1) {
					mfss.put(or.fileName(), or.statement());
				}

				final List<Writable> writables = new ArrayList<>();

				for (Map.Entry<String, Collection<EG_Statement>> entry : mfss.asMap().entrySet()) {
					writables.add(new Writable() {
						@Override
						public String filename() {
							return entry.getKey();
						}

						@Override
						public EG_Statement statement() {
							return new EG_SequenceStatement(new EG_Naming("writable-combined-file"), entry.getValue().stream().toList());
						}

						@Override
						public List<EIT_Input> inputs() {
							return List_of();
						}
					});
				}

				for (Writable writable : writables) {
					var off = new EOT_OutputFile(st.c, writable.inputs(), writable.filename(), EOT_OutputType.SOURCES2, writable.statement());
					cot.add(off);
				}
			}
		}
		_b = true;
	}

	interface Writable {
		String filename();

		EG_Statement statement();

		List<EIT_Input> inputs();
	}

	static class Default_WPIS_GenerateOutputs_Behavior_PrintDBLString implements WPIS_GenerateOutputs_Behavior_PrintDBLString {
		@Override
		public void print(final String sps) {
			System.err.println(sps);
		}
	}

	private void ngGenerate(final @NotNull WritePipelineSharedState st) {
		final OutputStrategy  osg             = st.sys.outputStrategyCreator.get();
		final OutputStrategyC outputStrategyC = new OutputStrategyC(osg);

		processOutputItems(outputStrategyC, st.outputs);
		addOutputItems(st);
	}

	private void processOutputItems(final OutputStrategyC outputStrategyC, final List<NG_OutputItem> os) {
		for (NG_OutputItem o : os) {
			var oxs = o.getOutputs();
			for (Pair<GenerateResult.TY, String> ox : oxs) {
				GenerateResult.TY oxt = ox.getLeft();
				String            oxb = ox.getRight();

				var s = o.outName(outputStrategyC, oxt);

				//System.err.println("5152 " + s);

				var or = new NG_OutputRequest(s, EG_Statement.of(oxb, EX_Explanation.withMessage("NG output class")));
				this.ors.add(or);
			}
		}
	}

	private static class MyGenerateResultSink implements GenerateResultSink {
		private final NG_OutputFunction of;

		public MyGenerateResultSink(final NG_OutputFunction aOf) {
			of = aOf;
		}

		@Override
		public void add(final EvaNode node) {
			throw new Error();
		}

		@Override
		public void addClass_0(final GarishClass aGarishClass, final Buffer aImplBuffer, final Buffer aHeaderBuffer) {
			throw new Error();
		}

		@Override
		public void addClass_1(final GarishClass aGarishClass, final GenerateResult aGenerateResult, final GenerateC aGenerateC) {
			throw new Error();
		}

		@Override
		public void addFunction(final BaseEvaFunction aGf, final List<Generate_Code_For_Method.C2C_Result> aRs, final GenerateFiles aGenerateFiles) {
			of.setFunction(aGf, aGenerateFiles, aRs);
			//throw new Error();
		}

		@Override
		public void additional(final GenerateResult aGenerateResult) {
			throw new Error();
		}

		@Override
		public void addNamespace_0(final GarishNamespace aLivingNamespace, final Buffer aImplBuffer, final Buffer aHeaderBuffer) {
			throw new Error();
		}

		@Override
		public void addNamespace_1(final GarishNamespace aGarishNamespace, final GenerateResult aGenerateResult, final GenerateC aGenerateC) {
			throw new Error();
		}

		@Override
		public LivingClass getLivingClassForEva(final EvaClass aEvaClass) {
			return null;
		}

		@Override
		public LivingNamespace getLivingNamespaceForEva(final EvaNamespace aEvaClass) {
			return null;
		}
	}
}
