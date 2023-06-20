/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.contexts;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.ContextImpl;
import tripleo.elijah.lang.impl.QualidentImpl;

import java.util.List;

/**
 * Created 8/15/20 7:09 PM
 */
public class ImportContext extends ContextImpl implements Context {
	private final Context         _parent;
	private final ImportStatement carrier;

	public ImportContext(final Context aParent, final ImportStatement imp) {
		_parent = aParent;
		carrier = imp;
	}

	@Override
	public Context getParent() {
		return _parent;
	}

	@Override
	public LookupResultList lookup(final String name, final int level, final LookupResultList Result, final List<Context> alreadySearched, final boolean one) {
		alreadySearched.add(this);
//		tripleo.elijah.util.Stupidity.println_err_2("2002 "+importStatement.importList());
		Compilation compilation = compilation();
		for (final Qualident importStatementItem : carrier.parts()) {
//			tripleo.elijah.util.Stupidity.println_err_2("2005 "+importStatementItem);
			if (compilation.isPackage(importStatementItem.toString())) {
				final OS_Package aPackage = compilation.getPackage(importStatementItem);
//				LogEvent.logEvent(4003 , ""+aPackage.getElements());
				for (final OS_Element element : aPackage.getElements()) {
//					tripleo.elijah.util.Stupidity.println_err_2("4002 "+element);
					if (element instanceof NamespaceStatement && ((NamespaceStatement) element).getKind() == NamespaceTypes.MODULE) {
//		                LogEvent.logEvent(4103, "");
						final NamespaceContext namespaceContext = (NamespaceContext) element.getContext();
						alreadySearched.add(namespaceContext);
						namespaceContext.lookup(name, level, Result, alreadySearched, true);
					} else if (element instanceof final OS_Element2 element2) {
						if (element2.name().equals(name)) {
							Result.add(name, level, element, this);
							break; // shortcut: should only have one in scope
						}
					}
				}
			} else {
				// find directly imported elements
				List<IdentExpression> x    = importStatementItem.parts();
				final IdentExpression last = x.get(x.size() - 1);
				if (last.getText().equals(name)) {
					Qualident cl = new QualidentImpl();
					for (int i = 0; i < x.size() - 1; i++) {
						cl.append(x.get(i));
					}
					// SAME AS ABOVE, WITH ADDITIONS
					if (compilation.isPackage(cl.toString())) {
						final OS_Package aPackage = compilation.getPackage(cl);
//						LogEvent.logEvent(4003 , ""+aPackage.getElements());
						for (final OS_Element element : aPackage.getElements()) {
//							tripleo.elijah.util.Stupidity.println_err_2("4002 "+element);
							if (element instanceof NamespaceStatement && ((NamespaceStatement) element).getKind() == NamespaceTypes.MODULE) {
//		                		LogEvent.logEvent(4103, "");
								final NamespaceContext namespaceContext = (NamespaceContext) element.getContext();
								alreadySearched.add(namespaceContext);
								LookupResultList xxx = namespaceContext.lookup(name, level, Result, alreadySearched, true);
								for (LookupResult result : xxx.results()) {
									Result.add(result.getName(), result.getLevel(), result.getElement(), result.getContext());
								}
							} else {
								if (element instanceof OS_Element2) {
									final String element_name = ((OS_Element2) element).name();
									if (element_name.equals(name)) {
										Result.add(name, level, element, aPackage.getContext()); // TODO which context do we set it to?
									}
								}
							}
						}
					}
				}
			}
		}
		if (carrier.getParent() != null) {
			final Context context = getParent();
			if (!alreadySearched.contains(context) || !one)
				return context.lookup(name, level + 1, Result, alreadySearched, false);
		}
		return Result;
	}

	@Override
	public @NotNull Compilation compilation() {
		return module().getCompilation();
	}

	@Override
	public @NotNull OS_Module module() {
		return _parent.module();
	}

}

//
//
//
