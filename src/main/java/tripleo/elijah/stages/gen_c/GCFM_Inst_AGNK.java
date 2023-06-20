package tripleo.elijah.stages.gen_c;

import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;

class GCFM_Inst_AGNK {

	private final GenerateC                gc;
	private final Generate_Code_For_Method generateCodeForMethod;
	private final BaseEvaFunction          gf;
	private final Instruction              instruction;

	public GCFM_Inst_AGNK(final Generate_Code_For_Method aGenerateCodeForMethod, final GenerateC aGc, final BaseEvaFunction aGf, final Instruction aInstruction) {
		generateCodeForMethod = aGenerateCodeForMethod;
		gc                    = aGc;
		gf                    = aGf;
		instruction           = aInstruction;
	}

	public String getText() {
		final InstructionArgument target = instruction.getArg(0);
		final InstructionArgument value  = instruction.getArg(1);

		final String realTarget      = gc.getRealTargetName(gf, (IntegerIA) target, Generate_Code_For_Method.AOG.ASSIGN);
		final String assignmentValue = gc.getAssignmentValue(gf.getSelf(), value, gf);
		final String s               = String.format(Emit.emit("/*278*/") + "%s = %s;", realTarget, assignmentValue);

		return s;
	}
}
