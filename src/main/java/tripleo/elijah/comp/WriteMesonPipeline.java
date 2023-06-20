/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp;

import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.nextgen.outputstatement.EX_Explanation;
import tripleo.elijah.nextgen.outputtree.EOT_OutputFile;
import tripleo.elijah.nextgen.outputtree.EOT_OutputType;
import tripleo.elijah.stages.gen_generic.DoubleLatch;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.Old_GenerateResult;
import tripleo.util.io.CharSink;
import tripleo.util.io.FileCharSink;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static tripleo.elijah.util.Helpers.List_of;
import static tripleo.elijah.util.Helpers.String_join;

/**
 * Created 9/13/21 11:58 PM
 */
public class WriteMesonPipeline implements PipelineMember, @NotNull Consumer<Supplier<Old_GenerateResult>> {
//	private final File file_prefix;
//	private final GenerateResult gr;

	final         Pattern                                          pullPat = Pattern.compile("/[^/]+/(.+)");
	private final Compilation                                      c;
	private final IPipelineAccess                                  pa;
	private final WritePipeline writePipeline;
	private       Consumer<Multimap<CompilerInstructions, String>> _wmc;
	DoubleLatch<Multimap<CompilerInstructions, String>> write_makefiles_latch = new DoubleLatch<>(this::write_makefiles_action);
	private       Supplier<Old_GenerateResult>                     grs;

	public WriteMesonPipeline(final @NotNull IPipelineAccess pa0) {
		final AccessBus     ab             = pa0.getAccessBus();
		final Compilation   compilation    = ab.getCompilation();
		final WritePipeline writePipeline1 = ab.getPipelineAccess().getWitePipeline();

		pa            = pa0;
		c             = compilation;
		writePipeline = writePipeline1;
	}

	@Override
	public void accept(final @NotNull Supplier<Old_GenerateResult> aGenerateResultSupplier) {
		final GenerateResult gr = aGenerateResultSupplier.get();
		System.err.println(gr);
		grs = aGenerateResultSupplier;
		int y = 2;
	}

	public Consumer<Supplier<Old_GenerateResult>> consumer() {
		return new Consumer<Supplier<Old_GenerateResult>>() {
			@Override
			public void accept(final Supplier<Old_GenerateResult> aGenerateResultSupplier) {
				if (grs != null) {
					tripleo.elijah.util.Stupidity.println_err_2("234 grs not null " + grs.getClass().getName());
					return;
				}

				assert false;
				grs = aGenerateResultSupplier;
				//final GenerateResult gr = aGenerateResultSupplier.get();
			}
		};
	}

	@NotNull
	private Path getPath(String aName, String aName2) {
		return new File(new File(pa.getBaseDir(), aName), aName2).toPath();
	}

	private @Nullable String pullFileName(String aFilename) {
		//return aFilename.substring(aFilename.lastIndexOf('/')+1);
		Matcher x = pullPat.matcher(aFilename);
		try {
			if (x.matches())
				return x.group(1);
		} catch (IllegalStateException aE) {
		}
		return null;
	}

	@Override
	public void run() throws Exception {
		write_makefiles();
	}

	private void write_makefiles() {
		Multimap<CompilerInstructions, String> lsp_outputs = writePipeline.st.lsp_outputs; // TODO move this
		write_makefiles_consumer().accept(lsp_outputs);

		//write_makefiles_latch.notify(lsp_outputs);
		write_makefiles_latch.notifyLatch(true);
	}

	private void write_lsp(@NotNull Multimap<CompilerInstructions, String> lsp_outputs, CompilerInstructions compilerInstructions, String aSub_dir) throws IOException {
		if (true || false) {
			final Path path = getPath(aSub_dir, "meson.build");

			final Collection<String> files_ = lsp_outputs.get(compilerInstructions);
			final Set<String> files = files_.stream()
					.filter(x -> x.endsWith(".c"))
					.map(x -> String.format("\t'%s',", pullFileName(x)))
					.collect(Collectors.toUnmodifiableSet());

			final StringBuilder sb = new StringBuilder();
			sb.append(String.format("%s_sources = files(\n%s\n)", aSub_dir, String_join("\n", files)));
			sb.append("\n");
			sb.append(String.format("%s = static_library('%s', %s_sources, install: false,)", aSub_dir, aSub_dir, aSub_dir)); // include_directories, dependencies: [],
			sb.append("\n");
			sb.append("\n");
			sb.append(String.format("%s_dep = declare_dependency( link_with: %s )", aSub_dir, aSub_dir)); // include_directories
			sb.append("\n");

			@NotNull final EG_Statement stmt = EG_Statement.of(sb.toString(), EX_Explanation.withMessage("WriteMesonPipeline"));
			final EOT_OutputFile        off  = new EOT_OutputFile(c, List_of(), path.toString(), EOT_OutputType.BUILD, stmt);
			c.getOutputTree().add(off);
		}

	}

