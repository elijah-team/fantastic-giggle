package tripleo.elijah.lang.i;

import tripleo.elijah.lang2.ElElementVisitor;

public interface TypeAliasStatement extends OS_Element {
	Context getContext();

	OS_Element getParent();

	void make(IdentExpression x, Qualident y);

	void setBecomes(Qualident qq);

	void setIdent(IdentExpression aToken);

	void visitGen(ElElementVisitor visit);
}
