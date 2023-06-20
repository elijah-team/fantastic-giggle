package tripleo.elijah.nextgen.output;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.garish.GarishClass;
import tripleo.elijah.stages.gen_c.GenerateC;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.generate.OutputStrategyC;
import tripleo.elijah.util.BufferTabbedOutputStream;
import tripleo.util.buffer.Buffer;

import java.util.List;

import static tripleo.elijah.util.Helpers.List_of;

public class NG_OutputClass implements NG_OutputItem {
	private GarishClass garishClass;
	private GenerateC   generateC;

	@Override public @NotNull List<Pair<GenerateResult.TY, String>> getOutputs() {
		final EvaClass x = garishClass.getLiving().evaNode();

		final BufferTabbedOutputStream tos = garishClass.getClassBuffer(generateC);
		final Buffer                   buf = tos.getBuffer();

		var implText = buf.getText();

		final BufferTabbedOutputStream tosHdr = garishClass.getHeaderBuffer(generateC);
		final Buffer                   hdrBuffer   = tosHdr.getBuffer();

		var headerText = hdrBuffer.getText();

		var impl   = Pair.of(GenerateResult.TY.IMPL, implText);
		var header = Pair.of(GenerateResult.TY.HEADER, headerText);

		return List_of(impl, header);
	}

	@Override
	public String outName(final OutputStrategyC aOutputStrategyC, final GenerateResult.TY ty) {
		final EvaClass x = garishClass.getLiving().evaNode();

		return aOutputStrategyC.nameForClass(x, ty);
	}

	public void setClass(final GarishClass aGarishClass, final GenerateC aGenerateC) {
		this.garishClass = aGarishClass;
		generateC        = aGenerateC;
	}
}
