package tripleo.elijah.stages.deduce.post_bytecode;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.gen_fn.*;

public interface DED {

	class DED_CTE implements DED {

		private final ConstantTableEntry constantTableEntry;

		@Contract(pure = true)
		public DED_CTE(final ConstantTableEntry aConstantTableEntry) {
			constantTableEntry = aConstantTableEntry;
		}

		public ConstantTableEntry getConstantTableEntry() {
			return constantTableEntry;
		}

		@Override
		public DED.Kind kind() {
			return DED.Kind.DED_Kind_ConstantTableEntry;
		}
	}

	class DED_ITE implements DED {

		private final IdentTableEntry identTableEntry;

		public DED_ITE(final IdentTableEntry aIdentTableEntry) {
			identTableEntry = aIdentTableEntry;
		}

		public IdentTableEntry getIdentTableEntry() {
			return identTableEntry;
		}

		@Override
		public Kind kind() {
			return Kind.DED_Kind_IdentTableEntry;
		}

	}

	static DED dispatch(final ConstantTableEntry aCte) {
		return new DED_CTE(aCte);
	}

	static DED dispatch(EvaContainer.VarTableEntry aPrincipal) {
		throw new Error();
	}

	class DED_VTE implements DED {

		private final VariableTableEntry variableTableEntry;

		public DED_VTE(final VariableTableEntry aVariableTableEntry) {
			variableTableEntry = aVariableTableEntry;
		}

		public VariableTableEntry getVariableTableEntry() {
			return variableTableEntry;
		}

		@Override
		public Kind kind() {
			return Kind.DED_Kind_VariableTableEntry;
		}

	}

	static DED dispatch(final IdentTableEntry aIte) {
		return new DED_ITE(aIte);
	}

	static DED dispatch(final ProcTableEntry aPte) {
		return new DED_PTE(aPte);
	}

	static DED dispatch(final TypeTableEntry aCte) {
		return new DED_TTE(aCte);
	}

	static DED dispatch(final VariableTableEntry aVte) {
		return new DED_VTE(aVte);
	}

	Kind kind();

	enum Kind {
		DED_Kind_ConstantTableEntry,
		DED_Kind_GeneratedFunction,
		DED_Kind_IdentTableEntry,
		DED_Kind_ProcTableEntry,
		/*
			DED_Kind_GeneratedFunction,
			DED_Kind_GeneratedFunction,
			DED_Kind_GeneratedFunction,
			DED_Kind_GeneratedFunction,
		 */
		DED_Kind_Type,
		DED_Kind_TypeTableEntry,
		DED_Kind_VariableTableEntry, DED_Kind_VarTableEntry
	}

	class DED_PTE implements DED {

		private final ProcTableEntry principal;

		public DED_PTE(final ProcTableEntry aPte) {
			principal = aPte;
		}

		@Override
		public @NotNull Kind kind() {
			return Kind.DED_Kind_ProcTableEntry;
		}

	}

	class DED_TTE implements DED {

		private final TypeTableEntry principal;

		public DED_TTE(final TypeTableEntry aTte) {
			principal = aTte;
		}

		@Override
		public Kind kind() {
			return Kind.DED_Kind_TypeTableEntry;
		}

	}

	class DED_VTE1 implements DED {

		private final TypeTableEntry principal;

		public DED_VTE1(final TypeTableEntry aTte) {
			principal = aTte;
		}

		@Override
		public Kind kind() {
			return Kind.DED_Kind_VarTableEntry;
		}

	}

}
