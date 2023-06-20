package tripleo.elijah.comp.i;

import tripleo.elijah.comp.Compilation;

public interface RuntimeProcess {
	void postProcess();

	void prepare() throws Exception;

	void run(final Compilation aComp);
}
