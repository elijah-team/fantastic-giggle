package tripleo.elijah.stages.gen_c;

import tripleo.elijah.stages.gen_fn.EvaNode;

interface ConstructorPathOp {
	String getCtorName();

	EvaNode getResolved();
}
