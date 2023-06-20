package tripleo.elijah.stages.deduce.post_bytecode;

import tripleo.elijah.lang.i.ClassStatement;
import tripleo.elijah.lang.i.OS_Element;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.ConstructableElementHolder;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.gen_fn.*;

// DeduceGrand
public class DG_ClassStatement implements DG_Item {
	private final ClassStatement     classStatement;
	GenericElementHolder genericElementHolder;
	private       EvaClass           _evaNode;
	private       ClassInvocation    classInvocation;
	private       FunctionInvocation fi;
	private ProcTableEntry pte;

	public DG_ClassStatement(final ClassStatement aClassStatement) {
		classStatement = aClassStatement;
	}

	public void attach(final FunctionInvocation aFi, final ProcTableEntry aPte) {
		fi  = aFi;
		pte = aPte;
	}

	public void attachClass(final EvaClass aResult) {
		_evaNode = aResult;
	}

	public ClassInvocation classInvocation() {
		if (classInvocation == null)
			classInvocation = new ClassInvocation((classStatement), null);
		return classInvocation;
	}

	public IElementHolder ConstructableElementHolder(final OS_Element aE, final VariableTableEntry aVte) {
		return new ConstructableElementHolder(classStatement, aVte);
	}

	public EvaClass evaClass() {
		return _evaNode;
	}

	public FunctionInvocation functionInvocation() {
		return fi;
	}

	public GenericElementHolder GenericElementHolder() {
		if (genericElementHolder == null)
			genericElementHolder = new GenericElementHolder(classStatement);
		return genericElementHolder;
	}
}
