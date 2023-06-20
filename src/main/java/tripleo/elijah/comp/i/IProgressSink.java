package tripleo.elijah.comp.i;

public interface IProgressSink {
	void note(int aCode, ProgressSinkComponent aCci, int aType, Object[] aParams);
}
