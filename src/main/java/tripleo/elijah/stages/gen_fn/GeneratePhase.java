/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.lang.i.OS_Module;
import tripleo.elijah.nextgen.reactive.ReactiveDimension;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.work.WorkManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created 5/16/21 12:35 AM
 */
public class GeneratePhase implements ReactiveDimension {
	final         PipelineLogic                              pipelineLogic;
	private final ElLog.Verbosity                            verbosity;
	private final DeferredObject<ICodeRegistrar, Void, Void> _codeRegistrarP   = new DeferredObject<>();
	public        WorkManager                                wm                = new WorkManager();
	private       Map<OS_Module, GenerateFunctions>          generateFunctions = new HashMap<OS_Module, GenerateFunctions>();
	private       ICodeRegistrar                             codeRegistrar     = null;

	public GeneratePhase(ElLog.Verbosity aVerbosity, final IPipelineAccess aPa, PipelineLogic aPipelineLogic) {
		verbosity     = aVerbosity;
		pipelineLogic = aPipelineLogic;

		aPa.getCompilationEnclosure().addReactiveDimension(this);
	}

	public void codeRegistrarP(final Consumer<ICodeRegistrar> cicr) {
		_codeRegistrarP.then(cicr::accept);
	}

	public ICodeRegistrar getCodeRegistrar() {
		return codeRegistrar;
	}

	public void setCodeRegistrar(ICodeRegistrar aCodeRegistrar) {
		codeRegistrar = aCodeRegistrar;

		_codeRegistrarP.resolve(codeRegistrar);
	}

	@NotNull
	public GenerateFunctions getGenerateFunctions(@NotNull OS_Module mod) {
		final GenerateFunctions Result;
		if (generateFunctions.containsKey(mod))
			Result = generateFunctions.get(mod);
		else {
			var pa0 = pipelineLogic.dp.pa;
			assert pa0 != null;
			Result = new GenerateFunctions(mod, pipelineLogic, pa0);
			generateFunctions.put(mod, Result);
		}
		return Result;
	}

	public ElLog.Verbosity getVerbosity() {
		return verbosity;
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
