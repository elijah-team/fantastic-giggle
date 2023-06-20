package tripleo.elijah.lang.i;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.LibraryStatementPart;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.contexts.ModuleContext;
import tripleo.elijah.entrypoints.EntryPoint;
import tripleo.elijah.lang2.ElElementVisitor;

import java.util.Collection;
import java.util.List;

public interface OS_Module extends OS_Element {
	void add(OS_Element anElement);

	@NotNull
	List<EntryPoint> entryPoints();

	@org.jetbrains.annotations.Nullable
	OS_Element findClass(String aClassName);

	void finish();

	@NotNull
	Compilation getCompilation();

	Context getContext();

	String getFileName();

	@NotNull
	Collection<ModuleItem> getItems();

	LibraryStatementPart getLsp();

	@org.jetbrains.annotations.Nullable
	OS_Element getParent();

	void setParent(@NotNull Compilation parent);

	void visitGen(@NotNull ElElementVisitor visit);

	void postConstruct();

	OS_Module prelude();

	boolean hasClass(String className); // OS_Container

	OS_Package pushPackageNamed(Qualident aPackageName);

	boolean isPrelude();

	OS_Package pullPackageName();

	void setContext(ModuleContext mctx);

	void setFileName(String fileName);

	void setIndexingStatement(IndexingStatement idx);

	void setPrelude(OS_Module success);

	void setLsp(@NotNull LibraryStatementPart lsp);
}
