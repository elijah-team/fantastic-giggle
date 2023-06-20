package tripleo.elijah.nextgen.output;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;

public record NG_OutputRequest(String fileName, @NotNull EG_Statement statement) {
}
