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

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.Subject;
import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.i.ErrSink;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.AliasStatementImpl;
import tripleo.elijah.lang.impl.BaseFunctionDef;
import tripleo.elijah.lang.impl.ConstructorDefImpl;
import tripleo.elijah.lang.impl.VariableStatementImpl;
import tripleo.elijah.lang.types.OS_BuiltinType;
import tripleo.elijah.lang.types.OS_FuncType;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.lang2.BuiltInTypes;
import tripleo.elijah.lang2.ElElementVisitor;
import tripleo.elijah.lang2.SpecialFunctions;
import tripleo.elijah.lang2.SpecialVariables;
import tripleo.elijah.nextgen.ClassDefinition;
import tripleo.elijah.nextgen.reactive.Reactivable;
import tripleo.elijah.nextgen.reactive.Reactive;
import tripleo.elijah.nextgen.reactive.ReactiveDimension;
import tripleo.elijah.stages.deduce.declarations.DeferredMember;
import tripleo.elijah.stages.deduce.declarations.DeferredMemberFunction;
import tripleo.elijah.stages.deduce.post_bytecode.*;
import tripleo.elijah.stages.deduce.tastic.FCA_Stop;
import tripleo.elijah.stages.deduce.tastic.FT_FnCallArgs;
import tripleo.elijah.stages.deduce.tastic.ITastic;
import tripleo.elijah.stages.gen_c.GenerateC;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.Old_GenerateResultItem;
import tripleo.elijah.stages.instructions.*;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.Holder;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkJob;
import tripleo.elijah.work.WorkList;
import tripleo.elijah.work.WorkManager;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static tripleo.elijah.util.Helpers.List_of;

/**
 * Created 9/15/20 12:51 PM
 */
public class DeduceTypes2 {
	private static final  String           PHASE           = "DeduceTypes2";
	public final @NotNull ElLog            LOG;
	public final @NotNull OS_Module        module;
	public final @NotNull DeducePhase      phase;
	public final @NotNull WorkManager      wm              = new WorkManager();
	final Zero _zero = new Zero();
	final List<FunctionInvocation> functionInvocations = new ArrayList<>(); // TODO never used!
	private final DeduceCentral _central = new DeduceCentral(this);
	private final List<IDeduceResolvable> _pendingResolves = new ArrayList<>();
	private final Map<OS_Element, DG_Item> dgs = new HashMap<>();
	private final ErrSink errSink;
	private final Map<Object, ITastic> tasticMap = new HashMap<>();
	@NotNull PromiseExpectations expectations = new PromiseExpectations();
	@NotNull List<Runnable> onRunnables = new ArrayList<Runnable>();

	public ErrSink _errSink() {
		return errSink;
	}

	public ElLog _LOG() {
		return LOG;
	}

	public DeducePhase _phase() {
		return phase;
	}

	public void addResolvePending(final IDeduceResolvable aResolvable, final IDeduceElement_old aDeduceElement, final Holder<OS_Element> aHolder) {
		int y = 2;

		assert !hasResolvePending(aResolvable);

		_pendingResolves.add(aResolvable);
	}

	public boolean hasResolvePending(final IDeduceResolvable aResolvable) {
		final boolean b = _pendingResolves.contains(aResolvable);
		return b;
	}

	public void assign_type_to_idte(@NotNull IdentTableEntry ite,
									@NotNull BaseEvaFunction generatedFunction,
									Context aFunctionContext,
									@NotNull Context aContext) {

		final DeduceElement3_IdentTableEntry x = ((DeduceElement3_IdentTableEntry) ite.getDeduceElement3(this, generatedFunction));
		x.assign_type_to_idte(aFunctionContext, aContext);
	}

	public void deduce_generated_constructor(final @NotNull EvaConstructor generatedFunction) {
		final @NotNull ConstructorDef fd = (ConstructorDef) generatedFunction.getFD();
		deduce_generated_function_base(generatedFunction, fd);
	}

	public void deduceClasses(final List<EvaNode> lgc) {
		for (EvaNode evaNode : lgc) {
			if (!(evaNode instanceof final EvaClass evaClass)) continue;

			deduceOneClass(evaClass);
		}
	}

	public void deduceOneClass(final @NotNull EvaClass aEvaClass) {
		for (EvaContainer.VarTableEntry entry : aEvaClass.varTable) {
			final OS_Type vt      = entry.varType;
			GenType       genType = GenType.makeFromOSType(vt, aEvaClass.ci.genericPart(), this, phase, LOG, errSink);
			if (genType != null) {
				if (genType.node != null) {
					entry.resolve(genType.node);
				} else {
					int y = 2; // 05/22
				}
			}

			NotImplementedException.raise();

		}
	}

	public void deduceFunctions(final @NotNull Iterable<EvaNode> lgf) {
		for (final EvaNode evaNode : lgf) {
			if (evaNode instanceof @NotNull final EvaFunction generatedFunction) {
				deduceOneFunction(generatedFunction, phase);
			}
		}
		@NotNull List<EvaNode> generatedClasses = (phase.generatedClasses.copy());
		// TODO consider using reactive here
		int size;
		do {
			size             = df_helper(generatedClasses, new dfhi_functions());
			generatedClasses = phase.generatedClasses.copy();
		} while (size > 0);
		do {
			size             = df_helper(generatedClasses, new dfhi_constructors());
			generatedClasses = phase.generatedClasses.copy();
		} while (size > 0);
	}

	public boolean deduceOneFunction(@NotNull EvaFunction aGeneratedFunction, @NotNull DeducePhase aDeducePhase) {
		if (aGeneratedFunction.deducedAlready) return false;
		deduce_generated_function(aGeneratedFunction);
		aGeneratedFunction.deducedAlready = true;
		for (@NotNull IdentTableEntry identTableEntry : aGeneratedFunction.idte_list) {
			if (identTableEntry.getResolvedElement() instanceof final @NotNull VariableStatementImpl vs) {
				OS_Element el  = vs.getParent().getParent();
				OS_Element el2 = aGeneratedFunction.getFD().getParent();
				if (el != el2) {
					if (el instanceof ClassStatement || el instanceof NamespaceStatement)
						// NOTE there is no concept of gf here
						aDeducePhase.registerResolvedVariable(identTableEntry, el, vs.getName());
				}
			}
		}
		{
			final @NotNull EvaFunction gf = aGeneratedFunction;

			@Nullable InstructionArgument result_index = gf.vte_lookup("Result");
			if (result_index == null) {
				// if there is no Result, there should be Value
				result_index = gf.vte_lookup("Value");
				// but Value might be passed in. If it is, discard value
				if (result_index != null) {
					@NotNull VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
					if (vte.vtt != VariableTableType.RESULT) {
						result_index = null;
					}
				}
			}
			if (result_index != null) {
				@NotNull VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
				if (vte.resolvedType() == null) {
					GenType b = vte.genType;
					OS_Type a = vte.type.getAttached();
					if (a != null) {
						// see resolve_function_return_type
						switch (a.getType()) {
						case USER_CLASS:
							dof_uc(vte, a);
							break;
						case USER:
							vte.genType.typeName = a;
							try {
								@NotNull GenType rt = resolve_type(a, a.getTypeName().getContext());
								if (rt.resolved != null && rt.resolved.getType() == OS_Type.Type.USER_CLASS) {
									if (rt.resolved.getClassOf().getGenericPart().size() > 0)
										vte.genType.nonGenericTypeName = a.getTypeName(); // TODO might be wrong
									dof_uc(vte, rt.resolved);
								}
							} catch (ResolveError aResolveError) {
								errSink.reportDiagnostic(aResolveError);
							}
							break;
						default:
							int y3 = 2;


							vte.typePromise().then(gt -> {
								int y4 = 2;

								if (vte.getStatus() == BaseTableEntry.Status.UNCHECKED) {
									// NOTE curious...
									int y5 = 25;
								} else throw new Error();

							});


							if (vte.vtt == VariableTableType.RESULT) {
								int y6 = 6;

//									vte.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(vte.getResolvedElement()));
							}


							break;
						}
					} /*else
							throw new NotImplementedException();*/
				}
			}
		}
		aDeducePhase.addFunction(aGeneratedFunction, (FunctionDef) aGeneratedFunction.getFD());
		return true;
	}

	/**
	 * Deduce functions or constructors contained in classes list
	 *
	 * @param aGeneratedClasses assumed to be a list of {@link EvaContainerNC}
	 * @param dfhi              specifies what to select for:<br>
	 *                          {@link dfhi_functions} will select all functions from {@code functionMap}, and <br>
	 *                          {@link dfhi_constructors} will select all constructors from {@code constructors}.
	 * @param <T>               generic parameter taken from {@code dfhi}
	 * @return the number of deduced functions or constructors, or 0
	 */
	<T> int df_helper(@NotNull List<EvaNode> aGeneratedClasses, @NotNull df_helper_i<T> dfhi) {
		int size = 0;
		for (EvaNode evaNode : aGeneratedClasses) {
			@NotNull EvaContainerNC      generatedContainerNC = (EvaContainerNC) evaNode;
			final @Nullable df_helper<T> dfh                  = dfhi.get(generatedContainerNC);
			if (dfh == null) continue;
			@NotNull Collection<T> lgf2 = dfh.collection();
			for (final T generatedConstructor : lgf2) {
				if (dfh.deduce(generatedConstructor))
					size++;
			}
		}
		return size;
	}

	public void deduce_generated_function(final @NotNull EvaFunction generatedFunction) {
		final @NotNull FunctionDef fd = (FunctionDef) generatedFunction.getFD();
		deduce_generated_function_base(generatedFunction, fd);
	}

	private void dof_uc(@NotNull VariableTableEntry aVte, @NotNull OS_Type aA) {
		// we really want a ci from somewhere
		assert aA.getClassOf().getGenericPart().size() == 0;
		@Nullable ClassInvocation ci = new ClassInvocation(aA.getClassOf(), null);
		ci = phase.registerClassInvocation(ci);

		aVte.genType.resolved = aA; // README assuming OS_Type cannot represent namespaces
		aVte.genType.ci       = ci;

		ci.resolvePromise().done(new DoneCallback<EvaClass>() {
			@Override
			public void onDone(EvaClass result) {
				aVte.resolveTypeToClass(result);
			}
		});
	}

	@NotNull
	public GenType resolve_type(final @NotNull OS_Type type, final Context ctx) throws ResolveError {
		//return ResolveType.resolve_type2(module, type, ctx, LOG, this);
		return ResolveType.resolve_type(module, type, ctx, LOG, this);
	}

	public boolean deduceOneConstructor(@NotNull EvaConstructor aEvaConstructor, @NotNull DeducePhase aDeducePhase) {
		if (aEvaConstructor.deducedAlready) return false;
		deduce_generated_function_base(aEvaConstructor, aEvaConstructor.getFD());
		aEvaConstructor.deducedAlready = true;
		for (@NotNull IdentTableEntry identTableEntry : aEvaConstructor.idte_list) {
			if (identTableEntry.getResolvedElement() instanceof final @NotNull VariableStatementImpl vs) {
				OS_Element el  = vs.getParent().getParent();
				OS_Element el2 = aEvaConstructor.getFD().getParent();
				if (el != el2) {
					if (el instanceof ClassStatement || el instanceof NamespaceStatement)
						// NOTE there is no concept of gf here
						aDeducePhase.registerResolvedVariable(identTableEntry, el, vs.getName());
				}
			}
		}
		{
			final @NotNull EvaConstructor gf = aEvaConstructor;

			@Nullable InstructionArgument result_index = gf.vte_lookup("Result");
			if (result_index == null) {
				// if there is no Result, there should be Value
				result_index = gf.vte_lookup("Value");
				// but Value might be passed in. If it is, discard value
				if (result_index != null) {
					@NotNull VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
					if (vte.vtt != VariableTableType.RESULT) {
						result_index = null;
					}
				}
			}
			if (result_index != null) {
				@NotNull VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
				if (vte.resolvedType() == null) {
					GenType b = vte.genType;
					OS_Type a = vte.type.getAttached();
					if (a != null) {
						// see resolve_function_return_type
						switch (a.getType()) {
						case USER_CLASS:
							dof_uc(vte, a);
							break;
						case USER:
							b.typeName = a;
							try {
								@NotNull GenType rt = resolve_type(a, a.getTypeName().getContext());
								if (rt.resolved != null && rt.resolved.getType() == OS_Type.Type.USER_CLASS) {
									if (rt.resolved.getClassOf().getGenericPart().size() > 0)
										b.nonGenericTypeName = a.getTypeName(); // TODO might be wrong
									dof_uc(vte, rt.resolved);
								}
							} catch (ResolveError aResolveError) {
								errSink.reportDiagnostic(aResolveError);
							}
							break;
						default:
							// TODO do nothing for now
							int y3 = 2;
							break;
						}
					} /*else
							throw new NotImplementedException();*/
				}
			}
		}
//		aDeducePhase.addFunction(aGeneratedConstructor, (FunctionDef) aGeneratedConstructor.getFD()); // TODO do we need this?
		return true;
	}

	private @NotNull DeferredMember deferred_member(OS_Element aParent, IInvocation aInvocation, VariableStatementImpl aVariableStatement, @NotNull IdentTableEntry ite) {
		@NotNull DeferredMember dm = deferred_member(aParent, aInvocation, aVariableStatement);
		dm.externalRef().then(new DoneCallback<EvaNode>() {
			@Override
			public void onDone(EvaNode result) {
				ite.externalRef = result;
			}
		});
		return dm;
	}

	private @Nullable DeferredMember deferred_member(OS_Element aParent, @Nullable IInvocation aInvocation, VariableStatementImpl aVariableStatement) {
		if (aInvocation == null) {
			if (aParent instanceof NamespaceStatement)
				aInvocation = phase.registerNamespaceInvocation((NamespaceStatement) aParent);
		}
		@Nullable DeferredMember dm = new DeferredMember(aParent, aInvocation, aVariableStatement);
		phase.addDeferredMember(dm);
		return dm;
	}

