package tripleo.elijah.stages.write_stage.pipeline_impl;

import tripleo.elijah.diagnostic.Diagnostic;

public interface CompletableProcess<T> {
	void add(T item);

	void complete();

	void error(Diagnostic d);

	void preComplete();

	void start();
}
