package tripleo.elijah.stages.deduce;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stateful.DefaultStateful;
import tripleo.elijah.stateful.State;
import tripleo.elijah.stateful.Stateful;

public interface IStateRunnable extends Stateful {
	void run();

	class ST {
		public static State EXIT_RUN;

		public static void register(final @NotNull DeducePhase aDeducePhase) {
			EXIT_RUN = aDeducePhase.register(new ExitRunState());
		}

		private static class ExitRunState implements State {
			private boolean runAlready;

			@Override
			public void apply(final DefaultStateful element) {
//                              boolean b = ((StatefulBool) element).getValue();
				if (!runAlready) {
					runAlready = true;
					((StatefulRunnable) element).run();
				}
			}

			@Override
			public boolean checkState(final DefaultStateful aElement3) {
				return true;
			}

			@Override
			public void setIdentity(final int aId) {

			}

		}

	}
}