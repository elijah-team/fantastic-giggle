package tripleo.elijah.nextgen.output;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.generate.OutputStrategyC;

import java.util.List;

public interface NG_OutputItem {
	@NotNull List<Pair<GenerateResult.TY, String>> getOutputs();

	String outName(OutputStrategyC aOutputStrategyC, final GenerateResult.TY ty);
}
