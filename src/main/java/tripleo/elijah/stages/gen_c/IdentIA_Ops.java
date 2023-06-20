package tripleo.elijah.stages.gen_c;

import tripleo.elijah.lang.impl.*;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;

public class IdentIA_Ops {
	public static IdentIA_Ops get(final IdentIA aIdentIA) {
		return new IdentIA_Ops(aIdentIA);
	}

	private final IdentIA identIA;

	public IdentIA_Ops(final IdentIA aIdentIA) {
		identIA = aIdentIA;
	}

	public ConstructorPathOp getConstructorPath() {
		final IdentTableEntry idte             = identIA.getEntry();
		final OS_Element      resolved_element = idte.getResolvedElement();

		if (idte.resolvedType() != null) {
			final EvaNode _resolved = idte.resolvedType();
			String        ctorName  = null;

			if (resolved_element != null) { // FIXME stop accepting null here
				switch (DecideElObjectType.getElObjectType(resolved_element)) {
				case CONSTRUCTOR -> {
					ctorName = ((ConstructorDef) resolved_element).name();
				}
				case CLASS -> {
					int y = 2;
					ctorName = ""; //((ClassStatement) resolved_element).name();
				}
				}
			} else ctorName = "";

			final String finalCtorName = ctorName;
			return new ConstructorPathOp() {
				@Override
				public String getCtorName() {
					return finalCtorName;
				}

				@Override
				public EvaNode getResolved() {
					return _resolved;
				}
			};
		} /*else if (resolved_element != null) {
					assert false;
					if (resolved_element instanceof VariableStatementImpl) {
						addRef(((VariableStatementImpl) resolved_element).getName(), CReference.Ref.MEMBER);
					} else if (resolved_element instanceof ConstructorDef) {
						assert i == sSize - 1; // Make sure we are ending with a constructor call
						int code = ((ClassStatement) resolved_element.getParent())._a.getCode();
						if (code == 0) {
							tripleo.elijah.util.Stupidity.println_err_2("** 31161 ClassStatement with 0 code " + resolved_element.getParent());
						}
						// README Assuming this is for named constructors
						String text = ((ConstructorDef) resolved_element).name();
						String text2 = String.format("ZC%d%s", code, text);

						ctorName = text;

//						addRef(text2, CReference.Ref.CONSTRUCTOR);

//						addRef(((ConstructorDef) resolved_element).name(), CReference.Ref.CONSTRUCTOR);
					}
				}*/

		return new ConstructorPathOp() {
			@Override
			public String getCtorName() {
				return null;
			}

			@Override
			public EvaNode getResolved() {
				return null;
			}
		};
	}
}
