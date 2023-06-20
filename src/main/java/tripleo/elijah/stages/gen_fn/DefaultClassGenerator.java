package tripleo.elijah.stages.gen_fn;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.i.ClassStatement;
import tripleo.elijah.lang.i.FunctionDef;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;
import tripleo.elijah.work.WorkList;

public class DefaultClassGenerator implements IClassGenerator {

	private final ICodeRegistrar cr;
	private final DeducePhase    deducePhase;
	private final WorkList       wl;

	public DefaultClassGenerator(DeducePhase aDeducePhase) {
		deducePhase = aDeducePhase;
		cr          = deducePhase.codeRegistrar;

		wl = new WorkList();
	}

	@Override
	public ICodeRegistrar getCodeRegistrar() {
		return cr;
	}

	@Override
	public DeducePhase.GeneratedClasses getGeneratedClasses() {
		return deducePhase.generatedClasses;
	}

	@Override
	public FunctionInvocation newFunctionInvocation(final FunctionDef fd,
													final ProcTableEntry pte,
													final ClassInvocation ci) {
		final @NotNull FunctionInvocation fi = deducePhase.newFunctionInvocation(fd, pte, ci);
		return fi;
	}

	@Override
	public @Nullable ClassInvocation registerClassInvocation(final ClassStatement cs, final String className) {
		return deducePhase.registerClassInvocation(cs, className);
	}

	@Override
	public void submitGenerateClass(final @NotNull ClassInvocation ci, final GenerateFunctions gf) {
		//@NotNull final ClassStatement cs = ci.getKlass();
		//
		//@org.jetbrains.annotations.Nullable ClassInvocation ci = deducePhase.registerClassInvocation(cs, null);
		wl.addJob(new WlGenerateClass(gf, ci, deducePhase.generatedClasses, cr));
	}

	@Override
	public void submitGenerateFunction(final FunctionInvocation fi, final GenerateFunctions gf) {
		wl.addJob(new WlGenerateFunction(gf, fi, cr));
	}

	@Override
	public WorkList wl() {
		return wl;
	}
}
