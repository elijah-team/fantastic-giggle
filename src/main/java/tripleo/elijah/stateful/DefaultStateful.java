package tripleo.elijah.stateful;

import org.jetbrains.annotations.NotNull;

public abstract class DefaultStateful implements Stateful {
	private State _state;

	@Override
	public void mvState(final State aO, @NotNull final State aState) {
		assert aO == null;

		if (!aState.checkState(this)) {
			//throw new BadState();
			return;
		}

		aState.apply(this);
		this.setState(aState);
	}

	@Override
	public void setState(final State aState) {
		_state = aState;
	}
}