	@NotNull DeferredMemberFunction deferred_member_function(OS_Element aParent, @Nullable IInvocation aInvocation, FunctionDef aFunctionDef, final FunctionInvocation aFunctionInvocation) {
		if (aInvocation == null) {
			if (aParent instanceof NamespaceStatement)
				aInvocation = phase.registerNamespaceInvocation((NamespaceStatement) aParent);
			else if (aParent instanceof OS_SpecialVariable) {
				aInvocation = ((OS_SpecialVariable) aParent).getInvocation(this);
			}
		}
		DeferredMemberFunction dm = new DeferredMemberFunction(aParent, aInvocation, aFunctionDef, this, aFunctionInvocation);
		phase.addDeferredMember(dm);
		return dm;
	}

	public DG_AliasStatement DG_AliasStatement(final AliasStatementImpl aE, final DeduceTypes2 aDt2) {
		if (dgs.containsKey(aE)) {
			return (DG_AliasStatement) dgs.get(aE);
		}

		final DG_AliasStatement R = new DG_AliasStatement(aE, aDt2);
		dgs.put(aE, R);
		return R;
	}

	public DG_ClassStatement DG_ClassStatement(final ClassStatement aClassStatement) {
		if (dgs.containsKey(aClassStatement)) {
			return (DG_ClassStatement) dgs.get(aClassStatement);
		}

		final DG_ClassStatement R = new DG_ClassStatement(aClassStatement);
		dgs.put(aClassStatement, R);
		return R;
	}

	public DG_FunctionDef DG_FunctionDef(final FunctionDef aFunctionDef) {
		if (dgs.containsKey(aFunctionDef)) {
			return (DG_FunctionDef) dgs.get(aFunctionDef);
		}

		final DG_FunctionDef R = new DG_FunctionDef(aFunctionDef);
		dgs.put(aFunctionDef, R);
		return R;
	}

	private void do_assign_constant(final @NotNull BaseEvaFunction generatedFunction, final @NotNull Instruction instruction, final @NotNull IdentTableEntry idte, final @NotNull ConstTableIA i2) {
		if (idte.type != null && idte.type.getAttached() != null) {
			// TODO check types
		}
		final @NotNull ConstantTableEntry cte = generatedFunction.getConstTableEntry(i2.getIndex());
		if (cte.type.getAttached() == null) {
			LOG.err("*** ERROR: Null type in CTE " + cte);
		}
		// idte.type may be null, but we still addPotentialType here
		idte.addPotentialType(instruction.getIndex(), cte.type);
	}

	private void do_assign_constant(final @NotNull BaseEvaFunction generatedFunction, final @NotNull Instruction instruction, final @NotNull VariableTableEntry vte, final @NotNull ConstTableIA i2) {
		if (vte.type.getAttached() != null) {
			// TODO check types
		}
		final @NotNull ConstantTableEntry cte = generatedFunction.getConstTableEntry(i2.getIndex());
		if (cte.type.getAttached() == null) {
			LOG.info("Null type in CTE " + cte);
		}
//		vte.type = cte.type;
		vte.addPotentialType(instruction.getIndex(), cte.type);
	}

	public void fix_tables(final @NotNull BaseEvaFunction evaFunction) {
		for (VariableTableEntry variableTableEntry : evaFunction.vte_list) {
			variableTableEntry._fix_table(this, evaFunction);
		}
		for (IdentTableEntry identTableEntry : evaFunction.idte_list) {
			identTableEntry._fix_table(this, evaFunction);
		}
		for (TypeTableEntry typeTableEntry : evaFunction.tte_list) {
			typeTableEntry._fix_table(this, evaFunction);
		}
		for (ProcTableEntry procTableEntry : evaFunction.prte_list) {
			procTableEntry._fix_table(this, evaFunction);
		}
	}

	public void forFunction(@NotNull FunctionInvocation gf, @NotNull ForFunction forFunction) {
		phase.forFunction(this, gf, forFunction);
	}

	public IInvocation getInvocation(@NotNull EvaFunction generatedFunction) {
		final ClassInvocation     classInvocation = generatedFunction.fi.getClassInvocation();
		final NamespaceInvocation ni;
		if (classInvocation == null) {
			ni = generatedFunction.fi.getNamespaceInvocation();
			return ni;
		} else
			return classInvocation;
	}

	@NotNull
	public ArrayList<TypeTableEntry> getPotentialTypesVte(@NotNull EvaFunction generatedFunction, @NotNull InstructionArgument vte_index) {
		return getPotentialTypesVte(generatedFunction.getVarTableEntry(to_int(vte_index)));
	}

	@NotNull ArrayList<TypeTableEntry> getPotentialTypesVte(@NotNull VariableTableEntry vte) {
		return new ArrayList<TypeTableEntry>(vte.potentialTypes());
	}

	public static int to_int(@NotNull final InstructionArgument arg) {
		if (arg instanceof IntegerIA)
			return ((IntegerIA) arg).getIndex();
		if (arg instanceof ProcIA)
			return ((ProcIA) arg).getIndex();
		if (arg instanceof IdentIA)
			return ((IdentIA) arg).getIndex();
		throw new NotImplementedException();
	}

	@NotNull
	public String getPTEString(final ProcTableEntry aProcTableEntry) {
		String pte_string;
		if (aProcTableEntry == null)
			pte_string = "[]";
		else {
			pte_string = aProcTableEntry.getLoggingString(this);
		}
		return pte_string;
	}

	public DeduceTypes2(@NotNull OS_Module module, @NotNull DeducePhase phase) {
		this(module, phase, ElLog.Verbosity.VERBOSE);
	}

	public DeduceTypes2(@NotNull OS_Module aModule, @NotNull DeducePhase aDeducePhase, ElLog.Verbosity aVerbosity) {
		this.module  = aModule;
		this.phase   = aDeducePhase;
		this.errSink = aModule.getCompilation().getErrSink();
		this.LOG     = new ElLog(aModule.getFileName(), aVerbosity, PHASE);
		//
		aDeducePhase.addLog(LOG);
		//
		IStateRunnable.ST.register(phase);
		DeduceElement3_VariableTableEntry.ST.register(phase);
	}

	public OS_Type gt(@NotNull GenType aType) {
		return aType.resolved != null ? aType.resolved : aType.typeName;
	}

	private void implement_calls(final @NotNull BaseEvaFunction gf, final @NotNull Context context, final InstructionArgument i2, final @NotNull ProcTableEntry fn1, final int pc) {
		if (gf.deferred_calls.contains(pc)) {
			LOG.err("Call is deferred "/*+gf.getInstruction(pc)*/ + " " + fn1);
			return;
		}
		implement_calls_(gf, context, i2, fn1, pc);
	}

	private void implement_calls_(final @NotNull BaseEvaFunction gf,
								  final @NotNull Context context,
								  final InstructionArgument i2,
								  final @NotNull ProcTableEntry pte,
								  final int pc) {
		Implement_Calls_ ic = new Implement_Calls_(gf, context, i2, pte, pc);
		ic.action();
	}

