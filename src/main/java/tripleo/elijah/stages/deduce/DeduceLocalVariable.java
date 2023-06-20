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

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.AliasStatementImpl;
import tripleo.elijah.lang.impl.BaseFunctionDef;
import tripleo.elijah.lang.types.OS_FuncType;
import tripleo.elijah.stages.deduce.declarations.DeferredMemberFunction;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_VariableTableEntry;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.VariableTableType;
import tripleo.elijah.util.NotImplementedException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created 11/30/21 1:32 AM
 */
public class DeduceLocalVariable {
	private final VariableTableEntry variableTableEntry;
	public DeferredObject2<GenType, Void, Void> type = new DeferredObject2();
	@Nullable
	private static OS_Element ___pt1_work_001b(final BaseEvaFunction generatedFunction,
											   final @NotNull DeducePath dp,
											   final OS_Element self_class,
											   final Object[] aO) {
		final OS_Element e = null;

		final OS_Element Self;
		final OS_Element e_parent = e.getParent();

		short          state = 0;
		ClassStatement b     = null;

		if (e_parent == self_class) {
			state = 1;
		} else {
			b = class_inherits((ClassStatement) self_class, e_parent);
			if (b != null)
				state = 3;
			else
				state = 2;
		}

		switch (state) {
		case 1:
			final InstructionArgument self1 = generatedFunction.vte_lookup("self");
			assert self1 instanceof IntegerIA;
			Self = new DeduceTypes2.OS_SpecialVariable(((IntegerIA) self1).getEntry(), VariableTableType.SELF, generatedFunction);
			break;
		case 2:
			Self = e_parent;
			break;
		case 3:
			final InstructionArgument self2 = generatedFunction.vte_lookup("self");
			assert self2 instanceof IntegerIA;
			Self = new DeduceTypes2.OS_SpecialVariable(((IntegerIA) self2).getEntry(), VariableTableType.SELF, generatedFunction);
			((DeduceTypes2.OS_SpecialVariable) Self).memberInvocation = new MemberInvocation(b, MemberInvocation.Role.INHERITED);
			break;
		default:
			throw new IllegalStateException();
		}
		return Self;
	}
	private Context context;
	private DeduceElement3_VariableTableEntry de3;
	private DeduceTypes2 deduceTypes2;
	private BaseEvaFunction generatedFunction;

	public DeduceLocalVariable(final VariableTableEntry aVariableTableEntry) {
		variableTableEntry = aVariableTableEntry;
	}

	@Nullable
	private static OS_Element ___pt1_work_001(final BaseEvaFunction generatedFunction,
											  final @NotNull OS_Element e,
											  final OS_Element self_class) {
		final OS_Element Self;
		final OS_Element e_parent = e.getParent();

		short          state = 0;
		ClassStatement b     = null;

		if (e_parent == self_class) {
			state = 1;
		} else {
			b = class_inherits((ClassStatement) self_class, e_parent);
			if (b != null)
				state = 3;
			else
				state = 2;
		}

		switch (state) {
		case 1:
			final InstructionArgument self1 = generatedFunction.vte_lookup("self");
			assert self1 instanceof IntegerIA;
			Self = new DeduceTypes2.OS_SpecialVariable(((IntegerIA) self1).getEntry(), VariableTableType.SELF, generatedFunction);
			break;
		case 2:
			Self = e_parent;
			break;
		case 3:
			final InstructionArgument self2 = generatedFunction.vte_lookup("self");
			assert self2 instanceof IntegerIA;
			Self = new DeduceTypes2.OS_SpecialVariable(((IntegerIA) self2).getEntry(), VariableTableType.SELF, generatedFunction);
			((DeduceTypes2.OS_SpecialVariable) Self).memberInvocation = new MemberInvocation(b, MemberInvocation.Role.INHERITED);
			break;
		default:
			throw new IllegalStateException();
		}
		return Self;
	}

