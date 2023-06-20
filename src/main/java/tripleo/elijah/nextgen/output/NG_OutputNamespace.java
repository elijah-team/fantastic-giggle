package tripleo.elijah.nextgen.output;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.garish.GarishNamespace;
import tripleo.elijah.stages.gen_c.GenerateC;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.generate.OutputStrategyC;
import tripleo.elijah.util.BufferTabbedOutputStream;
import tripleo.util.buffer.Buffer;

import java.util.List;

import static tripleo.elijah.util.Helpers.List_of;

public class NG_OutputNamespace implements NG_OutputItem {
	private GarishNamespace garishNamespace;
	private GenerateC       generateC;

	@Override
	public @NotNull List<Pair<GenerateResult.TY, String>> getOutputs() {
		final EvaNamespace x = garishNamespace.getLiving().evaNode();

		final BufferTabbedOutputStream tos = garishNamespace.getImplBuffer(x, x.getName(), x.getCode());
		final Buffer                   buf = tos.getBuffer();

		var implText = buf.getText();

		final BufferTabbedOutputStream tosHdr    = garishNamespace.getHeaderBuffer(generateC, x, x.getName(), x.getCode());
		final Buffer                   hdrBuffer = tosHdr.getBuffer();

		var headerText = hdrBuffer.getText();

		var impl   = Pair.of(GenerateResult.TY.IMPL, implText);
		var header = Pair.of(GenerateResult.TY.HEADER, headerText);

		return List_of(impl, header);
	}

	@Override
	public String outName(final OutputStrategyC aOutputStrategyC, final GenerateResult.TY ty) {
		final EvaNamespace x = garishNamespace.getLiving().evaNode();

		return aOutputStrategyC.nameForNamespace(x, ty);
	}

	public void setNamespace(final GarishNamespace aGarishNamespace, final GenerateC aGenerateC) {
		garishNamespace = aGarishNamespace;
		generateC       = aGenerateC;
	}
}