	void implement_construct(BaseEvaFunction generatedFunction, Instruction instruction, final Context aContext) {
		final @NotNull Implement_construct ic = newImplement_construct(generatedFunction, instruction);
		try {
			ic.action(aContext);
		} catch (FCA_Stop e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@NotNull
	public Implement_construct newImplement_construct(BaseEvaFunction generatedFunction, Instruction instruction) {
		return new Implement_construct(this, generatedFunction, instruction);
	}

	private void implement_is_a(final @NotNull BaseEvaFunction gf, final @NotNull Instruction instruction) {
		final IntegerIA testing_var_  = (IntegerIA) instruction.getArg(0);
		final IntegerIA testing_type_ = (IntegerIA) instruction.getArg(1);
		final Label     target_label  = ((LabelIA) instruction.getArg(2)).label;

		final VariableTableEntry testing_var    = gf.getVarTableEntry(testing_var_.getIndex());
		final TypeTableEntry     testing_type__ = gf.getTypeTableEntry(testing_type_.getIndex());

		GenType genType = testing_type__.genType;
		if (genType.resolved == null) {
			try {
				genType.resolved = resolve_type(genType.typeName, gf.getFD().getContext()).resolved;
			} catch (ResolveError aResolveError) {
//				aResolveError.printStackTrace();
				errSink.reportDiagnostic(aResolveError);
				return;
			}
		}
		if (genType.ci == null) {
			genType.genCI(genType.nonGenericTypeName, this, errSink, phase);
		}
		if (genType.node == null) {
			if (genType.ci instanceof ClassInvocation) {
				WlGenerateClass gen = new WlGenerateClass(getGenerateFunctions(module), (ClassInvocation) genType.ci, phase.generatedClasses, phase.codeRegistrar);
				gen.run(null);
				genType.node = gen.getResult();
			} else if (genType.ci instanceof NamespaceInvocation) {
				WlGenerateNamespace gen = new WlGenerateNamespace(getGenerateFunctions(module), (NamespaceInvocation) genType.ci, phase.generatedClasses, phase.codeRegistrar);
				gen.run(null);
				genType.node = gen.getResult();
			}
		}
		EvaNode testing_type = testing_type__.resolved();
		assert testing_type != null;
	}

	public @NotNull GenerateFunctions getGenerateFunctions(@NotNull OS_Module aModule) {
		return phase.generatePhase.getGenerateFunctions(aModule);
	}

	public void onEnterFunction(final @NotNull BaseEvaFunction generatedFunction, final Context aContext) {
		for (VariableTableEntry variableTableEntry : generatedFunction.vte_list) {
			variableTableEntry.setDeduceTypes2(this, aContext, generatedFunction);
		}
		for (IdentTableEntry identTableEntry : generatedFunction.idte_list) {
			identTableEntry.setDeduceTypes2(this, aContext, generatedFunction);
			//identTableEntry._fix_table(this, generatedFunction);
		}
		for (ProcTableEntry procTableEntry : generatedFunction.prte_list) {
			procTableEntry.setDeduceTypes2(this, aContext, generatedFunction, errSink);
		}
		//
		// resolve all cte expressions
		//
		for (final @NotNull ConstantTableEntry cte : generatedFunction.cte_list) {
			resolve_cte_expression(cte, aContext);
		}
		//
		// add proc table listeners
		//
		add_proc_table_listeners(generatedFunction);
		//
		// resolve ident table
		//
		for (@NotNull IdentTableEntry ite : generatedFunction.idte_list) {
			ite.resolveExpectation = promiseExpectation(ite, "Element Resolved");
			resolve_ident_table_entry(ite, generatedFunction, aContext);
		}
		//
		// resolve arguments table
		//
		@NotNull Resolve_Variable_Table_Entry    rvte = new Resolve_Variable_Table_Entry(generatedFunction, aContext, this);
		@NotNull DeduceTypes2.IVariableConnector connector;
		if (generatedFunction instanceof EvaConstructor) {
			connector = new CtorConnector((EvaConstructor) generatedFunction);
		} else {
			connector = new NullConnector();
		}
		for (@NotNull VariableTableEntry vte : generatedFunction.vte_list) {
			rvte.action(vte, connector);
		}
	}

	private void resolve_cte_expression(@NotNull ConstantTableEntry cte, Context aContext) {
		final IExpression initialValue = cte.initialValue;
		switch (initialValue.getKind()) {
		case NUMERIC:
			resolve_cte_expression_builtin(cte, aContext, BuiltInTypes.SystemInteger);
			break;
		case STRING_LITERAL:
			resolve_cte_expression_builtin(cte, aContext, BuiltInTypes.String_);
			break;
		case CHAR_LITERAL:
			resolve_cte_expression_builtin(cte, aContext, BuiltInTypes.SystemCharacter);
			break;
		case IDENT: {
			final OS_Type a = cte.getTypeTableEntry().getAttached();
			if (a != null) {
				assert a.getType() != null;
				if (a.getType() == OS_Type.Type.BUILT_IN && a.getBType() == BuiltInTypes.Boolean) {
					assert BuiltInTypes.isBooleanText(cte.getName());
				} else
					throw new NotImplementedException();
			} else {
				assert false;
			}
			break;
		}
		default: {
			LOG.err("8192 " + initialValue.getKind());
			throw new NotImplementedException();
		}
		}
	}
	List<DE3_Active> _actives = new ArrayList<>();

	private void add_proc_table_listeners(@NotNull BaseEvaFunction generatedFunction) {
		for (final @NotNull ProcTableEntry pte : generatedFunction.prte_list) {
			pte.addStatusListener(new ProcTableListener(pte, generatedFunction, new DeduceClient2(this)));

			InstructionArgument en = pte.expression_num;
			if (en != null) {
				if (en instanceof final @NotNull IdentIA identIA) {
					@NotNull IdentTableEntry idte = identIA.getEntry();
					idte.addStatusListener(new BaseTableEntry.StatusListener() {
						@Override
						public void onChange(IElementHolder eh, BaseTableEntry.Status newStatus) {
							if (newStatus != BaseTableEntry.Status.KNOWN)
								return;

							final OS_Element el = eh.getElement();

							@NotNull ElObjectType type = DecideElObjectType.getElObjectType(el);

							switch (type) {
							case NAMESPACE:
								@NotNull GenType genType = new GenType((NamespaceStatement) el);
								generatedFunction.addDependentType(genType);
								break;
							case CLASS:
								if (idte.type != null && idte.type.genType != null) {
									assert idte.type.genType.resolved != null;
									generatedFunction.addDependentType(idte.type.genType);
								} else {
									@NotNull GenType genType2 = new GenType((ClassStatement) el);
									generatedFunction.addDependentType(genType2);
								}
								break;
							case FUNCTION:
								@Nullable IdentIA identIA2 = null;
								if (pte.expression_num instanceof IdentIA)
									identIA2 = (IdentIA) pte.expression_num;
								if (identIA2 != null) {
									@NotNull IdentTableEntry idte2          = identIA.getEntry();
									@Nullable ProcTableEntry procTableEntry = idte2.getCallablePTE();
									if (procTableEntry == pte) tripleo.elijah.util.Stupidity.println_err_2("940 procTableEntry == pte");
									if (procTableEntry != null) {
										// TODO doesn't seem like we need this
										procTableEntry.onFunctionInvocation(new DoneCallback<FunctionInvocation>() {
											@Override
											public void onDone(@NotNull FunctionInvocation functionInvocation) {
												ClassInvocation     ci  = functionInvocation.getClassInvocation();
												NamespaceInvocation nsi = functionInvocation.getNamespaceInvocation();
												// do we register?? probably not
												assert ci != null || nsi != null;
												@NotNull FunctionInvocation fi = newFunctionInvocation((FunctionDef) el, pte, ci != null ? ci : nsi, phase);

												{
													if (functionInvocation.getClassInvocation() == fi.getClassInvocation() &&
															functionInvocation.getFunction() == fi.getFunction() &&
															functionInvocation.pte == fi.pte)
														tripleo.elijah.util.Stupidity.println_err_2("955 It seems like we are generating the same thing...");
													else {
														int ok = 2;
													}

												}
												generatedFunction.addDependentFunction(fi);
											}
										});
										// END
									}
								}
								break;
							case CONSTRUCTOR:
								int y = 2;
								break;
							default:
								LOG.err(String.format("228 Don't know what to do %s %s", type, el));
								break;
							}
						}
					});
				} else if (en instanceof IntegerIA) {
					// TODO this code does nothing so commented out
/*
					final @NotNull IntegerIA integerIA = (IntegerIA) en;
					@NotNull VariableTableEntry vte = integerIA.getEntry();
					vte.addStatusListener(new BaseTableEntry.StatusListener() {
						@Override
						public void onChange(IElementHolder eh, BaseTableEntry.Status newStatus) {
							if (newStatus != BaseTableEntry.Status.KNOWN)
								return;

							@NotNull VariableTableEntry vte2 = vte;

							final OS_Element el = eh.getElement();

							@NotNull ElObjectType type = DecideElObjectType.getElObjectType(el);

							switch (type) {
							case VAR:
								break;
							default:
								throw new NotImplementedException();
							}
						}
					});
*/
				} else
					throw new NotImplementedException();
			}
		}
	}

	public <B> @NotNull PromiseExpectation<B> promiseExpectation(ExpectationBase base, String desc) {
		final @NotNull PromiseExpectation<B> promiseExpectation = new PromiseExpectation<>(base, desc);
		expectations.add(promiseExpectation);
		return promiseExpectation;
	}

	public void resolve_ident_table_entry(@NotNull IdentTableEntry ite, @NotNull BaseEvaFunction generatedFunction, Context ctx) {
		@Nullable InstructionArgument itex = new IdentIA(ite.getIndex(), generatedFunction);
		{
			while (itex != null && itex instanceof IdentIA) {
				@NotNull IdentTableEntry itee = ((IdentIA) itex).getEntry();

				@Nullable BaseTableEntry x = null;
				if (itee.getBacklink() instanceof IntegerIA) {
					@NotNull VariableTableEntry vte = ((IntegerIA) itee.getBacklink()).getEntry();
					x = vte;
//					if (vte.constructable_pte != null)
					itex = null;
				} else if (itee.getBacklink() instanceof IdentIA) {
					x    = ((IdentIA) itee.getBacklink()).getEntry();
					itex = ((IdentTableEntry) x).getBacklink();
				} else if (itee.getBacklink() instanceof ProcIA) {
					x = ((ProcIA) itee.getBacklink()).getEntry();
//					if (itee.getCallablePTE() == null)
//						// turned out to be wrong (by double calling), so let's wrap it
//						itee.setCallablePTE((ProcTableEntry) x);
					itex = null; //((ProcTableEntry) x).backlink;
				} else if (itee.getBacklink() == null) {
					itex = null;
					x    = null;
				}

				if (x != null) {
//					LOG.err("162 Adding FoundParent for "+itee);
//					LOG.err(String.format("1656 %s \n\t %s \n\t%s", x, itee, itex));
					x.addStatusListener(new FoundParent(x, itee, itee.getIdent().getContext(), generatedFunction)); // TODO context??
				}
			}
		}
		if (ite.hasResolvedElement()) return;

		ite.calculateResolvedElement();

		if (ite.getResolvedElement() != null) {
			//ite.resolveExpectation.satisfy(ite.getResolvedElement());
			return;
		}
		if (true) {
//			final @NotNull IdentIA identIA = new IdentIA(ite.getIndex(), generatedFunction);
			ite.addStatusListener(new BaseTableEntry.StatusListener() {
				@Override
				public void onChange(final IElementHolder eh, final BaseTableEntry.Status newStatus) {
					if (newStatus != BaseTableEntry.Status.KNOWN) return;

					final OS_Element e = eh.getElement();
					found_element_for_ite(generatedFunction, ite, e, ctx, central());
				}
			});
			/*resolveIdentIA_(ite.getPC(), identIA, generatedFunction, new FoundElement(phase) {

				final String x = generatedFunction.getIdentIAPathNormal(identIA);

				@Override
				public void foundElement(OS_Element e) {
//					ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(e)); // this is called in resolveIdentIA_
					found_element_for_ite(generatedFunction, ite, e, ctx);
				}

				@Override
				public void noFoundElement() {
					ite.setStatus(BaseTableEntry.Status.UNKNOWN, null);
					//errSink.reportError("1004 Can't find element for "+ x); // Already reported by 1179
				}
			})*/
		}
	}

	private void resolve_cte_expression_builtin(@NotNull ConstantTableEntry cte, Context aContext, BuiltInTypes aBuiltInType) {
		final OS_Type a = cte.getTypeTableEntry().getAttached();
		if (a == null || a.getType() != OS_Type.Type.USER_CLASS) {
			try {
				cte.getTypeTableEntry().setAttached(resolve_type(new OS_BuiltinType(aBuiltInType), aContext));
			} catch (ResolveError resolveError) {
				tripleo.elijah.util.Stupidity.println_out_2("117 Can't be here");
//				resolveError.printStackTrace(); // TODO print diagnostic
			}
		}
	}

	@NotNull
	public FunctionInvocation newFunctionInvocation(FunctionDef aFunctionDef, ProcTableEntry aPte, @NotNull IInvocation aInvocation, @NotNull DeducePhase aDeducePhase) {
		@NotNull FunctionInvocation fi = aDeducePhase.newFunctionInvocation(aFunctionDef, aPte, aInvocation);
		// TODO register here
		return fi;
	}

	void found_element_for_ite(BaseEvaFunction generatedFunction, @NotNull IdentTableEntry ite, @Nullable OS_Element y, Context ctx, final DeduceCentral central) {
		if (y != ite.getResolvedElement())
			tripleo.elijah.util.Stupidity.println_err_2(String.format("2571 Setting FoundElement for ite %s to %s when it is already %s", ite, y, ite.getResolvedElement()));

		@NotNull Found_Element_For_ITE fefi = new Found_Element_For_ITE(generatedFunction, ctx, LOG, errSink, new DeduceClient1(this), central);
		fefi.action(ite);
	}

	private DeduceCentral central() {
		return _central;
	}

	public void deduce_generated_function_base(final @NotNull BaseEvaFunction generatedFunction, @NotNull FunctionDef fd) {
		fix_tables(generatedFunction);

		final Context fd_ctx = fd.getContext();
		//
		{
			ProcTableEntry        pte        = generatedFunction.fi.pte;
			final @NotNull String pte_string = getPTEString(pte);
			LOG.info("** deduce_generated_function " + fd.name() + " " + pte_string);//+" "+((OS_Container)((FunctionDef)fd).getParent()).name());
		}
		//
		//
		for (final @NotNull Instruction instruction : generatedFunction.instructions()) {
			final Context context = generatedFunction.getContextFromPC(instruction.getIndex());
//			LOG.info("8006 " + instruction);
			switch (instruction.getName()) {
			case E:
				onEnterFunction(generatedFunction, context);
				break;
			case X:
				onExitFunction(generatedFunction, fd_ctx, context);
				break;
			case ES:
				break;
			case XS:
				break;
			case AGN:
				do_assign_normal(generatedFunction, fd_ctx, instruction, context);
				break;
			case AGNK: {
				final @NotNull IntegerIA          arg  = (IntegerIA) instruction.getArg(0);
				final @NotNull VariableTableEntry vte  = generatedFunction.getVarTableEntry(arg.getIndex());
				final InstructionArgument         i2   = instruction.getArg(1);
				final @NotNull ConstTableIA       ctia = (ConstTableIA) i2;
				do_assign_constant(generatedFunction, instruction, vte, ctia);
			}
			break;
			case AGNT:
				break;
			case AGNF:
				LOG.info("292 Encountered AGNF");
				break;
			case JE:
				LOG.info("296 Encountered JE");
				break;
			case JNE:
				break;
			case JL:
				break;
			case JMP:
				break;
			case CALL: {
				final int                     pte_num = ((ProcIA) instruction.getArg(0)).getIndex();
				final @NotNull ProcTableEntry pte     = generatedFunction.getProcTableEntry(pte_num);
//				final InstructionArgument i2 = (instruction.getArg(1));
				{
					final @NotNull IdentIA identIA = (IdentIA) pte.expression_num;
					final String           x       = generatedFunction.getIdentIAPathNormal(identIA);
					LOG.info("298 Calling " + x);
					resolveIdentIA_(context, identIA, generatedFunction, new FoundElement(phase) {

						@SuppressWarnings("unused")
						final String xx = x;

						@Override
						public void foundElement(OS_Element e) {
							found_element_for_ite(generatedFunction, identIA.getEntry(), e, context, central());
//							identIA.getEntry().setCallablePTE(pte); // TODO ??

							pte.setStatus(BaseTableEntry.Status.KNOWN, new ConstructableElementHolder(e, identIA));
							if (fd instanceof DefFunctionDef) {
								final IInvocation invocation = getInvocation((EvaFunction) generatedFunction);
								forFunction(newFunctionInvocation((FunctionDef) e, pte, invocation, phase), new ForFunction() {
									@Override
									public void typeDecided(@NotNull GenType aType) {
										@Nullable InstructionArgument x = generatedFunction.vte_lookup("Result");
										assert x != null;
										((IntegerIA) x).getEntry().type.setAttached(gt(aType));
									}
								});
							}
						}

						@Override
						public void noFoundElement() {
							errSink.reportError("370 Can't find callsite " + x);
							// TODO don't know if this is right
							@NotNull IdentTableEntry entry = identIA.getEntry();
							if (entry.getStatus() != BaseTableEntry.Status.UNKNOWN)
								entry.setStatus(BaseTableEntry.Status.UNKNOWN, null);
						}
					});
				}
			}
			break;
			case CALLS: {
				final int                     i1  = to_int(instruction.getArg(0));
				final InstructionArgument     i2  = (instruction.getArg(1));
				final @NotNull ProcTableEntry fn1 = generatedFunction.getProcTableEntry(i1);
				{
					implement_calls(generatedFunction, fd_ctx, i2, fn1, instruction.getIndex());
				}
/*
				if (i2 instanceof IntegerIA) {
					int i2i = to_int(i2);
					VariableTableEntry vte = generatedFunction.getVarTableEntry(i2i);
					int y =2;
				} else
					throw new NotImplementedException();
*/
			}
			break;
			case RET:
				break;
			case YIELD:
				break;
			case TRY:
				break;
			case PC:
				break;
			case CAST_TO:
				// README potentialType info is already added by MatchConditional
				break;
			case DECL:
				// README for GenerateC, etc: marks the spot where a declaration should go. Wouldn't be necessary if we had proper Range's
				break;
			case IS_A:
				implement_is_a(generatedFunction, instruction);
				break;
			case NOP:
				break;
			case CONSTRUCT:
				implement_construct(generatedFunction, instruction, context);
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + instruction.getName());
			}
		}
		for (final @NotNull VariableTableEntry vte : generatedFunction.vte_list) {
			vte.typeResolvePromise().then(gt -> {
				if (gt.ci == null) {
					gt.ci = ((EvaClass) vte.resolvedType()).ci;
				}
				gt.resolved = ((EvaClass) vte.resolvedType()).getKlass().getOS_Type();
				gt.typeName = gt.resolved;

				vte.type.setAttached(gt);
			});
		}
		for (final @NotNull VariableTableEntry vte : generatedFunction.vte_list) {
			if (vte.type.getAttached() == null) {
				int potential_size = vte.potentialTypes().size();
				if (potential_size == 1)
					vte.type.setAttached(getPotentialTypesVte(vte).get(0).getAttached());
				else if (potential_size > 1) {
					// TODO Check type compatibility
					LOG.err("703 " + vte.getName() + " " + vte.potentialTypes());
					errSink.reportDiagnostic(new CantDecideType(vte, vte.potentialTypes()));
				} else {
					// potential_size == 0
					// Result is handled by phase.typeDecideds, self is always valid
					if (/*vte.getName() != null &&*/ !(vte.vtt == VariableTableType.RESULT || vte.vtt == VariableTableType.SELF))
						errSink.reportDiagnostic(new CantDecideType(vte, vte.potentialTypes()));
				}
			} else if (vte.vtt == VariableTableType.RESULT) {
				final OS_Type attached = vte.type.getAttached();
				if (attached.getType() == OS_Type.Type.USER) {
					try {
						vte.type.setAttached(resolve_type(attached, fd_ctx));
					} catch (ResolveError aResolveError) {
						aResolveError.printStackTrace();
						assert false;
					}
				}
			}
		}
		{
			//
			// NOW CALCULATE DEFERRED CALLS
			//
			for (final Integer deferred_call : generatedFunction.deferred_calls) {
				final Instruction instruction = generatedFunction.getInstruction(deferred_call);

				final int                     i1  = to_int(instruction.getArg(0));
				final InstructionArgument     i2  = (instruction.getArg(1));
				final @NotNull ProcTableEntry fn1 = generatedFunction.getProcTableEntry(i1);
				{
//					generatedFunction.deferred_calls.remove(deferred_call);
					implement_calls_(generatedFunction, fd_ctx, i2, fn1, instruction.getIndex());
				}
			}
		}
	}

	void resolve_function_return_type(@NotNull BaseEvaFunction generatedFunction) {
		final DeduceElement3_Function f = _zero.get(DeduceTypes2.this, generatedFunction);

		final GenType gt = f.resolve_function_return_type_int(errSink);
		if (gt != null)
			//phase.typeDecided((EvaFunction) generatedFunction, gt);
			generatedFunction.resolveTypeDeferred(gt);
	}

//	private GeneratedNode makeNode(GenType aGenType) {
//		if (aGenType.ci instanceof ClassInvocation) {
//			final ClassInvocation ci = (ClassInvocation) aGenType.ci;
//			@NotNull GenerateFunctions gen = phase.generatePhase.getGenerateFunctions(ci.getKlass().getContext().module());
//			WlGenerateClass wlgc = new WlGenerateClass(gen, ci, phase.generatedClasses);
//			wlgc.run(null);
//			return wlgc.getResult();
//		}
//		return null;
//	}

	private static void checkEvaClassVarTable(final @NotNull BaseEvaFunction generatedFunction) {
		//for (VariableTableEntry variableTableEntry : generatedFunction.vte_list) {
		//	variableTableEntry.setDeduceTypes2(this, aContext, generatedFunction);
		//}
		for (IdentTableEntry identTableEntry : generatedFunction.idte_list) {
			//identTableEntry.setDeduceTypes2(this, aContext, generatedFunction);

			identTableEntry.backlinkSet().then(new DoneCallback<InstructionArgument>() {
				@Override
				public void onDone(final InstructionArgument backlink0) {
					BaseTableEntry backlink;

					if (backlink0 instanceof IdentIA) {
						backlink = ((IdentIA) backlink0).getEntry();
						setBacklinkCallback(backlink);
					} else if (backlink0 instanceof IntegerIA) {
						backlink = ((IntegerIA) backlink0).getEntry();
						setBacklinkCallback(backlink);
					} else if (backlink0 instanceof ProcIA) {
						backlink = ((ProcIA) backlink0).getEntry();
						setBacklinkCallback(backlink);
					} else
						backlink = null;
				}

				public void setBacklinkCallback(BaseTableEntry backlink) {
					if (backlink instanceof ProcTableEntry) {
						final ProcTableEntry procTableEntry = (ProcTableEntry) backlink;

						procTableEntry.typeResolvePromise().done((final GenType result) -> {
							final DeduceElement3_IdentTableEntry de3_ite = identTableEntry.getDeduceElement3();

							if (result.ci == null && result.node == null)
								result.genCIForGenType2(de3_ite.deduceTypes2());

							for (EvaContainer.VarTableEntry entry : ((EvaContainerNC) result.node).varTable) {
								if (!entry.isResolved()) {
									System.err.println("629 entry not resolved " + entry.nameToken);
								}
							}
						});
					}
				}
			});
		}
		//for (ProcTableEntry procTableEntry : generatedFunction.prte_list) {
		//	procTableEntry.setDeduceTypes2(this, aContext, generatedFunction, errSink);
		//}
	}

	void onFinish(Runnable r) {
		onRunnables.add(r);
	}

	/*static*/
	@NotNull GenType resolve_type(final OS_Module module, final @NotNull OS_Type type, final Context ctx) throws ResolveError {
		return ResolveType.resolve_type(module, type, ctx, LOG, this);
	}

	public void resolveIdentIA_(@NotNull Context context, @NotNull DeduceElementIdent dei, BaseEvaFunction generatedFunction, @NotNull FoundElement foundElement) {
		@NotNull Resolve_Ident_IA ria = new Resolve_Ident_IA(new DeduceClient3(this), context, dei, generatedFunction, foundElement, errSink);
		ria.action();
	}

	public void resolveIdentIA_(@NotNull Context context, @NotNull IdentIA identIA, BaseEvaFunction generatedFunction, @NotNull FoundElement foundElement) {
		@NotNull Resolve_Ident_IA ria = new Resolve_Ident_IA(new DeduceClient3(this), context, identIA, generatedFunction, foundElement, errSink);
		ria.action();
	}

	public void do_assign_normal(final @NotNull BaseEvaFunction generatedFunction, final Context aFd_ctx, final @NotNull Instruction instruction, final Context aContext) {
		// TODO doesn't account for __assign__
		final InstructionArgument agn_lhs = instruction.getArg(0);
		if (agn_lhs instanceof IntegerIA) {
			final @NotNull IntegerIA          arg = (IntegerIA) agn_lhs;
			final @NotNull VariableTableEntry vte = generatedFunction.getVarTableEntry(arg.getIndex());
			final InstructionArgument         i2  = instruction.getArg(1);
			if (i2 instanceof IntegerIA) {
				final @NotNull VariableTableEntry vte2 = generatedFunction.getVarTableEntry(to_int(i2));
				vte.addPotentialType(instruction.getIndex(), vte2.type);
			} else if (i2 instanceof final @NotNull FnCallArgs fca) {

				final @Nullable ITastic fcat = tasticFor(fca);

				fcat.do_assign_call(generatedFunction, aContext, vte, instruction);
			} else if (i2 instanceof ConstTableIA) {
				do_assign_constant(generatedFunction, instruction, vte, (ConstTableIA) i2);
			} else if (i2 instanceof IdentIA) {
				@NotNull IdentTableEntry idte = generatedFunction.getIdentTableEntry(to_int(i2));
				if (idte.type == null) {
					final IdentIA identIA = new IdentIA(idte.getIndex(), generatedFunction);
					resolveIdentIA_(aContext, identIA, generatedFunction, new FoundElement(phase) {

						@Override
						public void foundElement(final OS_Element e) {
							found_element_for_ite(generatedFunction, idte, e, aContext, central());
							assert idte.hasResolvedElement();
							vte.addPotentialType(instruction.getIndex(), idte.type);
						}

						@Override
						public void noFoundElement() {
							// TODO: log error
							int y = 2;
						}
					});
				}
			} else if (i2 instanceof ProcIA) {
				throw new NotImplementedException();
			} else
				throw new NotImplementedException();
		} else if (agn_lhs instanceof IdentIA) {
			final @NotNull IdentIA         arg  = (IdentIA) agn_lhs;
			final @NotNull IdentTableEntry idte = arg.getEntry();
			final InstructionArgument      i2   = instruction.getArg(1);
			if (i2 instanceof IntegerIA) {
				final @NotNull VariableTableEntry vte2 = generatedFunction.getVarTableEntry(to_int(i2));
				idte.addPotentialType(instruction.getIndex(), vte2.type);
			} else if (i2 instanceof final @NotNull FnCallArgs fca) {
				tasticFor(i2).do_assign_call(generatedFunction, aFd_ctx, idte, instruction.getIndex());
			} else if (i2 instanceof IdentIA) {
				if (idte.getResolvedElement() instanceof VariableStatementImpl) {
					do_assign_normal_ident_deferred(generatedFunction, aFd_ctx, idte);
				}
				@NotNull IdentTableEntry idte2 = generatedFunction.getIdentTableEntry(to_int(i2));
				do_assign_normal_ident_deferred(generatedFunction, aFd_ctx, idte2);
				idte.addPotentialType(instruction.getIndex(), idte2.type);
			} else if (i2 instanceof ConstTableIA) {
				do_assign_constant(generatedFunction, instruction, idte, (ConstTableIA) i2);
			} else if (i2 instanceof ProcIA) {
				throw new NotImplementedException();
			} else
				throw new NotImplementedException();
		}
	}

	public void do_assign_normal_ident_deferred(final @NotNull BaseEvaFunction generatedFunction,
												final @NotNull Context aContext,
												final @NotNull IdentTableEntry aIdentTableEntry) {
		if (aIdentTableEntry.type == null) {
			aIdentTableEntry.makeType(generatedFunction, TypeTableEntry.Type.TRANSIENT, (OS_Type) null);
		}
		LookupResultList     lrl1 = aContext.lookup(aIdentTableEntry.getIdent().getText());
		@Nullable OS_Element best = lrl1.chooseBest(null);
		if (best != null) {
			aIdentTableEntry.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
			// TODO check for elements which may contain type information
			if (best instanceof final @NotNull VariableStatementImpl vs) {
				do_assign_normal_ident_deferred_VariableStatement(generatedFunction, aIdentTableEntry, vs);
			} else if (best instanceof final FormalArgListItem fali) {
				do_assign_normal_ident_deferred_FALI(generatedFunction, aIdentTableEntry, fali);
			} else
				throw new NotImplementedException();
		} else {
			aIdentTableEntry.setStatus(BaseTableEntry.Status.UNKNOWN, null);
			LOG.err("242 Bad lookup" + aIdentTableEntry.getIdent().getText());
		}
	}

	private void do_assign_normal_ident_deferred_FALI(final BaseEvaFunction generatedFunction, final IdentTableEntry aIdentTableEntry, final FormalArgListItem fali) {
		GenType           genType = new GenType();
		final IInvocation invocation;
		if (generatedFunction.fi.getClassInvocation() != null) {
			invocation       = generatedFunction.fi.getClassInvocation();
			genType.resolved = ((ClassInvocation) invocation).getKlass().getOS_Type();
		} else {
			invocation        = generatedFunction.fi.getNamespaceInvocation();
			genType.resolvedn = ((NamespaceInvocation) invocation).getNamespace();
		}
		genType.ci = invocation;
		final @Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(fali.name());
		assert vte_ia != null;
		((IntegerIA) vte_ia).getEntry().typeResolvePromise().then(new DoneCallback<GenType>() {
			@Override
			public void onDone(final GenType result) {
				assert result.resolved != null;
				aIdentTableEntry.type.setAttached(result.resolved);
			}
		});
		generatedFunction.addDependentType(genType);
	}

	public void do_assign_normal_ident_deferred_VariableStatement(final @NotNull BaseEvaFunction generatedFunction, final @NotNull IdentTableEntry aIdentTableEntry, final @NotNull VariableStatementImpl vs) {
		final IInvocation invocation;
		if (generatedFunction.fi.getClassInvocation() != null)
			invocation = generatedFunction.fi.getClassInvocation();
		else
			invocation = generatedFunction.fi.getNamespaceInvocation();
		@NotNull DeferredMember dm = deferred_member(vs.getParent().getParent(), invocation, vs, aIdentTableEntry);
		dm.typePromise().done(new DoneCallback<GenType>() {
			@Override
			public void onDone(@NotNull GenType result) {
				assert result.resolved != null;
				aIdentTableEntry.type.setAttached(result.resolved);
			}
		});
		GenType genType = new GenType();
		genType.ci = dm.getInvocation();
		if (genType.ci instanceof NamespaceInvocation) {
			genType.resolvedn = ((NamespaceInvocation) genType.ci).getNamespace();
		} else if (genType.ci instanceof ClassInvocation) {
			genType.resolved = ((ClassInvocation) genType.ci).getKlass().getOS_Type();
		} else {
			throw new IllegalStateException();
		}
		generatedFunction.addDependentType(genType);
	}

	public void resolveIdentIA2_(@NotNull Context context, @NotNull IdentIA identIA, @NotNull EvaFunction generatedFunction, @NotNull FoundElement foundElement) {
		final @NotNull List<InstructionArgument> s = BaseEvaFunction._getIdentIAPathList(identIA);
		resolveIdentIA2_(context, identIA, s, generatedFunction, foundElement);
	}

	public void resolveIdentIA2_(@NotNull final Context ctx,
								 @Nullable IdentIA identIA,
								 @Nullable List<InstructionArgument> s,
								 @NotNull final BaseEvaFunction generatedFunction,
								 @NotNull final FoundElement foundElement) {
		@NotNull Resolve_Ident_IA2 ria2 = new Resolve_Ident_IA2(this, errSink, phase, generatedFunction, foundElement);
		ria2.resolveIdentIA2_(ctx, identIA, s);
	}

	public ITastic tasticFor(Object o) {
		if (tasticMap.containsKey(o)) {
			return tasticMap.get(o);
		}

		ITastic r = null;

		if (o instanceof FnCallArgs) {
			r = new FT_FnCallArgs(this, (FnCallArgs) o);
		}

		return r;
	}

	public DeduceElement3_ProcTableEntry zeroGet(final ProcTableEntry aPte, final BaseEvaFunction aEvaFunction) {
		return _zero.get(aPte, aEvaFunction);
	}

	public String getFileName() {
		return module.getFileName();
	}

	public DeduceElement3_VariableTableEntry zeroGet(final VariableTableEntry aVte, final BaseEvaFunction aGeneratedFunction) {
		return _zero.get(aVte, aGeneratedFunction);
	}

	public enum ClassInvocationMake {
		;

		public static @NotNull ClassInvocation withGenericPart(ClassStatement best,
															   String constructorName,
															   NormalTypeName aTyn1,
															   DeduceTypes2 dt2,
															   final ErrSink aErrSink) {
			@NotNull GenericPart genericPart = new GenericPart(best, aTyn1);

			@Nullable ClassInvocation clsinv = new ClassInvocation(best, constructorName);

			if (genericPart.hasGenericPart()) {
				final @NotNull List<TypeName> gp  = best.getGenericPart();
				final @NotNull TypeNameList   gp2 = genericPart.getGenericPartFromTypeName();

				for (int i = 0; i < gp.size(); i++) {
					final TypeName   typeName = gp2.get(i);
					@NotNull GenType typeName2;
					try {
						typeName2 = dt2.resolve_type(new OS_UserType(typeName), typeName.getContext());
						// TODO transition to GenType
						clsinv.set(i, gp.get(i), typeName2.resolved);
					} catch (ResolveError aResolveError) {
//						aResolveError.printStackTrace();
						aErrSink.reportDiagnostic(aResolveError);
					}
				}
			}
			return clsinv;
		}
	}

	private @Nullable IInvocation getInvocationFromBacklink(@Nullable InstructionArgument aBacklink) {
		if (aBacklink == null) return null;
		// TODO implement me
		return null;
	}

	public enum ProcessElement {
		;

		public static void processElement(OS_Element el, IElementProcessor ep) {
			if (el == null)
				ep.elementIsNull();
			else
				ep.hasElement(el);
		}
	}

	interface df_helper<T> {
		@NotNull Collection<T> collection();

		boolean deduce(T generatedConstructor);
	}

	interface df_helper_i<T> {
		@Nullable df_helper<T> get(EvaContainerNC generatedClass);
	}

	public interface ExpectationBase {
		String expectationString();
	}

	public interface IElementProcessor {
		void elementIsNull();

		void hasElement(OS_Element el);
	}

	interface IVariableConnector {
		void connect(VariableTableEntry aVte, String aName);
	}

	static class CtorConnector implements IVariableConnector {
		private final EvaConstructor evaConstructor;

		public CtorConnector(final EvaConstructor aEvaConstructor) {
			evaConstructor = aEvaConstructor;
		}

		@Override
		public void connect(final VariableTableEntry aVte, final String aName) {
			final List<EvaContainer.VarTableEntry> vt = ((EvaClass) evaConstructor.getGenClass()).varTable;
			for (EvaContainer.VarTableEntry gc_vte : vt) {
				if (gc_vte.nameToken.getText().equals(aName)) {
					gc_vte.connect(aVte, evaConstructor);
					break;
				}
			}
		}
	}

	static class DC_ClassNote {
		private final DeduceCentral    central;
		private final Context          ctx;
		private final ClassStatement e;
		private       DC_ClassNote_DT2 dt2a;

		public DC_ClassNote(final ClassStatement aE, final Context aCtx, final DeduceCentral aCentral) {
			e       = aE;
			ctx     = aCtx;
			central = aCentral;
		}

		public void attach(final IdentTableEntry aIte, final BaseEvaFunction aGeneratedFunction) {
			dt2a = new DC_ClassNote_DT2(aIte, aGeneratedFunction, central.getDeduceTypes2());
		}

		static class DC_ClassNote_DT2 {
			private final DeduceTypes2    deduceTypes2;
			private final BaseEvaFunction generatedFunction;
			private final IdentTableEntry ite;

			public DC_ClassNote_DT2(final IdentTableEntry aIte, final BaseEvaFunction aGeneratedFunction, final DeduceTypes2 aDeduceTypes2) {
				ite               = aIte;
				generatedFunction = aGeneratedFunction;
				deduceTypes2      = aDeduceTypes2;
			}

			public DeduceTypes2 getDeduceTypes2() {
				return deduceTypes2;
			}

			public BaseEvaFunction getGeneratedFunction() {
				return generatedFunction;
			}

			public IdentTableEntry getIte() {
				return ite;
			}
		}
	}

	static class DeduceCentral {
		private final DeduceTypes2 deduceTypes2;

		public DeduceCentral(final DeduceTypes2 aDeduceTypes2) {
			deduceTypes2 = aDeduceTypes2;
		}

		public DeduceTypes2 getDeduceTypes2() {
			return deduceTypes2;
		}

		public DC_ClassNote note_Class(final ClassStatement aE, final Context aCtx) {
			DC_ClassNote cn = new DC_ClassNote(aE, aCtx, this);
			return cn;
		}
	}

	boolean lookup_name_calls(final @NotNull Context ctx, final @NotNull String pn, final @NotNull ProcTableEntry pte) {
		final LookupResultList     lrl  = ctx.lookup(pn);
		final @Nullable OS_Element best = lrl.chooseBest(null); // TODO check arity and arg matching
		if (best != null) {
			pte.setStatus(BaseTableEntry.Status.KNOWN, new ConstructableElementHolder(best, null)); // TODO why include if only to be null?
			return true;
		}
		return false;
	}

	public static class DeduceClient1 {
		private final DeduceTypes2 dt2;

		@Contract(pure = true)
		public DeduceClient1(DeduceTypes2 aDeduceTypes2) {
			dt2 = aDeduceTypes2;
		}

		public @Nullable OS_Element _resolveAlias(@NotNull AliasStatementImpl aAliasStatement) {
			return DeduceLookupUtils._resolveAlias(aAliasStatement, dt2);
		}

		public @NotNull DeferredMember deferred_member(OS_Element aParent, IInvocation aInvocation, VariableStatementImpl aVariableStatement, @NotNull IdentTableEntry aIdentTableEntry) {
			return dt2.deferred_member(aParent, aInvocation, aVariableStatement, aIdentTableEntry);
		}

		public void found_element_for_ite(BaseEvaFunction aGeneratedFunction, @NotNull IdentTableEntry aIte, OS_Element aX, Context aCtx) {
			dt2.found_element_for_ite(aGeneratedFunction, aIte, aX, aCtx, dt2.central());
		}

		public void genCI(final @NotNull GenType aResult, final TypeName aNonGenericTypeName) {
			aResult.genCI(aNonGenericTypeName, dt2, dt2.errSink, dt2.phase);
		}

		public void genCIForGenType2(final GenType genType) {
			genType.genCIForGenType2(dt2);
		}

		public @Nullable IInvocation getInvocationFromBacklink(InstructionArgument aInstructionArgument) {
			return dt2.getInvocationFromBacklink(aInstructionArgument);
		}

		public @NotNull ArrayList<TypeTableEntry> getPotentialTypesVte(VariableTableEntry aVte) {
			return dt2.getPotentialTypesVte(aVte);
		}

		public void LOG_err(String aS) {
			dt2.LOG.err(aS);
		}

		public @Nullable ClassInvocation registerClassInvocation(final ClassStatement aClassStatement, final String aS) {
			return dt2.phase.registerClassInvocation(aClassStatement, aS);
		}

		public @NotNull GenType resolve_type(@NotNull OS_Type aType, Context aCtx) throws ResolveError {
			return dt2.resolve_type(aType, aCtx);
		}
	}

	static class DeduceClient2 {
		private final DeduceTypes2 deduceTypes2;

		public DeduceClient2(DeduceTypes2 deduceTypes2) {
			this.deduceTypes2 = deduceTypes2;
		}

		public DeduceTypes2 deduceTypes2() {
			return deduceTypes2;
		}

		public @NotNull ClassInvocation genCI(@NotNull GenType genType, TypeName typeName) {
			return genType.genCI(typeName, deduceTypes2, deduceTypes2.errSink, deduceTypes2.phase);
		}

		public @NotNull ElLog getLOG() {
			return deduceTypes2.LOG;
		}

		public @NotNull FunctionInvocation newFunctionInvocation(FunctionDef constructorDef, ProcTableEntry pte, @NotNull IInvocation ci) {
			return deduceTypes2.newFunctionInvocation(constructorDef, pte, ci, deduceTypes2.phase);
		}

		public @Nullable ClassInvocation registerClassInvocation(@NotNull ClassInvocation ci) {
			return deduceTypes2.phase.registerClassInvocation(ci);
		}

		public NamespaceInvocation registerNamespaceInvocation(NamespaceStatement namespaceStatement) {
			return deduceTypes2.phase.registerNamespaceInvocation(namespaceStatement);
		}
	}

	public static class DeduceClient3 {
		final DeduceTypes2 deduceTypes2;

		public DeduceClient3(final DeduceTypes2 aDeduceTypes2) {
			deduceTypes2 = aDeduceTypes2;
		}

		public void addJobs(final WorkList aWl) {
			deduceTypes2.wm.addJobs(aWl);
		}

		public void found_element_for_ite(final BaseEvaFunction generatedFunction,
										  final @NotNull IdentTableEntry ite,
										  final @Nullable OS_Element y,
										  final Context ctx) {
			deduceTypes2.found_element_for_ite(generatedFunction, ite, y, ctx, deduceTypes2.central());
		}

		public void genCIForGenType2(final GenType genType) {
			genType.genCIForGenType2(deduceTypes2);
		}

		public GenerateFunctions getGenerateFunctions(final OS_Module aModule) {
			return deduceTypes2.getGenerateFunctions(aModule);
		}

		public IInvocation getInvocation(final EvaFunction aGeneratedFunction) {
			return deduceTypes2.getInvocation(aGeneratedFunction);
		}

		public ElLog getLOG() {
			return deduceTypes2.LOG;
		}

		public DeducePhase getPhase() {
			return deduceTypes2.phase;
		}

		public List<TypeTableEntry> getPotentialTypesVte(final VariableTableEntry aVte) {
			return deduceTypes2.getPotentialTypesVte(aVte);
		}

		public LookupResultList lookupExpression(final IExpression aExp, final Context aContext) throws ResolveError {
			return DeduceLookupUtils.lookupExpression(aExp, aContext, deduceTypes2);
		}

		public @NotNull FunctionInvocation newFunctionInvocation(final BaseFunctionDef aFunctionDef, final ProcTableEntry aPte, final @NotNull IInvocation aInvocation) {
			return deduceTypes2.newFunctionInvocation(aFunctionDef, aPte, aInvocation, deduceTypes2.phase);
		}

		public IElementHolder newGenericElementHolderWithType(final OS_Element aElement, final TypeName aTypeName) {
			final OS_Type typeName;
			if (aTypeName.isNull())
				typeName = null;
			else
				typeName = new OS_UserType(aTypeName);
			return new GenericElementHolderWithType(aElement, typeName, deduceTypes2);
		}

		public GenType resolve_type(final OS_Type aType, final Context aContext) throws ResolveError {
			return deduceTypes2.resolve_type(aType, aContext);
		}

		public void resolveIdentIA2_(final Context aEctx,
									 final IdentIA aIdentIA,
									 final @Nullable List<InstructionArgument> aInstructionArgumentList,
									 final BaseEvaFunction aGeneratedFunction,
									 final FoundElement aFoundElement) {
			deduceTypes2.resolveIdentIA2_(aEctx, aIdentIA, aInstructionArgumentList, aGeneratedFunction, aFoundElement);
		}
	}

	static class GenericPart {
		private final ClassStatement classStatement;
		private final TypeName       genericTypeName;

		@Contract(pure = true)
		public GenericPart(final ClassStatement aClassStatement, final TypeName aGenericTypeName) {
			classStatement  = aClassStatement;
			genericTypeName = aGenericTypeName;
		}

		@Contract(pure = true)
		public TypeNameList getGenericPartFromTypeName() {
			final NormalTypeName ntn = getGenericTypeName();
			return ntn.getGenericPart();
		}

		@Contract(pure = true)
		private NormalTypeName getGenericTypeName() {
			assert genericTypeName != null;
			assert genericTypeName instanceof NormalTypeName;

			return (NormalTypeName) genericTypeName;
		}

		@Contract(pure = true)
		public boolean hasGenericPart() {
			return classStatement.getGenericPart().size() > 0;
		}
	}

	static class NullConnector implements IVariableConnector {
		@Override
		public void connect(final VariableTableEntry aVte, final String aName) {
		}
	}

	public static class OS_SpecialVariable implements OS_Element {
		private final BaseEvaFunction                      generatedFunction;
		private final VariableTableType                    type;
		private final VariableTableEntry                   variableTableEntry;
		public        DeduceLocalVariable.MemberInvocation memberInvocation;

		public OS_SpecialVariable(final VariableTableEntry aVariableTableEntry, final VariableTableType aType, final BaseEvaFunction aGeneratedFunction) {
			variableTableEntry = aVariableTableEntry;
			type               = aType;
			generatedFunction  = aGeneratedFunction;
		}

		@Override
		public Context getContext() {
			return generatedFunction.getFD().getContext();
		}

		@Override
		public OS_Element getParent() {
			return generatedFunction.getFD();
		}

		@Override
		public void visitGen(final ElElementVisitor visit) {
			throw new IllegalArgumentException("not implemented");
		}

		@Nullable
		public IInvocation getInvocation(final DeduceTypes2 aDeduceTypes2) {
			final @Nullable IInvocation aInvocation;
			final OS_SpecialVariable    specialVariable = this;
			assert specialVariable.type == VariableTableType.SELF;
			// first parent is always a function
			switch (DecideElObjectType.getElObjectType(specialVariable.getParent().getParent())) {
			case CLASS:
				final ClassStatement classStatement = (ClassStatement) specialVariable.getParent().getParent();
				aInvocation = aDeduceTypes2.phase.registerClassInvocation(classStatement, null); // TODO generics
//				ClassInvocationMake.withGenericPart(classStatement, null, null, this);
				break;
			case NAMESPACE:
				throw new NotImplementedException(); // README ha! implemented in
			default:
				throw new IllegalArgumentException("Illegal object type for parent");
			}
			return aInvocation;
		}
	}

	public void register_and_resolve(@NotNull VariableTableEntry aVte, @NotNull ClassStatement aKlass) {
		@Nullable ClassInvocation ci = new ClassInvocation(aKlass, null);
		ci = phase.registerClassInvocation(ci);
		ci.resolvePromise().done(new DoneCallback<EvaClass>() {
			@Override
			public void onDone(EvaClass result) {
				aVte.resolveTypeToClass(result);
			}
		});
	}

	public void removeResolvePending(final IdentTableEntry aResolvable) {
		assert hasResolvePending(aResolvable);

		_pendingResolves.remove(aResolvable);
	}

	public class DeduceClient4 {
		private final DeduceTypes2 deduceTypes2;

		public DeduceClient4(final DeduceTypes2 aDeduceTypes2) {
			deduceTypes2 = aDeduceTypes2;
		}

		public OS_Element _resolveAlias(final AliasStatementImpl aAliasStatement) {
			return DeduceLookupUtils._resolveAlias(aAliasStatement, deduceTypes2);
		}

		public OS_Element _resolveAlias2(final AliasStatementImpl aAliasStatement) throws ResolveError {
			return DeduceLookupUtils._resolveAlias2(aAliasStatement, deduceTypes2);
		}

		public DeferredMemberFunction deferred_member_function(final OS_Element aParent, final IInvocation aInvocation, final FunctionDef aFunctionDef, final FunctionInvocation aFunctionInvocation) {
			return deduceTypes2.deferred_member_function(aParent, aInvocation, aFunctionDef, aFunctionInvocation);
		}

		public void forFunction(final FunctionInvocation aFunctionInvocation, final ForFunction aForFunction) {
			deduceTypes2.forFunction(aFunctionInvocation, aForFunction);
		}

		public void found_element_for_ite(final BaseEvaFunction aGeneratedFunction, final IdentTableEntry aEntry, final OS_Element aE, final Context aCtx) {
			deduceTypes2.found_element_for_ite(aGeneratedFunction, aEntry, aE, aCtx, central());
		}

		public ClassInvocation genCI(final GenType aType, final TypeName aGenericTypeName) {
			return aType.genCI(aGenericTypeName, deduceTypes2, deduceTypes2.errSink, deduceTypes2.phase);
		}

		public DeduceTypes2 get() {
			return deduceTypes2;
		}

		public ErrSink getErrSink() {
			return deduceTypes2.errSink;
		}

		public IInvocation getInvocation(final EvaFunction aGeneratedFunction) {
			return deduceTypes2.getInvocation(aGeneratedFunction);
		}

		public @NotNull ElLog getLOG() {
			return LOG;
		}

		public @NotNull OS_Module getModule() {
			return module;
		}

		public DeducePhase getPhase() {
			return deduceTypes2.phase;
		}

		public List<TypeTableEntry> getPotentialTypesVte(final EvaFunction aGeneratedFunction, final InstructionArgument aVte_ia) {
			return deduceTypes2.getPotentialTypesVte(aGeneratedFunction, aVte_ia);
		}

		public OS_Type gt(final GenType aType) {
			return deduceTypes2.gt(aType);
		}

		public void implement_calls(final BaseEvaFunction aGeneratedFunction, final Context aParent, final InstructionArgument aArg, final ProcTableEntry aPte, final int aInstructionIndex) {
			deduceTypes2.implement_calls(aGeneratedFunction, aParent, aArg, aPte, aInstructionIndex);
		}

		public OS_Element lookup(final IdentExpression aElement, final Context aContext) throws ResolveError {
			return DeduceLookupUtils.lookup(aElement, aContext, deduceTypes2);
		}

		public LookupResultList lookupExpression(final IExpression aExpression, final Context aContext) throws ResolveError {
			return DeduceLookupUtils.lookupExpression(aExpression, aContext, deduceTypes2);
		}

		public FunctionInvocation newFunctionInvocation(final FunctionDef aElement, final ProcTableEntry aPte, final @NotNull IInvocation aInvocation) {
			return deduceTypes2.newFunctionInvocation(aElement, aPte, aInvocation, deduceTypes2.phase);
		}

		public void onFinish(final Runnable aRunnable) {
			deduceTypes2.onFinish(aRunnable);
		}

		public <T> PromiseExpectation<T> promiseExpectation(final BaseEvaFunction aGeneratedFunction, final String aName) {
			return deduceTypes2.promiseExpectation(aGeneratedFunction, aName);
		}

		public void register_and_resolve(final VariableTableEntry aVte, final ClassStatement aClassStatement) {
			deduceTypes2.register_and_resolve(aVte, aClassStatement);
		}

		public ClassInvocation registerClassInvocation(final ClassInvocation aCi) {
			return deduceTypes2.phase.registerClassInvocation(aCi);
		}

		public ClassInvocation registerClassInvocation(final ClassStatement aClassStatement, final String constructorName) {
			return deduceTypes2.phase.registerClassInvocation(aClassStatement, constructorName);
		}

		public NamespaceInvocation registerNamespaceInvocation(final NamespaceStatement aNamespaceStatement) {
			return deduceTypes2.phase.registerNamespaceInvocation(aNamespaceStatement);
		}

		public void reportDiagnostic(final ResolveError aResolveError) {
			deduceTypes2.errSink.reportDiagnostic(aResolveError);
		}

		public GenType resolve_type(final OS_Type aTy, final Context aCtx) throws ResolveError {
			return deduceTypes2.resolve_type(aTy, aCtx);
		}

		public void resolveIdentIA_(final Context aCtx, final IdentIA aIdentIA, final BaseEvaFunction aGeneratedFunction, final FoundElement aFoundElement) {
			deduceTypes2.resolveIdentIA_(aCtx, aIdentIA, aGeneratedFunction, aFoundElement);
		}
	}

	public void onExitFunction(final @NotNull BaseEvaFunction generatedFunction, final Context aFd_ctx, final Context aContext) {
		//
		// resolve var table. moved from `E'
		//
		for (@NotNull VariableTableEntry vte : generatedFunction.vte_list) {
			vte.resolve_var_table_entry_for_exit_function();
		}
		for (@NotNull Runnable runnable : onRunnables) {
			runnable.run();
		}
//					LOG.info("167 "+generatedFunction);
		//
		// ATTACH A TYPE TO VTE'S
		// CONVERT USER TYPES TO USER_CLASS TYPES
		//
		for (final @NotNull VariableTableEntry vte : generatedFunction.vte_list) {
//                                              LOG.info("704 "+vte.type.attached+" "+vte.potentialTypes());
			final DeduceElement3_VariableTableEntry vte_de = (DeduceElement3_VariableTableEntry) vte.getDeduceElement3();
			vte_de.setDeduceTypes2(this, generatedFunction);
			vte_de.mvState(null, DeduceElement3_VariableTableEntry.ST.EXIT_CONVERT_USER_TYPES);
		}
		for (final @NotNull VariableTableEntry vte : generatedFunction.vte_list) {
			if (vte.vtt == VariableTableType.ARG) {
				final OS_Type attached = vte.type.getAttached();
				if (attached != null) {
					if (attached.getType() == OS_Type.Type.USER)
						//throw new AssertionError();
						errSink.reportError("369 ARG USER type (not deduced) " + vte);
				} else {
					errSink.reportError("457 ARG type not deduced/attached " + vte);
				}
			}
		}
		//
		// ATTACH A TYPE TO IDTE'S
		//
		for (@NotNull final IdentTableEntry ite : generatedFunction.idte_list) {
			final DeduceElement3_IdentTableEntry ite_de = (DeduceElement3_IdentTableEntry) ite.getDeduceElement3(this, generatedFunction);
			ite_de._ctxts(aFd_ctx, aContext);
			ite_de.mvState(null, DeduceElement3_IdentTableEntry.ST.EXIT_GET_TYPE);
		}
		{
			// TODO why are we doing this?
			final Resolve_each_typename ret = new Resolve_each_typename(phase, this, errSink);
			for (final TypeTableEntry typeTableEntry : generatedFunction.tte_list) {
				ret.action(typeTableEntry);
			}
		}
		{
			final @NotNull WorkManager  workManager = wm;//new WorkManager();
			@NotNull final Dependencies deps        = new Dependencies(/*this, *//*phase, this, errSink*/workManager);
			deps.subscribeTypes(generatedFunction.dependentTypesSubject());
			deps.subscribeFunctions(generatedFunction.dependentFunctionSubject());
//                                              for (@NotNull GenType genType : generatedFunction.dependentTypes()) {
//                                                      deps.action_type(genType, workManager);
//                                              }
//                                              for (@NotNull FunctionInvocation dependentFunction : generatedFunction.dependentFunctions()) {
//                                                      deps.action_function(dependentFunction, workManager);
//                                              }
			final int x = workManager.totalSize();

			// FIXME 06/14
			//workManager.drain();
		}

		//
		// RESOLVE FUNCTION RETURN TYPES
		//
		resolve_function_return_type(generatedFunction);
		{
			int y = 2;
			for (VariableTableEntry variableTableEntry : generatedFunction.vte_list) {
				final @NotNull Collection<TypeTableEntry> pot = variableTableEntry.potentialTypes();
				if (pot.size() == 1 && variableTableEntry.genType.isNull()) {
					final OS_Type x = pot.iterator().next().getAttached();
					if (x != null)
						if (x.getType() == OS_Type.Type.USER_CLASS) {
							try {
								final @NotNull GenType yy = resolve_type(x, aFd_ctx);
								// HACK TIME
								if (yy.resolved == null && yy.typeName.getType() == OS_Type.Type.USER_CLASS) {
									yy.resolved = yy.typeName;
									yy.typeName = null;
								}

								yy.genCIForGenType2(this);
								variableTableEntry.resolveType(yy);
								variableTableEntry.resolveTypeToClass(yy.node);
//								variableTableEntry.dlv.type.resolve(yy);
							} catch (ResolveError aResolveError) {
								aResolveError.printStackTrace();
							}
						}
				}
			}
		}
		//
		// LOOKUP FUNCTIONS
		//
		{
			@NotNull WorkList wl = new WorkList();

			for (@NotNull ProcTableEntry pte : generatedFunction.prte_list) {
				((DeduceElement3_ProcTableEntry) pte.getDeduceElement3(DeduceTypes2.this, generatedFunction))
						.lfoe_action(pte, DeduceTypes2.this, wl, (j) -> wm.addJobs(j));
			}

			wm.addJobs(wl);
			//wm.drain();
		}

		checkEvaClassVarTable(generatedFunction);

		expectations.check();

		phase.addActives(_actives);

		phase.addDrs(generatedFunction, generatedFunction.drs);
	}

	class df_helper_Constructors implements df_helper<EvaConstructor> {
		private final EvaClass evaClass;

		public df_helper_Constructors(EvaClass aEvaClass) {
			evaClass = aEvaClass;
		}

		@Override
		public @NotNull Collection<EvaConstructor> collection() {
			return evaClass.constructors.values();
		}

		@Override
		public boolean deduce(@NotNull EvaConstructor aEvaConstructor) {
			return deduceOneConstructor(aEvaConstructor, phase);
		}
	}

	class df_helper_Functions implements df_helper<EvaFunction> {
		private final EvaContainerNC generatedContainerNC;

		public df_helper_Functions(EvaContainerNC aGeneratedContainerNC) {
			generatedContainerNC = aGeneratedContainerNC;
		}

		@Override
		public @NotNull Collection<EvaFunction> collection() {
			return generatedContainerNC.functionMap.values();
		}

		@Override
		public boolean deduce(@NotNull EvaFunction aGeneratedFunction) {
			return deduceOneFunction(aGeneratedFunction, phase);
		}
	}

	class dfhi_constructors implements df_helper_i<EvaConstructor> {
		@Override
		public @Nullable df_helper_Constructors get(EvaContainerNC aGeneratedContainerNC) {
			if (aGeneratedContainerNC instanceof EvaClass) // TODO namespace constructors
				return new df_helper_Constructors((EvaClass) aGeneratedContainerNC);
			else
				return null;
		}
	}

	class dfhi_functions implements df_helper_i<EvaFunction> {
		@Override
		public @NotNull df_helper_Functions get(EvaContainerNC aGeneratedContainerNC) {
			return new df_helper_Functions(aGeneratedContainerNC);
		}
	}

	public class FoundParent implements BaseTableEntry.StatusListener {
		private final BaseTableEntry  bte;
		private final Context         ctx;
		private final BaseEvaFunction generatedFunction;
		private final IdentTableEntry ite;

		@Contract(pure = true)
		public FoundParent(BaseTableEntry aBte, IdentTableEntry aIte, Context aCtx, BaseEvaFunction aGeneratedFunction) {
			bte               = aBte;
			ite               = aIte;
			ctx               = aCtx;
			generatedFunction = aGeneratedFunction;
		}

		@Override
		public void onChange(IElementHolder eh, BaseTableEntry.Status newStatus) {
			if (newStatus == BaseTableEntry.Status.KNOWN) {
				if (bte instanceof final @NotNull VariableTableEntry vte) {
					onChangeVTE(vte);
				} else if (bte instanceof final @NotNull ProcTableEntry pte) {
					onChangePTE(pte);
				} else if (bte instanceof final @NotNull IdentTableEntry ite) {
					onChangeITE(ite);
				}
				postOnChange(eh);
			}
		}

		private void onChangeVTE(@NotNull VariableTableEntry vte) {
			@NotNull ArrayList<TypeTableEntry> pot = getPotentialTypesVte(vte);
			if (vte.getStatus() == BaseTableEntry.Status.KNOWN && vte.type.getAttached() != null && vte.getResolvedElement() != null) {

				final OS_Type ty = vte.type.getAttached();

				@Nullable OS_Element ele2 = null;

				try {
					if (ty.getType() == OS_Type.Type.USER) {
						@NotNull GenType ty2 = resolve_type(ty, ty.getTypeName().getContext());
						OS_Element       ele;
						if (vte.type.genType.resolved == null) {
							if (ty2.resolved.getType() == OS_Type.Type.USER_CLASS) {
								vte.type.genType.copy(ty2);
							}
						}
						ele = ty2.resolved.getElement();
						LookupResultList lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele.getContext(), DeduceTypes2.this);
						ele2 = lrl.chooseBest(null);
					} else
						ele2 = ty.getElement();

					if (ty instanceof OS_FuncType) {
						vte.typePromise().then(new DoneCallback<GenType>() {
							@Override
							public void onDone(final GenType result) {
								OS_Element                 ele3 = result.resolved.getClassOf();
								@Nullable LookupResultList lrl  = null;

								try {
									lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele3.getContext(), DeduceTypes2.this);
								} catch (ResolveError aResolveError) {
									aResolveError.printStackTrace();
									errSink.reportDiagnostic(aResolveError);
								}
								@Nullable OS_Element best = lrl.chooseBest(null);
								// README commented out because only firing for dir.listFiles, and we always use `best'
								//if (best != ele2) LOG.err(String.format("2824 Divergent for %s, %s and %s", ite, best, ele2));;
								ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolderWithType(best, ty, DeduceTypes2.this));
							}
						});
					} else {
						@Nullable LookupResultList lrl  = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele2.getContext(), DeduceTypes2.this);
						@Nullable OS_Element       best = lrl.chooseBest(null);
						// README commented out because only firing for dir.listFiles, and we always use `best'
//					if (best != ele2) LOG.err(String.format("2824 Divergent for %s, %s and %s", ite, best, ele2));;
						ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolderWithType(best, ty, DeduceTypes2.this));
					}
				} catch (ResolveError aResolveError) {
					aResolveError.printStackTrace();
					errSink.reportDiagnostic(aResolveError);
				}
			} else if (pot.size() == 1) {
				TypeTableEntry    tte = pot.get(0);
				@Nullable OS_Type ty  = tte.getAttached();
				if (ty != null) {
					switch (ty.getType()) {
					case USER:
						vte_pot_size_is_1_USER_TYPE(vte, ty);
						break;
					case USER_CLASS:
						vte_pot_size_is_1_USER_CLASS_TYPE(vte, ty);
						break;
					default:
						throw new Error();
					}
				} else {
					LOG.err("1696");
				}
			}
		}

		private void onChangePTE(@NotNull ProcTableEntry aPte) {
			if (aPte.getStatus() == BaseTableEntry.Status.KNOWN) { // TODO might be obvious
				if (aPte.getFunctionInvocation() != null) {
					FunctionInvocation fi = aPte.getFunctionInvocation();
					FunctionDef        fd = fi.getFunction();
					if (fd instanceof ConstructorDef) {
						fi.generateDeferred().done(new DoneCallback<BaseEvaFunction>() {
							@Override
							public void onDone(BaseEvaFunction result) {
								@NotNull EvaConstructor constructorDef = (EvaConstructor) result;

								@NotNull FunctionDef ele = constructorDef.getFD();

								try {
									LookupResultList     lrl  = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele.getContext(), DeduceTypes2.this);
									@Nullable OS_Element best = lrl.chooseBest(null);
									assert best != null;
									ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
								} catch (ResolveError aResolveError) {
									aResolveError.printStackTrace();
									errSink.reportDiagnostic(aResolveError);
								}
							}
						});
					}
				} else
					throw new NotImplementedException();
			} else {
				LOG.info("1621");
				@Nullable LookupResultList lrl = null;
				try {
					lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), ctx, DeduceTypes2.this);
					@Nullable OS_Element best = lrl.chooseBest(null);
					assert best != null;
					ite.setResolvedElement(best);
					found_element_for_ite(null, ite, best, ctx, central());
