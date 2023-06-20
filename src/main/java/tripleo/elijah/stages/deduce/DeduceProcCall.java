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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.i.ErrSink;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.VariableStatementImpl;
import tripleo.elijah.stages.deduce.nextgen.DR_Variable;
import tripleo.elijah.stages.deduce.tastic.FCA_Stop;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.VariableTableType;
import tripleo.elijah.util.NotImplementedException;

import java.util.stream.Collectors;

/**
 * Created 11/30/21 11:56 PM
 */
public class DeduceProcCall {
	private       Context         context;
	private       DeduceTypes2    deduceTypes2;
	private       ErrSink         errSink;
	private       BaseEvaFunction generatedFunction;

	public DeduceTypes2 _deduceTypes2() {
		return deduceTypes2;
	}
	private final ProcTableEntry  procTableEntry;

	DeduceElement target;

	@Contract(pure = true)
	public DeduceProcCall(final @NotNull ProcTableEntry aProcTableEntry) {
		procTableEntry = aProcTableEntry;
	}

	public BaseEvaFunction _generatedFunction() {
		return generatedFunction;
	}

	public @Nullable DeduceElement target() throws FCA_Stop {
		if (target != null) return target;

		final @NotNull IdentTableEntry t = ((IdentIA) procTableEntry.expression_num).getEntry();
		if (t.getBacklink() == null) {
			try {
				final LookupResultList     lrl  = DeduceLookupUtils.lookupExpression(t.getIdent(), context, deduceTypes2);
				final @Nullable OS_Element best = lrl.chooseBest(null);
				assert best != null;
				final OS_Type attached = generatedFunction.vte_list.stream().
						filter(x -> x.vtt == VariableTableType.SELF).
						collect(Collectors.toList()).
						get(0).
						type.getAttached();
				assert attached != null;
				final ClassStatement  self     = attached.getClassOf();
				final ClassStatement  inherits = DeduceLocalVariable.class_inherits(self, best.getParent());
				DeclAnchor.AnchorType anchorType;
				OS_Element            declAnchor;
				if (inherits != null) {
					anchorType = DeclAnchor.AnchorType.INHERITED;
					declAnchor = inherits;
				} else {
					anchorType = DeclAnchor.AnchorType.MEMBER;
					declAnchor = self;
				}
				target = new DeclTarget(best, declAnchor, anchorType, errSink);
			} catch (ResolveError aResolveError) {
				return null; // TODO
			}
		} else {
			final InstructionArgument bl_ = t.getBacklink();
			if (bl_ instanceof IntegerIA) {
				final @NotNull VariableTableEntry bl               = ((IntegerIA) bl_).getEntry();
				final OS_Element                  resolved_element = bl.getResolvedElement();
				if (resolved_element instanceof FormalArgListItem) {
					target = new DeclTarget(resolved_element, generatedFunction.getFD(), DeclAnchor.AnchorType.PARAMS, errSink);
				} else {
					if (resolved_element instanceof VariableStatementImpl) {
						final OS_Element parent = resolved_element.getParent().getParent();
						if (parent == generatedFunction.getFD()) {
							target = new DeclTarget(resolved_element, parent, DeclAnchor.AnchorType.VAR, errSink);
						} else
							throw new NotImplementedException();
					} else {
						if (resolved_element instanceof IdentExpression)
							target = new DeclTarget(resolved_element, resolved_element, DeclAnchor.AnchorType.MEMBER, errSink);
						else
							target = new DeclTarget(resolved_element, resolved_element.getParent(), DeclAnchor.AnchorType.MEMBER, errSink);
					}
				}
			}
			int y = 2;
		}
		return target;
	}

	public void setDeduceTypes2(final DeduceTypes2 aDeduceTypes2,
								final Context aContext,
								final BaseEvaFunction aGeneratedFunction,
								final ErrSink aErrSink) {
		deduceTypes2      = aDeduceTypes2;
		context           = aContext;
		generatedFunction = aGeneratedFunction;
		errSink           = aErrSink;
	}

	private class DeclTarget implements DeduceElement {
		private @NotNull
		final DeclAnchor anchor;
		private @NotNull
		final OS_Element element;

		public DeclTarget(final @NotNull OS_Element aBest,
						  final @NotNull OS_Element aDeclAnchor,
						  final @NotNull DeclAnchor.AnchorType aAnchorType,
						  final @NotNull ErrSink errSink) throws FCA_Stop {
			element = aBest;
			anchor  = new DeclAnchor(aDeclAnchor, aAnchorType);
			final IInvocation invocation;
			if (aAnchorType != DeclAnchor.AnchorType.VAR) {
				IInvocation declaredInvocation = generatedFunction.fi.getClassInvocation();
				if (declaredInvocation == null) {
					declaredInvocation = generatedFunction.fi.getNamespaceInvocation();
				}
				if (aAnchorType == DeclAnchor.AnchorType.INHERITED) {
					assert declaredInvocation instanceof ClassInvocation;
					invocation = new DerivedClassInvocation((ClassStatement) aDeclAnchor, (ClassInvocation) declaredInvocation);
				} else {
					invocation = declaredInvocation;
				}
			} else {
				DR_Variable v = generatedFunction.getVar((VariableStatement) element);

				if (v.declaredTypeIsEmpty()) {
					System.err.println("8787 declaredTypeIsEmpty for " + ((VariableStatement) element).getName());
					throw new FCA_Stop();
				} else {
					final NormalTypeName   normalTypeName = (NormalTypeName) ((VariableStatementImpl) element).typeName();
					final LookupResultList lrl            = normalTypeName.getContext().lookup(normalTypeName.getName());
					final ClassStatement   classStatement = (ClassStatement) lrl.chooseBest(null);
					invocation = DeduceTypes2.ClassInvocationMake.withGenericPart(classStatement, null, normalTypeName, deduceTypes2, errSink);
				}
			}
			anchor.setInvocation(invocation);
		}

		@Contract(pure = true)
		@Override
		public DeclAnchor declAnchor() {
			return anchor;
		}

		@Contract(pure = true)
		@Override
		public OS_Element element() {
			return element;
		}
	}

}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
