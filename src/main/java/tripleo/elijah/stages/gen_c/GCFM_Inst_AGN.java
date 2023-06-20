package tripleo.elijah.stages.gen_c;

import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;

class GCFM_Inst_AGN implements GenerateC_Statement {

	private final GenerateC                gc;
	private final Generate_Code_For_Method generateCodeForMethod;
	private final BaseEvaFunction          gf;
	private final Instruction              instruction;

	public GCFM_Inst_AGN(final Generate_Code_For_Method aGenerateCodeForMethod, final GenerateC aGc, final BaseEvaFunction aGf, final Instruction aInstruction) {
		generateCodeForMethod = aGenerateCodeForMethod;
		gc                    = aGc;
		gf                    = aGf;
		instruction           = aInstruction;
	}

	@Override
	public String getText() {
		final InstructionArgument target = instruction.getArg(0);
		final InstructionArgument value  = instruction.getArg(1);

		final String realTarget, s;
		if (target instanceof IntegerIA) {
			realTarget = gc.getRealTargetName(gf, (IntegerIA) target, Generate_Code_For_Method.AOG.ASSIGN);
			final String assignmentValue = gc.getAssignmentValue(gf.getSelf(), value, gf);
			s = String.format(Emit.emit("/*267*/") + "%s = %s;", realTarget, assignmentValue);
		} else {
			final String assignmentValue = gc.getAssignmentValue(gf.getSelf(), value, gf);
			String       s2              = Emit.emit("/*501*/") + gc.getRealTargetName(gf, (IdentIA) target, Generate_Code_For_Method.AOG.ASSIGN, assignmentValue);
			s = String.format(Emit.emit("/*249*/") + "%s = %s;", s2, assignmentValue);
		}

		return s;
	}

	@Override
	public GCR_Rule rule() {
		return GCR_Rule.withMessage("GCFM_Inst_AGN");
	}
}
