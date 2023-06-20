package tripleo.elijah.stages.gen_c;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.*;
import tripleo.elijah.nextgen.outputstatement.*;
import tripleo.elijah.stages.deduce.ExpressionConfession;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.util.Helpers;

import java.util.List;

public class GCX_FunctionCall implements EG_Statement {
	private final GenerateC       gc;
	private final BaseEvaFunction gf;
	private final Instruction     instruction;
	private final ProcTableEntry  pte;

	@Contract(pure = true)
	public GCX_FunctionCall(final ProcTableEntry aPte,
							final @NotNull BaseEvaFunction aGf,
							final GenerateC aGc,
							final @NotNull Instruction aInstruction) {
		pte         = aPte;
		gf          = aGf;
		gc          = aGc;
		instruction = aInstruction;
	}

	@Override
	public EX_Explanation getExplanation() {
		return EX_Explanation.withMessage("GCX_FunctionCall >> action_CALL");
	}

	@Override
	public String getText() {
		final StringBuilder sb = new StringBuilder();

		ExpressionConfession ec = pte.expressionConfession();

		switch (ec.getType()) {
		case exp_num -> {
			final IdentExpression               ptex = (IdentExpression) pte.expression;
			final String                        text = ptex.getText();
			@Nullable final InstructionArgument xx   = gf.vte_lookup(text);
			assert xx != null;
			final String realTargetName = gc.getRealTargetName(gf, (IntegerIA) xx, Generate_Code_For_Method.AOG.GET);

			sb.append(Emit.emit("/*424*/") + realTargetName);
			sb.append('(');
			final List<String> sl3 = gc.getArgumentStrings(gf, instruction);
			sb.append(Helpers.String_join(", ", sl3));
			sb.append(");");

			final EG_SingleStatement   beg = new EG_SingleStatement("(", null);
			final EG_SingleStatement   mid = new EG_SingleStatement(Helpers.String_join(", ", sl3), null);
			final EG_SingleStatement   end = new EG_SingleStatement(");", null);
			final boolean              ind = false;
			final EX_Explanation       exp = EX_Explanation.withMessage("GCX_FunctionCall exp_num");

			final EG_CompoundStatement est = new EG_CompoundStatement(beg, mid, end, ind, exp);

			final String               ss  = est.getText();

			System.out.println(ss);
		}
		case exp -> {
			final CReference reference = new CReference(gc.repo(), gc.ce);
			final IdentIA    ia2       = (IdentIA) pte.expression_num;
			reference.getIdentIAPath(ia2, Generate_Code_For_Method.AOG.GET, null);
			final List<String> sl3 = gc.getArgumentStrings(gf, instruction);
			reference.args(sl3);
			final String path = reference.build();

			reference.debugPath(ia2, path);

			sb.append(Emit.emit("/*427-2*/") + path + ";");
		}
		default -> throw new IllegalStateException("Unexpected value: " + ec.getType());
		}

		if (false) {
			if (pte.expression_num == null) {
				final IdentExpression               ptex = (IdentExpression) pte.expression;
				final String                        text = ptex.getText();
				@Nullable final InstructionArgument xx   = gf.vte_lookup(text);
				assert xx != null;
				final String realTargetName = gc.getRealTargetName(gf, (IntegerIA) xx, Generate_Code_For_Method.AOG.GET);
				sb.append(Emit.emit("/*424*/") + realTargetName);
				sb.append('(');
				final List<String> sl3 = gc.getArgumentStrings(gf, instruction);
				sb.append(Helpers.String_join(", ", sl3));
				sb.append(");");
			} else {
				final CReference reference = new CReference(gc.repo(), gc.ce);
				final IdentIA    ia2       = (IdentIA) pte.expression_num;
				reference.getIdentIAPath(ia2, Generate_Code_For_Method.AOG.GET, null);
				final List<String> sl3 = gc.getArgumentStrings(gf, instruction);
				reference.args(sl3);
				final String path = reference.build();

				reference.debugPath(ia2, path);

				sb.append(Emit.emit("/*427-3*/") + path + ";");
			}
		}

		return sb.toString();
	}
}
