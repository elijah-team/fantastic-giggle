package tripleo.elijah.stages.deduce.nextgen;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.impl.DeferredObject;
import tripleo.elijah.lang.i.IdentExpression;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;

import java.util.ArrayList;
import java.util.List;

public class DR_Ident implements DR_Item {
	private final IdentExpression                             ident;
	private final VariableTableEntry                          vteBl1;
	private final BaseEvaFunction                             baseEvaFunction;
	private final int                                         mode;
	private final List<DR_PossibleType>                       typeProposals        = new ArrayList<>();
	private final DeferredObject<DR_PossibleType, Void, Void> typePossibleDeferred = new DeferredObject<>();
	boolean _b;
	List<DoneCallback<DR_PossibleType>> typePossibles = new ArrayList<>();

	public DR_Ident(final IdentExpression aIdent, final VariableTableEntry aVteBl1, final BaseEvaFunction aBaseEvaFunction) {
		ident           = aIdent;
		vteBl1          = aVteBl1;
		baseEvaFunction = aBaseEvaFunction;
		mode            = 1;
	}

	public DR_Ident(final VariableTableEntry aVteBl1, final BaseEvaFunction aBaseEvaFunction) {
		vteBl1          = aVteBl1;
		baseEvaFunction = aBaseEvaFunction;
		ident           = null;
		mode            = 2;
	}

	public void foo() {
	}

	public void proposeType(final DR_PossibleType aPt) {
		//if (_b) throw new Error(); // FIXME testing only call once

		typeProposals.add(aPt);

		_b = true;
	}

	public void onPossibleType(final DoneCallback<DR_PossibleType> cb) {
		//this.typePossibleDeferred.then(cb);
		typePossibles.add(cb);
	}

	public void addPossibleType(final DR_PossibleType aPt) {
		for (DoneCallback<DR_PossibleType> typePossible : typePossibles) {
			typePossible.onDone(aPt);
		}
	}
}
