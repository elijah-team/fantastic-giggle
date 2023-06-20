/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.FailCallback;
import org.jdeferred2.Promise;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.lang.i.OS_Element;
import tripleo.elijah.lang.impl.AliasStatementImpl;
import tripleo.elijah.stages.deduce.DeduceTypeResolve;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.ResolveError;
import tripleo.elijah.stages.deduce.ResolveUnknown;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 2/4/21 10:11 PM
 */
public abstract class BaseTableEntry {
	private final DeferredObject2<OS_Element, Diagnostic, Void> elementPromise = new DeferredObject2<OS_Element, Diagnostic, Void>();
	private final List<StatusListener> statusListenerList = new ArrayList<StatusListener>();
	public        DeduceTypes2                                  __dt2;
	public        BaseEvaFunction                               __gf;
	protected OS_Element resolved_element;
	// region status
	protected Status status = Status.UNCHECKED;
	DeduceTypeResolve typeResolve;

	// region resolved_element

	public void _fix_table(final DeduceTypes2 aDeduceTypes2, final @NotNull BaseEvaFunction aEvaFunction) {
		__dt2 = aDeduceTypes2;
		__gf  = aEvaFunction;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status newStatus, IElementHolder eh) {
		status = newStatus;
		assert newStatus != Status.KNOWN || eh.getElement() != null;
		for (int i = 0; i < statusListenerList.size(); i++) {
			final StatusListener statusListener = statusListenerList.get(i);
			statusListener.onChange(eh, newStatus);
		}
		if (newStatus == Status.UNKNOWN)
			if (!elementPromise.isRejected())
				elementPromise.reject(new ResolveUnknown());
	}

	public void addStatusListener(StatusListener sl) {
		statusListenerList.add(sl);
	}

	public void elementPromise(DoneCallback<OS_Element> dc, FailCallback<Diagnostic> fc) {
		if (dc != null)
			elementPromise.then(dc);
		if (fc != null)
			elementPromise.fail(fc);
	}

	public OS_Element getResolvedElement() {
		return resolved_element;
	}

	// endregion resolved_element

	public Promise<GenType, ResolveError, Void> typeResolvePromise() {
		return typeResolve.typeResolution();
	}

	public void setResolvedElement(OS_Element aResolved_element) {
		if (elementPromise.isResolved()) {
			if (resolved_element instanceof AliasStatementImpl) {
				elementPromise.reset();
			} else {
				if (aResolved_element instanceof AliasStatementImpl && resolved_element != null)
					return;
				assert resolved_element == aResolved_element;
				return;
			}
		}
		resolved_element = aResolved_element;
		elementPromise.resolve(resolved_element);
	}


	public enum Status {
		KNOWN, UNCHECKED, UNKNOWN
	}
	// endregion status

	// endregion status

	protected void setupResolve() {
		typeResolve = new DeduceTypeResolve(this);
	}

	@FunctionalInterface
	public interface StatusListener {
		void onChange(IElementHolder eh, Status newStatus);
	}
}

//
//
//
