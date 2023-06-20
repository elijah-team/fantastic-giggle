package tripleo.elijah.lang.i;

import tripleo.elijah.contexts.MatchContext;
import tripleo.elijah.lang.impl.MatchConditionalImpl;
import tripleo.elijah.lang2.ElElementVisitor;

import java.util.List;

public interface MatchConditional extends OS_Element, StatementItem, FunctionItem {
	void expr(IExpression expr);

	@Override
	Context getContext();

	@Override
	OS_Element getParent();

	IExpression getExpr();

	void setParent(OS_Element aParent);

	List<MC1> getParts();

	@Override
	void visitGen(ElElementVisitor visit);

	void postConstruct();

	void setContext(MatchContext ctx);

	MatchConditionalImpl.MatchConditionalPart2 normal();

	//
	//
	//
	MatchConditionalImpl.MatchArm_TypeMatch typeMatch();

	MatchConditionalImpl.MatchConditionalPart3 valNormal();

	public interface MC1 extends OS_Element, Documentable {
		void add(FunctionItem aItem);

		@Override
		Context getContext();

		@Override
		default void visitGen(ElElementVisitor visit) {
			visit.visitMC1(this);
		}

		Iterable<? extends FunctionItem> getItems();
	}
}
