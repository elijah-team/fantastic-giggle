/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce.declarations;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.lang.i.FunctionDef;
import tripleo.elijah.lang.i.OS_Element;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.deduce.IInvocation;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaFunction;
import tripleo.elijah.stages.gen_fn.GenType;

/**
 * Created 11/21/21 6:32 AM
 */
public class DeferredMemberFunction {
	private final DeduceTypes2                                deduceTypes2;
	private final DeferredObject<BaseEvaFunction, Void, Void> externalRef = new DeferredObject<BaseEvaFunction, Void, Void>();
	private final FunctionDef                                 functionDef;
	private final FunctionInvocation                          functionInvocation;
	/**
	 * A {@link tripleo.elijah.stages.deduce.ClassInvocation} or {@link tripleo.elijah.stages.deduce.NamespaceInvocation}.
	 * useless if parent is a {@link tripleo.elijah.stages.deduce.DeduceTypes2.OS_SpecialVariable} and its
	 * {@link tripleo.elijah.stages.deduce.DeduceTypes2.OS_SpecialVariable#memberInvocation} role value is
	 * {@link tripleo.elijah.stages.deduce.DeduceTypes2.MemberInvocation.Role#INHERITED}
	 */
	private       IInvocation                                 invocation;
	private final OS_Element                                  parent;
	private final DeferredObject<GenType, Diagnostic, Void>   typePromise = new DeferredObject<GenType, Diagnostic, Void>();

	public DeferredMemberFunction(final @NotNull OS_Element aParent,
								  final @Nullable IInvocation aInvocation,
								  final @NotNull FunctionDef aBaseFunctionDef,
								  final @NotNull DeduceTypes2 aDeduceTypes2,
								  final @NotNull FunctionInvocation aFunctionInvocation) { // TODO can this be nullable?
		parent             = aParent;
		invocation         = aInvocation;
		functionDef        = aBaseFunctionDef;
		deduceTypes2       = aDeduceTypes2;
		functionInvocation = aFunctionInvocation;
		//
		functionInvocation.generatePromise().then(new DoneCallback<BaseEvaFunction>() {
			@Override
			public void onDone(final BaseEvaFunction result) {
				deduceTypes2.deduceOneFunction((EvaFunction) result, deduceTypes2.phase); // !!
				result.typePromise().then(new DoneCallback<GenType>() {
					@Override
					public void onDone(final GenType result) {
						typePromise.resolve(result);
					}
				});
			}
		});
	}

	public Promise<BaseEvaFunction, Void, Void> externalRef() {
		return externalRef.promise();
	}

	public @NotNull DeferredObject<BaseEvaFunction, Void, Void> externalRefDeferred() {
		return externalRef;
	}

	public FunctionInvocation functionInvocation() {
		return functionInvocation;
	}

	public FunctionDef getFunctionDef() {
		return functionDef;
	}

	public IInvocation getInvocation() {
		if (invocation == null) {
			if (parent instanceof final DeduceTypes2.OS_SpecialVariable specialVariable) {
				invocation = specialVariable.getInvocation(deduceTypes2);
			}
		}
		return invocation;
	}

	public OS_Element getParent() {
		return parent;
	}

	@Override
	public @NotNull String toString() {
		return "DeferredMemberFunction{" +
				"parent=" + parent +
				", functionName=" + functionDef.name() +
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
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
