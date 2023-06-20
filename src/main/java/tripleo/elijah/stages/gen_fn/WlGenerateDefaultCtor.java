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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.*;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.util.Holder;
import tripleo.elijah.work.WorkJob;
import tripleo.elijah.work.WorkManager;

/**
 * Created 5/31/21 2:26 AM
 */
public class WlGenerateDefaultCtor implements WorkJob {
	private final FunctionInvocation functionInvocation;
	private       boolean            _isDone = false;
	private final GenerateFunctions  generateFunctions;
	private       BaseEvaFunction    Result;

	@Contract(pure = true)
	public WlGenerateDefaultCtor(@NotNull GenerateFunctions aGenerateFunctions, FunctionInvocation aFunctionInvocation) {
		generateFunctions  = aGenerateFunctions;
		functionInvocation = aFunctionInvocation;
	}

	private boolean getPragma(String aAuto_construct) {
		return false;
	}

	public BaseEvaFunction getResult() {
		return Result;
	}

	@Override
	public boolean isDone() {
		return _isDone;
	}

	@Override
	public void run(WorkManager aWorkManager) {
		if (functionInvocation.generateDeferred().isPending()) {
			final ClassStatement klass     = functionInvocation.getClassInvocation().getKlass();
			Holder<EvaClass>     hGenClass = new Holder<>();
			functionInvocation.getClassInvocation().resolvePromise().then(new DoneCallback<EvaClass>() {
				@Override
				public void onDone(EvaClass result) {
					hGenClass.set(result);
				}
			});
			EvaClass genClass = hGenClass.get();
			assert genClass != null;

			ConstructorDef cd = new ConstructorDefImpl(null, (_CommonNC) klass, klass.getContext());
//			cd.setName(Helpers.string_to_ident("<ctor>"));
			cd.setName(ConstructorDefImpl.emptyConstructorName);
			Scope3Impl scope3 = new Scope3Impl(cd);
			cd.scope(scope3);
			for (EvaContainer.VarTableEntry varTableEntry : genClass.varTable) {
				if (varTableEntry.initialValue != IExpression.UNASSIGNED) {
					IExpression left  = varTableEntry.nameToken;
					IExpression right = varTableEntry.initialValue;

					IExpression e = ExpressionBuilder.build(left, ExpressionKind.ASSIGNMENT, right);
					scope3.add(new WrappedStatementWrapper(e, cd.getContext(), cd, (VariableStatementImpl) varTableEntry.vs()));
				} else {
					if (true) {
						scope3.add(new ConstructStatementImpl(cd, cd.getContext(), varTableEntry.nameToken, null, null));
					}
				}
			}

			OS_Element classStatement = cd.getParent();
			assert classStatement instanceof ClassStatement;
			@NotNull EvaConstructor gf = generateFunctions.generateConstructor(cd, (ClassStatement) classStatement, functionInvocation);
//		lgf.add(gf);

			final ClassInvocation ci = functionInvocation.getClassInvocation();
			ci.resolvePromise().done(new DoneCallback<EvaClass>() {
				@Override
				public void onDone(EvaClass result) {
					gf.setCode(generateFunctions.module.getCompilation().nextFunctionCode());
					gf.setClass(result);
					result.constructors.put(cd, gf);
				}
			});

			functionInvocation.generateDeferred().resolve(gf);
			functionInvocation.setGenerated(gf);
			Result = gf;
		} else {
			functionInvocation.generatePromise().then(new DoneCallback<BaseEvaFunction>() {
				@Override
				public void onDone(final BaseEvaFunction result) {
					Result = result;
				}
			});
		}

		_isDone = true;
	}
}

//
//
//
