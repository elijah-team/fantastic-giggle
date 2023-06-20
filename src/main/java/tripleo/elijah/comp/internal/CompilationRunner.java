package tripleo.elijah.comp.internal;

import antlr.ANTLRException;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.CompilerInput;
import tripleo.elijah.comp.IO;
import tripleo.elijah.comp.Operation;
import tripleo.elijah.comp.i.*;
import tripleo.elijah.comp.queries.QueryEzFileToModule;
import tripleo.elijah.comp.queries.QueryEzFileToModuleParams;
import tripleo.elijah.stateful.DefaultStateful;
import tripleo.elijah.stateful.State;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.Stupidity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static tripleo.elijah.nextgen.query.Mode.FAILURE;
import static tripleo.elijah.nextgen.query.Mode.SUCCESS;
import static tripleo.elijah.util.Helpers.List_of;

public class CompilationRunner {
	private static final List<State> registeredStates = new ArrayList<>();
	public final Compilation _compilation;
	public final ICompilationBus cb;
	public final CR_State crState;
	public final IProgressSink progressSink;
	final CIS cis;
	private final CCI cci;
	public       EZS         _ezs = new EZS();
	CR_FindCIs cr_find_cis;
	private StartCompilationRunnerAction startAction;

	public CompilationRunner(final @NotNull ICompilationAccess aca, final CR_State aCrState) {
		_compilation = aca.getCompilation();

		_compilation.getCompilationEnclosure().setCompilationAccess(aca);

		cis = _compilation._cis;
		cb  = _compilation.getCompilationEnclosure().getCompilationBus();

		progressSink = new IProgressSink() {
			@Override
			public void note(final int aCode, final ProgressSinkComponent aProgressSinkComponent, final int aType, final Object[] aParams) {
				Stupidity.println_err_2(aProgressSinkComponent.printErr(aCode, aType, aParams));
			}
		};

		cci     = new DefaultCCI(_compilation, cis, progressSink);
		crState = aCrState;
	}

	@Contract("_ -> param1")
	public static State registerState(final State aState) {
		if (!(registeredStates.contains(aState))) {
			registeredStates.add(aState);

			final int id = registeredStates.indexOf(aState);

			aState.setIdentity(id);
			return aState;
		}

		return aState;
	}

	public void logProgress(final int number, final String text) {
		if (number == 130) return;

		tripleo.elijah.util.Stupidity.println_err_2(number + " " + text);
	}

	public @NotNull Operation<CompilerInstructions> parseEzFile(final SourceFileParserParams p) {
		final CompilerInstructions x = _ezs.parseEzFile(p);

		if (x == null) {
			return Operation.failure(new Exception("CompilationRunner::parseEzFile"));
		} else return Operation.success(x);
	}

	public @NotNull Operation<CompilerInstructions> parseEzFile1(final @NotNull SourceFileParserParams p) {
		return _ezs.parseEzFile1(p);
	}

	public Operation<CompilerInstructions> realParseEzFile(final @NotNull SourceFileParserParams p) {
		return _ezs.realParseEzFile(p);
	}

	public void start(final CompilerInstructions ci, final @NotNull IPipelineAccess pa) {
		// FIXME only run once 06/16
		if (startAction == null) {
			startAction = new StartCompilationRunnerAction(pa, ci);
			// FIXME CompilerDriven vs Process ('steps' matches "CK", so...)
			cb.add(startAction.cb_Process());
		}
	}

	public enum ST {
		;

		static class ExitConvertUserTypes implements State {
			private int identity;

			@Override
			public void apply(final DefaultStateful element) {
				//final VariableTableEntry vte = ((DeduceElement3_VariableTableEntry) element).principal;

				//final DeduceTypes2         dt2     = ((DeduceElement3_VariableTableEntry) element).deduceTypes2();
			}

			@Override
			public boolean checkState(final DefaultStateful aElement3) {
				return true;
			}

			@Override
			public void setIdentity(final int aId) {
				identity = aId;
			}
		}

		public static State EXIT_CONVERT_USER_TYPES;
		public static State EXIT_RESOLVE;
		public static State INITIAL;

