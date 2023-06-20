package tripleo.elijah.stages.gen_generic;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.gen_c.GenerateC;
import tripleo.elijah.util.NotImplementedException;

import java.util.Objects;

public enum OutputFileFactory {
	;

	@Contract("_, _ -> new")
	public static @NotNull GenerateFiles create(final @NotNull String lang,
												final @NotNull OutputFileFactoryParams params) {
		if (Objects.equals(lang, "c")) {
			return new GenerateC(params);
		} else
			throw new NotImplementedException();
	}
}
