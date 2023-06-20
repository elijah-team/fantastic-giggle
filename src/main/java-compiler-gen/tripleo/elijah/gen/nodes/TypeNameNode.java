/**
 *
 */
package tripleo.elijah.gen.nodes;

import org.eclipse.jdt.annotation.NonNull;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.*;

/**
 * @author Tripleo(acer)
 *
 */
public class TypeNameNode {

	public String genType;

	public TypeNameNode(@NonNull final IdentExpression return_type) {
		// TODO Auto-generated constructor stub
		genType = return_type.getText(); // TODO wrong prolly
	}

	public String getText() {
		return null;
	}
}
