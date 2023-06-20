package tripleo.elijah.stages.deduce;

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_IdentTableEntry;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.ProcIA;
import tripleo.elijah.util.NotImplementedException;

import java.util.Map;

class DTR_VariableStatement {
	private void normalTypeName_generic_butNotNull(final DTR_VS_Ctx ctx) {
		final IElementHolder          eh             = ctx.getEh();
		final GenType                 genType        = ctx.getGenType();
		final @NotNull NormalTypeName normalTypeName = ctx.getNormalTypeName();

		if (eh instanceof final GenericElementHolderWithType eh1) {
			final DeduceTypes2 dt2  = eh1.getDeduceTypes2();
			final OS_Type      type = eh1.getType();

			genType.typeName = new OS_UserType(normalTypeName);
			try {
				final @NotNull GenType resolved = dt2.resolve_type(genType.typeName, variableStatement.getContext());
				if (resolved.resolved.getType() == OS_Type.Type.GENERIC_TYPENAME) {
					final BaseTableEntry backlink = deduceTypeResolve.backlink;

					normalTypeName_generic_butNotNull_resolveToGeneric(genType, resolved, backlink);
				} else {
					normalTypeName_generic_butNotNull_resolveToNonGeneric(genType, resolved);
				}
			} catch (ResolveError aResolveError) {
				aResolveError.printStackTrace();
				assert false;
			}
		} else if (eh instanceof DeduceElement3Holder) {
			NotImplementedException.raise();
		} else
			genType.typeName = new OS_UserType(normalTypeName);
	}

	private final DeduceTypeResolve deduceTypeResolve;

	private final VariableStatement variableStatement;

	public DTR_VariableStatement(final DeduceTypeResolve aDeduceTypeResolve, final VariableStatement aVariableStatement) {
		deduceTypeResolve = aDeduceTypeResolve;
		variableStatement = aVariableStatement;
	}

	private /*static*/ void normalTypeName_generic_butNotNull_resolveToGeneric(final GenType genType, final @NotNull GenType resolved, final @NotNull BaseTableEntry backlink) {
		backlink.typeResolvePromise().then(new DoneCallback<GenType>() {
			@Override
			public void onDone(final GenType result_gt) {
				((Constructable) backlink).constructablePromise().then((final ProcTableEntry result_pte) -> {
					final ClassInvocation ci = result_pte.getClassInvocation();
					assert ci != null;
					final @Nullable Map<TypeName, OS_Type> gp  = ci.genericPart().getMap();
					final TypeName                         sch = resolved.typeName.getTypeName();

					// 05/23 24

					assert gp != null;
					for (Map.Entry<TypeName, OS_Type> entrySet : gp.entrySet()) {
						if (entrySet.getKey().equals(sch)) {
							genType.resolved = entrySet.getValue();
							break;
						}
					}
				});
			}
		});
	}

	private /*static*/ void normalTypeName_generic_butNotNull_resolveToNonGeneric(final @NotNull GenType genType, final @NotNull GenType resolved) {
		genType.resolved = resolved.resolved;
	}

	private void normalTypeName_notGeneric(final DTR_VS_Ctx ctx) {
		final IElementHolder          eh             = ctx.getEh();
		final GenType                 genType        = ctx.getGenType();
		final @NotNull NormalTypeName normalTypeName = ctx.getNormalTypeName();

		final TypeNameList genericPart = normalTypeName.getGenericPart();
		if (eh instanceof GenericElementHolderWithType) {
			normalTypeName_notGeneric_typeProvided(ctx);
		} else
			normalTypeName_notGeneric_typeNotProvided(ctx);
	}

	private void normalTypeName_notGeneric_typeProvided(final DTR_VS_Ctx ctx) {
		final GenType                 genType        = ctx.getGenType();
		final @NotNull NormalTypeName normalTypeName = ctx.getNormalTypeName();

		final GenericElementHolderWithType eh1  = (GenericElementHolderWithType) ctx.getEh();
		final DeduceTypes2                 dt2  = eh1.getDeduceTypes2();
		final OS_Type                      type = eh1.getType();


		genType.nonGenericTypeName = normalTypeName;

		assert normalTypeName == type.getTypeName();

		OS_Type typeName = new OS_UserType(normalTypeName);
		try {
			final @NotNull GenType resolved = dt2.resolve_type(typeName, variableStatement.getContext());
			genType.resolved = resolved.resolved;
		} catch (ResolveError aResolveError) {
			aResolveError.printStackTrace();
			assert false;
		}
	}

