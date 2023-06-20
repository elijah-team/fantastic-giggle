package tripleo.elijah.nextgen.output;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.gen_c.Generate_Code_For_Method;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaFunction;
import tripleo.elijah.stages.gen_generic.GenerateFiles;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.generate.OutputStrategyC;

import java.util.ArrayList;
import java.util.List;

public class NG_OutputFunction implements NG_OutputItem {
	private List<Generate_Code_For_Method.C2C_Result> collect;
	private GenerateFiles                             generateFiles;
	private BaseEvaFunction                           gf;

	@Override
	public @NotNull List<Pair<GenerateResult.TY, String>> getOutputs() {
		final List<Pair<GenerateResult.TY, String>> r = new ArrayList<>();

		for (Generate_Code_For_Method.C2C_Result c2c : collect) {
			var               x = c2c.getStatement();
			GenerateResult.TY y = c2c.ty();

			r.add(Pair.of(y, x.getText()));
		}

		//return List_of();
		return r;
	}

	@Override
	public String outName(final @NotNull OutputStrategyC aOutputStrategyC, final GenerateResult.TY ty) {
		return aOutputStrategyC.nameForFunction((EvaFunction) gf, ty);
	}

	public void setFunction(final BaseEvaFunction aGf, final GenerateFiles aGenerateFiles, final List<Generate_Code_For_Method.C2C_Result> aCollect) {
		gf            = aGf;
		generateFiles = aGenerateFiles;
		collect       = aCollect;
	}
}
