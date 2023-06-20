package tripleo.elijah.stages.gen_c;

import tripleo.elijah.stages.gen_generic.GenerateFiles;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.pipeline_impl.GenerateResultSink;
import tripleo.elijah.work.WorkList;

public record GenC_FileGen(GenerateResultSink resultSink,
						   GenerateResult gr,
						   tripleo.elijah.work.WorkManager wm,
						   WorkList wl,
						   GenerateFiles generateC) {

}
