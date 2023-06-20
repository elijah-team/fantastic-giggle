package tripleo.elijah.stages.deduce.fluffy.i;

import tripleo.elijah.diagnostic.Locatable;
import tripleo.elijah.nextgen.composable.IComposable;

public interface FluffyVar {
	String name();

	IComposable nameComposable();

	Locatable nameLocatable();

	FluffyVarTarget target();
}
