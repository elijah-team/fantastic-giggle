package tripleo.elijah.lang.i;

import java.util.List;

public interface ClassHeader {
	List<AnnotationClause> annos();

	TypeNameList genericPart();

	ClassInheritance inh();

	ClassInheritance inheritancePart();

	IdentExpression nameToken();

	void setConst(boolean aIsConst);

	void setGenericPart(TypeNameList aTypeNameList);

	void setName(IdentExpression aNameToken);

	void setType(ClassTypes ct);

	ClassTypes type();
}