//						ite.setStatus(BaseTableEntry.Status.KNOWN, best);
				} catch (ResolveError aResolveError) {
					aResolveError.printStackTrace();
				}
			}
		}

		private void onChangeITE(@NotNull IdentTableEntry identTableEntry) {
			if (identTableEntry.type != null) {
				final OS_Type ty = identTableEntry.type.getAttached();

				@Nullable OS_Element ele2 = null;

				try {
					if (ty.getType() == OS_Type.Type.USER) {
						@NotNull GenType ty2 = resolve_type(ty, ty.getTypeName().getContext());
						OS_Element       ele;
						if (identTableEntry.type.genType.resolved == null) {
							if (ty2.resolved.getType() == OS_Type.Type.USER_CLASS) {
								identTableEntry.type.genType.copy(ty2);
							}
						}
						ele = ty2.resolved.getElement();
						LookupResultList lrl = DeduceLookupUtils.lookupExpression(this.ite.getIdent(), ele.getContext(), DeduceTypes2.this);
						ele2 = lrl.chooseBest(null);
					} else
						ele2 = ty.getClassOf(); // TODO might fail later (use getElement?)

					@Nullable LookupResultList lrl = null;

					lrl = DeduceLookupUtils.lookupExpression(this.ite.getIdent(), ele2.getContext(), DeduceTypes2.this);
					@Nullable OS_Element best = lrl.chooseBest(null);
					// README commented out because only firing for dir.listFiles, and we always use `best'
//					if (best != ele2) LOG.err(String.format("2824 Divergent for %s, %s and %s", identTableEntry, best, ele2));;
					this.ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
				} catch (ResolveError aResolveError) {
					aResolveError.printStackTrace();
					errSink.reportDiagnostic(aResolveError);
				}
			} else {
				if (!identTableEntry.fefi) {
					final Found_Element_For_ITE fefi = new Found_Element_For_ITE(generatedFunction, ctx, LOG, errSink, new DeduceClient1(DeduceTypes2.this), central());
					fefi.action(identTableEntry);
					identTableEntry.fefi = true;
					identTableEntry.onFefiDone(new DoneCallback<GenType>() {
						@Override
						public void onDone(final GenType result) {
							LookupResultList lrl = null;
							OS_Element       ele2;
							try {
								lrl  = DeduceLookupUtils.lookupExpression(ite.getIdent(), result.resolved.getClassOf().getContext(), DeduceTypes2.this);
								ele2 = lrl.chooseBest(null);

								if (ele2 != null) {
									ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(ele2));
									ite.resolveTypeToClass(result.node);

									if (ite.getCallablePTE() != null) {
										final @Nullable ProcTableEntry    pte        = ite.getCallablePTE();
										final @NotNull IInvocation        invocation = result.ci;
										final @NotNull FunctionInvocation fi         = newFunctionInvocation((BaseFunctionDef) ele2, pte, invocation, phase);

										generatedFunction.addDependentFunction(fi);
									}
								}
							} catch (ResolveError aResolveError) {
								aResolveError.printStackTrace();
							}
						}
					});
				}
				// TODO we want to setStatus but have no USER or USER_CLASS to perform lookup with
			}
		}

		/* @ensures ite.type != null; */
		private void postOnChange(@NotNull IElementHolder eh) {
			if (ite.type == null && eh.getElement() instanceof VariableStatementImpl) {
				@NotNull TypeName typ = ((VariableStatementImpl) eh.getElement()).typeName();
				@NotNull OS_Type  ty  = new OS_UserType(typ);

				try {
					@Nullable GenType ty2 = getTY2(typ, ty);

					// no expression or TableEntryIV below
					if (ty2 != null) {
						final @NotNull TypeTableEntry tte = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null);
						// trying to keep genType up to date

						if (!ty.getTypeName().isNull())
							tte.setAttached(ty);
						tte.setAttached(ty2);

						ite.type = tte;
						if (/*!ty.getTypeName().isNull() &&*/ !ty2.isNull()) {
							boolean skip = false;

							if (!ty.getTypeName().isNull()) {
								final TypeNameList gp = ((NormalTypeName) ty.getTypeName()).getGenericPart();
								if (gp != null) {
									if (gp.size() > 0 && ite.type.genType.nonGenericTypeName == null) {
										skip = true;
									}
								}
							}
							if (!skip)
								ite.type.genType.genCIForGenType2(DeduceTypes2.this);
						}
					}
				} catch (ResolveError aResolveError) {
					errSink.reportDiagnostic(aResolveError);
				}
			}
		}

		private void vte_pot_size_is_1_USER_TYPE(@NotNull VariableTableEntry vte, @Nullable OS_Type aTy) {
			try {
				@NotNull GenType ty2 = resolve_type(aTy, aTy.getTypeName().getContext());
				// TODO ite.setAttached(ty2) ??
				OS_Element           ele  = ty2.resolved.getElement();
				LookupResultList     lrl  = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele.getContext(), DeduceTypes2.this);
				@Nullable OS_Element best = lrl.chooseBest(null);
				ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
//									ite.setResolvedElement(best);

				final @NotNull ClassStatement klass = (ClassStatement) ele;

				register_and_resolve(vte, klass);
			} catch (ResolveError resolveError) {
				errSink.reportDiagnostic(resolveError);
			}
		}

		private void vte_pot_size_is_1_USER_CLASS_TYPE(@NotNull VariableTableEntry vte, @Nullable OS_Type aTy) {
			ClassStatement             klass = aTy.getClassOf();
			@Nullable LookupResultList lrl   = null;
			try {
				lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), klass.getContext(), DeduceTypes2.this);
				@Nullable OS_Element best = lrl.chooseBest(null);
