package tripleo.elijah.stages.deduce.post_bytecode;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.i.ErrSink;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.*;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;

class DeduceType3 implements DED {
	private final IDeduceElement3 deduceElement3;
	private final Diagnostic diagnostic;

	private       GenType         _genType;
	private final OS_Type osType;

	public DeduceType3(final OS_Type aOSType, final Diagnostic aDiagnostic) {
		deduceElement3 = null;
		osType         = aOSType;
		diagnostic     = aDiagnostic;
	}

	public static IDeduceElement3 dispatch(final IdentTableEntry aIdentTableEntry, final DeduceTypes2 aDeduceTypes2, final BaseEvaFunction aGeneratedFunction) {
		return aIdentTableEntry.getDeduceElement3(aDeduceTypes2, aGeneratedFunction);
	}

	public DeduceType3(final IDeduceElement3 aDeduceElement3, final OS_Type aOSType, final Diagnostic aDiagnostic1) {
		deduceElement3 = aDeduceElement3;
		osType         = aOSType;
		diagnostic     = aDiagnostic1;
	}

//	public static IDeduceElement3 dispatch(final @NotNull IdentTableEntry aIdentTableEntry) {
//		return aIdentTableEntry.getDeduceElement3(null/*aDeduceTypes2*/, null/*aGeneratedFunction*/);
//	}

//	public static IDeduceElement3 dispatch(final @NotNull ConstantTableEntry aConstantTableEntry) {
//		return aConstantTableEntry.getDeduceElement3();
//	}

	public static IDeduceElement3 dispatch(final @NotNull VariableTableEntry aVariableTableEntry) {
		return aVariableTableEntry.getDeduceElement3();
	}

	public GenType getGenType() {
		if (_genType == null) {
			_genType          = new GenType();
			_genType.resolved = osType;
		}

		return _genType;
	}

	public boolean isException() {
		return diagnostic != null;
	}

	@Override
	public Kind kind() {
		return Kind.DED_Kind_Type;
	}

	public void reportDiagnostic(final @NotNull ErrSink aErrSink) {
		assert isException();

		aErrSink.reportDiagnostic(diagnostic);
	}
}
