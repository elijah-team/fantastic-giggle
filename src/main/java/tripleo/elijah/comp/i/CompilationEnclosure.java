package tripleo.elijah.comp.i;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.AccessBus;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.CompilerInput;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.internal.CR_State;
import tripleo.elijah.comp.internal.CompilationBus;
import tripleo.elijah.comp.internal.CompilationRunner;
import tripleo.elijah.comp.internal.CompilerDriver;
import tripleo.elijah.nextgen.reactive.Reactivable;
import tripleo.elijah.nextgen.reactive.Reactive;
import tripleo.elijah.nextgen.reactive.ReactiveDimension;

import java.util.List;

public class CompilationEnclosure {

	public final DeferredObject<IPipelineAccess, Void, Void> pipelineAccessPromise = new DeferredObject<>();
	private final    DeferredObject<AccessBus, Void, Void> accessBusPromise = new DeferredObject<>();
	private final    Compilation                           compilation;
	private final Subject<ReactiveDimension> dimensionSubject = ReplaySubject.<ReactiveDimension>create();
	private final Subject<Reactivable> reactivableSubject = ReplaySubject.<Reactivable>create();
	Observer<ReactiveDimension> dimensionObserver = new Observer<ReactiveDimension>() {
		@Override
		public void onSubscribe(@NonNull final Disposable d) {

		}

		@Override
		public void onNext(@NonNull final ReactiveDimension aReactiveDimension) {
			//aReactiveDimension.observe();
			throw new Error();
		}

		@Override
		public void onError(@NonNull final Throwable e) {

		}

		@Override
		public void onComplete() {

		}
	};
	Observer<Reactivable> reactivableObserver = new Observer<Reactivable>() {

		@Override
		public void onSubscribe(@NonNull final Disposable d) {

		}

		@Override
		public void onNext(@NonNull final Reactivable aReactivable) {
//			ReplaySubject
			throw new Error();
		}

		@Override
		public void onError(@NonNull final Throwable e) {

		}

		@Override
		public void onComplete() {

		}
	};
	private          AccessBus                             ab;
	private @NotNull ICompilationAccess                    ca;
	private          CompilationBus                        compilationBus;
	private          CompilationRunner                     compilationRunner;
	private          CompilerDriver                        compilerDriver;
	private       List<CompilerInput>        inp;
	private       IPipelineAccess            pa;
	private PipelineLogic pipelineLogic;

	public CompilationEnclosure(final Compilation aCompilation) {
		compilation = aCompilation;

		getPipelineAccessPromise().then(pa -> {
			ab = new AccessBus(getCompilation(), pa);

			accessBusPromise.resolve(ab);

			ab.addPipelinePlugin(new CR_State.EvaPipelinePlugin());
			ab.addPipelinePlugin(new CR_State.DeducePipelinePlugin());
			ab.addPipelinePlugin(new CR_State.WritePipelinePlugin());
			ab.addPipelinePlugin(new CR_State.WriteMesonPipelinePlugin());

			pa._setAccessBus(ab);

			this.pa = pa;
		});
	}

	public void addReactiveDimension(final ReactiveDimension aReactiveDimension) {
		dimensionSubject.onNext(aReactiveDimension);

		reactivableSubject.subscribe(this::addReactive);

//		aReactiveDimension.setReactiveSink(addReactive);
	}

	public void addReactive(Reactivable r) {
		int y = 2;
		//reactivableObserver.onNext(r);
		reactivableSubject.onNext(r);

		//reactivableObserver.
		dimensionSubject.subscribe(new Consumer<ReactiveDimension>() {
			@Override
			public void accept(final ReactiveDimension aReactiveDimension) throws Throwable {
				//r.join(aReactiveDimension);
				r.respondTo(aReactiveDimension);
			}
		});
	}

	public Promise<AccessBus, Void, Void> getAccessBusPromise() {
		return accessBusPromise;
	}

	@Contract(pure = true)
	private Compilation getCompilation() {
		return compilation;
	}

	public ICompilationAccess getCompilationAccess() {
		return ca;
	}

	public CompilationClosure getCompilationClosure() {
		return this.getCompilation().getCompilationClosure();
	}

	public CompilerDriver getCompilationDriver() {
		return getCompilationBus().cd;
	}

	public CompilationBus getCompilationBus() {
		return compilationBus;
	}

	public CompilationRunner getCompilationRunner() {
		return compilationRunner;
	}

	public CompilerDriver getCompilerDriver() {
		return compilerDriver;
	}

	public void setCompilerDriver(final CompilerDriver aCompilerDriver) {
		compilerDriver = aCompilerDriver;
	}

	public List<CompilerInput> getCompilerInput() {
		return inp;
	}

	@Contract(pure = true)
	public Promise<IPipelineAccess, Void, Void> getPipelineAccessPromise() {
		return pipelineAccessPromise;
	}

	public PipelineLogic getPipelineLogic() {
		return pipelineLogic;
	}

	public IPipelineAccess getPipelineAccess() {
		return pa;
	}

	public void setCompilationAccess(@NotNull ICompilationAccess aca) {
		ca = aca;
	}

	public void setCompilationBus(final CompilationBus aCompilationBus) {
		compilationBus = aCompilationBus;
	}

	public void setCompilationRunner(final CompilationRunner aCompilationRunner) {
		compilationRunner = aCompilationRunner;
	}

	public void reactiveJoin(final Reactive aReactive) {
		throw new Error();
	}

	public void setCompilerInput(final List<CompilerInput> aInputs) {
		inp = aInputs;
	}

	public void setPipelineLogic(final PipelineLogic aPipelineLogic) {
		pipelineLogic = aPipelineLogic;
	}

	public void addReactive(Reactive r) {
		dimensionSubject.subscribe(new Consumer<ReactiveDimension>() {
			@Override
			public void accept(@NonNull ReactiveDimension dim) throws Throwable {
				r.join(dim);
			}
		});
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
