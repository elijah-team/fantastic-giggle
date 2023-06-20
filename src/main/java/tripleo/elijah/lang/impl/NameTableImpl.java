/**
 *
 */
package tripleo.elijah.lang.impl;

import tripleo.elijah.lang.i.OS_Element;
import tripleo.elijah.lang.i.OS_Type;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tripleo
 * <p>
 * Created Mar 29, 2020 at 7:00:10 PM
 */
public class NameTableImpl implements tripleo.elijah.lang.i.NameTable {

	class TypedElement {
		OS_Element element;
		OS_Type    type;

		public TypedElement(final OS_Element element2, final OS_Type dtype) {
			this.element = element2;
			this.type    = dtype;
		}

		@Override
		public String toString() {
			return "TypedElement{" + "element=" + element + ", type=" + type + '}';
		}
	}

	Map<String, TypedElement> members = new HashMap<String, TypedElement>();

	@Override
	public void add(final OS_Element element, final String name, final OS_Type dtype) {
//		element.setType(dtype);
		members.put(name, new TypedElement(element, dtype));
		tripleo.elijah.util.Stupidity.println_err_2("[NameTable#add] " + members);
	}

}

//
//
//
