/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import org.jdeferred2.Deferred;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.i.Context;
import tripleo.elijah.lang.i.OS_Element;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_IdentTableEntry;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.BaseTableEntry;
import tripleo.elijah.stages.gen_fn.IElementHolder;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.util.Holder;

import java.text.MessageFormat;

/**
 * Created 11/22/21 8:23 PM
 */
public class DeduceElementIdent implements IDeduceElement_old {
	private final Deferred<OS_Element, Void, Void> _resolvedElementPromise = new DeferredObject<>();
	private       Context                          context;
	private final IdentTableEntry                  identTableEntry;
	private       BaseEvaFunction                  generatedFunction;
	private       DeduceTypes2                     deduceTypes2;
	public DeduceElementIdent(final IdentTableEntry aIdentTableEntry) {
		identTableEntry = aIdentTableEntry;

		if (false) {
			if (identTableEntry.isResolved()) {
				resolveElement(identTableEntry.getResolvedElement()); // README hmm 06/19
			}
		}

		identTableEntry.addStatusListener(new BaseTableEntry.StatusListener() {
			@Override
			public void onChange(final IElementHolder eh, final BaseTableEntry.Status newStatus) {
				if (newStatus == BaseTableEntry.Status.KNOWN) {
					resolveElement(eh.getElement());
				}
			}
		});
	}

	public IdentTableEntry getIdentTableEntry() {
		return identTableEntry;
	}

	public OS_Element getResolvedElement() {
		if (deduceTypes2 == null) { // TODO remove this ASAP. Should never happen
			tripleo.elijah.util.Stupidity.println_err_2("5454 Should never happen. gf is not deduced.");
			return null;
		}

		final Holder<OS_Element> holder = new Holder<>();

		boolean rp = false;

		if (deduceTypes2.hasResolvePending(identTableEntry)) {
			identTableEntry.elementPromise(holder::set, null);
			final DeducePath                     dp  = identTableEntry.buildDeducePath(generatedFunction);
			final DeduceElement3_IdentTableEntry de3 = (DeduceElement3_IdentTableEntry) identTableEntry.getDeduceElement3(deduceTypes2, generatedFunction);

			de3.sneakResolve();

			rp = true;
		} else {
			deduceTypes2.addResolvePending(identTableEntry, this, holder);

			final DeduceElement3_IdentTableEntry de3 = (DeduceElement3_IdentTableEntry) identTableEntry.getDeduceElement3(deduceTypes2, generatedFunction);
			de3.sneakResolve();
		}

		final boolean[] is_set = {false};

		if (!rp) {
			final DeduceTypes2.PromiseExpectation<OS_Element> pe1 = deduceTypes2.promiseExpectation(identTableEntry, "DeduceElementIdent getResolvedElement");

			deduceTypes2.resolveIdentIA_(context, this, generatedFunction, new FoundElement(deduceTypes2.phase) {
				@Override
				public void foundElement(final OS_Element e) {
					is_set[0] = true;
					holder.set(e);
					pe1.satisfy(e);
					deduceTypes2.LOG.info(MessageFormat.format("DeduceElementIdent: found element for {0} {1}", identTableEntry, e));

					deduceTypes2.removeResolvePending(identTableEntry);
				}

				@Override
				public void noFoundElement() {
					deduceTypes2.LOG.err("DeduceElementIdent: can't resolve element for " + identTableEntry);

					deduceTypes2.removeResolvePending(identTableEntry);
				}
			});
		}
		final OS_Element R = holder.get();
		//tripleo.elijah.util.Stupidity.println_err_2(MessageFormat.format("8989 {0} {1}", R != null, is_set[0]));
		return R;
	}

	public Promise<OS_Element, Void, Void> resolvedElementPromise() {
		return _resolvedElementPromise.promise();
	}

	public void resolveElement(final OS_Element aElement) {
		if (!_resolvedElementPromise.isResolved())
			_resolvedElementPromise.resolve(aElement);
	}

	public void setDeduceTypes2(final DeduceTypes2 aDeduceTypes2, final Context aContext, final @NotNull BaseEvaFunction aGeneratedFunction) {
		deduceTypes2      = aDeduceTypes2;
		context           = aContext;
		generatedFunction = aGeneratedFunction;
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
