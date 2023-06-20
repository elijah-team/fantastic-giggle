/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.contexts;

import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.ContextImpl;
import tripleo.elijah.lang.impl.MatchConditionalImpl;
import tripleo.elijah.lang.impl.VariableSequenceImpl;

import java.util.List;

/**
 * Created 10/6/20 4:22 PM
 */
public class MatchConditionalContext extends ContextImpl {
	private final Context                  _parent;
	private final MatchConditionalImpl.MC1 carrier;

	public MatchConditionalContext(final Context parent, final MatchConditionalImpl.MC1 part) {
		this._parent = parent;
		this.carrier = part;
	}

	@Override
	public Context getParent() {
		return _parent;
	}

	@Override
	public LookupResultList lookup(final String name, final int level, final LookupResultList Result, final List<Context> alreadySearched, final boolean one) {
		alreadySearched.add(carrier.getContext());

		if (carrier instanceof final MatchConditionalImpl.MatchArm_TypeMatch carrier2) {
			if (name.equals(carrier2.getIdent().getText()))
				Result.add(name, level, carrier2, this);
		}

		for (final FunctionItem item : carrier.getItems()) {
			if (!(item instanceof ClassStatement) &&
					!(item instanceof NamespaceStatement) &&
					!(item instanceof FunctionDef) &&
					!(item instanceof VariableSequenceImpl)
			) continue;
			if (item instanceof OS_Element2) {
				if (((OS_Element2) item).name().equals(name)) {
					Result.add(name, level, item, this);
				}
			} else if (item instanceof VariableSequenceImpl) {
//				tripleo.elijah.util.Stupidity.println_out_2("[FunctionContext#lookup] VariableSequenceImpl "+item);
				for (final VariableStatement vs : ((VariableSequenceImpl) item).items()) {
					if (vs.getName().equals(name))
						Result.add(name, level, vs, this);
				}
			}
		}

		/*if (carrier.getParent() != null)*/
		{
			final Context context = getParent();
			if (!alreadySearched.contains(context) || !one)
				context.lookup(name, level + 1, Result, alreadySearched, false);
		}
		return Result;

	}

}

//
//
//