	public Consumer<Multimap<CompilerInstructions, String>> write_makefiles_consumer() {
		if (_wmc != null)
			return _wmc;

		_wmc = write_makefiles_latch::notifyData;

		return _wmc;
	}

	private void write_makefiles_action(final Multimap<CompilerInstructions, String> lsp_outputs) {
		List<String> dep_dirs = new LinkedList<String>();

		try {
			write_root(lsp_outputs, dep_dirs);

			for (final CompilerInstructions compilerInstructions : lsp_outputs.keySet()) {
				final String sub_dir = compilerInstructions.getName();
				final Path   dpath   = getPath(sub_dir);

				if (dpath.toFile().exists()) {
					write_lsp(lsp_outputs, compilerInstructions, sub_dir);
				}
			}

			write_prelude();
		} catch (IOException aE) {
			throw new RuntimeException(aE);
		}
	}

	private void write_root(@NotNull Multimap<CompilerInstructions, String> lsp_outputs, List<String> aDep_dirs) throws IOException {
		CharSink root_file = c.getIO().openWrite(getPath("meson.build"));
		try {
			String project_name   = c.getProjectName();
			String project_string = String.format("project('%s', 'c', version: '1.0.0', meson_version: '>= 0.48.0',)", project_name);
			root_file.accept(project_string);
			root_file.accept("\n");

			for (CompilerInstructions compilerInstructions : lsp_outputs.keySet()) {
				String     name  = compilerInstructions.getName();
				final Path dpath = getPath(name);
				if (dpath.toFile().exists()) {
					String name_subdir_string = String.format("subdir('%s')\n", name);
					root_file.accept(name_subdir_string);
					aDep_dirs.add(name);
				}
			}
			aDep_dirs.add("Prelude");
//			String prelude_string = String.format("subdir(\"Prelude_%s\")\n", /*c.defaultGenLang()*/"c");
			String prelude_string = "subdir('Prelude')\n";
			root_file.accept(prelude_string);

//			root_file.accept("\n");

			String deps_names = String_join(", ", aDep_dirs.stream()
					.map(x -> String.format("%s", x)) // TODO _lib ??
					.collect(Collectors.toList()));
			root_file.accept(String.format("%s_bin = executable('%s', link_with: [ %s ], install: true)", project_name, project_name, deps_names)); // dependencies, include_directories
		} finally {
			((FileCharSink) root_file).close();
		}
	}

	private void write_prelude() throws IOException {
		if (true || false) {
			final Path ppath1 = getPath("Prelude");
			final Path ppath  = ppath1.resolve("meson.build"); // Java is wierd

			if (false) {
				System.err.println("mkdirs 215 " + ppath1.toFile());

				ppath1.toFile().mkdirs(); // README just in case -- but should be unnecessary at this point
				//ppath.getParent().toFile().mkdirs(); // README just in case -- but should be unnecessary at this point
			}

			//Collection<String> files_ = lsp_outputs.get(compilerInstructions);
			List<String> files = List_of("'Prelude.c'")/*files_.stream()
					.filter(x -> x.endsWith(".c"))
					.map(x -> String.format("\t'%s',", x))
					.collect(Collectors.toList())*/;

			final StringBuilder sb = new StringBuilder();

			sb.append(String.format("Prelude_sources = files(\n%s\n)", String_join("\n", files)));
			sb.append("\n");
			sb.append("Prelude = static_library('Prelude', Prelude_sources, install: false,)"); // include_directories, dependencies: [],
			sb.append("\n");
			sb.append("\n");
			sb.append(String.format("%s_dep = declare_dependency( link_with: %s )", "Prelude", "Prelude")); // include_directories
			sb.append("\n");

			@NotNull final EG_Statement stmt = EG_Statement.of(sb.toString(), EX_Explanation.withMessage("WriteMesonPipeline"));
			final EOT_OutputFile        off  = new EOT_OutputFile(c, List_of(), ppath.toString(), EOT_OutputType.BUILD, stmt);
			c.getOutputTree().add(off);
		}

	}

	@NotNull
	private Path getPath(String aName) {
		return new File(pa.getBaseDir(), aName).toPath();
	}
}

//
//
//
