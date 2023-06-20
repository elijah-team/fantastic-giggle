package tripleo.elijah.comp.i;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.Operation;
import tripleo.elijah.comp.internal.SourceFileParserParams;
import tripleo.elijah.nextgen.query.Mode;

import java.io.File;

public interface ILazyCompilerInstructions {
	@Contract(value = "_ -> new", pure = true)
	static @NotNull ILazyCompilerInstructions of(final CompilerInstructions aCompilerInstructions) {
		return new ILazyCompilerInstructions() {
			@Override
			public CompilerInstructions get() {
				return aCompilerInstructions;
			}
		};
	}

	@Contract(value = "_, _ -> new", pure = true)
	static @NotNull ILazyCompilerInstructions of(final File aFile, final Compilation c) {
		return new ILazyCompilerInstructions() {
			@Override
			public CompilerInstructions get() {
				try {
					var f         = aFile;
					var file_name = aFile.getPath();
					var errSink   = c.getErrSink();
					var io        = c.getIO();

					var p = new SourceFileParserParams(f, file_name, errSink, io, c);

					final Operation<CompilerInstructions> oci = c.getCompilationEnclosure().getCompilationRunner().parseEzFile1(p);

					if (oci.mode() == Mode.SUCCESS) {
						final CompilerInstructions parsed = oci.success();
						return parsed;
					} else {
						throw new RuntimeException(oci.failure()); // TODO ugh
					}
				} catch (Exception aE) {
					//return Operation.failure(aE);
					throw new RuntimeException(aE); // TODO ugh
				}
			}
		};
	}

	CompilerInstructions get();
}
