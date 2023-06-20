package tripleo.elijah.lang.types;

import tripleo.elijah.lang.i.OS_Element;
import tripleo.elijah.lang.i.OS_Type;


public class OS_UnitType extends __Abstract_OS_Type {

	@Override
	public String asString() {
		return "<OS_UnitType>";
	}

	@Override
	protected boolean _isEqual(final OS_Type aType) {
		return aType.getType() == Type.UNIT_TYPE;
	}

	@Override
	public OS_Element getElement() {
		return null;
	}

	@Override
	public Type getType() {
		return Type.UNIT_TYPE;
	}
	@Override
	public boolean isUnitType() {
		return true;
	}

	@Override
	public String toString() {
		return "<UnitType>";
	}
}


