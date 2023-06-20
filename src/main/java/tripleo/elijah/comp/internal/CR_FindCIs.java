package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.CompilerInput;
import tripleo.elijah.comp.Operation;
import tripleo.elijah.comp.diagnostic.TooManyEz_ActuallyNone;
import tripleo.elijah.comp.diagnostic.TooManyEz_BeSpecific;
import tripleo.elijah.comp.i.*;
import tripleo.elijah.comp.queries.QuerySearchEzFiles;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.nextgen.query.Operation2;
import tripleo.elijah.stateful.DefaultStateful;
import tripleo.elijah.stateful.State;
import tripleo.elijah.util.Maybe;

import java.io.File;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static tripleo.elijah.util.Helpers.List_of;

public class CR_FindCIs extends DefaultStateful implements CR_Action {
	private final List<CompilerInput> inputs;
	private final CCI                 cci;
	private       CompilationRunner   compilationRunner;
	private final State               st;
	private       ICompilationBus     cb;

	public CR_FindCIs(final @NotNull CompilerBeginning beginning) {
		this(beginning.compilerInput(), beginning.compilation(), beginning.progressSink());
	}

	@Contract(pure = true)
	public CR_FindCIs(final List<CompilerInput> aInputs, final @NotNull Compilation comp, final IProgressSink aPs) {
		inputs = aInputs;
		st     = CompilationRunner.ST.INITIAL;

		cci = new DefaultCCI(comp, comp._cis, aPs);
	}

	private void _inputIsDirectory(final Compilation c, final List<CompilerInput> x, final CompilerInput input,
								   final File f) {
		CompilerInstructions ez_file;
		input.setDirectory(f);

		final List<CompilerInstructions> ezs = searchEzFiles(f, c.getCompilationClosure());

		switch (ezs.size()) {
		case 0:
			final Diagnostic d_toomany = new TooManyEz_ActuallyNone();
			final Maybe<ILazyCompilerInstructions> m = new Maybe<>(null, d_toomany);
			input.accept_ci(m);
			x.add(input);
			break;
		case 1:
			ez_file = ezs.get(0);
			final ILazyCompilerInstructions ilci = ILazyCompilerInstructions.of(ez_file);
			final Maybe<ILazyCompilerInstructions> m3 = new Maybe<>(ilci, null);
			input.accept_ci(m3);
			x.add(input);
			break;
		default:
			//final Diagnostic d_toomany = new TooManyEz_UseFirst();
			//add_ci(ezs.get(0));

			// more than 1 (negative is not possible)
			final Diagnostic d_toomany2 = new TooManyEz_BeSpecific();
			final Maybe<ILazyCompilerInstructions> m2 = new Maybe<>(null, d_toomany2);
			input.accept_ci(m2);
			x.add(input);
			break;
		}
	}

	@Override
	public void attach(final @NotNull CompilationRunner cr) {
		compilationRunner = cr;
	}

	@Override
	public Operation<Boolean> execute(final @NotNull CR_State st, final CB_Output aO) {
		final Compilation c = st.ca().getCompilation();

		final IProgressSink ps = new IProgressSink() {
			@Override
			public void note(final int aCode, final ProgressSinkComponent aComponent, final int aType, final Object[] aParams) {
				final int y = 2;
				aO.print(Arrays.toString(aParams));
			}
		};

		final List<CompilerInput> x = find_cis(inputs, c, c.getErrSink());
		for (final CompilerInput compilerInput : x) {
			cci.accept(compilerInput.acceptance_ci(), ps);
		}

		return Operation.success(true);
	}

	protected List<CompilerInput> find_cis(final @NotNull List<CompilerInput> inputs,
										   final @NotNull Compilation c,
										   final @NotNull ErrSink errSink) {
		final List<CompilerInput> x = new ArrayList<>();


		//final IProgressSink ps = cis.ps;
		final IProgressSink ps = new IProgressSink() {
			@Override
			public void note(final int aCode, final ProgressSinkComponent aCci, final int aType, final Object[] aParams) {
				tripleo.elijah.util.Stupidity.println_err_2(aCci.printErr(aCode, aType, aParams));
			}
		};


		CompilerInstructions ez_file;

		for (final CompilerInput input : inputs) {
			_processInput(c, errSink, x, input);
		}

		return x;
	}

	private void _processInput(final Compilation c, final ErrSink errSink, final List<CompilerInput> x,
							   final CompilerInput input) {
		CompilerInstructions ez_file;
		switch (input.ty()) {
		case NULL -> {
		}
		case SOURCE_ROOT -> {
		}
		default -> {
			return;
		}
		}

		final String  file_name = input.getInp();
		final File    f         = new File(file_name);
		final boolean matches2  = Pattern.matches(".+\\.ez$", file_name);
		if (matches2) {
			final ILazyCompilerInstructions ilci = ILazyCompilerInstructions.of(f, c);

			final Maybe<ILazyCompilerInstructions> m4 = new Maybe<>(ilci, null);
			input.accept_ci(m4);
			x.add(input);
		} else {
			//errSink.reportError("9996 Not an .ez file "+file_name);
			if (f.isDirectory()) {
				_inputIsDirectory(c, x, input, f);
			} else {
				final NotDirectoryException d = new NotDirectoryException(f.toString());
				errSink.reportError("9995 Not a directory " + f.getAbsolutePath());
			}
		}
	}

	@Override
	public String name() {
		return "find cis";
	}

	private List<CompilerInstructions> searchEzFiles(final File directory, final CompilationClosure ccl) {
		final QuerySearchEzFiles                     q    = new QuerySearchEzFiles(ccl);
		final Operation2<List<CompilerInstructions>> olci = q.process(directory);

		if (olci.mode() == Mode.SUCCESS) {
			return olci.success();
		}

		ccl.errSink().reportDiagnostic(olci.failure());
		return List_of();
	}
}
