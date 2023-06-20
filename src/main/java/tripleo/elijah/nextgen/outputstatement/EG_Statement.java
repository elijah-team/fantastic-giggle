/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package tripleo.elijah.nextgen.outputstatement;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @author Tripleo Nova
 */
public interface EG_Statement {
	@Contract(value = "_, _ -> new", pure = true)
	static @NotNull EG_Statement of(String aText, EX_Explanation aExplanation) {
		return new EG_Statement() {
			@Override
			public EX_Explanation getExplanation() {
				return aExplanation;
			}

			@Override
			public String getText() {
				return aText;
			}
		};
	}

	EX_Explanation getExplanation();

	String getText();
}
