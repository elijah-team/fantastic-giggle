package tripleo.elijah.stateful;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stateful.State;

public interface Stateful {
	void mvState(State aO, @NotNull State aState);

	void setState(final State aState);
}
