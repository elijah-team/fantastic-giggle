/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.i.FunctionDef;
import tripleo.elijah.lang.i.OS_Element;
import tripleo.elijah.lang.i.OS_Module;
import tripleo.elijah.lang.impl.ConstructorDefImpl;
import tripleo.elijah.stages.gen_fn.*;

import java.util.List;

import static tripleo.elijah.util.Helpers.List_of;

/**
 * Created 1/21/21 9:04 PM
 */
public class FunctionInvocation {
	public final      ProcTableEntry                              pte;
	private @Nullable BaseEvaFunction                             _generated       = null;
	private final     FunctionDef                                 fd;
	private final     DeferredObject<BaseEvaFunction, Void, Void> generateDeferred = new DeferredObject<BaseEvaFunction, Void, Void>();
	private           NamespaceInvocation                         namespaceInvocation;
	private           ClassInvocation                             classInvocation;

	public FunctionInvocation(FunctionDef aFunctionDef, ProcTableEntry aProcTableEntry, @NotNull IInvocation invocation, GeneratePhase phase) {
		this.fd  = aFunctionDef;
		this.pte = aProcTableEntry;
		assert invocation != null;
		invocation.setForFunctionInvocation(this);
//		setPhase(phase);
	}

/*
	public void setPhase(final GeneratePhase generatePhase) {
		if (pte != null)
			pte.completeDeferred().then(new DoneCallback<ProcTableEntry>() {
				@Override
				public void onDone(ProcTableEntry result) {
					makeGenerated(generatePhase, null);
				}
			});
		else
			makeGenerated(generatePhase, null);
	}
*/

	public @NotNull DeferredObject<BaseEvaFunction, Void, Void> generateDeferred() {
		return generateDeferred;
	}

	public WlGenerateFunction generateFunction(final DeduceTypes2 aDeduceTypes2, final OS_Element aBest) {
		throw new Error();
	}

	public Promise<BaseEvaFunction, Void, Void> generatePromise() {
		return generateDeferred.promise();
	}

	public List<TypeTableEntry> getArgs() {
		if (pte == null)
			return List_of();
		return pte.args;
	}

	public ClassInvocation getClassInvocation() {
		return classInvocation;
	}

	public void setClassInvocation(@NotNull ClassInvocation aClassInvocation) {
		classInvocation = aClassInvocation;
	}

	public BaseEvaFunction getEva() {
		return null; // TODO 04/15
	}

	public FunctionDef getFunction() {
		return fd;
	}

	public @Nullable BaseEvaFunction getGenerated() {
		return _generated;
	}

	public EvaFunction makeGenerated(@NotNull GeneratePhase generatePhase, @NotNull DeducePhase aPhase) {
		@Nullable OS_Module module = null;
		if (fd != null)
			module = fd.getContext().module();
		if (module == null)
			module = classInvocation.getKlass().getContext().module(); // README for constructors
		if (fd == ConstructorDefImpl.defaultVirtualCtor) {
			@NotNull WlGenerateDefaultCtor wlgdc = new WlGenerateDefaultCtor(generatePhase.getGenerateFunctions(module), this);
			wlgdc.run(null);
//			EvaFunction gf = wlgdc.getResult();
		} else {
			@NotNull WlGenerateFunction wlgf = new WlGenerateFunction(generatePhase.getGenerateFunctions(module), this, aPhase.codeRegistrar);
			wlgf.run(null);
			EvaFunction gf = wlgf.getResult();
			if (gf.getGenClass() == null) {
				if (namespaceInvocation != null) {
//					namespaceInvocation = aPhase.registerNamespaceInvocation(namespaceInvocation.getNamespace());
					@NotNull WlGenerateNamespace wlgn = new WlGenerateNamespace(generatePhase.getGenerateFunctions(module),
																				namespaceInvocation,
																				aPhase.generatedClasses, aPhase.codeRegistrar);
					wlgn.run(null);
					int y = 2;
				}
			}

			return gf;
		}
//		if (generateDeferred.isPending()) {
//			generateDeferred.resolve(gf);
//			_generated = gf;
//		}
		return null;
	}

	public NamespaceInvocation getNamespaceInvocation() {
		return namespaceInvocation;
	}

	public void setGenerated(BaseEvaFunction aGeneratedFunction) {
		_generated = aGeneratedFunction;
	}

	public void setNamespaceInvocation(NamespaceInvocation aNamespaceInvocation) {
		namespaceInvocation = aNamespaceInvocation;
	}
}

//
//
//
