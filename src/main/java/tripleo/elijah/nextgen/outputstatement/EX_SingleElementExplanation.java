package tripleo.elijah.nextgen.outputstatement;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.*;

public class EX_SingleElementExplanation implements EX_Explanation {

	private final @NotNull OS_Element _element;

	@Contract(pure = true)
	public EX_SingleElementExplanation(final OS_Element aElement) {
		_element = aElement;
	}

	public @NotNull OS_Element getElement() {
		return _element;
	}

	@Override
	public String message() {
		return "EX_SingleElementExplanation";
	}
}
