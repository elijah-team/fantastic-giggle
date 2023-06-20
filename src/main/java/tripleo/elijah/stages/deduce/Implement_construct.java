package tripleo.elijah.stages.deduce;

import com.google.common.base.Preconditions;
import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.RegularTypeNameImpl;
import tripleo.elijah.lang.impl.VariableStatementImpl;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.nextgen.query.Operation2;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_IdentTableEntry;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_Type;
import tripleo.elijah.stages.deduce.tastic.FCA_Stop;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.*;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

class Implement_construct {

	private final InstructionArgument expression;
	private final BaseEvaFunction generatedFunction;
	private final DeduceTypes2        deduceTypes2;
	private final Instruction     instruction;
	DeduceConstructStatement dcs;

	private void _implement_construct_type(final @Nullable Constructable co,
										   final @Nullable String constructorName,
										   final @NotNull NormalTypeName aTyn1,
										   final @Nullable GenType aGenType) {
		final String          s      = aTyn1.getName();
		final ICH             ich    = new ICH(aGenType);
		final ClassStatement  best   = ich.lookupTypeName(aTyn1, s);
		final ClassInvocation clsinv = ich.getClassInvocation(constructorName, aTyn1, aGenType, best);
		if (co != null) {
			genTypeCI_and_ResolveTypeToClass(co, clsinv);
		}
		pte.setClassInvocation(clsinv);
		pte.setResolvedElement(best);
		// set FunctionInvocation with pte args
		{
			@Nullable ConstructorDef cc = null;
			if (constructorName != null) {
				Collection<ConstructorDef> cs = best.getConstructors();
				for (@NotNull ConstructorDef c : cs) {
					if (c.name().equals(constructorName)) {
						cc = c;
						break;
					}
				}
			}
			// TODO also check arguments
			{
				// TODO is cc ever null (default_constructor)
				if (cc == null) {
					//assert pte.getArgs().size() == 0;
					for (ClassItem item : best.getItems()) {
						if (item instanceof final ConstructorDef constructorDef) {
							if (constructorDef.getArgs().size() == pte.getArgs().size()) {
								// TODO we now have to find a way to check arg matching of two different types
								//  of arglists. This is complicated by the fact that constructorDef doesn't have
								//  to specify the argument types and currently, pte args is underspecified

								// TODO this is explicitly wrong, but it works for now
								cc = constructorDef;
								break;
							}
						}
					}
				}
				// TODO do we still want to do this if cc is null?
				@NotNull FunctionInvocation fi = deduceTypes2.newFunctionInvocation(cc, pte, clsinv, deduceTypes2.phase);
				pte.setFunctionInvocation(fi);
			}
		}
	}

	private final @NotNull ProcTableEntry pte;

	public Implement_construct(final DeduceTypes2 aDeduceTypes2, BaseEvaFunction aGeneratedFunction, Instruction aInstruction) {
		deduceTypes2      = aDeduceTypes2;
		generatedFunction = aGeneratedFunction;
		instruction       = aInstruction;

		// README all these asserts are redundant, I know
		assert instruction.getName() == InstructionName.CONSTRUCT;
		assert instruction.getArg(0) instanceof ProcIA;

		final int pte_num = ((ProcIA) instruction.getArg(0)).getIndex();
		pte = generatedFunction.getProcTableEntry(pte_num);

		expression = pte.expression_num;

		assert expression instanceof IntegerIA || expression instanceof IdentIA;
	}

	private void genTypeCI_and_ResolveTypeToClass(@NotNull final Constructable co, final ClassInvocation aClsinv) {
		if (co instanceof final @Nullable IdentTableEntry idte3) {
			idte3.type.genTypeCI(aClsinv);
			aClsinv.resolvePromise().then(
					idte3::resolveTypeToClass);
		} else if (co instanceof final @NotNull VariableTableEntry vte) {
			vte.type.genTypeCI(aClsinv);
			aClsinv.resolvePromise().then(
					vte::resolveTypeToClass
										 );
		}
	}

