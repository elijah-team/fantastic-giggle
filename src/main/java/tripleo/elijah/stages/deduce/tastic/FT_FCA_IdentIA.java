package tripleo.elijah.stages.deduce.tastic;

import org.jdeferred2.Promise;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.i.ErrSink;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.AliasStatementImpl;
import tripleo.elijah.lang.impl.StatementWrapperImpl;
import tripleo.elijah.lang.impl.VariableStatementImpl;
import tripleo.elijah.lang.types.OS_BuiltinType;
import tripleo.elijah.lang.types.OS_UnknownType;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.lang2.BuiltInTypes;
import tripleo.elijah.stages.deduce.*;
import tripleo.elijah.stages.deduce.declarations.DeferredMemberFunction;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.*;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.NotImplementedException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static tripleo.elijah.stages.deduce.DeduceTypes2.to_int;

/*static*/ class FT_FCA_IdentIA {

	private final FT_FnCallArgs FTFnCallArgs;
	private final IdentIA identIA;

	public void loop1(final @NotNull ProcTableEntry pte,
					  final BaseEvaFunction generatedFunction,
					  final Context ctx,
					  final int instructionIndex,
					  final DeduceTypes2.DeduceClient4 dc,
					  final ErrSink errSink) {
		List<TypeTableEntry> args = pte.getArgs();

		for (int i = 0; i < args.size(); i++) {
			final TypeTableEntry tte = args.get(i); // TODO this looks wrong
//			LOG.info("770 "+tte);

			final FT_FCA_Ctx fdctx = new FT_FCA_Ctx(generatedFunction, tte, ctx, errSink, dc);

			IExpression e = tte.expression;
			if (e == null) continue;
			if (e instanceof SubExpression) e = ((SubExpression) e).getExpression();
			switch (e.getKind()) {
			case NUMERIC:
				tte.setAttached(new OS_BuiltinType(BuiltInTypes.SystemInteger));
				//vte.type = tte;
				break;
			case CHAR_LITERAL:
				tte.setAttached(new OS_BuiltinType(BuiltInTypes.SystemCharacter));
				break;
			case IDENT:
				do_assign_call_args_ident(vte, instructionIndex, pte, i, (IdentExpression) e, fdctx);
				break;
			case PROCEDURE_CALL:
				__loop1__PROCEDURE_CALL(pte, (ProcedureCallExpression) e, fdctx);
				break;
			case DOT_EXP:
				final @NotNull DotExpression de = (DotExpression) e;
				__loop1__DOT_EXP(de, fdctx);
				break;
			case ADDITION:
			case MODULO:
			case SUBTRACTION:
				int y = 2;
				tripleo.elijah.util.Stupidity.println_err_2("2363");
				break;
			case GET_ITEM:
				final @NotNull GetItemExpression gie = (GetItemExpression) e;
				do_assign_call_GET_ITEM(gie, fdctx);
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + e.getKind());
			}
		}
	}

	void do_assign_call_args_ident(@NotNull VariableTableEntry vte,
								   int aInstructionIndex,
								   @NotNull ProcTableEntry aPte,
								   int aI,
								   @NotNull IdentExpression aExpression,
								   final FT_FCA_Ctx fdctx) {
		final DeduceTypes2.DeduceClient4 dc                = fdctx.dc();
		final ErrSink                    errSink           = fdctx.errSink();
		final TypeTableEntry             aTte              = fdctx.tte();
		final BaseEvaFunction            generatedFunction = fdctx.generatedFunction();
		final Context                    ctx               = fdctx.ctx();

		final String                        e_text = aExpression.getText();
		final @Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(e_text);
//		LOG.info("10000 "+vte_ia);
		if (vte_ia != null) {
			final @NotNull VariableTableEntry  vte1 = generatedFunction.getVarTableEntry(to_int(vte_ia));
			final Promise<GenType, Void, Void> p    = VTE_TypePromises.do_assign_call_args_ident_vte_promise(aTte, vte1);
			@NotNull Runnable runnable = new Runnable() {
				boolean isDone;

				private void __doLogic0__FormalArgListItem(final @NotNull FormalArgListItem fali) {
					new FT_FCA_FormalArgListItem(fali, generatedFunction).doLogic0(vte, vte1, errSink);
				}

				private void __doLogic0__VariableStatement(final @NotNull VariableStatementImpl vs) {
					new FT_FCA_VariableStatement(vs, generatedFunction).doLogic0(e_text, p, vte1, vte);
				}

				public void doLogic(@NotNull List<TypeTableEntry> potentialTypes) {
					switch (potentialTypes.size()) {
					case 1:
						doLogic1(potentialTypes);
						break;
					case 0:
						doLogic0();
						break;
					default:
						doLogic_default(potentialTypes);
						break;
					}
				}

				private void doLogic_default(final @NotNull List<TypeTableEntry> potentialTypes) {
					// TODO hopefully this works
					final @NotNull List<TypeTableEntry> potentialTypes1 = potentialTypes.stream()
							.filter(input -> input.getAttached() != null)
							.collect(Collectors.toList());

					// prevent infinite recursion
					if (potentialTypes1.size() < potentialTypes.size())
						doLogic(potentialTypes1);
					else
						FTFnCallArgs.LOG.info("913 Don't know");
				}

				private void doLogic0() {
					// README moved up here to elimiate work
					if (p.isResolved()) {
						System.out.printf("890-1 Already resolved type: vte1.type = %s, gf = %s %n", vte1.type, generatedFunction);
						return;
					}
					LookupResultList     lrl  = ctx.lookup(e_text);
					@Nullable OS_Element best = lrl.chooseBest(null);
					if (best instanceof @NotNull final FormalArgListItem fali) {
						__doLogic0__FormalArgListItem(fali);
					} else if (best instanceof final @NotNull VariableStatementImpl vs) {
						__doLogic0__VariableStatement(vs);
					} else {
						int y = 2;
						FTFnCallArgs.LOG.err("543 " + best.getClass().getName());
						throw new NotImplementedException();
					}
				}

				private void doLogic1(final @NotNull List<TypeTableEntry> potentialTypes) {
//						tte.attached = ll.get(0).attached;
//						vte.addPotentialType(instructionIndex, ll.get(0));
					if (p.isResolved()) {
						FTFnCallArgs.LOG.info(String.format("1047 (vte already resolved) %s vte1.type = %s, gf = %s, tte1 = %s %n", vte1.getName(), vte1.type, generatedFunction, potentialTypes.get(0)));
						return;
					}

					final OS_Type attached = potentialTypes.get(0).getAttached();
					if (attached == null) return;
					switch (attached.getType()) {
					case USER:
						vte1.type.setAttached(attached); // !!
						break;
					case USER_CLASS:
						final GenType gt = vte1.genType;
						gt.resolved = attached;
						vte1.resolveType(gt);
						break;
					default:
						errSink.reportWarning("Unexpected value: " + attached.getType());
//							throw new IllegalStateException("Unexpected value: " + attached.getType());
					}
				}

				@Override
				public void run() {
					if (isDone) return;
					final @NotNull List<TypeTableEntry> ll = dc.getPotentialTypesVte((EvaFunction) generatedFunction, vte_ia);
					doLogic(ll);
					isDone = true;
				}
			};
			dc.onFinish(runnable);
		} else {
			int                      ia   = generatedFunction.addIdentTableEntry(aExpression, ctx);
			@NotNull IdentTableEntry idte = generatedFunction.getIdentTableEntry(ia);
			idte.addPotentialType(aInstructionIndex, aTte); // TODO DotExpression??
			final int ii = aI;
			idte.onType(dc.getPhase(), new OnType() {
				@Override
				public void noTypeFound() {
					FTFnCallArgs.LOG.err("719 no type found " + generatedFunction.getIdentIAPathNormal(new IdentIA(ia, generatedFunction)));
				}

				@Override
				public void typeDeduced(@NotNull OS_Type aType) {
					aPte.setArgType(ii, aType); // TODO does this belong here or in FunctionInvocation?
					aTte.setAttached(aType); // since we know that tte.attached is always null here
				}
			});
		}
	}

	private final VariableTableEntry vte;

	public FT_FCA_IdentIA(final FT_FnCallArgs aFTFnCallArgs, final IdentIA aIdentIA, final VariableTableEntry aVte) {
		FTFnCallArgs = aFTFnCallArgs;
		identIA      = aIdentIA;
		vte          = aVte;
	}

	private void __loop1__DOT_EXP(final @NotNull DotExpression de, final FT_FCA_Ctx fdctx) {
		final DeduceTypes2.DeduceClient4 dc                = fdctx.dc();
		final ErrSink                    errSink           = fdctx.errSink();
		final TypeTableEntry             tte               = fdctx.tte();
		final BaseEvaFunction            generatedFunction = fdctx.generatedFunction();
		final Context                    ctx               = fdctx.ctx();

		try {
			final LookupResultList lrl  = dc.lookupExpression(de.getLeft(), ctx);
			@Nullable OS_Element   best = lrl.chooseBest(null);
			if (best != null) {
				while (best instanceof AliasStatementImpl) {
					best = dc._resolveAlias2((AliasStatementImpl) best);
				}
				if (best instanceof FunctionDef) {
					tte.setAttached(((FunctionDef) best).getOS_Type());
					//vte.addPotentialType(instructionIndex, tte);
				} else if (best instanceof ClassStatement) {
					tte.setAttached(((ClassStatement) best).getOS_Type());
				} else if (best instanceof final @NotNull VariableStatementImpl vs) {
					@Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(vs.getName());
					TypeTableEntry                tte1   = ((IntegerIA) Objects.requireNonNull(vte_ia)).getEntry().type;
					tte.setAttached(tte1.getAttached());
				} else {
					final int y = 2;
					FTFnCallArgs.LOG.err(best.getClass().getName());
					throw new NotImplementedException();
				}
			} else {
				final int y = 2;
				throw new NotImplementedException();
			}
		} catch (ResolveError aResolveError) {
			aResolveError.printStackTrace();
			int y = 2;
			throw new NotImplementedException();
		}
	}

	private void __loop1__PROCEDURE_CALL(final ProcTableEntry pte, final ProcedureCallExpression pce, final @NotNull FT_FCA_Ctx fdctx) {
		final DeduceTypes2.DeduceClient4 dc                = fdctx.dc();
		final ErrSink                    errSink           = fdctx.errSink();
		final TypeTableEntry             tte               = fdctx.tte();
		final BaseEvaFunction            generatedFunction = fdctx.generatedFunction();
		final Context                    ctx               = fdctx.ctx();

		FT_FCA_ProcedureCall fcapce = new FT_FCA_ProcedureCall(pce, ctx);

		try {
			final LookupResultList lrl  = dc.lookupExpression(pce.getLeft(), ctx);
			@Nullable OS_Element   best = lrl.chooseBest(null);
			if (best != null) {
				while (best instanceof AliasStatementImpl) {
					best = dc._resolveAlias2((AliasStatementImpl) best);
				}
				if (best instanceof FunctionDef) {
					final OS_Element      parent = best.getParent();
					@Nullable IInvocation invocation;
					if (parent instanceof NamespaceStatement) {
						invocation = dc.registerNamespaceInvocation((NamespaceStatement) parent);
					} else if (parent instanceof ClassStatement) {
						@NotNull ClassInvocation ci = new ClassInvocation((ClassStatement) parent, null);
						invocation = dc.registerClassInvocation(ci);
					} else
						throw new NotImplementedException(); // TODO implement me

					dc.forFunction(dc.newFunctionInvocation((FunctionDef) best, pte, invocation), new ForFunction() {
						@Override
						public void typeDecided(@NotNull GenType aType) {
							tte.setAttached(aType);
//									vte.addPotentialType(instructionIndex, tte);
						}
					});
//							tte.setAttached(new OS_FuncType((FunctionDef) best));

				} else {
					final int y = 2;
					throw new NotImplementedException();
				}
			} else {
				final int y = 2;
				throw new NotImplementedException();
			}
		} catch (ResolveError aResolveError) {
//					aResolveError.printStackTrace();
//					int y=2;
//					throw new NotImplementedException();
			dc.reportDiagnostic(aResolveError);
			tte.setAttached(new OS_UnknownType(new StatementWrapperImpl(pce.getLeft(), null, null)));
		}
	}

	void do_assign_call_GET_ITEM(@NotNull GetItemExpression gie, final FT_FCA_Ctx fdctx) {
		final DeduceTypes2.DeduceClient4 dc                = fdctx.dc();
		final ErrSink                    errSink           = fdctx.errSink();
		final TypeTableEntry             tte               = fdctx.tte();
		final BaseEvaFunction            generatedFunction = fdctx.generatedFunction();
		final Context                    ctx               = fdctx.ctx();

		try {
			final LookupResultList     lrl  = dc.lookupExpression(gie.getLeft(), ctx);
			final @Nullable OS_Element best = lrl.chooseBest(null);
			if (best != null) {
				if (best instanceof @NotNull final VariableStatementImpl vs) { // TODO what about alias?
					String                        s      = vs.getName();
					@Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(s);
					if (vte_ia != null) {
						@NotNull VariableTableEntry vte1 = generatedFunction.getVarTableEntry(to_int(vte_ia));
						throw new NotImplementedException();
					} else {
						final IdentTableEntry idte = generatedFunction.getIdentTableEntryFor(vs.getNameToken());
						assert idte != null;
						if (idte.type == null) {
							final IdentIA identIA = new IdentIA(idte.getIndex(), generatedFunction);
							dc.resolveIdentIA_(ctx, identIA, generatedFunction, new FT_FnCallArgs.DoAssignCall.NullFoundElement(dc));
						}
						@Nullable OS_Type ty;
						if (idte.type == null) ty = null;
						else ty = idte.type.getAttached();
						idte.onType(dc.getPhase(), new OnType() {
							@Override
							public void noTypeFound() {
								throw new NotImplementedException();
							}

							@Override
							public void typeDeduced(final @NotNull OS_Type ty) {
								assert ty != null;
								@NotNull GenType rtype = null;
								try {
									rtype = dc.resolve_type(ty, ctx);
								} catch (ResolveError resolveError) {
									//								resolveError.printStackTrace();
									errSink.reportError("Cant resolve " + ty); // TODO print better diagnostic
									return;
								}
								if (rtype.resolved != null && rtype.resolved.getType() == OS_Type.Type.USER_CLASS) {
									LookupResultList     lrl2  = rtype.resolved.getClassOf().getContext().lookup("__getitem__");
									@Nullable OS_Element best2 = lrl2.chooseBest(null);
									if (best2 != null) {
										if (best2 instanceof @NotNull final FunctionDef fd) {
											@Nullable ProcTableEntry pte        = null;
											final IInvocation        invocation = dc.getInvocation((EvaFunction) generatedFunction);
											dc.forFunction(dc.newFunctionInvocation(fd, pte, invocation), new ForFunction() {
												@Override
												public void typeDecided(final @NotNull GenType aType) {
													assert fd == generatedFunction.getFD();
													//
													if (idte.type == null) {
														idte.makeType(generatedFunction, TypeTableEntry.Type.TRANSIENT, dc.gt(aType));  // TODO expression?
													} else
														idte.type.setAttached(dc.gt(aType));
												}
											});
										} else {
											throw new NotImplementedException();
										}
									} else {
										throw new NotImplementedException();
									}
								}
							}
						});
						if (ty == null) {
							@NotNull TypeTableEntry tte3 = generatedFunction.newTypeTableEntry(
									TypeTableEntry.Type.SPECIFIED, new OS_UserType(vs.typeName()), vs.getNameToken());
							idte.type = tte3;
//							ty = idte.type.getAttached();
						}
					}

					//				tte.attached = new OS_FuncType((FunctionDef) best); // TODO: what is this??
					//vte.addPotentialType(instructionIndex, tte);
				} else if (best instanceof final @Nullable FormalArgListItem fali) {
					String                            s      = fali.name();
					@Nullable InstructionArgument     vte_ia = generatedFunction.vte_lookup(s);
					if (vte_ia != null) {
						@NotNull VariableTableEntry vte2 = generatedFunction.getVarTableEntry(to_int(vte_ia));

//						final @Nullable OS_Type ty2 = vte2.type.attached;
						VTE_TypePromises.getItemFali(generatedFunction, ctx, vte2, dc.get());
//					vte2.onType(phase, new OnType() {
//						@Override public void typeDeduced(final OS_Type ty2) {
//						}
//
//						@Override
//						public void noTypeFound() {
//							throw new NotImplementedException();
//						}
//					});
/*
					if (ty2 == null) {
						@NotNull TypeTableEntry tte3 = generatedFunction.newTypeTableEntry(
								TypeTableEntry.Type.SPECIFIED, new OS_UserType(fali.typeName()), fali.getNameToken());
						vte2.type = tte3;
//						ty2 = vte2.type.attached; // TODO this is final, but why assign anyway?
					}
*/
					}
				} else {
					final int y = 2;
					throw new NotImplementedException();
				}
			} else {
				final int y = 2;
				throw new NotImplementedException();
			}
		} catch (ResolveError aResolveError) {
			aResolveError.printStackTrace();
			int y = 2;
			throw new NotImplementedException();
		}
	}

	void loop2(final @NotNull Instruction instruction,
			   final @NotNull VariableTableEntry vte,
			   final @NotNull FnCallArgs fca,
			   final @NotNull Context ctx,
			   final int instructionIndex,
			   final @NotNull ProcTableEntry pte,
			   final FT_FnCallArgs.DoAssignCall aDoAssignCall) {
		if (pte.expression_num == null) {
			if (fca.expression_to_call.getName() != InstructionName.CALLS) {
				final String           text = ((IdentExpression) pte.expression).getText();
				final LookupResultList lrl  = ctx.lookup(text);

				final @Nullable OS_Element best = lrl.chooseBest(null);
				if (best != null)
					pte.setResolvedElement(best); // TODO do we need to add a dependency for class?
				else {
					aDoAssignCall.errSink.reportError("Cant resolve " + text);
				}
			} else {
				aDoAssignCall.dc.implement_calls(aDoAssignCall.generatedFunction, ctx.getParent(), instruction.getArg(1), pte, instructionIndex);
			}
		} else {
			final int y = 2;
			aDoAssignCall.dc.resolveIdentIA_(ctx, identIA, aDoAssignCall.generatedFunction, new FoundElement(aDoAssignCall.dc.getPhase()) {

				final String x = aDoAssignCall.generatedFunction.getIdentIAPathNormal(identIA);

				@Override
				public void foundElement(OS_Element el) {
					if (pte.getResolvedElement() == null)
						pte.setResolvedElement(el);
					if (el instanceof FunctionDef) {
						final FT_FCA_FunctionDef fcafd = new FT_FCA_FunctionDef((FunctionDef) el);
						fcafd.loop2_i(aDoAssignCall, pte, vte, instructionIndex);
					} else if (el instanceof @NotNull final ClassStatement kl) {
						final FT_FCA_ClassStatement fcafd = new FT_FCA_ClassStatement((ClassStatement) el);
						loop2_i(kl);
					} else {
						aDoAssignCall.LOG.err("7890 " + el.getClass().getName());
					}
				}

				private void loop2_i(final @NotNull ClassStatement kl) {
					@NotNull OS_Type        type = kl.getOS_Type();
					@NotNull TypeTableEntry tte  = aDoAssignCall.generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, type, pte.expression, pte);
					vte.addPotentialType(instructionIndex, tte);
					vte.setConstructable(pte);

					aDoAssignCall.dc.register_and_resolve(vte, kl);
				}

				@Override
				public void noFoundElement() {
					aDoAssignCall.LOG.err("IdentIA path cannot be resolved " + x);
				}
			});
		}
	}

	public void make2(final @NotNull OS_Module module,
					  final ProcTableEntry pte,
					  final DeduceTypes2.DeduceClient4 dc,
					  final Context ctx,
					  final BaseEvaFunction generatedFunction,
					  final int instructionIndex,
					  final ElLog LOG) throws FCA_Stop {
		if (identIA != null) {
//				LOG.info("594 "+identIA.getEntry().getStatus());

			make2_1(module, pte, dc, LOG);
			make2_2(pte, dc, ctx, generatedFunction, instructionIndex, LOG);
		}
	}

	private void make2_1(final @NotNull OS_Module module, final ProcTableEntry pte, final DeduceTypes2.DeduceClient4 dc, final ElLog LOG) throws FCA_Stop {
		final @NotNull IdentTableEntry ite              = identIA.getEntry();
		final OS_Element               resolved_element = ite.getResolvedElement();

		if (resolved_element == null) return;

		final @NotNull OS_Module mod1 = resolved_element.getContext().module();

		if (mod1 != module) {
			if (resolved_element instanceof FunctionDef) {
				final OS_Element parent = resolved_element.getParent();

				final @Nullable DeduceElement target = pte.dpc.target();
				assert target != null;
				IInvocation invocation2 = target.declAnchor().getInvocation();
				if (invocation2 instanceof ClassInvocation)
					invocation2 = dc.getPhase().registerClassInvocation((ClassInvocation) invocation2);

//					final @Nullable ClassInvocation invocation = dc.registerClassInvocation((ClassStatement) parent, null);

				final @NotNull FunctionInvocation fi  = dc.newFunctionInvocation((FunctionDef) resolved_element, pte, invocation2);
				final DeferredMemberFunction      dmf = dc.deferred_member_function(parent, invocation2, (FunctionDef) resolved_element, fi);

				dmf.typeResolved().then((final GenType result) -> {
					LOG.info("2717 " + dmf.getFunctionDef() + " " + result);
					if (pte.typeDeferred().isPending())
						pte.typeDeferred().resolve(result);
					else {
						int y = 2;
					}
				});
			}
		}
	}

	private void make2_2(final ProcTableEntry pte, final DeduceTypes2.DeduceClient4 dc, final Context ctx, final BaseEvaFunction generatedFunction, final int instructionIndex, final ElLog LOG) {
		dc.resolveIdentIA_(ctx, identIA, generatedFunction, new FoundElement(dc.getPhase()) {

			final String xx = generatedFunction.getIdentIAPathNormal(identIA);

			@Override
			public void foundElement(OS_Element e) {
				assert e != null;

//					LOG.info(String.format("600 %s %s", xx ,e));
//					LOG.info("601 "+identIA.getEntry().getStatus());
				dc.found_element_for_ite(generatedFunction, identIA.getEntry(), e, ctx);

				final OS_Element resolved_element = identIA.getEntry().getResolvedElement();

				while (e instanceof AliasStatementImpl)
					e = dc._resolveAlias((AliasStatementImpl) e);

				assert e == resolved_element
						|| /*HACK*/ resolved_element instanceof AliasStatementImpl
						|| resolved_element == null
						;

//					set_resolved_element_pte(identIA, e, pte);
				pte.setStatus(BaseTableEntry.Status.KNOWN, new ConstructableElementHolder(e, identIA));
				pte.onFunctionInvocation((@NotNull FunctionInvocation functionInvocation) -> {
					functionInvocation.generateDeferred().done((@NotNull BaseEvaFunction bgf) -> {
						@NotNull DeduceTypes2.PromiseExpectation<GenType> pe = dc.promiseExpectation(bgf, "Function Result type");
						bgf.typePromise().then((@NotNull GenType result) -> {
							pe.satisfy(result);
							@NotNull TypeTableEntry tte = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, result.resolved); // TODO there has to be a better way
							tte.genType.copy(result);
							vte.addPotentialType(instructionIndex, tte);
						});
					});
				});
			}

			@Override
			public void noFoundElement() {
				// TODO create Diagnostic and quit
				LOG.info("1005 Can't find element for " + xx);
			}
		});
	}

	public void resolve_vte(final DeduceTypes2.DeduceClient4 dc, final Context ctx, final ProcTableEntry pte) {
		if (vte.getStatus() == BaseTableEntry.Status.UNCHECKED) {
			pte.typePromise().then(vte::resolveType);

			if (vte.getResolvedElement() != null) {
				try {
					OS_Element el;
					if (vte.getResolvedElement() instanceof IdentExpression)
						el = dc.lookup((IdentExpression) vte.getResolvedElement(), ctx);
					else
						el = dc.lookup(((VariableStatementImpl) vte.getResolvedElement()).getNameToken(), ctx);
					vte.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(el));
				} catch (ResolveError aResolveError) {
					dc.reportDiagnostic(aResolveError);
				}
			}
		}
	}

	record FT_FCA_Ctx(
			BaseEvaFunction generatedFunction,
			TypeTableEntry tte,
			Context ctx,
			ErrSink errSink,
			DeduceTypes2.DeduceClient4 dc
	) {
	}

	class FT_FCA_ProcedureCall {
		private final Context                 ctx;
		private final ProcedureCallExpression pce;

		public FT_FCA_ProcedureCall(final ProcedureCallExpression aPce, final Context aCtx) {

			pce = aPce;
			ctx = aCtx;
		}
	}
}
