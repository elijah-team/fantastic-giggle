package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.i.CompilationChange;
import tripleo.elijah.comp.i.CompilationEnclosure;
import tripleo.elijah.comp.i.ICompilationBus;
import tripleo.elijah.comp.i.ILazyCompilerInstructions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static tripleo.elijah.util.Helpers.List_of;

public class CompilationBus implements ICompilationBus {
	public final CompilerDriver cd;
	private final Compilation c;
	private List<CB_Process> _processes = new ArrayList<>();

	public CompilationBus(final @NotNull CompilationEnclosure ace) {
		c  = ace.getCompilationAccess().getCompilation();
		cd = new CompilerDriver(this);

		ace.setCompilerDriver(cd);
	}

	@Override
	public void add(final @NotNull CB_Action action) {
		_processes.add(new SingleActionProcess(action));
	}

	@Override
	public void add(final @NotNull CB_Process aProcess) {
		_processes.add(aProcess);
		//aProcess.steps().stream().forEach(CB_Action::execute);
	}

	@Override
	public void inst(final @NotNull ILazyCompilerInstructions aLazyCompilerInstructions) {
		System.out.println("** [ci] " + aLazyCompilerInstructions.get());
	}

	@Override
	public void option(final @NotNull CompilationChange aChange) {
		aChange.apply(c);
	}

	@Override
	public List<CB_Process> processes() {
		return _processes;
	}

	static class ProcessCollection<E extends CB_Process> implements Collection<E> {
		@Override
		public int size() {
			return 0;
		}

		@Override
		public boolean contains(final Object o) {
			return false;
		}

		@NotNull
		@Override
		public Object[] toArray() {
			return new Object[0];
		}

		@NotNull
		@Override
		public <T> T[] toArray(@NotNull final T[] a) {
			return null;
		}

		@Override
		public boolean add(final E aE) {
			return false;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@NotNull
		@Override
		public Iterator<E> iterator() {
			return new ProcessIterator(this);
		}

		@Override
		public boolean remove(final Object o) {
			return false;
		}

		@Override
		public boolean removeAll(@NotNull final Collection<?> c) {
			return false;
		}

		@Override
		public boolean retainAll(@NotNull final Collection<?> c) {
			return false;
		}

		@Override
		public boolean containsAll(@NotNull final Collection<?> c) {
			return false;
		}

		@Override
		public boolean addAll(@NotNull final Collection<? extends E> c) {
			return false;
		}

		@Override
		public void clear() {

		}
	}

	static class ProcessIterator<E extends CB_Process> implements Iterator<E> {

		int index;
		private final ProcessCollection pc;

		public <E extends CB_Process> ProcessIterator(final ProcessCollection apc) {
			pc = apc;
		}

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public E next() {
			return null;
		}
	}

	static class SingleActionProcess implements CB_Process {

		private final CB_Action a;

		public SingleActionProcess(final CB_Action aAction) {
			a = aAction;
		}

		@Override
		public List<CB_Action> steps() {
			return List_of(a);
		}
	}
}
