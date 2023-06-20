package tripleo.elijah.lang.i;

import antlr.Token;
import tripleo.elijah.contexts.SyntacticBlockContext;
import tripleo.elijah.lang2.ElElementVisitor;

import java.util.List;

public interface SyntacticBlock extends FunctionItem {
	void add(OS_Element anElement);

	void addDocString(Token s1);

	Context getContext();

	List<FunctionItem> getItems();

	OS_Element getParent();

	List<OS_Element2> items();

	void postConstruct();

	void scope(Scope3 sco);

	void visitGen(ElElementVisitor visit);

	void setContext(SyntacticBlockContext ctx);
}
