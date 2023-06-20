package tripleo.elijah.stages.gen_c;

import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.instructions.IntegerIA;

public class IntegerIA_Ops {
	public static IntegerIA_Ops get(final IntegerIA aIntegerIA, final int aSSize) {
		return new IntegerIA_Ops(aIntegerIA, aSSize);
	}

	private final IntegerIA integerIA;

	private final int sSize;

	public IntegerIA_Ops(final IntegerIA aIntegerIA, final int aSSize) {
		integerIA = aIntegerIA;
		sSize     = aSSize;
	}

	public ConstructorPathOp getConstructorPath() {
		EvaNode _resolved = null;

		final VariableTableEntry vte = integerIA.getEntry();

		if (sSize == 1) {
			final EvaNode resolved = vte.type.resolved();
			if (resolved != null) {
				_resolved = resolved;
			} else {
				_resolved = vte.resolvedType();
			}
		}

		final EvaNode final_resolved = _resolved;
		return new ConstructorPathOp() {
			@Override
			public String getCtorName() {
				return null;
			}

			@Override
			public EvaNode getResolved() {
				return final_resolved;
			}
		};
	}
}
