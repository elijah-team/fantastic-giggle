package tripleo.elijah.stages.deduce.post_bytecode;

import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.*;
import tripleo.elijah.nextgen.query.Operation2;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;

/**
 * Also {@link tripleo.elijah.stages.deduce.post_bytecode.DeduceType3}
 */
public interface DeduceElement3_Type {
	GenType genType();

	Operation2<GenType> resolved(Context ectx);

	TypeTableEntry typeTableEntry();
}
