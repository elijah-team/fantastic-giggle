/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce.declarations;

import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.lang.i.OS_Element;
import tripleo.elijah.lang.impl.VariableStatementImpl;
import tripleo.elijah.stages.deduce.IInvocation;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_fn.GenType;

/**
 * Created 6/27/21 1:41 AM
 */
public class DeferredMember {
	private final DeferredObject<EvaNode, Void, Void>       externalRef = new DeferredObject<EvaNode, Void, Void>();
	private final IInvocation                               invocation;
	private final OS_Element                                parent;
	private final DeferredObject<GenType, Diagnostic, Void> typePromise = new DeferredObject<GenType, Diagnostic, Void>();
	private final VariableStatementImpl                     variableStatement;

	public DeferredMember(OS_Element aParent, IInvocation aInvocation, VariableStatementImpl aVariableStatement) {
		parent            = aParent;
		invocation        = aInvocation;
		variableStatement = aVariableStatement;
	}

	public Promise<EvaNode, Void, Void> externalRef() {
		return externalRef.promise();
	}

	public @NotNull DeferredObject<EvaNode, Void, Void> externalRefDeferred() {
		return externalRef;
	}

	public IInvocation getInvocation() {
		return invocation;
	}

	public OS_Element getParent() {
		return parent;
	}

	public VariableStatementImpl getVariableStatement() {
		return variableStatement;
	}

	@Override
	public @NotNull String toString() {
		return "DeferredMember{" +
				"parent=" + parent +
				", variableName=" + variableStatement.getName() +
				'}';
	}

	public @NotNull Promise<GenType, Diagnostic, Void> typePromise() {
		return typePromise;
	}

	// for DeducePhase
	public @NotNull DeferredObject<GenType, Diagnostic, Void> typeResolved() {
		return typePromise;
	}
}

//
//
//
