package tripleo.elijah.stages.deduce;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.util.NotImplementedException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created 8/3/20 8:41 AM
 */
public class DeduceUtils {
	static class IsConstructor implements com.google.common.base.Predicate<OS_Element> {
		@Override
		public boolean apply(@Nullable final OS_Element input) {
			return input instanceof ConstructorDef;
		}
	}

	static class MatchArgs implements Predicate<OS_Element2> {

		private final ExpressionList args;

		public MatchArgs(final ExpressionList args) {
			this.args = args;
		}

		@Override
		public boolean apply(@Nullable final OS_Element2 input) {
			if (!(input instanceof FunctionDef)) return false;
			//
			if (args == null && ((FunctionDef) input).fal().falis().size() == 0)
				return true;
			else {
				NotImplementedException.raise();
				return false; // TODO implement me
			}
		}
	}

	static class MatchConstructorArgs implements java.util.function.Predicate {
		private final ProcedureCallExpression pce;

		public MatchConstructorArgs(final ProcedureCallExpression pce) {
			this.pce = pce;
		}

		@Override
		public boolean test(final @NotNull Object o) {
			final ExpressionList args = pce.getArgs();
			// See if candidate matches args
			if (((LookupResult) o).getElement() instanceof final @NotNull ClassStatement klass) {
				//o filter isCtor each (each args isCompat)

				final @NotNull Iterable<ClassItem> ctors  = Iterables.filter(klass.getItems(), new IsConstructor());
				final @NotNull Iterable<ClassItem> ctors2 = Iterables.filter(ctors, new MatchFunctionArgs(pce));
//				return ctors.iterator().hasNext();
				return Lists.newArrayList(ctors2).size() > 0;

//				return true; // TODO
			}
			tripleo.elijah.util.Stupidity.println_out_2(String.valueOf(o));
			return false;
		}
	}

	static class MatchFunctionArgs implements com.google.common.base.Predicate<OS_Element> {
		private final ProcedureCallExpression pce;

		public MatchFunctionArgs(final ProcedureCallExpression pce) {
			this.pce = pce;
		}

		@Override
		public boolean apply(final OS_Element o) {
			assert o instanceof ClassItem;
			//  TODO what about __call__ and __ctor__ for ClassStatement?
//			tripleo.elijah.util.Stupidity.println_out_2("2000 "+o);
			if (!(o instanceof FunctionDef)) return false;
			//
			final ExpressionList args = pce.getArgs();
			// See if candidate matches args
			/*if (((LookupResult)o).getElement() instanceof FunctionDef)*/
			{
				//o filter isCtor each (each args isCompat)
				final @NotNull FunctionDef fd = (FunctionDef) (/*(LookupResult)*/o)/*.getElement()*/;
				final List<OS_Element2> matching_functions = fd.items()
						.stream()
						.filter(new MatchArgs(pce.getArgs()))
						.collect(Collectors.toList());
				return matching_functions.size() > 0;
			}
//			return false;
		}
	}
}

//
//
//