	static ClassStatement class_inherits(final ClassStatement aFirstClass, final OS_Element aInherited) {
		if (!(aInherited instanceof ClassStatement)) return null;

		final Map<TypeName, ClassStatement> inh1 = aFirstClass.getContext().inheritance();
		for (Map.Entry<TypeName, ClassStatement> entry : inh1.entrySet()) {
			if (entry.getValue().equals(aInherited))
				return (ClassStatement) aInherited;
		}
		return null;
	}

	private void __pt_work_002(final @NotNull VariableTableEntry vte, final ProcTableEntry procTableEntry, final OS_Element Self) {
		final @Nullable DeferredMemberFunction dm = deduceTypes2.deferred_member_function(Self, null, (BaseFunctionDef) procTableEntry.getResolvedElement(), procTableEntry.getFunctionInvocation());
		dm.externalRef().then(new DoneCallback<BaseEvaFunction>() {
			@Override
			public void onDone(final BaseEvaFunction result) {
				NotImplementedException.raise();
			}
		});
		dm.typePromise().then(new DoneCallback<GenType>() {
			@Override
			public void onDone(final GenType result) {
				procTableEntry.typeDeferred().resolve(result);
			}
		});
		procTableEntry.typePromise().then(new DoneCallback<GenType>() {
			@Override
			public void onDone(final GenType result) {
				vte.type.setAttached(result);
				vte.resolveType(result);
				vte.resolveTypeToClass(result.node);
			}
		});
	}

	private void __pt_work_002b(final @NotNull VariableTableEntry vte,
								final @NotNull ProcTableEntry procTableEntry,
								final Object[] o) {
		final OS_Element Self = (OS_Element) o[0];

		final OS_Element resolvedElement1 = procTableEntry.getResolvedElement();
		OS_Element       resolvedElement0 = resolvedElement1;

		while (resolvedElement0 instanceof AliasStatementImpl) {
			try {
				resolvedElement0 = DeduceLookupUtils._resolveAlias2((AliasStatementImpl) resolvedElement1, deduceTypes2);
			} catch (ResolveError aE) {
				//throw new RuntimeException(aE);
				return;
			}
		}

		if (Self == null) {
			tripleo.elijah.util.Stupidity.println_err_2("336 ======================================================");
			tripleo.elijah.util.Stupidity.println_err_2("336 ======================================================");
			tripleo.elijah.util.Stupidity.println_err_2("336 ======================================================");
			tripleo.elijah.util.Stupidity.println_err_2("336 ======================================================");
			tripleo.elijah.util.Stupidity.println_err_2("336 ======================================================");
			tripleo.elijah.util.Stupidity.println_err_2("336 ======================================================");
			tripleo.elijah.util.Stupidity.println_err_2("336 ======================================================");
			tripleo.elijah.util.Stupidity.println_err_2("336 ======================================================");
			tripleo.elijah.util.Stupidity.println_err_2("336 ======================================================");
			tripleo.elijah.util.Stupidity.println_err_2("336 ======================================================");
			tripleo.elijah.util.Stupidity.println_err_2("336 ======================================================");
			tripleo.elijah.util.Stupidity.println_err_2("336 ======================================================");
			tripleo.elijah.util.Stupidity.println_err_2("336 ======================================================");
			tripleo.elijah.util.Stupidity.println_err_2("336 ======================================================");

			return;
		}

		final @Nullable DeferredMemberFunction dm = deduceTypes2.deferred_member_function(Self, null, (BaseFunctionDef) resolvedElement0, procTableEntry.getFunctionInvocation());
		dm.externalRef().then(new DoneCallback<BaseEvaFunction>() {
			@Override
			public void onDone(final BaseEvaFunction result) {
				NotImplementedException.raise();
			}
		});
		dm.typePromise().then(new DoneCallback<GenType>() {
			@Override
			public void onDone(final GenType result) {
				procTableEntry.typeDeferred().resolve(result);
			}
		});
		procTableEntry.typePromise().then(new DoneCallback<GenType>() {
			@Override
			public void onDone(final GenType result) {
				vte.type.setAttached(result);
				vte.resolveType(result);
				vte.resolveTypeToClass(result.node);
			}
		});
	}

