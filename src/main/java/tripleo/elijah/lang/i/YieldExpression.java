package tripleo.elijah.lang.i;

import tripleo.elijah.lang2.ElElementVisitor;

public interface YieldExpression extends IExpression, OS_Element {
	Context getContext();

	OS_Element getParent();

	void visitGen(ElElementVisitor visit);
}
