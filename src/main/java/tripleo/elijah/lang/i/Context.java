package tripleo.elijah.lang.i;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import tripleo.elijah.comp.Compilation;

public interface Context {
	@NotNull
	Compilation compilation();

	Context getParent();

	LookupResultList lookup(@NotNull String name);

	LookupResultList lookup(String name, int level, LookupResultList Result, List<Context> alreadySearched,
							boolean one);

	@NotNull
	OS_Module module();
}
