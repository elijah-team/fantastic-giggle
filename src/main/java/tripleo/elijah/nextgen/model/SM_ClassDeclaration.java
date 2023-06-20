package tripleo.elijah.nextgen.model;

public interface SM_ClassDeclaration extends SM_Node {
	SM_ClassBody classBody();

	SM_ClassInheritance inheritance();

	SM_Name name();

	SM_ClassSubtype subType();
}
