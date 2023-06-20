package tripleo.elijah.lang.i;

import java.util.Collection;
import java.util.Iterator;

public interface ExpressionList extends Iterable<IExpression> {
	void add(IExpression aExpr);

	Collection<IExpression> expressions();

	@Override
	Iterator<IExpression> iterator();

	IExpression next(IExpression aExpr);

	int size();

	@Override
	String toString();
}
