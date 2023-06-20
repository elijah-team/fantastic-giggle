/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.i.ConstructStatement;
import tripleo.elijah.lang.i.OS_Element;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.ProcIA;

import java.util.List;

/**
 * Created 12/11/21 9:27 PM
 */
public class DeduceConstructStatement implements DeduceElement {
	private final BaseEvaFunction           generatedFunction;
	public        List<InstructionArgument> args;
	private final ConstructStatement        constructStatement;
	public        ProcIA                    call;
	public        InstructionArgument       target;
	public        boolean                   toEvaluateTarget;

	public DeduceConstructStatement(final @NotNull BaseEvaFunction aGeneratedFunction, final ConstructStatement aConstructStatement) {
		generatedFunction  = aGeneratedFunction;
		constructStatement = aConstructStatement;
	}

	@Override
	public DeclAnchor declAnchor() {
		// TODO should this be the VariableStatementImpl used to declare the type?
		return null;
	}

	@Override
	public OS_Element element() {
		return constructStatement;
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
