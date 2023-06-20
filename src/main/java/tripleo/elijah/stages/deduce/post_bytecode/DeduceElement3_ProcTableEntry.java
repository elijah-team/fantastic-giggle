package tripleo.elijah.stages.deduce.post_bytecode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.i.ErrSink;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.BaseFunctionDef;
import tripleo.elijah.lang.impl.ConstructorDefImpl;
import tripleo.elijah.lang.types.OS_FuncType;
import tripleo.elijah.stages.deduce.*;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkList;
import tripleo.elijah.work.WorkManager;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DeduceElement3_ProcTableEntry implements IDeduceElement3 {
	private final DeduceTypes2 deduceTypes2;
	private final BaseEvaFunction generatedFunction;

	public void lfoe_action(@NotNull ProcTableEntry pte,
							final DeduceTypes2 aDeduceTypes2,
							final @NotNull WorkList wl,
							final Consumer<WorkList> addJobs) {
		assert pte == principal;

		final __LFOE_Q q = new __LFOE_Q(aDeduceTypes2.wm, wl, aDeduceTypes2);

		FunctionInvocation fi = pte.getFunctionInvocation();
		if (fi == null) {
			fi = __lfoe_action__getFunctionInvocation(pte, aDeduceTypes2);
			if (fi == null) return;
		}

		if (fi.getFunction() == null) {
			if (fi.pte == null) {
				return;
			} else {
//					LOG.err("592 " + fi.getClassInvocation());
				if (fi.pte.getClassInvocation() != null)
					fi.setClassInvocation(fi.pte.getClassInvocation());
//					else
//						fi.pte.setClassInvocation(fi.getClassInvocation());
			}
		}

		@Nullable ClassInvocation ci = fi.getClassInvocation();
		if (ci == null) {
			ci = fi.pte.getClassInvocation();
		}
		FunctionDef fd3 = fi.getFunction();
		if (fd3 == ConstructorDefImpl.defaultVirtualCtor) {
			if (ci == null) {
				if (/*fi.getClassInvocation() == null &&*/ fi.getNamespaceInvocation() == null) {
					// Assume default constructor
					ci = aDeduceTypes2.phase.registerClassInvocation((ClassStatement) pte.getResolvedElement());
					fi.setClassInvocation(ci);
				} else
					throw new NotImplementedException();
			}
			final ClassStatement klass = ci.getKlass();

			Collection<ConstructorDef> cis = klass.getConstructors();
/*
			for (@NotNull ConstructorDef constructorDef : cis) {
				final Iterable<FormalArgListItem> constructorDefArgs = constructorDef.getArgs();

				if (!constructorDefArgs.iterator().hasNext()) { // zero-sized arg list
					fd3 = constructorDef;
					break;
				}
			}
*/

			final Optional<ConstructorDef> ocd = cis.stream()
					.filter(acd -> acd.getArgs().iterator().hasNext())
					.findFirst();
			if (ocd.isPresent()) {
				fd3 = ocd.get();
			}
		}

		final OS_Element parent;
		if (fd3 != null) {
			parent = fd3.getParent();
			if (parent instanceof ClassStatement) {
				if (ci != pte.getClassInvocation()) {
					ci = new ClassInvocation((ClassStatement) parent, null);
					{
						final ClassInvocation classInvocation = pte.getClassInvocation();
						if (classInvocation != null) {
							Map<TypeName, OS_Type> gp = classInvocation.genericPart().getMap();
							if (gp != null) {
								int i = 0;
								for (Map.@NotNull Entry<TypeName, OS_Type> entry : gp.entrySet()) {
									ci.set(i, entry.getKey(), entry.getValue());
									i++;
								}
							}
						}
					}
				}
				__lfoe_action__proceed(fi, ci, (ClassStatement) parent, addJobs, q, aDeduceTypes2.phase);
			} else if (parent instanceof NamespaceStatement) {
				__lfoe_action__proceed(fi, (NamespaceStatement) parent, wl, aDeduceTypes2.phase, addJobs, q);
			}
		} else {
			parent = ci.getKlass();
			{
				final ClassInvocation classInvocation = pte.getClassInvocation();
				if (classInvocation != null && classInvocation.genericPart().hasGenericPart()) {
					Map<TypeName, OS_Type> gp = classInvocation.genericPart().getMap();
					int                    i  = 0;
					for (Map.@NotNull Entry<TypeName, OS_Type> entry : gp.entrySet()) {
						ci.set(i, entry.getKey(), entry.getValue());
						i++;
					}
				}
			}
			__lfoe_action__proceed(fi, ci, (ClassStatement) parent, addJobs, q, aDeduceTypes2.phase);
		}
	}
	private final ProcTableEntry principal;
	private Instruction instruction;

	public DeduceElement3_ProcTableEntry(final ProcTableEntry aProcTableEntry, final DeduceTypes2 aDeduceTypes2, final BaseEvaFunction aGeneratedFunction) {
		principal         = aProcTableEntry;
		deduceTypes2      = aDeduceTypes2;
		generatedFunction = aGeneratedFunction;
	}

	@Nullable
	private static FunctionInvocation __lfoe_action__getFunctionInvocation(final @NotNull ProcTableEntry pte, final DeduceTypes2 aDeduceTypes2) {
		FunctionInvocation fi;
		if (pte.expression != null && pte.expression_num != null) {
			if (pte.expression instanceof final ProcedureCallExpression exp) {
				if (exp.getLeft() instanceof final IdentExpression expLeft) {
					String                     left = expLeft.getText();
					final LookupResultList     lrl  = expLeft.getContext().lookup(left);
					@Nullable final OS_Element e    = lrl.chooseBest(null);
					if (e != null) {
						if (e instanceof ClassStatement) {
							ClassStatement classStatement = (ClassStatement) e;

							final ClassInvocation ci = aDeduceTypes2.phase.registerClassInvocation(classStatement);
							pte.setClassInvocation(ci);
						} else if (e instanceof final FunctionDef functionDef) {


							ClassStatement classStatement = (ClassStatement) e.getParent();

							final ClassInvocation ci = aDeduceTypes2.phase.registerClassInvocation(classStatement);
							pte.setClassInvocation(ci);
						} else
							throw new NotImplementedException();
					}
				}
			}
		}

		ClassInvocation invocation = pte.getClassInvocation();


		if (invocation == null && pte.getFunctionInvocation() != null/*never true if we are in this function (check only use guard)!*/) {
			invocation = pte.getFunctionInvocation().getClassInvocation();
		}
		if (invocation == null) return null;


		final DeduceElement3_ProcTableEntry de3_pte = (DeduceElement3_ProcTableEntry) pte.getDeduceElement3();
		//de3.set


		@NotNull FunctionInvocation fi2 = aDeduceTypes2._phase().newFunctionInvocation(ConstructorDefImpl.defaultVirtualCtor, pte, invocation);

		// FIXME use `q'
		final WlGenerateDefaultCtor wldc = new WlGenerateDefaultCtor(aDeduceTypes2.getGenerateFunctions(invocation.getKlass().getContext().module()), fi2);
		wldc.run(null);
		BaseEvaFunction ef = wldc.getResult();


		DeduceElement3_ProcTableEntry zp = aDeduceTypes2.zeroGet(pte, ef);


		fi = aDeduceTypes2.newFunctionInvocation(ef.getFD(), pte, invocation, aDeduceTypes2.phase);

		return fi;
	}

	void __lfoe_action__proceed(@NotNull FunctionInvocation fi,
								@NotNull NamespaceStatement aParent,
								@NotNull WorkList wl,
								final @NotNull DeducePhase phase,
								final @NotNull Consumer<WorkList> addJobs,
								final _LFOE_Q q) {
		//ci = phase.registerClassInvocation(ci);

		final @NotNull OS_Module module1 = aParent.getContext().module();

		final NamespaceInvocation nsi = phase.registerNamespaceInvocation(aParent);

		final ICodeRegistrar cr = phase.codeRegistrar;

		q.enqueue_namespace(() -> phase.generatePhase.getGenerateFunctions(module1), nsi, phase.generatedClasses, cr);
		q.enqueue_function(() -> phase.generatePhase.getGenerateFunctions(module1), fi, cr);

		//addJobs.accept(wl);
	}

	private void action_002_1(@NotNull final ProcTableEntry pte,
							  @NotNull final IdentTableEntry ite,
							  final boolean setClassInvocation,
							  final DeducePhase phase, final DeduceTypes2.DeduceClient3 dc) {
		final OS_Element resolvedElement = ite.getResolvedElement();

		assert resolvedElement != null;

		ClassInvocation ci = null;

		if (pte.getFunctionInvocation() == null) {
			@NotNull final FunctionInvocation fi;

			if (resolvedElement instanceof ClassStatement) {
				// assuming no constructor name or generic parameters based on function syntax
				ci = new ClassInvocation((ClassStatement) resolvedElement, null);
				ci = phase.registerClassInvocation(ci);
				fi = phase.newFunctionInvocation(null, pte, ci);
			} else if (resolvedElement instanceof final FunctionDef functionDef) {
				final IInvocation invocation = dc.getInvocation((EvaFunction) generatedFunction);
				fi = phase.newFunctionInvocation((BaseFunctionDef) functionDef, pte, invocation);
				if (functionDef.getParent() instanceof ClassStatement) {
					final ClassStatement classStatement = (ClassStatement) fi.getFunction().getParent();
					ci = new ClassInvocation(classStatement, null); // TODO generics
					ci = phase.registerClassInvocation(ci);
				}
			} else {
				throw new IllegalStateException();
			}

			if (setClassInvocation) {
				if (ci != null) {
					pte.setClassInvocation(ci);
				} else
					tripleo.elijah.util.Stupidity.println_err2("542 Null ClassInvocation");
			}

			pte.setFunctionInvocation(fi);
		}

//        el   = resolvedElement;
//        ectx = el.getContext();
	}

	@Override
	public DeduceTypes2 deduceTypes2() {
		return deduceTypes2;
	}

	public void _action_002_no_resolved_element(final InstructionArgument _backlink,
												final ProcTableEntry backlink,
												final DeduceTypes2.DeduceClient3 dc,
												final IdentTableEntry ite,
												final ErrSink errSink,
												final DeducePhase phase) {
		final OS_Element resolvedElement = backlink.getResolvedElement();

		if (resolvedElement == null) return; //throw new AssertionError(); // TODO feb 20

		try {
			final LookupResultList     lrl2 = dc.lookupExpression(ite.getIdent(), resolvedElement.getContext());
			@Nullable final OS_Element best = lrl2.chooseBest(null);
			assert best != null;
			ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
		} catch (final ResolveError aResolveError) {
			errSink.reportDiagnostic(aResolveError);
			assert false;
		}

		action_002_1(principal, ite, false, phase, dc);
	}

	@Override
	public DED elementDiscriminator() {
		return new DED.DED_PTE(principal);
	}

	@Override
	public BaseEvaFunction generatedFunction() {
		return generatedFunction;
	}

	public void doFunctionInvocation() {
		final FunctionInvocation fi = principal.getFunctionInvocation();

		if (fi == null) {
			if (principal.expression instanceof final ProcedureCallExpression exp) {
				final IExpression left = exp.getLeft();

				if (left instanceof final DotExpression dotleft) {

					if (dotleft.getLeft() instanceof final IdentExpression rl && dotleft.getRight() instanceof final IdentExpression rr) {

						if (rl.getText().equals("a1")) {
							final EvaClass[] gc = new EvaClass[1];

							final InstructionArgument vrl = generatedFunction.vte_lookup(rl.getText());

							assert vrl != null;

							final VariableTableEntry vte = ((IntegerIA) vrl).getEntry();

							vte.typePromise().then(left_type -> {
								final ClassStatement cs = left_type.resolved.getClassOf(); // TODO we want a DeduceClass here. EvaClass may suffice

								final ClassInvocation ci = deduceTypes2._phase().registerClassInvocation(cs);
								ci.resolvePromise().then(gc2 -> {
									gc[0] = gc2;
								});

								final LookupResultList     lrl  = cs.getContext().lookup(rr.getText());
								@Nullable final OS_Element best = lrl.chooseBest(null);

								if (best != null) {
									final FunctionDef fun = (FunctionDef) best;

									final FunctionInvocation fi2 = deduceTypes2._phase().newFunctionInvocation(fun, null, ci); // TODO pte??

									principal.setFunctionInvocation(fi2); // TODO pte above

									final WlGenerateFunction j = fi2.generateFunction(deduceTypes2, best);
									j.run(null);

									final @NotNull IdentTableEntry ite      = ((IdentIA) principal.expression_num).getEntry();
									final OS_Type                  attached = ite.type.getAttached();

									fi2.generatePromise().then(gf -> {
										final int y4 = 4;
									});

									if (attached instanceof final OS_FuncType funcType) {

										final EvaClass x = gc[0];

										fi2.generatePromise().then(gf -> {
											final int y4 = 4;
										});

										final int y = 2;
									}
									final int yy = 2;
								}
							});
							final int y = 2;
						}
					}
				}
			}
		}
	}

	@Override
	public GenType genType() {
		throw new UnsupportedOperationException("no type for PTE");
	} // TODO check correctness

	@Override
	public OS_Element getPrincipal() {
		//return principal.getDeduceElement3(deduceTypes2, generatedFunction).getPrincipal(); // README infinite loop

		return principal.getResolvedElement();//getDeduceElement3(deduceTypes2, generatedFunction).getPrincipal();
	}

	@Override
	public DeduceElement3_Kind kind() {
		return DeduceElement3_Kind.GEN_FN__PTE;
	}

	@Override
	public void resolve(final Context aContext, final DeduceTypes2 dt2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void resolve(final IdentIA aIdentIA, final Context aContext, final FoundElement aFoundElement) {
		throw new UnsupportedOperationException();
	}

	public BaseEvaFunction getGeneratedFunction() {
		return generatedFunction;
	}

	public Instruction getInstruction() {
		return instruction;
	}

	public void setInstruction(final Instruction aInstruction) {
		instruction = aInstruction;
	}

	void __lfoe_action__proceed(@NotNull FunctionInvocation fi,
								ClassInvocation ci,
								ClassStatement aParent,
								final Consumer<WorkList> addJobs,
								final _LFOE_Q q,
								final @NotNull DeducePhase phase) {
		ci = phase.registerClassInvocation(ci);

		@NotNull ClassStatement kl = ci.getKlass(); // TODO Don't you see aParent??
		assert kl != null;

		final FunctionDef fd2   = fi.getFunction();
		int               state = 0;

		if (fd2 == ConstructorDefImpl.defaultVirtualCtor) {
			if (fi.pte.getArgs().size() == 0)
				state = 1;
			else
				state = 2;
		} else if (fd2 instanceof ConstructorDef) {
			if (fi.getClassInvocation().getConstructorName() != null)
				state = 3;
			else
				state = 2;
		} else {
			if (fi.getFunction() == null && fi.getClassInvocation() != null)
				state = 3;
			else
				state = 4;
		}

		final GenerateFunctions generateFunctions = null;

		switch (state) {
		case 1:
			assert fi.pte.getArgs().size() == 0;
			// default ctor
			q.enqueue_default_ctor(generateFunctions, fi);
			break;
		case 2:
			q.enqueue_ctor(generateFunctions, fi, fd2.getNameNode());
			break;
		case 3:
			// README this is a special case to generate constructor
			// TODO should it be GenerateDefaultCtor? (check args size and ctor-name)
			final String constructorName = fi.getClassInvocation().getConstructorName();
			final @NotNull IdentExpression constructorName1 = constructorName != null ? IdentExpression.forString(constructorName) : null;
			q.enqueue_ctor(generateFunctions, fi, constructorName1);
			break;
		case 4:
			q.enqueue_function(generateFunctions, fi, phase.codeRegistrar);
			break;
		default:
			throw new NotImplementedException();
		}

		//addJobs.accept(wl);
	}

	public ProcTableEntry getTablePrincipal() {
		return principal;
	}

	interface _LFOE_Q {
		void enqueue_ctor(final GenerateFunctions aGenerateFunctions, final @NotNull FunctionInvocation aFi, final IdentExpression aConstructorName);

		void enqueue_default_ctor(final GenerateFunctions aGenerateFunctions, final @NotNull FunctionInvocation aFi);

		void enqueue_function(final GenerateFunctions aGenerateFunctions, final @NotNull FunctionInvocation aFi, final ICodeRegistrar cr);

		void enqueue_function(final Supplier<GenerateFunctions> som, final @NotNull FunctionInvocation aFi, final ICodeRegistrar aCr);

		void enqueue_namespace(Supplier<GenerateFunctions> som, NamespaceInvocation aNsi, DeducePhase.GeneratedClasses aGeneratedClasses, final ICodeRegistrar aCr);
	}

	static class __LFOE_Q implements _LFOE_Q {

		final         GenerateFunctions generateFunctions;
		private final WorkList          wl;

		__LFOE_Q(WorkManager awm, WorkList awl, final @NotNull DeduceTypes2 aDeduceTypes2) {
			generateFunctions = aDeduceTypes2.phase.generatePhase.getGenerateFunctions(aDeduceTypes2.module);
			wl                = awl;
		}

		@Override
		public void enqueue_ctor(final GenerateFunctions generateFunctions1, final @NotNull FunctionInvocation fi, final IdentExpression aConstructorName) {
			wl.addJob(new WlGenerateCtor(generateFunctions, fi, aConstructorName));
		}

		@Override
		public void enqueue_default_ctor(final GenerateFunctions generateFunctions1, final @NotNull FunctionInvocation fi) {
			wl.addJob(new WlGenerateDefaultCtor(generateFunctions, fi));
		}

		@Override
		public void enqueue_function(final GenerateFunctions generateFunctions1, final @NotNull FunctionInvocation fi, final ICodeRegistrar cr) {
			wl.addJob(new WlGenerateFunction(generateFunctions, fi, cr));
		}

		@Override
		public void enqueue_function(final @NotNull Supplier<GenerateFunctions> som, final @NotNull FunctionInvocation aFi, final ICodeRegistrar cr) {
			wl.addJob(new WlGenerateFunction(generateFunctions/*som.get()*/, aFi, cr));
		}

		@Override
		public void enqueue_namespace(final @NotNull Supplier<GenerateFunctions> som, final NamespaceInvocation aNsi, final DeducePhase.GeneratedClasses aGeneratedClasses, final ICodeRegistrar aCr) {
			wl.addJob(new WlGenerateNamespace(generateFunctions/*som.get()*/, aNsi, aGeneratedClasses, aCr));
		}
	}
}
