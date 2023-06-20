/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp;

import io.reactivex.rxjava3.core.Observer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.ci.CompilerInstructionsImpl;
import tripleo.elijah.comp.i.*;
import tripleo.elijah.comp.internal.CIS;
import tripleo.elijah.comp.internal.DefaultCompilerController;
import tripleo.elijah.comp.internal.DriverToken;
import tripleo.elijah.comp.internal.USE;
import tripleo.elijah.comp.nextgen.CP_Paths;
import tripleo.elijah.lang.i.ClassStatement;
import tripleo.elijah.lang.i.OS_Module;
import tripleo.elijah.lang.i.OS_Package;
import tripleo.elijah.lang.i.Qualident;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleInput;
import tripleo.elijah.nextgen.outputtree.EOT_OutputTree;
import tripleo.elijah.nextgen.query.Operation2;
import tripleo.elijah.stages.deduce.IFunctionMapHook;
import tripleo.elijah.stages.deduce.fluffy.i.FluffyComp;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.world.i.LivingRepo;
import tripleo.elijah.world.impl.DefaultLivingRepo;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class Compilation {

	public final  CIS                     _cis                 = new CIS();
	public final  CompilationConfig       cfg                  = new CompilationConfig();
	public final Map<String, CompilerInstructions> fn2ci = new HashMap<String, CompilerInstructions>();
	public final List<OS_Module> modules = new ArrayList<OS_Module>();
	public final USE use = new USE(this);
	final         ErrSink                 errSink;
	private final int                     _compilationNumber;
	private final CompFactory             _con                 = new CompFactory() {
		@Override
		public EIT_ModuleInput createModuleInput(final OS_Module aModule) {
			return new EIT_ModuleInput(aModule, Compilation.this);
		}
	};
	private final Map<String, OS_Package> _packages            = new HashMap<String, OS_Package>();
	private final Pipeline pipelines = new Pipeline();
	public        LivingRepo              _repo                = new DefaultLivingRepo();
	CompilerInstructions rootCI;
	//
	//
	private       int                     _classCode           = 101;
	private       int                     _functionCode        = 1001;
	private       List<CompilerInput>     _inputs;
	private       IPipelineAccess         _pa;
	private       CompilationEnclosure    compilationEnclosure = new CompilationEnclosure(this);
	private IO io;

	public static ElLog.Verbosity gitlabCIVerbosity() {
		final boolean gitlab_ci = isGitlab_ci();
		return gitlab_ci ? ElLog.Verbosity.SILENT : ElLog.Verbosity.VERBOSE;
	}

	public static boolean isGitlab_ci() {
		return System.getenv("GITLAB_CI") != null;
	}

	// TODO remove this 04/20
	public void addFunctionMapHook(final IFunctionMapHook aFunctionMapHook) {
		getCompilationEnclosure().getCompilationAccess().addFunctionMapHook(aFunctionMapHook);
	}

	public CompilationEnclosure getCompilationEnclosure() {
		return compilationEnclosure;
	}

	public void setCompilationEnclosure(final CompilationEnclosure aCompilationEnclosure) {
		compilationEnclosure = aCompilationEnclosure;
	}

	public Compilation(final ErrSink errSink, final IO io) {
		this.errSink            = errSink;
		this.io                 = io;
		this._compilationNumber = new Random().nextInt(Integer.MAX_VALUE);
	}

	public void addModule(final OS_Module module, final String fn) {
		//modules.add(module);
		//use.addModule(module, fn);
		throw new Error();
	}

	public void addModule__(final @NotNull OS_Module module, final @NotNull String fn) {
		modules.add(module);
		use.addModule(module, fn);
	}

	public void addPipeline(PipelineMember aPl) {
		pipelines.add(aPl);
	}

	public int compilationNumber() {
		return _compilationNumber;
	}

	public CompFactory con() {
		return _con;
	}

	public void eachModule(final Consumer<OS_Module> object) {
		for (OS_Module mod : modules) {
			object.accept(mod);
		}
	}

	public int errorCount() {
		return errSink.errorCount();
	}

	public void feedInputs(final @NotNull List<CompilerInput> inputs, final CompilerController ctl) {
		if (inputs.size() == 0) {
			ctl.printUsage();
			return;
		}

/*
		final List<CompilerInput> inputs2 = inputs.stream()
				.map(input -> {
					final File file = new File(input.getInp());
					if (file.isDirectory()) {
						input.setSourceRoot();
					}
					return input;
				})
				.collect(Collectors.toList());
*/

		_inputs = inputs; // !!
		compilationEnclosure.setCompilerInput(inputs);

		if (ctl instanceof DefaultCompilerController) {
			ctl._setInputs(this, inputs);
			//} else if (ctl instanceof UT_Controller uctl) {
			//	uctl._setInputs(this, inputs);
		}

		ctl.processOptions();
		ctl.runner();
	}

	public abstract void fakeFlow(final List<CompilerInput> aInputs, final CompilationFlow aFlow);

	public void feedCmdLine(final @NotNull List<String> args) throws Exception {
		final CompilerController controller = new DefaultCompilerController();

		if (args.size() == 0) {
			controller.printUsage();
			//System.err.println("Usage: eljc [--showtree] [-sE|O] <directory or .ez file names>");
			return;
		}

		final List<CompilerInput> inputs = args.stream()
				.map(s -> {
					final CompilerInput input = new CompilerInput(s);
					if (s.equals(input.getInp())) {
						input.setSourceRoot();
					}
					return input;
				})
				.collect(Collectors.toList());

		_inputs = inputs;

		controller._setInputs(this, inputs);
		controller.processOptions();
		controller.runner();
	}

	public CompilationClosure getCompilationClosure() {
		return new CompilationClosure() {

			@Override
			public ErrSink errSink() {
				return errSink;
			}

			@Override
			public Compilation getCompilation() {
				return Compilation.this;
			}

			@Override
			public IO io() {
				return io;
			}
		};
	}

	public List<ClassStatement> findClass(final String aClassName) {
		final List<ClassStatement> l = new ArrayList<ClassStatement>();
		for (final OS_Module module : modules) {
			if (module.hasClass(aClassName)) {
				l.add((ClassStatement) module.findClass(aClassName));
			}
		}
		return l;
	}

	public Operation2<OS_Module> findPrelude(final String prelude_name) {
		return use.findPrelude(prelude_name);
	}

	public IPipelineAccess get_pa() {
		return _pa;
	}

	public abstract @NotNull FluffyComp getFluffy();

	@Contract(pure = true)
	private List<CompilerInput> getInputs() {
		return _inputs;
	}

	public String getCompilationNumberString() {
		return String.format("%08x", _compilationNumber);
	}

	public ErrSink getErrSink() {
		return errSink;
	}

	public IO getIO() {
		return io;
	}

	public void setIO(final IO io) {
		this.io = io;
	}

	public abstract @NotNull EOT_OutputTree getOutputTree();

	public OS_Package getPackage(final @NotNull Qualident pkg_name) {
		return _repo.getPackage(pkg_name.toString());
	}

	public Pipeline getPipelines() {
		return pipelines;
	}

	public String getProjectName() {
		return rootCI.getName();
	}

	public void hasInstructions(final List<CompilerInstructions> aL) {
		hasInstructions(aL, pa());
	}

	public IPipelineAccess pa() {
		//assert _pa != null;

		if (_pa == null) {
			getCompilationEnclosure().getCompilationRunner().crState.ca();
		}

		return _pa;
	}

	void hasInstructions(final @NotNull List<CompilerInstructions> cis,
						 final @NotNull IPipelineAccess pa) {
		//assert cis.size() == 1; // FIXME this is corect. below is wrong (allows cis.size()==2)
		assert cis.size() > 0;
		if (cis.size() <= 0) {
			// README IDEA misconfiguration
			System.err.println("No CIs found. Current dir is " + new File(".").getAbsolutePath());
			return;
		}

		rootCI = cis.get(0);

		pa.setCompilerInput(pa.getCompilation().getInputs());

		getCompilationEnclosure().getCompilationRunner().start(rootCI, pa);
	}

	@Deprecated
	public int instructionCount() {
		return 4; // TODO shim !!!cis.size();
	}

	public boolean isPackage(final @NotNull String pkg) {
		return _repo.hasPackage(pkg);
	}

	public OS_Package makePackage(final Qualident pkg_name) {
		return _repo.makePackage(pkg_name);
	}

	// endregion

	//
	// region CLASS AND FUNCTION CODES
	//

	public ModuleBuilder moduleBuilder() {
		return new ModuleBuilder(this);
	}

	public int nextClassCode() {
		int i = _classCode;
		_classCode++;
		return i;
	}

	public int nextFunctionCode() {
		int i = _functionCode;
		_functionCode++;
		return i;
	}

	public void pushItem(CompilerInstructions aci) {
		_cis.onNext(aci);
	}

	// endregion

	public void set_pa(IPipelineAccess a_pa) {
		_pa = a_pa;

		compilationEnclosure.pipelineAccessPromise.resolve(_pa);
	}

	public void subscribeCI(final Observer<CompilerInstructions> aCio) {
		_cis.subscribe(aCio);
	}

	public void use(final @NotNull CompilerInstructionsImpl compilerInstructions, final boolean do_out) throws Exception {
		use.use(compilerInstructions, do_out);    // NOTE Rust
	}

	public LivingRepo world() {
		return _repo;
	}

	private final CP_Paths paths = new CP_Paths();

	public CP_Paths paths() {
		return paths;
	}

	public enum CompilationAlways {
		;

		@NotNull
		public static String defaultPrelude() {
			return "c";
		}

		public enum Tokens {
			;
			public static final DriverToken COMPILATION_RUNNER_FIND_STDLIB2 = DriverToken.makeToken("COMPILATION_RUNNER_FIND_STDLIB2");
			public static final DriverToken COMPILATION_RUNNER_START        = DriverToken.makeToken("COMPILATION_RUNNER_START");
		}
	}

	public interface CompFactory {

		EIT_ModuleInput createModuleInput(OS_Module aModule);
	}

	public static class CompilationConfig {
		public boolean do_out;
		public boolean showTree = false;
		public boolean silent   = false;
		public Stages  stage    = Stages.O; // Output
	}
}

//
//
//
