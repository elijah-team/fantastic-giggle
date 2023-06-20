package tripleo.elijah.stages.deduce.fluffy.i;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import tripleo.elijah.lang.i.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public interface FluffyComp {
	default void find_multiple_items(final OS_Module aModule) {
		final Multimap<String, ModuleItem> items_map = ArrayListMultimap.create(aModule.getItems().size(), 1);

		aModule.getItems().stream()
				.filter(Objects::nonNull)
				.filter(x -> !(x instanceof ImportStatement))
				.forEach(item -> {
					// README likely for member functions.
					// README Also note elijah has single namespace
					items_map.put(item.name(), item);
				});

		for (final String key : items_map.keys()) {
			boolean warn = false;

			final Collection<ModuleItem> moduleItems = items_map.get(key);
			if (moduleItems.size() == 1)
				continue;

			final Collection<ElObjectType> t = moduleItems
					.stream()
					.map(DecideElObjectType::getElObjectType)
					.collect(Collectors.toList());

			final Set<ElObjectType> st = new HashSet<ElObjectType>(t);
			if (st.size() > 1)
				warn = true;
			if (moduleItems.size() > 1)
				if (moduleItems.iterator().next() instanceof NamespaceStatement && st.size() == 1)
					;
				else
					warn = true;

			//
			//
			//

			if (warn) {
				final String module_name = aModule.toString(); // TODO print module name or something
				final String s = String.format(
						"[Module#add] %s Already has a member by the name of %s",
						module_name, key);
				aModule.getCompilation().getErrSink().reportWarning(s);
			}
		}
	}

	FluffyModule module(OS_Module aModule);
}