	public void action(final Context aContext) throws FCA_Stop {
		dcs = (DeduceConstructStatement) instruction.deduceElement;

		if (expression instanceof IntegerIA) {
			action_IntegerIA();
		} else if (expression instanceof IdentIA) {
			action_IdentIA(aContext);
		} else {
			throw new IllegalStateException("this.expression is of the wrong type");
		}

		deduceTypes2.activePTE(pte, dcs, pte.getClassInvocation());
	}

	public void action_IdentIA(final Context aContext) throws FCA_Stop {
		@NotNull IdentTableEntry idte       = ((IdentIA) expression).getEntry();
		DeducePath               deducePath = idte.buildDeducePath(generatedFunction);

		if (pte.dpc == null) {
			pte.dpc = new DeduceProcCall(pte);
			pte.dpc.setDeduceTypes2(deduceTypes2, aContext, generatedFunction, deduceTypes2._errSink()); // TODO setting here seems right. Don't check member
		}

		final DeduceProcCall          dpc    = pte.dpc;
		final @Nullable DeduceElement target = dpc.target();
		int                           y      = 2;

		DeclAnchor xxv = target.declAnchor();
		System.out.println(xxv);

		{
			// for class_instantiation2: class Bar {constructor x{}} class Main {main(){var bar:Bar[SysInt];construct bar.x ...}}
			// deducePath.getElement(0) == [bar]
			// deducePath.getElement(1) == [x]
			//deducePath.

			if (target != null) {
				deducePath.setTarget(target);
			}
		}

		{
			@Nullable OS_Element el3;
			@Nullable Context    ectx = generatedFunction.getFD().getContext();
			for (int i = 0; i < deducePath.size(); i++) {
				InstructionArgument ia2 = deducePath.getIA(i);

				el3 = deducePath.getElement(i);

				if (ia2 instanceof IntegerIA) {
					@NotNull VariableTableEntry vte = ((IntegerIA) ia2).getEntry();
					// TODO will fail if we try to construct a tmp var, but we never try to do that
					assert vte.vtt != VariableTableType.TEMP;
					assert el3 != null;
					assert i == 0;
					ectx = deducePath.getContext(i);
				} else if (ia2 instanceof IdentIA) {
					@NotNull IdentTableEntry idte2 = ((IdentIA) ia2).getEntry();
					final String             s     = idte2.getIdent().toString();
					LookupResultList         lrl   = ectx.lookup(s);


					if (el3 == null) {
						int yy = 2;
					}


					if (lrl == null && ectx instanceof DeducePath.MemberContext) {
						final DeduceElement3_IdentTableEntry de3_idte      = deduceTypes2._zero.getIdent(idte2, generatedFunction, deduceTypes2);
						final DeduceElement3_Type            de3_idte_type = de3_idte.type();

						final OS_Type ty = de3_idte_type.genType().typeName;

						Preconditions.checkState(ty.getType() == OS_Type.Type.USER);

						final Operation2<GenType> resolved = de3_idte_type.resolved(ectx);

						if (resolved.mode() == Mode.FAILURE) {
							deduceTypes2._errSink().reportDiagnostic(resolved.failure());
						} else {
							GenType success = resolved.success();

							idte2.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(success.resolved.getElement()));

							deduceTypes2.LOG.err("892 resolved: " + success);

							implement_construct_type(idte2, ty, s, null);

/*
							if (success == null) {
								try {
									success = resolve_type(ty, ectx);
								} catch (ResolveError aResolveError) {
									errSink.reportDiagnostic(aResolveError);
//									aResolveError.printStackTrace();
									assert false;
								}
							}
*/
							final VariableTableEntry x = (VariableTableEntry) (deducePath.getEntry(i - 1));
							x.resolveType(success);
							//success.genCIForGenType2(DeduceTypes2.this);
							return;

						}
					} else {

						@Nullable OS_Element el2 = lrl.chooseBest(null);
						if (el2 == null) {
							assert el3 instanceof VariableStatementImpl;
							@Nullable VariableStatementImpl vs = (VariableStatementImpl) el3;
							@NotNull TypeName               tn = vs.typeName();
							@NotNull OS_Type                ty = new OS_UserType(tn);

							GenType resolved = null;
							if (idte2.type == null) {
								// README Don't remember enough about the constructors to select a different one
								@NotNull TypeTableEntry tte = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, ty);
								try {
									resolved = deduceTypes2.resolve_type(ty, tn.getContext());
									deduceTypes2.LOG.err("892 resolved: " + resolved);
									tte.setAttached(resolved);
								} catch (ResolveError aResolveError) {
									deduceTypes2._errSink().reportDiagnostic(aResolveError);
								}

								idte2.type = tte;
							}
							// s is constructor name
							implement_construct_type(idte2, ty, s, null);

							if (resolved == null) {
								try {
									resolved = deduceTypes2.resolve_type(ty, tn.getContext());
								} catch (ResolveError aResolveError) {
									deduceTypes2._errSink().reportDiagnostic(aResolveError);
//									aResolveError.printStackTrace();
									assert false;
								}
							}
							final VariableTableEntry x = (VariableTableEntry) (deducePath.getEntry(i - 1));
							x.resolveType(resolved);
							resolved.genCIForGenType2(deduceTypes2);
							return;
						} else {
							if (i + 1 == deducePath.size() && deducePath.size() > 1) {
								//assert el3 == el2;

								if (el2 instanceof ConstructorDef) {
									@Nullable GenType type = deducePath.getType(i);
									if (type.nonGenericTypeName == null) {
										type.nonGenericTypeName = Objects.requireNonNull(deducePath.getType(i - 1)).nonGenericTypeName; // HACK. not guararnteed to work!
									}
									@NotNull OS_Type ty = new OS_UserType(type.nonGenericTypeName);
									implement_construct_type(idte2, ty, s, type);

									final VariableTableEntry x = (VariableTableEntry) (deducePath.getEntry(i - 1));
									if (type.ci == null && type.node == null)
										type.genCIForGenType2(deduceTypes2);
									assert x != null;
									x.resolveTypeToClass(type.node);
								} else if (el2 instanceof ClassStatement) {
									final ClassStatement classStatement = (ClassStatement) el2;

									@Nullable GenType type = deducePath.getType(i);

									// FIXME or idte2??
									if (idte.type == null) {
										final OS_UserType osType = new OS_UserType(((VariableStatementImpl) target.element()).typeName());
										idte.type = dpc._generatedFunction().newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, osType);

										type = idte.type.genType;

										deducePath.injectType(i, type);
									}

									if (type.nonGenericTypeName != null) {
										if (((NormalTypeName) type.nonGenericTypeName).getGenericPart().size() > 0) {
											// goal: create an equavalent (Regular)TypeName without the genericPart
											// method: copy the context and the name
											final RegularTypeName rtn = new RegularTypeNameImpl(type.nonGenericTypeName.getContext());
											rtn.setName(((NormalTypeName) type.nonGenericTypeName).getRealName());
											type.nonGenericTypeName = rtn;
										}
										//type.nonGenericTypeName = Objects.requireNonNull(deducePath.getType(i - 1)).nonGenericTypeName; // HACK. not guararnteed to work!
									}

									implement_construct_type(idte2, type.typeName, s, type);

									final VariableTableEntry x = (VariableTableEntry) (deducePath.getEntry(i - 1));
									if (type.ci == null && type.node == null)
										type.genCIForGenType2(deduceTypes2);
									assert x != null;
									x.resolveTypeToClass(type.node);

								} else
									throw new NotImplementedException();
							} else {
								ectx = deducePath.getContext(i);
							}
						}
//						implement_construct_type(idte/*??*/, ty, null); // TODO how bout when there is no ctor name
//						} else{
//							throw new NotImplementedException();
					}
				}
			}
		}
	}

	public void action_IntegerIA() {
		@NotNull VariableTableEntry vte      = ((IntegerIA) expression).getEntry();
		final @Nullable OS_Type     attached = vte.type.getAttached();
//			assert attached != null; // TODO will fail when empty variable expression
		if (attached != null && attached.getType() == OS_Type.Type.USER) {
			implement_construct_type(vte, attached, null, vte.type.genType);
		} else {
			final OS_Type ty2 = vte.type.genType.typeName;
			assert ty2 != null;
			implement_construct_type(vte, ty2, null, vte.type.genType);
		}
	}

	class ICH {
		private final GenType genType;

		public ICH(final GenType aGenType) {
			genType = aGenType;
		}

		@NotNull
		ClassInvocation getClassInvocation(final @Nullable String constructorName,
										   final @NotNull NormalTypeName aTyn1,
										   final @Nullable GenType aGenType,
										   final @NotNull ClassStatement aBest) {
			final ClassInvocation clsinv;
			if (aGenType != null && aGenType.ci != null) {
				assert aGenType.ci instanceof ClassInvocation;
				clsinv = (ClassInvocation) aGenType.ci;
			} else {
				ClassInvocation clsinv2 = DeduceTypes2.ClassInvocationMake.withGenericPart(aBest, constructorName, aTyn1, deduceTypes2, deduceTypes2._errSink());
				clsinv = deduceTypes2.phase.registerClassInvocation(clsinv2);
			}
			return clsinv;
		}

		ClassStatement lookupTypeName(final NormalTypeName normalTypeName, final String typeName) {
			final OS_Element best;
			if (genType != null && genType.resolved != null) {
				best = genType.resolved.getClassOf();
			} else {
				LookupResultList lrl = normalTypeName.getContext().lookup(typeName);
				best = lrl.chooseBest(null);
			}
			assert best instanceof ClassStatement;
			return (ClassStatement) best;
		}
	}

	private void implement_construct_type(final @Nullable Constructable co,
										  final @NotNull OS_Type aTy,
										  final @Nullable String constructorName,
										  final @Nullable GenType aGenType) {
		if (aTy.getType() != OS_Type.Type.USER)
			throw new IllegalStateException("must be USER type");

		TypeName tyn = aTy.getTypeName();
		if (tyn instanceof final @NotNull NormalTypeName tyn1) {
			_implement_construct_type(co, constructorName, tyn1, aGenType);
		}

		final ClassInvocation classInvocation = pte.getClassInvocation();
		if (co != null) {
			co.setConstructable(pte);
			assert classInvocation != null;
			classInvocation.resolvePromise().done(co::resolveTypeToClass);
		}

		if (classInvocation != null) {
			if (classInvocation.getConstructorName() != null) {
				final ClassStatement     classStatement    = classInvocation.getKlass();
				final GenerateFunctions  generateFunctions = deduceTypes2.getGenerateFunctions(classStatement.getContext().module());
				@Nullable ConstructorDef cc                = null;
				{
					Collection<ConstructorDef> cs = classStatement.getConstructors();
					for (@NotNull ConstructorDef c : cs) {
						if (c.name().equals(constructorName)) {
							cc = c;
							break;
						}
					}
				}
				WlGenerateCtor gen = new WlGenerateCtor(generateFunctions, pte.getFunctionInvocation(), cc.getNameNode());
				gen.run(null);
				final EvaConstructor gc = gen.getResult();
				classInvocation.resolveDeferred().then(new DoneCallback<EvaClass>() {
					@Override
					public void onDone(final EvaClass result) {
						result.addConstructor(gc.cd, gc);
						final WorkList wl = new WorkList();
						wl.addJob(deduceTypes2.new WlDeduceFunction(gen, new ArrayList()));
						deduceTypes2.wm.addJobs(wl);
					}
				});
			}
		}
	}
}
