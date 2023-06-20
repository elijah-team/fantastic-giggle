package tripleo.elijah.stages.deduce.post_bytecode;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.contexts.ModuleContext;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.BaseFunctionDef;
import tripleo.elijah.lang.imports.NormalImportStatement;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.nextgen.query.Operation2;
import tripleo.elijah.stages.deduce.*;
import tripleo.elijah.stages.deduce.nextgen.DR_Ident;
import tripleo.elijah.stages.deduce.nextgen.DR_PossibleType;
import tripleo.elijah.stages.deduce.post_bytecode.DED.DED_ITE;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.ProcIA;
import tripleo.elijah.stateful.DefaultStateful;
import tripleo.elijah.stateful.State;
import tripleo.elijah.util.NotImplementedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DeduceElement3_IdentTableEntry extends DefaultStateful implements IDeduceElement3 {

	public final IdentTableEntry principal;
	public        DeduceTypes2        deduceTypes2;
	public BaseEvaFunction generatedFunction;
	private       GenType             _resolved;
	private final DeduceElement3_Type _type = new DeduceElement3_Type() {

		@Contract(pure = true)
		@Override
		public GenType genType() {
			return typeTableEntry().genType;
		}

		@Override
		public Operation2<GenType> resolved(final Context ectx) {
			try {
				if (_resolved == null) {
					_resolved = deduceTypes2.resolve_type(genType().typeName, ectx);

					typeTableEntry().setAttached(_resolved);
				}

				return Operation2.success(_resolved);
			} catch (ResolveError aResolveError) {
				return Operation2.failure(aResolveError);
			}
		}

		@Contract(pure = true)
		@Override
		public TypeTableEntry typeTableEntry() {
			return principal.type;
		}
	};
	private       Context             context;
	private       Context             fdCtx;
	private GenType genType;

	@Contract(pure = true)
	public DeduceElement3_IdentTableEntry(final IdentTableEntry aIdentTableEntry) {
		principal = aIdentTableEntry;
	}

	public void _ctxts(final Context aFdCtx, final Context aContext) {
		fdCtx   = aFdCtx;
		context = aContext;
	}

	public void assign_type_to_idte(final Context aFunctionContext, final Context aContext) {
		new ST.ExitGetType().assign_type_to_idte(principal, generatedFunction, aFunctionContext, aContext, deduceTypes2, deduceTypes2._phase());
	}

	public void backlinkPte(final @NotNull ClassStatement classStatement,
							final ProcTableEntry ignoredPte,
							final IElementHolder __eh) {
		// README classStatement [T310-231]

		// README setStause on callablePTE and principal

		final String text = principal.getIdent().getText();

		final LookupResultList     lrl = classStatement.getContext().lookup(text);
		final @Nullable OS_Element e   = lrl.chooseBest(null);

		//procTableEntry.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(classStatement));  // infinite recursion
		final ProcTableEntry callablePTE = principal.getCallablePTE();
		if (callablePTE != null && e != null) {
			assert e instanceof BaseFunctionDef;  // sholud fail for constructor and destructor
			callablePTE.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(e));
		}

		if (principal.getStatus() == BaseTableEntry.Status.UNCHECKED) {
			final OS_Element e2 = __eh.getElement();
			assert e2 != null;

			assert e != null;
			if (e instanceof VariableStatement) {
				principal.setStatus(BaseTableEntry.Status.KNOWN, new DE3_EH_GroundedVariableStatement((VariableStatement) e, this));
			} else if (e instanceof FunctionDef) {
				principal.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder((FunctionDef) e));
			}
		}
	}

	@Override
	public DeduceTypes2 deduceTypes2() {
		return deduceTypes2;
	}

	@Override
	public DED elementDiscriminator() {
		return new DED_ITE(principal);
	}

	@Override
	public BaseEvaFunction generatedFunction() {
		return generatedFunction;
	}

	@Override
	public @NotNull GenType genType() {
		if (genType == null) {
			genType = new GenType();
		}
		return genType;
		//return principal.type.genType;
	}

	@Override
	public OS_Element getPrincipal() {
		return principal.getDeduceElement3(deduceTypes2, generatedFunction).getPrincipal();
	}

	@Override
	public DeduceElement3_Kind kind() {
		return DeduceElement3_Kind.GEN_FN__ITE;
	}

	//	@NotNull final GenType xx = // TODO xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
	@Override
	public void resolve(final Context aContext, final DeduceTypes2 aDeduceTypes2) {
		//		deduceTypes2.resolveIdentIA_(aContext, aIdentIA, generatedFunction, aFoundElement);
		throw new NotImplementedException();
		// careful with this
		//		throw new UnsupportedOperationException("Should not be reached");
	}

	@Override
	public void resolve(final IdentIA aIdentIA, final @NotNull Context aContext, final FoundElement aFoundElement) {
		// FoundElement is the "disease"
		deduceTypes2.resolveIdentIA_(aContext, aIdentIA, generatedFunction, aFoundElement);
	}

	public EvaNode getResolved() {
		EvaClass R    = null;
		Context  ectx = principal.getIdent().getContext();

		if (_type.typeTableEntry().genType.typeName == null) {
			// README we don't actually care about the typeName, we just
			// wanted to use it to recreate a GenType, where we can then
			// extract .node

			//_type.typeTableEntry().genType.typeName = null;
			//throw new Error();
		}

		if (principal.getResolvedElement() instanceof ClassStatement) {
			// README but skip this and get the evaClass saved from earlier to
			// Grande [T168-089] when all these objects are being created and
			// manipulated (dern video yttv)
			final DG_ClassStatement dcs = principal.__dt2.DG_ClassStatement((ClassStatement) principal.getResolvedElement());

			// README fixup GenType
			//   Still ignoring TypeName and nonGenericTypeName
			//   b/c only client is in gen_c, not deduce
			final GenType gt1 = _type.typeTableEntry().genType;
			//gt1.typeName
			gt1.ci                 = dcs.classInvocation();
			gt1.node               = dcs.evaClass();
			gt1.functionInvocation = dcs.functionInvocation();

			return dcs.evaClass();
		}

		if (false) {
			// README to "prosecute" this we need a ContextImpl. But where to get
			// it from?  And can we #resolveTypeToClass with `dcs' above?
			// Technically, there is one (more) above, but this line does not
			// produce results.
			final Operation2<GenType> or = _type.resolved(ectx);
			if (or.mode() == Mode.SUCCESS) {
				R = (EvaClass) or.success().resolved;
			}

			assert R != null;
			principal.resolveTypeToClass(R);
		}
		return R;
	}

	public Operation2<GenType> resolve1(final IdentTableEntry ite, final @NotNull Context aContext) {
		// FoundElement is the "disease"
		try {
			return Operation2.success(deduceTypes2.resolve_type(ite.type.getAttached(), aContext));
		} catch (final ResolveError aE) {
			return Operation2.failure(aE);
		}
	}

	public void setDeduceTypes(final DeduceTypes2 aDeduceTypes2, final BaseEvaFunction aGeneratedFunction) {
		deduceTypes2      = aDeduceTypes2;
		generatedFunction = aGeneratedFunction;
	}

	public void sneakResolve() {
		final IdentExpression ident = principal.getIdent();
		final Context         ctx   = ident.getContext();

		final LookupResultList lrl = ctx.lookup(ident.getText());
		OS_Element[]           elx = {null};
		OS_Element             el  = lrl.chooseBest(null);

		if (el == null) {
			final InstructionArgument bl1 = principal.getBacklink();
			if (bl1 != null) {
				el = sneak_el_null__bl1_not_null(ident, elx, el, bl1);
			} else {
				el = sneak_el_null__bl1_null(ident, el);
			}

			if (el == null)
				//////////////
				//////////////
				//////////////
				//////////////calculate
				//////////////
				//////////////
				//////////////
				//////////////
				//////////////
				//////////////
				//////////////
				//////////////
				//////////////
				return; ///throw new Error();
			//////////////
			//////////////
			//////////////
			//////////////
			//////////////
			//////////////
			//////////////
			//////////////
			//////////////
			//////////////
			//////////////
			//////////////
			//////////////
			//////////////
			//////////////
			//////////////
			//////////////
			//////////////
			//////////////
		}

		if (principal.getCallablePTE() != null) {
			@NotNull final ProcTableEntry callable = principal.getCallablePTE();

			final IExpression left = callable.expression.getLeft();
			if (left == principal.getIdent()) {
				// TODO is this duplication ok??
				final DE3_ITE_Holder de3_ite_holder = new DE3_ITE_Holder(el);
				callable.setStatus(BaseTableEntry.Status.KNOWN, de3_ite_holder);
				de3_ite_holder.commitGenTypeActions();
			}
		}

		final DE3_ITE_Holder de3_ite_holder = new DE3_ITE_Holder(el);
		principal.setStatus(BaseTableEntry.Status.KNOWN, de3_ite_holder);
		de3_ite_holder.commitGenTypeActions();
	}

	private OS_Element sneak_el_null__bl1_not_null(final IdentExpression ident, final OS_Element[] elx, OS_Element el, final InstructionArgument bl1) {
		if (bl1 instanceof IntegerIA) {
			final IntegerIA                   integerIA = (IntegerIA) bl1;
			@NotNull final VariableTableEntry vte_bl1   = integerIA.getEntry();

			// get DR
			final DR_Ident x = generatedFunction.getIdent(ident, vte_bl1);
			x.foo();

			// get backlink
			final DR_Ident b = generatedFunction.getIdent(vte_bl1);
			b.foo();

			// set up rel
			b.onPossibleType((DR_PossibleType pt) -> x.proposeType(pt));

			{
				for (TypeTableEntry potentialType : vte_bl1.potentialTypes()) {
					var y = potentialType.tableEntry;
					if (y instanceof ProcTableEntry pte) {
						var z  = pte.expression;
						var zz = generatedFunction.getProcCall(z, pte);

						if (pte.getStatus() == BaseTableEntry.Status.KNOWN) {
							var ci = pte.getClassInvocation();
							var fi = pte.getFunctionInvocation();

							System.err.println(ci);
							System.err.println(fi);

							var pt = new DR_PossibleTypeCI(ci, fi);
							b.addPossibleType(pt);

							//deduceTypes2.phase.reg
							var gf = fi.makeGenerated(deduceTypes2.phase.generatePhase, deduceTypes2.phase);
							System.err.println(gf);

							InstructionArgument ret = (gf.vte_lookup("Result"));
							if (ret instanceof IntegerIA aIntegerIA) {
								var retvte = aIntegerIA.getEntry();
								retvte.typeResolvePromise().then(gt -> {
									throw new Error();
								});
								var retvtept = retvte.potentialTypes();
								for (TypeTableEntry typeTableEntry : retvtept) {

								}

								var retvtety = retvte.type;
								if (retvtety.getAttached() != null) {
									var att  = retvtety.getAttached();
									var resl = att.resolve(principal.getIdent().getContext());

									var ci11 = deduceTypes2.phase.registerClassInvocation(resl.getClassOf());

									System.err.println(resl);
									var pt2 = new DR_PossibleTypeCI(ci11, null);
									b.addPossibleType(pt2);
								}
							}
						}
					}

				}
			}

			final DeduceTypeResolve tr = vte_bl1.typeResolve();

			if (vte_bl1.typeDeferred_isResolved()) {
				vte_bl1.typePromise().then(type1 -> {
					if (tr.typeResolution().isPending())
						((DeferredObject<GenType, ?, ?>) tr.typeResolution()).resolve(type1);

					if (type1.resolved != null) {
						final Context          ctx2 = type1.resolved.getClassOf().getContext();
						final LookupResultList lrl2 = ctx2.lookup(ident.getText());
						elx[0] = lrl2.chooseBest(null);
					}
				});

				el = elx[0];
			} else {
				vte_bl1.dlv.resolve_var_table_entry_for_exit_function();

				GenType gt = new GenType();
				vte_bl1.resolveType(gt);
			}
			int y = 2;
		} else if (bl1 instanceof ProcIA) {
			final ProcIA         procIA = (ProcIA) bl1;
			final ProcTableEntry pte1   = procIA.getEntry();

			int yyy  = 2;
			int yyyy = yyy + 1;

			final IDeduceElement3 de3 = pte1.getDeduceElement3();
		}
		return el;
	}

	@Nullable
	private OS_Element sneak_el_null__bl1_null(final IdentExpression ident, OS_Element el) {
		List<Context> ctxs = new ArrayList<>();

		Context ctx2 = principal.getIdent().getContext();
		boolean f    = true;
		while (f) {
			if (ctxs.contains(ctx2)) {
				f = false;
				continue;
			}
			if (ctx2 == null) {
				f = false;
				continue;
			}

			if (ctx2 instanceof ModuleContext) {
				ctxs.add(ctx2);

				final @NotNull Collection<ModuleItem> itms = ((ModuleContext) ctx2).getCarrier().getItems();
				for (ModuleItem moduleItem : itms) {
					if (moduleItem instanceof NormalImportStatement) {
						NormalImportStatement importStatement = (NormalImportStatement) moduleItem;
						ctx2 = importStatement.myContext();

						final LookupResultList lrl2 = ctx2.lookup(ident.getText());
						el = lrl2.chooseBest(null);

						if (el != null) {
							f = false;
							break;
						}
					}
				}

				ctx2 = ctx2.getParent();
			} else {
				ctxs.add(ctx2);
				ctx2 = ctx2.getParent();
			}
		}
		return el;
	}

	public DeduceElement3_Type type() {
		return _type;
	}

	public enum ST {
		;

		public static State EXIT_GET_TYPE;

		public static void register(final DeducePhase phase) {
			EXIT_GET_TYPE = phase.register(new ExitGetType());
		}

		static class ExitGetType implements State {
			private int identity;

			@Override
			public void apply(final DefaultStateful element) {
				final DeduceElement3_IdentTableEntry ite_de             = ((DeduceElement3_IdentTableEntry) element);
				final IdentTableEntry                ite                = ite_de.principal;
				final BaseEvaFunction                generatedFunction1 = ite_de.generatedFunction();
				final DeduceTypes2                   dt2                = ite_de.deduceTypes2;
				final DeducePhase                    phase1             = ite_de.deduceTypes2._phase();

				final Context          aFd_ctx  = ite_de.fdCtx;
				@NotNull final Context aContext = ite_de.context;

				assign_type_to_idte(ite, generatedFunction1, aFd_ctx, aContext, dt2, phase1);
			}

			public void assign_type_to_idte(@NotNull final IdentTableEntry ite,
											@NotNull final BaseEvaFunction generatedFunction,
											@NotNull final Context aFunctionContext,
											@NotNull final Context aContext,
											@NotNull final DeduceTypes2 dt2,
											@NotNull final DeducePhase phase) {
				if (!ite.hasResolvedElement()) {
					@NotNull final IdentIA ident_a = new IdentIA(ite.getIndex(), generatedFunction);
					dt2.resolveIdentIA_(aContext, ident_a, generatedFunction, new FoundElement(phase) {

						final String path = generatedFunction.getIdentIAPathNormal(ident_a);

						@Override
						public void foundElement(final OS_Element x) {
							if (ite.getResolvedElement() != x)
								ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(x));
							if (ite.type != null && ite.type.getAttached() != null) {
								switch (ite.type.getAttached().getType()) {
								case USER:
									try {
										@NotNull final GenType xx = dt2.resolve_type(ite.type.getAttached(), aFunctionContext);
										ite.type.setAttached(xx);
									} catch (final ResolveError resolveError) {
										dt2._LOG().info("192 Can't attach type to " + path);
										dt2._errSink().reportDiagnostic(resolveError);
									}
									if (ite.type.getAttached().getType() == OS_Type.Type.USER_CLASS) {
										use_user_class(ite.type.getAttached(), ite);
									}
									break;
								case USER_CLASS:
									use_user_class(ite.type.getAttached(), ite);
									break;
								case FUNCTION: {
									// TODO All this for nothing
									//  the ite points to a function, not a function call,
									//  so there is no point in resolving it
									if (ite.type.tableEntry instanceof final @NotNull ProcTableEntry pte) {

									} else if (ite.type.tableEntry instanceof final @NotNull IdentTableEntry identTableEntry) {
										if (identTableEntry.getCallablePTE() != null) {
											@Nullable final ProcTableEntry cpte = identTableEntry.getCallablePTE();
											cpte.typePromise().then(new DoneCallback<GenType>() {
												@Override
												public void onDone(@NotNull final GenType result) {
													tripleo.elijah.util.Stupidity.println2("1483 " + result.resolved + " " + result.node);
												}
											});
										}
									}
								}
								break;
								default:
									throw new IllegalStateException("Unexpected value: " + ite.type.getAttached().getType());
								}
							} else {
								final int yy = 2;
								if (!ite.hasResolvedElement()) {
									@Nullable LookupResultList lrl = null;
									try {
										lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), aFunctionContext, dt2);
										@Nullable final OS_Element best = lrl.chooseBest(null);
										if (best != null) {
											ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(x));
											if (ite.type != null && ite.type.getAttached() != null) {
												if (ite.type.getAttached().getType() == OS_Type.Type.USER) {
													try {
														@NotNull final GenType xx = dt2.resolve_type(ite.type.getAttached(), aFunctionContext);
														ite.type.setAttached(xx);
													} catch (final ResolveError resolveError) { // TODO double catch
														dt2._LOG().info("210 Can't attach type to " + ite.getIdent());
														dt2._errSink().reportDiagnostic(resolveError);
//												continue;
													}
												}
											}
										} else {
											dt2._LOG().err("184 Couldn't resolve " + ite.getIdent());
										}
									} catch (final ResolveError aResolveError) {
										dt2._LOG().err("184-506 Couldn't resolve " + ite.getIdent());
										aResolveError.printStackTrace();
									}
									if (ite.type.getAttached().getType() == OS_Type.Type.USER_CLASS) {
										use_user_class(ite.type.getAttached(), ite);
									}
								}
							}
						}

						@Override
						public void noFoundElement() {
							ite.setStatus(BaseTableEntry.Status.UNKNOWN, null);
							dt2._errSink().reportError("165 Can't resolve " + path);
						}

						private void use_user_class(@NotNull final OS_Type aType, @NotNull final IdentTableEntry aEntry) {
							final ClassStatement cs = aType.getClassOf();
							if (aEntry.constructable_pte != null) {
								final int yyy = 3;
								tripleo.elijah.util.Stupidity.println2("use_user_class: " + cs);
							}
						}
					});
				}
			}

			@Override
			public boolean checkState(final DefaultStateful aElement3) {
				return true;
			}

			@Override
			public void setIdentity(final int aId) {
				identity = aId;
			}
		}
	}

	public class DE3_EH_GroundedVariableStatement implements IElementHolder {
		private final @NotNull VariableStatement              element;
		private final          DeduceElement3_IdentTableEntry ground;
		private                List<setup_GenType_Action>     actions = new ArrayList<>();

		public DE3_EH_GroundedVariableStatement(final @NotNull VariableStatement aVariableStatement, final DeduceElement3_IdentTableEntry aPrincipal) {
			element = aVariableStatement;
			ground  = aPrincipal;
		}

		public void commitGenTypeActions() {
			final setup_GenType_Action_Arena arena = new setup_GenType_Action_Arena();

			if (principal.type == null) {
				principal.type = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null);
			}
			if (genType == null) {
				genType = principal.type.genType;
			}

			for (setup_GenType_Action action : actions) {
				action.run(principal.type.genType, arena);
			}
		}

		public void genTypeAction(final SGTA_SetResolvedClass aSGTASetResolvedClass) {
			actions.add(aSGTASetResolvedClass);
		}

		@Override
		public @NotNull VariableStatement getElement() {
			return element;
		}

		public DeduceElement3_IdentTableEntry getGround() {
			return ground;
		}
	}

	public class DE3_ITE_Holder implements IElementHolder {
		private                List<setup_GenType_Action> actions = new ArrayList<>();
		private final @NotNull OS_Element                 element;

		public DE3_ITE_Holder(final @NotNull OS_Element aElement) {
			element = aElement;
		}

		public void commitGenTypeActions() {
			final setup_GenType_Action_Arena arena = new setup_GenType_Action_Arena();

			if (principal.type == null) {
				principal.type = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null);
			}
			if (genType == null) {
				genType = principal.type.genType;
			}

			for (setup_GenType_Action action : actions) {
				action.run(principal.type.genType, arena);
			}
		}

		public void genTypeAction(final SGTA_SetResolvedClass aSGTASetResolvedClass) {
			actions.add(aSGTASetResolvedClass);
		}

		@Override
		public @NotNull OS_Element getElement() {
			return element;
		}
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
