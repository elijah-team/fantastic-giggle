/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.i.ErrSink;
import tripleo.elijah.lang.i.Context;
import tripleo.elijah.lang.i.IExpression;
import tripleo.elijah.lang.i.OS_Type;
import tripleo.elijah.stages.deduce.*;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_ProcTableEntry;
import tripleo.elijah.stages.deduce.post_bytecode.IDeduceElement3;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 9/12/20 10:07 PM
 */
public class ProcTableEntry extends BaseTableEntry implements TableEntryIV {
	public final  List<TypeTableEntry>                       args;
	/**
	 * Either a hint to the programmer-- The compiler should be able to work without this.
	 * <br/>
	 * Or for synthetic methods
	 */
	public final  IExpression                                expression;
	public final  InstructionArgument                        expression_num;
	public final int                index;
	private final DeferredObject<ProcTableEntry, Void, Void> completeDeferred = new DeferredObject<ProcTableEntry, Void, Void>();
	private final DeferredObject<GenType, Void, Void> typeDeferred = new DeferredObject<GenType, Void, Void>();
	public        DeduceProcCall                             dpc              = new DeduceProcCall(this);
	ExpressionConfession expressionConfession = new ExpressionConfession() {
		@Override
		public ECT getType() {
			return ECT.exp;
		}
	};
	private       DeduceElement3_ProcTableEntry              _de3;
	private       ClassInvocation                            classInvocation;
	private      FunctionInvocation functionInvocation;

	private final DeferredObject2<FunctionInvocation, Void, Void> onFunctionInvocations = new DeferredObject2<FunctionInvocation, Void, Void>();

	public ProcTableEntry(final int aIndex, final IExpression aExpression, final InstructionArgument aExpressionNum, final List<TypeTableEntry> aArgs) {
		index          = aIndex;
		expression     = aExpression;
		expression_num = aExpressionNum;

//		expressionConfession = ExpressionConfession.from(expression, expression_num);

		args = aArgs;

		addStatusListener(new StatusListener() {
			@Override
			public void onChange(IElementHolder eh, Status newStatus) {
				if (newStatus == Status.KNOWN) {
					setResolvedElement(eh.getElement());
				}
			}
		});

		for (final TypeTableEntry tte : args) {
			tte.addSetAttached(new TypeTableEntry.OnSetAttached() {
				@Override
				public void onSetAttached(final TypeTableEntry aTypeTableEntry) {
					ProcTableEntry.this.onSetAttached();
				}
			});
		}

		setupResolve();
	}

	private DeferredObject<ProcTableEntry, Void, Void> completeDeferred() {
		return completeDeferred;
	}

	public DeduceProcCall deduceProcCall() {
		return dpc;
	}

	public ExpressionConfession expressionConfession() {
		if (expressionConfession == null) {
			if (expression_num == null) {
				expressionConfession = new ExpressionConfession() {
					@Override
					public ECT getType() {
						return ECT.exp;
					}
				};
			} else {
				expressionConfession = new ExpressionConfession() {
					@Override
					public ECT getType() {
						return ECT.exp_num;
					}
				};
			}
		}

		return expressionConfession;
	}

	public ClassInvocation getClassInvocation() {
		return classInvocation;
	}

	public IDeduceElement3 getDeduceElement3() {
		//assert dpc._deduceTypes2() != null; // TODO setDeduce... called; Promise?
		//
		//return getDeduceElement3(dpc._deduceTypes2(), dpc._generatedFunction());

		return getDeduceElement3(__dt2, __gf);
	}

	public IDeduceElement3 getDeduceElement3(final DeduceTypes2 aDeduceTypes2, final BaseEvaFunction aGeneratedFunction) {
		if (_de3 == null) {
			_de3 = new DeduceElement3_ProcTableEntry(this, aDeduceTypes2, aGeneratedFunction);
		}
		return _de3;
	}

	public FunctionInvocation getFunctionInvocation() {
		return functionInvocation;
	}

	@NotNull
	public String getLoggingString(final @Nullable DeduceTypes2 aDeduceTypes2) {
		final String          pte_string;
		@NotNull List<String> l = new ArrayList<String>();

		for (@NotNull TypeTableEntry typeTableEntry : getArgs()) {
			OS_Type attached = typeTableEntry.getAttached();

			if (attached != null)
				l.add(attached.toString());
			else {
				if (aDeduceTypes2 != null)
					aDeduceTypes2.LOG.err("267 attached == null for " + typeTableEntry);

				if (typeTableEntry.expression != null)
					l.add(String.format("<Unknown expression: %s>", typeTableEntry.expression));
				else
					l.add("<Unknkown>");
			}
		}

		final String sb2 = "[" +
				Helpers.String_join(", ", l) +
				"]";
		pte_string = sb2;
		return pte_string;
	}

	public List<TypeTableEntry> getArgs() {
		return args;
	}

	// have no idea what this is for
	public void onFunctionInvocation(final DoneCallback<FunctionInvocation> callback) {
		onFunctionInvocations.then(callback);
	}

	public void setDeduceTypes2(final DeduceTypes2 aDeduceTypes2, final Context aContext, final BaseEvaFunction aGeneratedFunction, final ErrSink aErrSink) {
		dpc.setDeduceTypes2(aDeduceTypes2, aContext, aGeneratedFunction, aErrSink);
	}

	public void onSetAttached() {
		int state = 0;
		if (args != null) {
			final int ac  = args.size();
			int       acx = 0;
			for (TypeTableEntry tte : args) {
				if (tte.getAttached() != null)
					acx++;
			}
			if (acx < ac) {
				state = 1;
			} else if (acx > ac) {
				state = 2;
			} else if (acx == ac) {
				state = 3;
			}
		} else {
			state = 3;
		}
		switch (state) {
		case 0:
			throw new IllegalStateException();
		case 1:
			tripleo.elijah.util.Stupidity.println_err_2("136 pte not finished resolving " + this);
			break;
		case 2:
			tripleo.elijah.util.Stupidity.println_err_2("138 Internal compiler error");
			break;
		case 3:
			if (completeDeferred.isPending())
				completeDeferred.resolve(this);
			break;
		default:
			throw new NotImplementedException();
		}
	}

	public void setArgType(int aIndex, OS_Type aType) {
		args.get(aIndex).setAttached(aType);
	}

	public void setClassInvocation(ClassInvocation aClassInvocation) {
		classInvocation = aClassInvocation;
	}

	public void setExpressionConfession(final @NotNull ExpressionConfession aExpressionConfession) {
		expressionConfession = aExpressionConfession;
	}

	@Override
	@NotNull
	public String toString() {
		return "ProcTableEntry{" +
				"index=" + index +
				", expression=" + expression +
				", expression_num=" + expression_num +
				", status=" + status +
				", args=" + args +
				'}';
	}

	// have no idea what this is for
	public void setFunctionInvocation(FunctionInvocation aFunctionInvocation) {
		if (functionInvocation != aFunctionInvocation) {
			functionInvocation = aFunctionInvocation;
			onFunctionInvocations.reset();
			onFunctionInvocations.resolve(functionInvocation);
		}
	}

	public enum ECT {exp, exp_num}

	public DeferredObject<GenType, Void, Void> typeDeferred() {
		return typeDeferred;
	}

	public Promise<GenType, Void, Void> typePromise() {
		return typeDeferred.promise();
	}

	//public PTE_Zero zero() {
	//	if (_zero == null)
	//		_zero = new PTE_Zero(this);
	//
	//	return _zero;
	//}
}

//
//
//