//							ite.setStatus(BaseTableEntry.Status.KNOWN, best);
				assert best != null;
				ite.setResolvedElement(best);

				final @NotNull GenType          genType  = new GenType(klass);
				final TypeName                  typeName = vte.type.genType.nonGenericTypeName;
				final @Nullable ClassInvocation ci       = genType.genCI(typeName, DeduceTypes2.this, errSink, phase);
//							resolve_vte_for_class(vte, klass);
				ci.resolvePromise().done(new DoneCallback<EvaClass>() {
					@Override
					public void onDone(EvaClass result) {
						vte.resolveTypeToClass(result);
					}
				});
			} catch (ResolveError aResolveError) {
				errSink.reportDiagnostic(aResolveError);
			}
		}

		private @Nullable GenType getTY2(@NotNull TypeName aTyp, @NotNull OS_Type aTy) throws ResolveError {
			if (aTy.getType() != OS_Type.Type.USER) {
				assert false;
				@NotNull GenType genType = new GenType();
				genType.set(aTy);
				return genType;
			}

			@Nullable GenType ty2 = null;
			if (!aTyp.isNull()) {
				assert aTy.getTypeName() != null;
				ty2 = resolve_type(aTy, aTy.getTypeName().getContext());
				return ty2;
			}

			if (bte instanceof VariableTableEntry) {
				final OS_Type attached = ((VariableTableEntry) bte).type.getAttached();
				if (attached == null) {
					type_is_null_and_attached_is_null_vte();
					// ty2 will probably be null here
				} else {
					ty2 = new GenType();
					ty2.set(attached);
				}
			} else if (bte instanceof IdentTableEntry) {
				final TypeTableEntry tte = ((IdentTableEntry) bte).type;
				if (tte != null) {
					final OS_Type attached = tte.getAttached();

					if (attached == null) {
						type_is_null_and_attached_is_null_ite((IdentTableEntry) bte);
						// ty2 will be null here
					} else {
						ty2 = new GenType();
						ty2.set(attached);
					}
				}
			}

			return ty2;
		}

		private void type_is_null_and_attached_is_null_vte() {
			//LOG.err("2842 attached == null for "+((VariableTableEntry) bte).type);
			@NotNull PromiseExpectation<GenType> pe = promiseExpectation((VariableTableEntry) bte, "Null USER type attached resolved");
			VTE_TypePromises.found_parent(pe, generatedFunction, ((VariableTableEntry) bte), ite, DeduceTypes2.this);
		}

		private void type_is_null_and_attached_is_null_ite(final IdentTableEntry ite) {
			int                         y  = 2;
			PromiseExpectation<GenType> pe = promiseExpectation(ite, "Null USER type attached resolved");
//			ite.onType(phase, new OnType() {
//
//				@Override
//				public void typeDeduced(@NotNull OS_Type aType) {
//					// TODO Auto-generated method stub
//					pe.satisfy(aType);
//				}
//
//				@Override
//				public void noTypeFound() {
//					// TODO Auto-generated method stub
//
//				}
//			})
			//;.done(new DoneCallback<GenType>() {
//				@Override
//				public void onDone(GenType result) {
//					pe.satisfy(result);
//					final OS_Type attached1 = result.resolved != null ? result.resolved : result.typeName;
//					if (attached1 != null) {
//						switch (attached1.getType()) {
//						case USER_CLASS:
//							FoundParent.this.ite.type = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, attached1);
//							break;
//						case USER:
//							try {
//								OS_Type ty3 = resolve_type(attached1, attached1.getTypeName().getContext());
//								// no expression or TableEntryIV below
//								@NotNull TypeTableEntry tte4 = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null);
//								// README trying to keep genType up to date
//								tte4.setAttached(attached1);
//								tte4.setAttached(ty3);
//								FoundParent.this.ite.type = tte4; // or ty2?
//							} catch (ResolveError aResolveError) {
//								aResolveError.printStackTrace();
//							}
//							break;
//						}
//					}
//				}
//			});
		}
	}

	class Implement_Calls_ {
		private final Context             context;
		private final BaseEvaFunction     gf;
		private final InstructionArgument i2;
		private final int                 pc;
		private final ProcTableEntry      pte;

		public Implement_Calls_(final @NotNull BaseEvaFunction aGf,
								final @NotNull Context aContext,
								final @NotNull InstructionArgument aI2,
								final @NotNull ProcTableEntry aPte,
								final int aPc) {
			gf      = aGf;
			context = aContext;
			i2      = aI2;
			pte     = aPte;
			pc      = aPc;
		}

		void action() {
			final IExpression pn1 = pte.expression;
			if (!(pn1 instanceof IdentExpression)) {
				throw new IllegalStateException("pn1 is not IdentExpression");
			}

			final String pn    = ((IdentExpression) pn1).getText();
			boolean      found = lookup_name_calls(context, pn, pte);
			if (found) return;

			final @Nullable String pn2 = SpecialFunctions.reverse_name(pn);
			if (pn2 != null) {
//				LOG.info("7002 "+pn2);
				found = lookup_name_calls(context, pn2, pte);
				if (found) return;
			}

			if (i2 instanceof IntegerIA) {
				found = action_i2_IntegerIA(pn, pn2);
			} else {
				found = action_dunder(pn);
			}

			if (!found)
				pte.setStatus(BaseTableEntry.Status.UNKNOWN, null);
		}

		private boolean action_i2_IntegerIA(String pn, @Nullable String pn2) {
			boolean                           found;
			final @NotNull VariableTableEntry vte     = gf.getVarTableEntry(to_int(i2));
			final Context                     ctx     = gf.getContextFromPC(pc); // might be inside a loop or something
			final String                      vteName = vte.getName();
			if (vteName != null) {
				found = action_i2_IntegerIA_vteName_is_null(pn, pn2, ctx, vteName);
			} else {
				found = action_i2_IntegerIA_vteName_is_not_null(pn, pn2, vte);
			}
			return found;
		}

		private boolean action_dunder(String pn) {
			assert Pattern.matches("__[a-z]+__", pn);
//			LOG.info(String.format("i2 is not IntegerIA (%s)",i2.getClass().getName()));
			//
			// try to get dunder method from class
			//
			IExpression exp = pte.getArgs().get(0).expression;
			if (exp instanceof IdentExpression) {
				return action_dunder_doIt(pn, (IdentExpression) exp);
			}
			return false;
		}

		private boolean action_i2_IntegerIA_vteName_is_null(String pn, @Nullable String pn2, Context ctx, String vteName) {
			boolean found = false;
			if (SpecialVariables.contains(vteName)) {
				LOG.err("Skipping special variable " + vteName + " " + pn);
			} else {
				final LookupResultList lrl2 = ctx.lookup(vteName);
//				LOG.info("7003 "+vteName+" "+ctx);
				final @Nullable OS_Element best2 = lrl2.chooseBest(null);
				if (best2 != null) {
					found = lookup_name_calls(best2.getContext(), pn, pte);
					if (found) return true;

					if (pn2 != null) {
						found = lookup_name_calls(best2.getContext(), pn2, pte);
						if (found) return true;
					}

					errSink.reportError("Special Function not found " + pn);
				} else {
					throw new NotImplementedException(); // Cant find vte, should never happen
				}
			}
			return found;
		}

		private boolean action_i2_IntegerIA_vteName_is_not_null(String pn, @Nullable String pn2, @NotNull VariableTableEntry vte) {
			final @NotNull List<TypeTableEntry> tt = getPotentialTypesVte(vte);
			if (tt.size() != 1) {
				return false;
			}
			final OS_Type x = tt.get(0).getAttached();
			assert x != null;
			switch (x.getType()) {
			case USER_CLASS:
				pot_types_size_is_1_USER_CLASS(pn, pn2, x);
				return true;
			case BUILT_IN:
				final Context ctx2 = context;//x.getTypeName().getContext();
				try {
					@NotNull GenType ty2 = resolve_type(x, ctx2);
					pot_types_size_is_1_USER_CLASS(pn, pn2, ty2.resolved);
					return true;
				} catch (ResolveError resolveError) {
					resolveError.printStackTrace();
					errSink.reportDiagnostic(resolveError);
					return false;
				}
			default:
				assert false;
				return false;
			}
		}

		private boolean action_dunder_doIt(String pn, IdentExpression exp) {
			final @NotNull IdentExpression identExpression = exp;
			@Nullable InstructionArgument  vte_ia          = gf.vte_lookup(identExpression.getText());
			if (vte_ia != null) {
				VTE_TypePromises.dunder(pn, (IntegerIA) vte_ia, pte, DeduceTypes2.this);
				return true;
			}
			return false;
		}

		private void pot_types_size_is_1_USER_CLASS(String pn, @Nullable String pn2, OS_Type x) {
			boolean       found;
			final Context ctx1 = x.getClassOf().getContext();

			found = lookup_name_calls(ctx1, pn, pte);
			if (found) return;

			if (pn2 != null) {
				found = lookup_name_calls(ctx1, pn2, pte);
			}

			if (!found) {
				//throw new NotImplementedException(); // TODO
				errSink.reportError("Special Function not found " + pn);
			}
		}
	}

	public class PromiseExpectation<B> {

		private final ExpectationBase base;
		private final String          desc;
		private       boolean         _printed;
		private       long            counter;
		private       B               result;
		private       boolean         satisfied;

		public PromiseExpectation(ExpectationBase aBase, String aDesc) {
			base = aBase;
			desc = aDesc;
		}

		public void fail() {
			if (!_printed) {
				LOG.err(String.format("Expectation (%s, %d) not met: %s", DeduceTypes2.this, counter, desc));
				_printed = true;
			}
		}

		public boolean isSatisfied() {
			return satisfied;
		}

		public void satisfy(B aResult) {
			final String satisfied_already = satisfied ? " already" : "";
			//assert !satisfied;
			if (!satisfied) {
				result    = aResult;
				satisfied = true;
				LOG.info(String.format("Expectation (%s, %d)%s met: %s %s", DeduceTypes2.this, counter, satisfied_already, desc, base.expectationString()));
			}
		}

		public void setCounter(long aCounter) {
			counter = aCounter;

///////			LOG.info(String.format("Expectation (%s, %d) set: %s %s", DeduceTypes2.this, counter, desc, base.expectationString()));
		}
	}

	class PromiseExpectations {
		long counter = 0;

		@NotNull List<PromiseExpectation> exp = new ArrayList<>();

		public void add(@NotNull PromiseExpectation aExpectation) {
			counter++;
			aExpectation.setCounter(counter);
			exp.add(aExpectation);
		}

		public void check() {
			for (@NotNull PromiseExpectation promiseExpectation : exp) {
				if (!promiseExpectation.isSatisfied())
					promiseExpectation.fail();
			}
		}
	}

	class Resolve_each_typename {

		private final DeduceTypes2 dt2;
		private final ErrSink      errSink;
		private final DeducePhase  phase;

		public Resolve_each_typename(final DeducePhase aPhase, final DeduceTypes2 aDeduceTypes2, final ErrSink aErrSink) {
			phase   = aPhase;
			dt2     = aDeduceTypes2;
			errSink = aErrSink;
		}

		public void action(@NotNull final TypeTableEntry typeTableEntry) {
			@Nullable final OS_Type attached = typeTableEntry.getAttached();
			if (attached == null) return;
			if (attached.getType() == OS_Type.Type.USER) {
				action_USER(typeTableEntry, attached);
			} else if (attached.getType() == OS_Type.Type.USER_CLASS) {
				action_USER_CLASS(typeTableEntry, attached);
			}
		}

		public void action_USER(@NotNull final TypeTableEntry typeTableEntry, @Nullable final OS_Type aAttached) {
			final TypeName tn = aAttached.getTypeName();
			if (tn == null) return; // hack specifically for Result
			switch (tn.kindOfType()) {
			case FUNCTION:
			case GENERIC:
			case TYPE_OF:
				return;
			}
			try {
				typeTableEntry.setAttached(dt2.resolve_type(aAttached, aAttached.getTypeName().getContext()));
				switch (typeTableEntry.getAttached().getType()) {
				case USER_CLASS:
					action_USER_CLASS(typeTableEntry, typeTableEntry.getAttached());
					break;
				case GENERIC_TYPENAME:
					LOG.err(String.format("801 Generic Typearg %s for %s", tn, "genericFunction.getFD().getParent()"));
					break;
				default:
					LOG.err("245 typeTableEntry attached wrong type " + typeTableEntry);
					break;
				}
			} catch (final ResolveError aResolveError) {
				LOG.err("288 Failed to resolve type " + aAttached);
				errSink.reportDiagnostic(aResolveError);
			}
		}

		public void action_USER_CLASS(@NotNull final TypeTableEntry typeTableEntry, @NotNull final OS_Type aAttached) {
			final ClassStatement c = aAttached.getClassOf();
			assert c != null;
			phase.onClass(c, cc -> typeTableEntry.resolve(cc));
		}
	}

	class WlDeduceFunction implements WorkJob {
		private final List<BaseEvaFunction> coll;
		private final WorkJob               workJob;
		private       boolean               _isDone;

		public WlDeduceFunction(final WorkJob aWorkJob, List<BaseEvaFunction> aColl) {
			workJob = aWorkJob;
			coll    = aColl;
		}

		@Override
		public boolean isDone() {
			return _isDone;
		}

		@Override
		public void run(final WorkManager aWorkManager) {
			// TODO assumes result is in the same file as this (DeduceTypes2)

			if (workJob instanceof WlGenerateFunction) {
				final EvaFunction generatedFunction1 = ((WlGenerateFunction) workJob).getResult();
				if (!coll.contains(generatedFunction1)) {
					coll.add(generatedFunction1);
					if (!generatedFunction1.deducedAlready) {
						deduce_generated_function(generatedFunction1);
					}
					generatedFunction1.deducedAlready = true;
				}
			} else if (workJob instanceof WlGenerateDefaultCtor) {
				final EvaConstructor evaConstructor = (EvaConstructor) ((WlGenerateDefaultCtor) workJob).getResult();
				if (!coll.contains(evaConstructor)) {
					coll.add(evaConstructor);
					if (!evaConstructor.deducedAlready) {
						deduce_generated_constructor(evaConstructor);
					}
					evaConstructor.deducedAlready = true;
				}
			} else if (workJob instanceof WlGenerateCtor) {
				final EvaConstructor evaConstructor = ((WlGenerateCtor) workJob).getResult();
				if (!coll.contains(evaConstructor)) {
					coll.add(evaConstructor);
					if (!evaConstructor.deducedAlready) {
						deduce_generated_constructor(evaConstructor);
					}
					evaConstructor.deducedAlready = true;
				}
			} else
				throw new NotImplementedException();

			assert coll.size() == 1;

			_isDone = true;
		}
	}

	class Zero {
		final Map<Object, IDeduceElement3> l = new HashMap<>();

		public DeduceElement3_Function get(final DeduceTypes2 aDeduceTypes2, final BaseEvaFunction aGeneratedFunction) {
			if (l.containsKey(aGeneratedFunction)) {
				return (DeduceElement3_Function) l.get(aGeneratedFunction);
			}

			final DeduceElement3_Function de3 = new DeduceElement3_Function(DeduceTypes2.this, aGeneratedFunction);
			l.put(aGeneratedFunction, de3);
			return de3;
		}

		public DeduceElement3_ProcTableEntry get(final ProcTableEntry pte, final BaseEvaFunction aGeneratedFunction) {
			if (l.containsKey(pte)) {
				return (DeduceElement3_ProcTableEntry) l.get(pte);
			}

			final DeduceElement3_ProcTableEntry de3 = new DeduceElement3_ProcTableEntry(pte, DeduceTypes2.this, aGeneratedFunction);
			l.put(pte, de3);
			return de3;
		}

		public DeduceElement3_VariableTableEntry get(final VariableTableEntry vte, final BaseEvaFunction aGeneratedFunction) {
			if (l.containsKey(vte)) {
				return (DeduceElement3_VariableTableEntry) l.get(vte);
			}

			final DeduceElement3_VariableTableEntry de3 = new DeduceElement3_VariableTableEntry(vte, DeduceTypes2.this, aGeneratedFunction);
			l.put(vte, de3);
			return de3;
		}

		public DeduceElement3_IdentTableEntry getIdent(final IdentTableEntry ite, final BaseEvaFunction aGeneratedFunction, final DeduceTypes2 aDeduceTypes2) {
			if (l.containsKey(ite)) {
				return (DeduceElement3_IdentTableEntry) l.get(ite);
			}

			final DeduceElement3_IdentTableEntry de3 = new DeduceElement3_IdentTableEntry(ite);
			de3.setDeduceTypes(aDeduceTypes2, aGeneratedFunction);
			l.put(ite, de3);
			return de3;
		}
	}

	public void activePTE(@NotNull ProcTableEntry pte, DeduceConstructStatement dcs, ClassInvocation classInvocation) {
		// TODO Auto-generated method stub
		_actives.add(new DE3_ActivePTE(pte, dcs, classInvocation));
	}

	interface DE3_Active extends Reactive {
	}

	class Dependencies {
		final WorkList    wl = new WorkList();
		final WorkManager wm;

		Dependencies(final WorkManager aWm) {
			wm = aWm;
		}

		public void subscribeFunctions(final Subject<FunctionInvocation> aDependentFunctionSubject) {
			aDependentFunctionSubject.subscribe(new Observer<FunctionInvocation>() {
				@Override
				public void onSubscribe(@NonNull final Disposable d) {

				}

				@Override
				public void onNext(@NonNull final FunctionInvocation aFunctionInvocation) {
					action_function(aFunctionInvocation);
				}

				@Override
				public void onError(@NonNull final Throwable e) {

				}

				@Override
				public void onComplete() {

				}
			});
		}

		public void action_function(@NotNull FunctionInvocation aDependentFunction) {
			final FunctionDef        function = aDependentFunction.getFunction();
			WorkJob                  gen;
			final @NotNull OS_Module mod;
			if (function == ConstructorDefImpl.defaultVirtualCtor) {
				ClassInvocation ci = aDependentFunction.getClassInvocation();
				if (ci == null) {
					NamespaceInvocation ni = aDependentFunction.getNamespaceInvocation();
					assert ni != null;
					mod = ni.getNamespace().getContext().module();

					ni.resolvePromise().then(new DoneCallback<EvaNamespace>() {
						@Override
						public void onDone(final EvaNamespace result) {
							result.dependentFunctions().add(aDependentFunction);
						}
					});
				} else {
					mod = ci.getKlass().getContext().module();
					ci.resolvePromise().then(new DoneCallback<EvaClass>() {
						@Override
						public void onDone(final EvaClass result) {
							result.dependentFunctions().add(aDependentFunction);
						}
					});
				}
				final @NotNull GenerateFunctions gf = getGenerateFunctions(mod);
				gen = new WlGenerateDefaultCtor(gf, aDependentFunction);
			} else {
				mod = function.getContext().module();
				final @NotNull GenerateFunctions gf = getGenerateFunctions(mod);
				gen = new WlGenerateFunction(gf, aDependentFunction, phase.codeRegistrar);
			}
			wl.addJob(gen);
			List<BaseEvaFunction> coll = new ArrayList<>();
			wl.addJob(new WlDeduceFunction(gen, coll));
			wm.addJobs(wl);
		}

		public void subscribeTypes(final Subject<GenType> aDependentTypesSubject) {
			aDependentTypesSubject.subscribe(new Observer<GenType>() {
				@Override
				public void onSubscribe(@NonNull final Disposable d) {

				}

				@Override
				public void onNext(final GenType aGenType) {
					action_type(aGenType);
				}

				@Override
				public void onError(final Throwable aThrowable) {

				}

				@Override
				public void onComplete() {

				}
			});
		}

		public void action_type(@NotNull GenType genType) {
			// TODO work this out further, maybe like a Deepin flavor
			if (genType.resolvedn != null) {
				@NotNull OS_Module               mod = genType.resolvedn.getContext().module();
				final @NotNull GenerateFunctions gf  = phase.generatePhase.getGenerateFunctions(mod);
				NamespaceInvocation              ni  = phase.registerNamespaceInvocation(genType.resolvedn);
				@NotNull WlGenerateNamespace     gen = new WlGenerateNamespace(gf, ni, phase.generatedClasses, phase.codeRegistrar);

				assert genType.ci == null || genType.ci == ni;
				genType.ci = ni;

				wl.addJob(gen);

				ni.resolvePromise().then(new DoneCallback<EvaNamespace>() {
					@Override
					public void onDone(final EvaNamespace result) {
						genType.node = result;
						result.dependentTypes().add(genType);
					}
				});
			} else if (genType.resolved != null) {
				if (genType.functionInvocation != null) {
					action_function(genType.functionInvocation);
					return;
				}

				final ClassStatement             c   = genType.resolved.getClassOf();
				final @NotNull OS_Module         mod = c.getContext().module();
				final @NotNull GenerateFunctions gf  = phase.generatePhase.getGenerateFunctions(mod);
				@Nullable ClassInvocation        ci;
				if (genType.ci == null) {
					ci = new ClassInvocation(c, null);
					ci = phase.registerClassInvocation(ci);

					genType.ci = ci;
				} else {
					assert genType.ci instanceof ClassInvocation;
					ci = (ClassInvocation) genType.ci;
				}

				final Promise<ClassDefinition, Diagnostic, Void> pcd = phase.generateClass(gf, ci);

				pcd.then(new DoneCallback<ClassDefinition>() {
					@Override
					public void onDone(final ClassDefinition result) {
						final EvaClass genclass = result.getNode();

						genType.node = genclass;
						genclass.dependentTypes().add(genType);
					}
				});
			}
			//
			wm.addJobs(wl);
		}
	}

	class DE3_ActivePTE implements DE3_Active {

		private @NotNull ProcTableEntry           pte;
		private          DeduceConstructStatement dcs;
		private          ClassInvocation          ci;
		private List<Reactivable> ables = new ArrayList<>();

		public DE3_ActivePTE(@NotNull ProcTableEntry pte,
							 DeduceConstructStatement dcs,
							 ClassInvocation classInvocation) {
			this.pte = pte;
			this.dcs = dcs;
			this.ci  = classInvocation;
		}

		@Override
		public void add(final Reactivable aReactivable) {
			ables.add(aReactivable);
		}

		@Override
		public <T> void addListener(final Consumer<T> t) {

		}

		@Override
		public void join(final ReactiveDimension aDimension) {
			if (aDimension instanceof DeducePhase) {
				int y = 2;
			}
			if (aDimension instanceof GenerateC) {
				pte.getClassInvocation().resolvePromise().then(node -> {
					int            y         = 2;
					GenerateC      dimension = (GenerateC) aDimension;
					GenerateResult x         = dimension.resultsFromNodes(List_of(node), new WorkManager(), dimension.resultSink);
					//generatePhase.
					for (Old_GenerateResultItem result : x.results()) {
						System.err.println(result);
					}
				});
			}
		}
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
