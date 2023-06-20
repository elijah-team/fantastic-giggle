/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce.tastic;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.i.ErrSink;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.BaseFunctionDef;
import tripleo.elijah.lang.types.OS_BuiltinType;
import tripleo.elijah.lang.types.OS_UserClassType;
import tripleo.elijah.lang2.BuiltInTypes;
import tripleo.elijah.stages.deduce.*;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.*;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.NotImplementedException;

import java.util.List;

import static tripleo.elijah.stages.deduce.DeduceTypes2.to_int;

public class FT_FnCallArgs implements ITastic {
	final ElLog LOG;

	private final DeduceTypes2 deduceTypes2;
	private final FnCallArgs   fca;

	@Override
	public void do_assign_call(final @NotNull BaseEvaFunction generatedFunction,
							   final @NotNull Context ctx,
							   final @NotNull VariableTableEntry vte,
							   final @NotNull Instruction instruction) {
		// NOTE Java is crazy
		final DeduceTypes2.DeduceClient4 client4          = deduceTypes2.new DeduceClient4(deduceTypes2);
		final DoAssignCall               dac              = new DoAssignCall(client4, generatedFunction);
		final int                        instructionIndex = instruction.getIndex();
		final @NotNull ProcTableEntry    pte              = ((ProcIA) fca.getArg(0)).getEntry();

		final ExpressionConfession ece = pte.expressionConfession();

		switch (ece.getType()) {
		case exp_num -> {
		}
		case exp -> {
		}
		default -> throw new IllegalStateException("Unexpected value: " + ece.getType());
		}

		if (pte.expression_num instanceof @NotNull final IdentIA identIA) {

			FT_FCA_IdentIA fca_ident = new FT_FCA_IdentIA(FT_FnCallArgs.this, identIA, vte);

			try {
				fca_ident.resolve_vte(dac.dc, ctx, pte);
				fca_ident.make2(dac.module, pte, dac.dc, ctx, dac.generatedFunction, instructionIndex, dac.LOG);
				fca_ident.loop1(pte, dac.generatedFunction, ctx, instructionIndex, dac.dc, dac.errSink);
				fca_ident.loop2(instruction, vte, fca, ctx, instructionIndex, pte, dac);
			} catch (FCA_Stop e) {
			}
		} else if (pte.expression_num instanceof final IntegerIA integerIA) {
			int y = 2;
		}
	}

	@Contract(pure = true)
	public FT_FnCallArgs(final @NotNull DeduceTypes2 aDeduceTypes2, final FnCallArgs aO) {
		deduceTypes2 = aDeduceTypes2;
		fca          = aO;
		//
		LOG = aDeduceTypes2.LOG;
	}

	@Override
	public void do_assign_call(final @NotNull BaseEvaFunction generatedFunction,
							   final @NotNull Context ctx,
							   final @NotNull IdentTableEntry idte,
							   final int instructionIndex) {
		final @NotNull ProcTableEntry pte = generatedFunction.getProcTableEntry(to_int(fca.getArg(0)));
		for (final @NotNull TypeTableEntry tte : pte.getArgs()) {
			LOG.info("771 " + tte);
			final IExpression e = tte.expression;
			if (e == null) continue;
			switch (e.getKind()) {
			case NUMERIC: {
				tte.setAttached(new OS_BuiltinType(BuiltInTypes.SystemInteger));
				idte.type = tte; // TODO why not addPotentialType ? see below for example
			}
			break;
			case IDENT: {
				final @Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(((IdentExpression) e).getText());
				final @NotNull List<TypeTableEntry> ll     = deduceTypes2.getPotentialTypesVte((EvaFunction) generatedFunction, vte_ia);
				if (ll.size() == 1) {
					tte.setAttached(ll.get(0).getAttached());
					idte.addPotentialType(instructionIndex, ll.get(0));
				} else
					throw new NotImplementedException();
			}
			break;
			default: {
				throw new NotImplementedException();
			}
			}
		}
		{
			final String               s    = ((IdentExpression) pte.expression).getText();
			final LookupResultList     lrl  = ctx.lookup(s);
			final @Nullable OS_Element best = lrl.chooseBest(null);
			if (best != null) {
				pte.setResolvedElement(best);

				// TODO do we need to add a dependency for class, etc?
				if (true) {
					if (best instanceof ConstructorDef) {
						// TODO Dont know how to handle this
						int y = 2;
					} else if (best instanceof FunctionDef || best instanceof DefFunctionDef) {
						final OS_Element parent = best.getParent();
						IInvocation      invocation;
						if (parent instanceof NamespaceStatement) {
							invocation = new NamespaceInvocation((NamespaceStatement) parent);
						} else if (parent instanceof ClassStatement) {
							invocation = new ClassInvocation((ClassStatement) parent, null);
						} else
							throw new NotImplementedException();

						FunctionInvocation fi = deduceTypes2.newFunctionInvocation((BaseFunctionDef) best, pte, invocation, deduceTypes2.phase);
						generatedFunction.addDependentFunction(fi);
					} else if (best instanceof ClassStatement) {
						GenType genType = new GenType();
						genType.resolved = new OS_UserClassType((ClassStatement) best);
						// ci, typeName, node
						//					genType.
						genType.genCI(null, deduceTypes2, deduceTypes2._errSink(), deduceTypes2._phase());
						generatedFunction.addDependentType(genType);
					}
				}
			} else
				throw new NotImplementedException();
		}
	}

	/**
	 * Created 12/12/21 12:30 AM
	 */
	class DoAssignCall {
		final DeduceTypes2.DeduceClient4 dc;
		final ErrSink                    errSink;
		final BaseEvaFunction            generatedFunction;
		final ElLog                      LOG;
		private final OS_Module module;

		public DoAssignCall(final DeduceTypes2.DeduceClient4 aDeduceClient4, final @NotNull BaseEvaFunction aGeneratedFunction) {
			dc                = aDeduceClient4;
			generatedFunction = aGeneratedFunction;
			//
			module  = dc.getModule();
			LOG     = dc.getLOG();
			errSink = dc.getErrSink();
		}

		static class NullFoundElement extends FoundElement {
			public NullFoundElement(DeduceTypes2.DeduceClient4 dc) {
				super(dc.getPhase());
			}

			@Override
			public void foundElement(final OS_Element e) {
			}

			@Override
			public void noFoundElement() {

			}
		}
	}

}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