	private /*static*/ void normalTypeName_notGeneric_typeNotProvided(final DTR_VS_Ctx ctx) {
		final GenType                 genType        = ctx.getGenType();
		final @NotNull NormalTypeName normalTypeName = ctx.getNormalTypeName();

		genType.nonGenericTypeName = normalTypeName;
	}

	static class DTR_VS_Ctx {
		private final IElementHolder eh;
		private final GenType        genType;
		private final NormalTypeName normalTypeName;

		public DTR_VS_Ctx(final IElementHolder aEh, final GenType aGenType, final NormalTypeName aNormalTypeName) {
			eh             = aEh;
			genType        = aGenType;
			normalTypeName = aNormalTypeName;
		}

		public IElementHolder getEh() {
			return eh;
		}

		public GenType getGenType() {
			return genType;
		}

		public NormalTypeName getNormalTypeName() {
			return normalTypeName;
		}
	}

	public void run(final IElementHolder eh, final GenType genType) {
		final TypeName typeName1 = variableStatement.typeName();

		if (!(typeName1 instanceof final NormalTypeName normalTypeName)) {
			throw new IllegalStateException();
		}

		int state = 0;

		if (normalTypeName.getGenericPart() != null) {
			state = 1;
		} else {
			if (!normalTypeName.isNull()) {
				state = 2;
			}
		}

		DTR_VS_Ctx ctx = new DTR_VS_Ctx(eh, genType, normalTypeName);

		switch (state) {
		case 1:
			normalTypeName_notGeneric(ctx);
			break;
		case 2:
			normalTypeName_generic_butNotNull(ctx);
			break;
		default:
			if (eh instanceof DeduceElement3_IdentTableEntry.DE3_EH_GroundedVariableStatement) {
				DeduceElement3_IdentTableEntry.DE3_EH_GroundedVariableStatement grounded = (DeduceElement3_IdentTableEntry.DE3_EH_GroundedVariableStatement) eh;
				final DeduceElement3_IdentTableEntry                            ground   = grounded.getGround();

				final InstructionArgument bl = ground.principal.getBacklink();
				if (bl instanceof final ProcIA procIA) {
					@NotNull final ProcTableEntry pte_bl = procIA.getEntry();

					assert pte_bl.getStatus() == BaseTableEntry.Status.KNOWN;

					pte_bl.typeResolvePromise().then(gt -> {
						// README when pte_bl has gets a type (GenType),
						//   - it will only have resolved (OS_UserClassType
						//     with ClassStatement).
						//   - we then get the ci and node
						//   - use the node (to an EvaClass) to look at varTableEntries
						//   - pick the first one that matches variableStatement
						//   - wait for it to get an OS_Type
						//      * this actually never happens

						gt.genCIForGenType2(ground.principal.__dt2); // README any will do

						assert gt.ci != null;
						assert gt.node != null;

						for (EvaContainer.VarTableEntry entry : ((EvaContainerNC) gt.node).varTable) {
							if (entry.nameToken.getText().equals(variableStatement.getName())) {
								entry.resolve_varType_cb(new DoneCallback<OS_Type>() {
									@Override
									public void onDone(final OS_Type result) {
										int y = 2;
										System.err.println("7676 DTR_VariableStatement >> " + result);
									}
								});
								break;
							}
						}
					});

					final OS_Element re1 = pte_bl.getResolvedElement();

					final LookupResultList     lrl = re1.getContext().lookup(variableStatement.getName());
					@Nullable final OS_Element e2  = lrl.chooseBest(null);

					if (e2 == null) {
						int y = 2;
					} else {
						int y = 2;
					}
				}
			} else {
				//throw new IllegalStateException("Unexpected value: " + state);
				tripleo.elijah.util.Stupidity.println_err("Unexpected value: " + state);
			}
			break;
		}
	}
}
