package tripleo.elijah.stages.gen_c;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.i.FunctionDef;
import tripleo.elijah.lang.i.OS_Element;
import tripleo.elijah.lang.impl.VariableSequenceImpl;
import tripleo.elijah.lang.impl.VariableStatementImpl;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_fn.IEvaFunctionBase;

public class GI_VariableStatement implements GenerateC_Item {
	private       EvaNode               _evaNode;
	private final GI_Repo               repo;
	private final VariableStatementImpl variableStatement;

	public GI_VariableStatement(final VariableStatementImpl aVariableStatement, final GI_Repo aRepo) {
		variableStatement = aVariableStatement;
		repo              = aRepo;
	}

	/**
	 * Create a {@link tripleo.elijah.stages.gen_c.CReference.Reference} for {@link this.variableStatement}
	 *
	 * <p>If the parent of the variableStatemnt is the same as the generatedFunction's parent,
	 * create a DIRECT_MEMBER with the string {@code value}
	 * </p>
	 * <p>If the parent of the variableStatemnt is the same as the generatedFunction,
	 * create a LOCAL with no value
	 * </p>
	 * <p>Otherwise, create a MEMBER with the string {@code value} for value
	 * </p>
	 */
	@Contract("_, _ -> new")
	private CReference.@NotNull Reference __createReferenceForVariableStatement(final @NotNull IEvaFunctionBase generatedFunction, final @Nullable String value) {
		final String text2 = variableStatement.getName();

		// first getParent is VariableSequenceImpl
		final VariableSequenceImpl variableSequence = (VariableSequenceImpl) variableStatement.getParent();
		final OS_Element           parent           = variableSequence.getParent();

		final FunctionDef fd = generatedFunction.getFD();

		if (parent == fd.getParent()) {
			// A direct member value. Doesn't handle when indirect
//				text = Emit.emit("/*124*/")+"vsc->vm" + text2;
			return new CReference.Reference(text2, CReference.Ref.DIRECT_MEMBER, value);
		} else if (parent == fd) {
			return new CReference.Reference(text2, CReference.Ref.LOCAL);
		}

		//if (parent instanceof NamespaceStatement) {
		//	int y=2;
		//}

		return new CReference.Reference(text2, CReference.Ref.MEMBER, value);
	}

	public void _createReferenceForVariableStatement(final @NotNull CReference aCReference,
													 final @NotNull IEvaFunctionBase generatedFunction,
													 final @Nullable String value) {
		final CReference.Reference r = __createReferenceForVariableStatement(generatedFunction, value);
		aCReference.addRef(r);
	}

	@Override
	public EvaNode getEvaNode() {
		return _evaNode;
	}

	@Override
	public void setEvaNode(final EvaNode aEvaNode) {
		_evaNode = aEvaNode;
	}
}