		public static void register() {
			//EXIT_RESOLVE            = registerState(new ST.ExitResolveState());
			INITIAL = registerState(new ST.InitialState());
			//EXIT_CONVERT_USER_TYPES = registerState(new ST.ExitConvertUserTypes());
		}

		static class ExitResolveState implements State {
			private int identity;

			@Override
			public void apply(final DefaultStateful element) {
				//final VariableTableEntry vte = ((DeduceElement3_VariableTableEntry) element).principal;
			}

			@Override
			public boolean checkState(final DefaultStateful aElement3) {
				//return ((DeduceElement3_VariableTableEntry) aElement3).st == DeduceElement3_VariableTableEntry.ST.INITIAL;
				return false; // FIXME
			}

			@Override
			public void setIdentity(final int aId) {
				identity = aId;
			}
		}

		static class InitialState implements State {
			private int identity;

			@Override
			public void apply(final DefaultStateful element) {

			}

			@Override
			public boolean checkState(final DefaultStateful aElement3) {
				return true;
			}

			@Override
			public void setIdentity(final int aId) {
				identity = aId;
			}
		}
	}

	//public void doFindCIs(final List<CompilerInput> aInputs, final @NotNull CompilationBus cb) {
	//	final ErrSink errSink1 = _compilation.getErrSink();
	//	final IO      io       = _compilation.getIO();
	//
	//	cb.add(new FindCIs_CB_Action(aInputs, errSink1, io));
	//}

	class EZS {
		@Nullable CompilerInstructions parseEzFile(SourceFileParserParams p) {
			final Operation<CompilerInstructions> om = parseEzFile1(p);

			final CompilerInstructions m;

			if (om.mode() == SUCCESS) {
				m = om.success();

/*
		final String prelude;
		final String xprelude = m.genLang();
		tripleo.elijah.util.Stupidity.println_err_2("230 " + prelude);
		if (xprelude == null)
			prelude = CompilationAlways.defaultPrelude(); // TODO should be java for eljc
		else
			prelude = null;
*/
			} else {
				m = null;
			}

			return m;
		}

		private Operation<CompilerInstructions> parseEzFile_(final String f, final InputStream s) throws RecognitionException, TokenStreamException {
			final QueryEzFileToModuleParams qp = new QueryEzFileToModuleParams(f, s);
			return new QueryEzFileToModule(qp).calculate();
		}

		public @NotNull Operation<CompilerInstructions> parseEzFile1(@NotNull SourceFileParserParams p) {
			@NotNull final File f = p.f();
			System.out.printf("   %s%n", f.getAbsolutePath());
			if (!f.exists()) {
				p.errSink().reportError(
						"File doesn't exist " + f.getAbsolutePath());
				return null;
			} else {
				final Operation<CompilerInstructions> om = realParseEzFile(p);
				return om;
			}
		}

		private Operation<CompilerInstructions> realParseEzFile(@NotNull SourceFileParserParams p) {
			final String f    = p.file_name();
			final File   file = new File(f);

			try {
				final InputStream s = p.io().readFile(file);
				return realParseEzFile(f, s, file, p.c());
			} catch (FileNotFoundException aE) {
				return Operation.failure(aE);
			}
		}

