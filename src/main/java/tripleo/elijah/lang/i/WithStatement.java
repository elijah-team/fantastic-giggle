package tripleo.elijah.lang.i;

import antlr.Token;
import tripleo.elijah.contexts.WithContext;
import tripleo.elijah.lang2.ElElementVisitor;

import java.util.Collection;
import java.util.List;

public interface WithStatement extends FunctionItem {
	void add(OS_Element anElement);

	void addDocString(Token aText);

	Context getContext();

	List<FunctionItem> getItems();

	OS_Element getParent();

	Collection<VariableStatement> getVarItems();

	List<OS_Element2> items();

	VariableStatement nextVarStmt();

	void postConstruct();

	void scope(Scope3 sco);

	void visitGen(ElElementVisitor visit);

	void setContext(WithContext ctx);
}