	public void setDeduceTypes2(final DeduceTypes2 aDeduceTypes2, final Context aContext, final BaseEvaFunction aGeneratedFunction) {
		deduceTypes2      = aDeduceTypes2;
		context           = aContext;
		generatedFunction = aGeneratedFunction;

		de3 = new DeduceElement3_VariableTableEntry(variableTableEntry, aDeduceTypes2, aGeneratedFunction);
	}

	public void resolve_var_table_entry_for_exit_function() {
		final VariableTableEntry vte = variableTableEntry;
		final Context            ctx = context;

		if (vte.vtt == VariableTableType.TEMP) {
			final GenType genType = vte.type.genType;
			int           pts     = vte.potentialTypes().size();
			if (genType.typeName != null && genType.typeName == genType.resolved) {
				try {
					genType.resolved = deduceTypes2.resolve_type(genType.typeName, ctx/*genType.typeName.getTypeName().getContext()*/).resolved;
					genType.genCIForGenType2(deduceTypes2);
					vte.resolveType(genType);
					vte.resolveTypeToClass(genType.node);
					int y = 2;
				} catch (ResolveError aResolveError) {
//					aResolveError.printStackTrace();
					deduceTypes2._errSink().reportDiagnostic(aResolveError);
				}
			}
		}

		if (vte.getResolvedElement() == null)
			return;
		{
			if (vte.type.getAttached() == null && vte.constructable_pte != null) {
				ClassStatement         c        = vte.constructable_pte.getFunctionInvocation().getClassInvocation().getKlass();
				final @NotNull OS_Type attached = c.getOS_Type();
				// TODO this should have been set somewhere already
				//  typeName and nonGenericTypeName are not set
				//  but at this point probably wont be needed
				vte.type.genType.resolved = attached;
				vte.type.setAttached(attached);
			}
			if (vte.type.getAttached() == null && vte.potentialTypes().size() > 0) {
				final List<TypeTableEntry> attached_list = vte.potentialTypes()
						.stream()
						.filter(x -> x.getAttached() != null)
						.collect(Collectors.toList());

				if (attached_list.size() == 1) {
					final TypeTableEntry pot = attached_list.get(0);
					vte.type.setAttached(pot.getAttached());
					vte.type.genType.genCI(null, deduceTypes2, deduceTypes2._errSink(), deduceTypes2.phase);
					final @Nullable ClassInvocation classInvocation = (ClassInvocation) vte.type.genType.ci;
					if (classInvocation != null) {
						classInvocation.resolvePromise().then(new DoneCallback<EvaClass>() {
							@Override
							public void onDone(final EvaClass result) {
								vte.resolveTypeToClass(result);
								vte.genType.copy(vte.type.genType); // TODO who knows if this is necessary?
							}
						});
					} // TODO else ??
				} else {
					resolve_var_table_entry_potential_types_1(vte, generatedFunction);
				}
			} else if (vte.type.getAttached() == null && vte.potentialTypes().size() == 0) {
				NotImplementedException.raise();
			}
			{
				final GenType genType = vte.type.genType;
				int           pts     = vte.potentialTypes().size();
				if (genType.typeName != null && genType.typeName == genType.resolved) {
					try {
						genType.resolved = deduceTypes2.resolve_type(genType.typeName, ctx/*genType.typeName.getTypeName().getContext()*/).resolved;
						genType.genCIForGenType2(deduceTypes2);
						vte.resolveType(genType);
						vte.resolveTypeToClass(genType.node);
					} catch (ResolveError aResolveError) {
//						aResolveError.printStackTrace();
						deduceTypes2._errSink().reportDiagnostic(aResolveError);
					}
				}
			}
			vte.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(vte.getResolvedElement()));
			{
				final GenType genType = vte.type.genType;
				if (genType.resolved != null && genType.node == null) {
					if (genType.resolved.getType() != OS_Type.Type.USER_CLASS && genType.resolved.getType() != OS_Type.Type.FUNCTION) {
						try {
							genType.resolved = deduceTypes2.resolve_type(genType.resolved, ctx).resolved;
						} catch (ResolveError aResolveError) {
							aResolveError.printStackTrace();
							assert false;
						}
					}

					//genCI(genType, genType.nonGenericTypeName);

					//
					// registerClassInvocation does the job of makeNode, so results should be immediately available
					//
					short state = 1;
					if (vte.getCallablePTE() != null) {
						final @Nullable ProcTableEntry callable_pte = vte.getCallablePTE();
						if (callable_pte.expression instanceof FuncExpr) {
							state = 2;
						}
					}

					switch (state) {
					case 1:
						genType.genCIForGenType2(deduceTypes2); // TODO what is this doing here? huh?
						break;
					case 2: {
						final FuncExpr       fe  = (FuncExpr) vte.getCallablePTE().expression;
						final DeduceProcCall dpc = vte.getCallablePTE().dpc;

						//final DeduceFuncExpr target = (DeduceFuncExpr) dpc.target;

						final DeduceElement target = dpc.target;
						NotImplementedException.raise();

//							type.resolve(new GenType() {target.prototype}): // DeduceType??
						// TODO because we can already represent a function expression,
						//  the question is can we generatedFunction.lookupExpression(fe) and get the DeduceFuncExpr?
					}
					break;
					}

					if (genType.ci != null) { // TODO we may need this call...
						((ClassInvocation) genType.ci).resolvePromise().then(new DoneCallback<EvaClass>() {
							@Override
							public void onDone(EvaClass result) {
								genType.node = result;
								if (!vte.typePromise().isResolved()) { // HACK
									if (genType.resolved instanceof final OS_FuncType resolved) {
										result.functionMapDeferred(((FunctionDef) resolved.getElement()), new FunctionMapDeferred() {
											@Override
											public void onNotify(final EvaFunction aGeneratedFunction) {
												// TODO check args (hint functionInvocation.pte)
												//  but against what? (vte *should* have callable_pte)
												//  if not, then try potential types for a PCE
												aGeneratedFunction.typePromise().then(new DoneCallback<GenType>() {
													@Override
													public void onDone(final GenType result) {
														vte.resolveType(result);
													}
												});
											}
										});
									} else
										vte.resolveType(genType);
								}
							}
						});
					}
				}
			}
		}
	}

	public void resolve_var_table_entry_potential_types_1(final @NotNull VariableTableEntry vte, final BaseEvaFunction generatedFunction) {
		if (vte.potentialTypes().size() == 1) {
			final TypeTableEntry tte1 = vte.potentialTypes().iterator().next();
			if (tte1.tableEntry instanceof final ProcTableEntry procTableEntry) {
				final DeduceProcCall dpc = procTableEntry.deduceProcCall();
				// TODO for argument, we need a DeduceExpression (DeduceProcCall) which is bounud to self
				//  (inherited), so we can extract the invocation
				final InstructionArgument ia = procTableEntry.expression_num;

				if (ia instanceof IntegerIA) return;

				final DeducePath dp = (((IdentIA) ia).getEntry()).buildDeducePath(generatedFunction);

				OS_Element Self = null;

				boolean callbackFlag = false;

				Object[] o = new Object[1];

				if (dp.size() == 1) { //ia.getEntry().backlink == null
					final @Nullable OS_Element e          = dp.getElement(0);
					final OS_Element           self_class = generatedFunction.getFD().getParent();

					if (e == null) {
						//___pt1_work_001b(generatedFunction, dp, self_class, o);
						callbackFlag = true;
					} else {
						Self = ___pt1_work_001(generatedFunction, e, self_class);
					}
				} else
					Self = dp.getElement(dp.size() - 2); // TODO fix this

				if (callbackFlag) {
					__pt_work_002b(vte, procTableEntry, o);
				} else {
					__pt_work_002(vte, procTableEntry, Self);
				}

			}
		} else {
			throw new IllegalStateException("Unexpected value: " + vte.potentialTypes().size());
		}
	}

	static class MemberInvocation {
		final OS_Element element;
		final Role role;

		public MemberInvocation(final OS_Element aElement, final Role aRole) {
			element = aElement;
			role    = aRole;
		}

		enum Role {DIRECT, INHERITED}

	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
