package tripleo.elijah.stages.gen_c;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.diagnostic.Diagnostic.Severity;
import tripleo.elijah.lang.i.IdentExpression;
import tripleo.elijah.lang.impl.ConstructorDefImpl;
import tripleo.elijah.nextgen.outputstatement.EG_SingleStatement;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.nextgen.outputstatement.EX_Explanation;
import tripleo.elijah.nextgen.query.Operation2;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.DeduceConstructStatement;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_ProcTableEntry;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.ProcIA;
import tripleo.elijah.util.NotImplementedException;

import java.text.MessageFormat;
import java.util.List;
import java.util.function.Consumer;

class GI_ProcIA implements GenerateC_Item {
	private       EvaNode _evaNode;
	private final ProcIA  carrier;

	public GI_ProcIA(final ProcIA aProcIA) {
		carrier = aProcIA;
	}

	public Operation2<EG_Statement> action_CONSTRUCT(@NotNull Instruction aInstruction, GenerateC gc) {
		final ProcTableEntry       pte = carrier.getEntry();
		final List<TypeTableEntry> x   = pte.getArgs();
		final int                  y   = aInstruction.getArgsSize();
//		InstructionArgument z = instruction.getArg(1);

		final DeduceConstructStatement dcs = (DeduceConstructStatement) aInstruction.deduceElement;


		final ClassInvocation clsinv = pte.getClassInvocation();
		if (clsinv != null) {
			final InstructionArgument target = pte.expression_num;
//			final InstructionArgument value  = instruction;

			if (target instanceof IdentIA) {
				// how to tell between named ctors and just a path?
				@NotNull final IdentTableEntry target2 = ((IdentIA) target).getEntry();
				final String                   str     = target2.getIdent().getText();

				System.out.println("130  " + str);
			}

			final String s = MessageFormat.format("{0}{1};", Emit.emit("/*500*/"), getAssignmentValue(aInstruction, gc));

			return Operation2.success(new EG_SingleStatement(s, EX_Explanation.withMessage("aaa")));
		}

		return Operation2.failure(Diagnostic.withMessage("12900", "no construct possible for GI_Proc", Severity.INFO));
	}

	public String getAssignmentValue(final @NotNull Instruction aInstruction, final @NotNull GenerateC gc) {
		final BaseEvaFunction gf     = carrier.gf;
		final ClassInvocation clsinv = carrier.getEntry().getClassInvocation();

		//return gc.getAssignmentValue(gf.getSelf(), aInstruction, clsinv, gf);

		GenerateC.GetAssignmentValue gav = gc.new GetAssignmentValue();
//		return gav.forClassInvocation(aInstruction, clsinv, gf, gc.LOG);

		InstructionArgument     _arg0 = aInstruction.getArg(0);
		@NotNull ProcTableEntry pte   = carrier.getEntry();

		final CtorReference reference = new CtorReference();
		reference.getConstructorPath(pte.expression_num, gf);
		@NotNull List<String> x = gav.getAssignmentValueArgs(aInstruction, gf, gc.LOG);
		reference.args(x);
		final String build = reference.build(clsinv);
		return build;
	}

	@Override
	public EvaNode getEvaNode() {
		return _evaNode;
	}

	public String getIdentIAPath(final Consumer<Pair<String, CReference.Ref>> addRef) {
		return getIdentIAPath_Proc(carrier.getEntry(), addRef);
	}

	public @Nullable String getIdentIAPath_Proc(final @NotNull ProcTableEntry pte, final Consumer<Pair<String, CReference.Ref>> addRef) {
		final String[]           text = new String[1];
		final FunctionInvocation fi   = pte.getFunctionInvocation();

		if (fi == null) {
			tripleo.elijah.util.Stupidity.println_err_2("7777777777777777 fi getIdentIAPath_Proc " + pte);

			return null;//throw new IllegalStateException();
		}

		/*final*/
		BaseEvaFunction                     generated = fi.getGenerated();
		final DeduceElement3_ProcTableEntry de_pte    = (DeduceElement3_ProcTableEntry) pte.getDeduceElement3();

		if (generated == null) {
			System.err.println("6464 " + fi.pte);

			final WlGenerateCtor wlgf = new WlGenerateCtor(de_pte.deduceTypes2().getGenerateFunctions(de_pte.getPrincipal().getContext().module()), fi, null/*de_pte.deduceTypes2().phase.codeRegistrar*/);
			wlgf.run(null);
			generated = wlgf.getResult();

			for (IdentTableEntry identTableEntry : generated.idte_list) {
				identTableEntry._fix_table(de_pte.deduceTypes2(), de_pte.generatedFunction());
			}

			//throw new IllegalStateException();
		}

		if (generated instanceof EvaConstructor) {
			NotImplementedException.raise();
			final BaseEvaFunction finalGenerated = generated;
			generated.onGenClass(genClass -> {
				final IdentExpression constructorName = finalGenerated.getFD().getNameNode();
				final String          constructorNameText;
				if (constructorName == ConstructorDefImpl.emptyConstructorName) {
					constructorNameText = "";
				} else {
					constructorNameText = constructorName.getText();
				}
				text[0] = String.format("ZC%d%s", genClass.getCode(), constructorNameText);
				addRef.accept(Pair.of(text[0], CReference.Ref.CONSTRUCTOR));
			});
			//final EvaContainerNC genClass = (EvaContainerNC) generated.getGenClass();
			//if (true || genClass == null) {
			//	final int y = 2;
			//	generated.setClass(genClass);
			//}
		} else {
			final BaseEvaFunction finalGenerated1 = generated;
			generated.onGenClass(genClass -> {
				text[0] = String.format("Z%d%s", genClass.getCode(), finalGenerated1.getFD().getNameNode().getText());
				addRef.accept(Pair.of(text[0], CReference.Ref.FUNCTION));
			});
		}

		return text[0];
	}

	@Override
	public void setEvaNode(final EvaNode a_evaNaode) {
		_evaNode = a_evaNaode;
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
