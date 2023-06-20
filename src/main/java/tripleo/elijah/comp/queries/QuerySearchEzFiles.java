package tripleo.elijah.comp.queries;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.IO;
import tripleo.elijah.comp.i.CompilationClosure;
import tripleo.elijah.comp.i.ErrSink;
import tripleo.elijah.comp.internal.SourceFileParserParams;
import tripleo.elijah.nextgen.query.Operation2;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class QuerySearchEzFiles {
	private final Compilation c;
	private final ErrSink     errSink;
	private final IO          io;

	public QuerySearchEzFiles(final @NotNull CompilationClosure ccl) {
		c       = ccl.getCompilation();
		errSink = ccl.errSink();
		io      = ccl.io();
	}

	@Nullable CompilerInstructions parseEzFile(final @NotNull File f, final String file_name, final ErrSink errSink, final IO io, final Compilation c) throws Exception {
		var p = new SourceFileParserParams(f, file_name, errSink, io, c);
		return c.getCompilationEnclosure().getCompilationRunner().parseEzFile(p).success();
	}

	public Operation2<List<CompilerInstructions>> process(final @NotNull File directory) {
		final List<CompilerInstructions> R = new ArrayList<CompilerInstructions>();
		final FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(final File file, final String s) {
				final boolean matches2 = Pattern.matches(".+\\.ez$", s);
				return matches2;
			}
		};
		final String[] list = directory.list(filter);
		if (list != null) {
			for (final String file_name : list) {
				try {
					final File                 file   = new File(directory, file_name);
					final CompilerInstructions ezFile = parseEzFile(file, file.toString(), errSink, io, c);
					if (ezFile != null)
						R.add(ezFile);
					else
						errSink.reportError("9995 ezFile is null " + file);
				} catch (final Exception e) {
					errSink.exception(e);
				}
			}
		}
		return Operation2.success(R);
	}
}