		public Operation<CompilerInstructions> realParseEzFile(final String f,
															   final InputStream s,
															   final @NotNull File file,
															   final Compilation c) {
			final String absolutePath;
			try {
				absolutePath = file.getCanonicalFile().toString(); // TODO 04/10 hash this and "attach"
				//queryDB.attach(compilerInput, new EzFileIdentity_Sha256($hash)); // ??
			} catch (IOException aE) {
				//throw new RuntimeException(aE);
				return Operation.failure(aE);
			}

			Operation<String> hash = Helpers.getHashForFilename(f);
			System.err.println("166 " + hash.success());

			// TODO 04/10
			// Cache<CompilerInput, CompilerInstructions> fn2ci /*EzFileIdentity??*/(MAP/*??*/, resolver is try stmt)
			if (c.fn2ci.containsKey(absolutePath)) { // don't parse twice
				// TODO 04/10
				// ...queryDB.attach(compilerInput, new EzFileIdentity_Sha256($hash)); // ?? fnci
				return Operation.success(c.fn2ci.get(absolutePath));
			}

			try {
				try {
					final Operation<CompilerInstructions> cio = parseEzFile_(f, s);

					if (cio.mode() != SUCCESS) {
						final Exception e = cio.failure();
						assert e != null;

						tripleo.elijah.util.Stupidity.println_err_2(("parser exception: " + e));
						e.printStackTrace(System.err);
						//s.close();
						return cio;
					}

					final CompilerInstructions R = cio.success();
					R.setFilename(file.toString());
					c.fn2ci.put(absolutePath, R);
					return cio;
				} catch (final ANTLRException e) {
					tripleo.elijah.util.Stupidity.println_err_2(("parser exception: " + e));
					e.printStackTrace(System.err);
					return Operation.failure(e);
				}
			} finally {
				if (s != null) {
					try {
						s.close();
					} catch (IOException aE) {
						// TODO return inside finally: is this ok??
						return new Operation<>(null, aE, FAILURE);
					}
				}
			}
		}
	}

	class FindCIs_CB_Action implements ICompilationBus.CB_Action {
		private final ErrSink             _errSink;
		private final List<CompilerInput> _inputs;
		private final IO                  io;

		@Contract(pure = true)
		public FindCIs_CB_Action(final List<CompilerInput> aInputs, final ErrSink aErrSink1, final IO aIo) {
			_inputs  = aInputs;
			_errSink = aErrSink1;
			io       = aIo;
			//
		}

		@Override
		public void execute() {
			final CB_Output o = new CB_Output();

			//beginning = new CompilerBeginning(_compilation, _compilation.rootCI, _inputs, progressSink, _compilation.cfg);
			//final CR_FindCIs           findCIs        = new CR_FindCIs(beginning);

			//assert  cr_find_cis != null;
			//final CR_FindCIs        findCIs        = cr_find_cis; //new CR_FindCIs(_inputs, _compilation, progressSink);

			final CR_FindCIs        findCIs        = new CR_FindCIs(_inputs, _compilation, progressSink);
			final CR_AlmostComplete almostComplete = new CR_AlmostComplete();

			final List<CR_Action> crActionList = List_of(findCIs, almostComplete);

			for (final CR_Action action : crActionList) {
				action.attach(CompilationRunner.this);
				action.execute(crState, o);
			}
		}

		@Contract(pure = true)
		@Override
		public @NotNull String name() {
			return "FindCIs";
		}

		@Override
		public @NotNull List<ICompilationBus.OutputString> outputStrings() {
			return List_of();
		}
	}

	private class StartCompilationRunnerAction implements ICompilationBus.CB_Action {
		private final          CompilerInstructions ci;
		private final @NotNull IPipelineAccess      pa;

		@Contract(pure = true)
		public StartCompilationRunnerAction(final @NotNull IPipelineAccess aPa, final CompilerInstructions aCi) {
			pa = aPa;
			ci = aCi;
		}

		@Contract(value = " -> new", pure = true)
		public ICompilationBus.@NotNull CB_Process cb_Process() {
			return new ICompilationBus.CB_Process() {
				@Override
				public List<ICompilationBus.CB_Action> steps() {
					return List_of(
							StartCompilationRunnerAction.this
								  );
				}
			};
		}

		@Override
		public void execute() {
			final CompilerDriver compilationDriver = pa
					.getCompilationEnclosure()
					.getCompilationDriver();
			final Operation<CompilerDriven> ocrsd = compilationDriver.get(Compilation.CompilationAlways.Tokens.COMPILATION_RUNNER_START);

			switch (ocrsd.mode()) {
			case SUCCESS -> {
				final CD_CompilationRunnerStart compilationRunnerStart = (CD_CompilationRunnerStart) ocrsd.success();

				compilationRunnerStart.start(ci, crState);
			}
			case FAILURE, NOTHING -> throw new Error();
			}
		}

		@Contract(pure = true)
		@Override
		public @NotNull String name() {
			return "StartCompilationRunnerAction";
		}

		@Contract(pure = true)
		@Override
		public @Nullable List<ICompilationBus.OutputString> outputStrings() {
			return null;
		}
	}
}
