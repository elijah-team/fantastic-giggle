/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_c;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.i.CompilationEnclosure;
import tripleo.elijah.comp.i.ErrSink;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.ConstructorDefImpl;
import tripleo.elijah.lang.types.OS_FuncExprType;
import tripleo.elijah.lang2.BuiltInTypes;
import tripleo.elijah.nextgen.model.SM_ClassDeclaration;
import tripleo.elijah.nextgen.model.SM_Node;
import tripleo.elijah.nextgen.reactive.ReactiveDimension;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_IdentTableEntry;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.gen_generic.*;
import tripleo.elijah.stages.gen_generic.pipeline_impl.GenerateResultSink;
import tripleo.elijah.stages.instructions.*;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.IFixedList;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkJob;
import tripleo.elijah.work.WorkList;
import tripleo.elijah.work.WorkManager;
import tripleo.elijah.world.i.LivingClass;
import tripleo.elijah.world.i.LivingNamespace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static tripleo.elijah.stages.deduce.DeduceTypes2.to_int;

/**
 * Created 10/8/20 7:13 AM
 */
public class GenerateC implements CodeGenerator, GenerateFiles, ReactiveDimension {
	private static final String PHASE = "GenerateC";
	final         GI_Repo      _repo = new GI_Repo();
	final CompilationEnclosure ce;
	final ErrSink errSink;
	final ElLog LOG;
	private final Zone         _zone = new Zone();
	public GenerateResultSink resultSink;
	private       GenC_FileGen _fileGen;

	static boolean isValue(BaseEvaFunction gf, @NotNull String name) {
		if (!name.equals("Value")) return false;
		//
		FunctionDef fd = (FunctionDef) gf.getFD();
		switch (fd.getSpecies()) {
		case REG_FUN:
		case DEF_FUN:
			if (!(fd.getParent() instanceof ClassStatement)) return false;
			for (AnnotationPart anno : ((ClassStatement) fd.getParent()).annotationIterable()) {
				if (anno.annoClass().equals(Helpers.string_to_qualident("Primitive"))) {
					return true;
				}
			}
			return false;
		case PROP_GET:
		case PROP_SET:
			return true;
		default:
			throw new IllegalStateException("Unexpected value: " + fd.getSpecies());
		}
	}

	private void generate_class(final @NotNull GenC_FileGen aFileGen, final EvaClass aEvaClass) {
		generate_class(aEvaClass, aFileGen.gr(), aFileGen.resultSink());
	}

	@Override
	public void generate_namespace(final @NotNull EvaNamespace x, final GenerateResult gr, final @NotNull GenerateResultSink aResultSink) {
		final LivingNamespace ln = aResultSink.getLivingNamespaceForEva(x); // TODO could also add _living property
		ln.garish(this, gr, aResultSink);
	}

	@NotNull
	List<String> getArgumentStrings(final BaseEvaFunction gf, final Instruction instruction) {
		final List<String> sl3       = new ArrayList<String>();
		final int          args_size = instruction.getArgsSize();
		for (int i = 1; i < args_size; i++) {
			final InstructionArgument ia = instruction.getArg(i);
			if (ia instanceof IntegerIA) {
//				VariableTableEntry vte = gf.getVarTableEntry(DeduceTypes2.to_int(ia));
				final String realTargetName = getRealTargetName(gf, (IntegerIA) ia, Generate_Code_For_Method.AOG.GET);
				sl3.add(Emit.emit("/*669*/") + realTargetName);
			} else if (ia instanceof IdentIA) {
				final CReference reference = new CReference(_repo, ce);
				reference.getIdentIAPath((IdentIA) ia, Generate_Code_For_Method.AOG.GET, null);
				String text = reference.build();
				sl3.add(Emit.emit("/*673*/") + text);
			} else if (ia instanceof final ConstTableIA c) {
				ConstantTableEntry cte = gf.getConstTableEntry(c.getIndex());
				String             s   = new GetAssignmentValue().const_to_string(cte.initialValue);
				sl3.add(s);
				int y = 2;
			} else if (ia instanceof ProcIA) {
				LOG.err("740 ProcIA");
				throw new NotImplementedException();
			} else {
				LOG.err(ia.getClass().getName());
				throw new IllegalStateException("Invalid InstructionArgument");
			}
		}
		return sl3;
	}

	public GenerateC(@NotNull OS_Module aM, ErrSink aErrSink, ElLog.Verbosity verbosity, final CompilationEnclosure aCe) {
		errSink = aErrSink;
		LOG     = new ElLog(aM.getFileName(), verbosity, PHASE);
		//
		ce = aCe;
		ce.getPipelineLogic().addLog(LOG);
		//
		ce.addReactiveDimension(this);
	}

	public GenerateC(final @NotNull OutputFileFactoryParams aParams) {
		errSink = aParams.getErrSink();

		final OS_Module       mod       = aParams.getMod();
		final ElLog.Verbosity verbosity = aParams.getVerbosity();

		LOG = new ElLog(mod.getFileName(), verbosity, PHASE);

		ce = aParams.getCompilationEnclosure();
		ce.getAccessBusPromise()
				.then(ab -> {
					ab.subscribePipelineLogic(pl -> pl.addLog(LOG));
				});

		ce.addReactiveDimension(this);
	}

	@Override
	public void forNode(final SM_Node aNode) {
		final int y = 2;
		if (aNode instanceof final SM_ClassDeclaration classDecl) {
			//			return classDecl;
		}
//		return null;
	}

	@Override
	public void generate_class(EvaClass x, GenerateResult gr, final @NotNull GenerateResultSink aResultSink) {
		final LivingClass lc = aResultSink.getLivingClassForEva(x); // TODO could also add _living property
		lc.getGarish().garish(this, gr, aResultSink);
	}

	String getRealTargetName(final @NotNull BaseEvaFunction gf, final @NotNull IntegerIA target, final Generate_Code_For_Method.AOG aog) {
		final VariableTableEntry varTableEntry = gf.getVarTableEntry(target.getIndex());
		return getRealTargetName(gf, varTableEntry);
	}

	/*static*/ String getRealTargetName(final BaseEvaFunction gf, final VariableTableEntry varTableEntry) {

		ZoneVTE zone_vte = _zone.get(varTableEntry, gf);

		return zone_vte.getRealTargetName();

	}	@Override
	public void generate_constructor(EvaConstructor aEvaConstructor, GenerateResult gr, WorkList wl, final GenerateResultSink aResultSink) {
		generateCodeForConstructor(aEvaConstructor, gr, wl);
		for (IdentTableEntry identTableEntry : aEvaConstructor.idte_list) {

			identTableEntry.reactive().addResolveListener(this::generateIdent);

			if (identTableEntry.isResolved()) {
				generateIdent(identTableEntry);
			}
		}
		for (ProcTableEntry pte : aEvaConstructor.prte_list) {
//			ClassInvocation ci = pte.getClassInvocation();
			FunctionInvocation fi = pte.getFunctionInvocation();
			if (fi == null) {
				// TODO constructor
				int y = 2;
			} else {
				BaseEvaFunction gf = fi.getEva();
				if (gf != null) {
					wl.addJob(new WlGenerateFunctionC(gf, gr, wl, this, aResultSink));
				}
			}
		}
	}

	@Override
	public void generate_function(EvaFunction aEvaFunction, GenerateResult gr, WorkList wl, final GenerateResultSink aResultSink) {
		@NotNull final GenC_FileGen fileGen = getFileGen(gr, aResultSink, new WorkManager()); // FIXME 06/15

		generateCodeForMethod(fileGen, aEvaFunction);
		for (IdentTableEntry identTableEntry : aEvaFunction.idte_list) {
			if (identTableEntry.isResolved()) {
				EvaNode x = identTableEntry.resolvedType();

				if (x instanceof EvaClass) {
					generate_class(fileGen, (EvaClass) x);
				} else if (x instanceof EvaFunction) {
					wl.addJob(new WlGenerateFunctionC(fileGen, (EvaFunction) x));
				} else {
					LOG.err(x.toString());
					throw new NotImplementedException();
				}
			}
		}
		for (ProcTableEntry pte : aEvaFunction.prte_list) {
//			ClassInvocation ci = pte.getClassInvocation();
			FunctionInvocation fi = pte.getFunctionInvocation();
			if (fi == null) {
				// TODO constructor
				int y = 2;
/*
				if (pte.getClassInvocation() == null)
					assert pte.getStatus() == BaseTableEntry.Status.UNKNOWN;
*/
			} else {
				BaseEvaFunction gf = fi.getEva();
				if (gf != null) {
					wl.addJob(new WlGenerateFunctionC(gf, gr, wl, this, aResultSink));
				}
			}
		}
	}

	@NotNull
	List<String> getArgumentStrings(final @NotNull Supplier<IFixedList<InstructionArgument>> instructionSupplier) {
		final @NotNull List<String> sl3       = new ArrayList<String>();
		final int                   args_size = instructionSupplier.get().size();
		for (int i = 1; i < args_size; i++) {
			final InstructionArgument ia = instructionSupplier.get().get(i);
			if (ia instanceof IntegerIA) {
//				VariableTableEntry vte = gf.getVarTableEntry(DeduceTypes2.to_int(ia));
				final String realTargetName = getRealTargetName((IntegerIA) ia, Generate_Code_For_Method.AOG.GET);
				sl3.add(Emit.emit("/*669*/") + realTargetName);
			} else if (ia instanceof IdentIA) {
				final CReference reference = new CReference(_repo, ce);
				reference.getIdentIAPath((IdentIA) ia, Generate_Code_For_Method.AOG.GET, null);
				final String text = reference.build();
				sl3.add(Emit.emit("/*673*/") + text);
			} else if (ia instanceof final ConstTableIA c) {
				final ConstantTableEntry cte = c.getEntry();
				final String             s   = new GetAssignmentValue().const_to_string(cte.initialValue);
				sl3.add(s);
				final int y = 2;
			} else if (ia instanceof ProcIA) {
				LOG.err("740 ProcIA");
				throw new NotImplementedException();
			} else {
				LOG.err(ia.getClass().getName());
				throw new IllegalStateException("Invalid InstructionArgument");
			}
		}
		return sl3;
	}

	String getRealTargetName(final @NotNull IntegerIA target, final Generate_Code_For_Method.AOG aog) {
		final BaseEvaFunction    gf            = target.gf;
		final VariableTableEntry varTableEntry = gf.getVarTableEntry(target.getIndex());

		final ZoneVTE zone_vte = _zone.get(varTableEntry, gf);

		return zone_vte.getRealTargetName();
	}

	@Override
	public GenerateResult generateCode(final Collection<EvaNode> lgn, final WorkManager wm, final GenerateResultSink aResultSink) {
		GenerateResult gr = new Old_GenerateResult();
		WorkList       wl = new WorkList();

		for (final EvaNode evaNode : lgn) {
			if (evaNode instanceof final EvaFunction generatedFunction) {
				generate_function(generatedFunction, gr, wl, aResultSink);
				if (!wl.isEmpty())
					wm.addJobs(wl);
			} else if (evaNode instanceof final EvaContainerNC containerNC) {
				containerNC.generateCode(this, gr, aResultSink);
			} else if (evaNode instanceof final EvaConstructor evaConstructor) {
				generate_constructor(evaConstructor, gr, wl, aResultSink);
				if (!wl.isEmpty())
					wm.addJobs(wl);
			}
		}

		return gr;
	}

	private GenC_FileGen getFileGen(final GenerateResult aGr, final GenerateResultSink aResultSink, final WorkManager aWorkManager) {
		if (_fileGen == null) {
			_fileGen = new GenC_FileGen(aResultSink, aGr, aWorkManager, new WorkList(), this);
		} else {
			//throw new Error();
		}

		return _fileGen;
	}	private void generateCodeForConstructor(EvaConstructor gf, GenerateResult gr, WorkList aWorkList) {
		if (gf.getFD() == null) return;
		Generate_Code_For_Method gcfm = new Generate_Code_For_Method(this, LOG);
		gcfm.generateCodeForConstructor(gf, gr, aWorkList);
	}

	public void generateCodeForMethod(final GenC_FileGen aFileGen, final BaseEvaFunction aEvaFunction) {
		if (((BaseEvaFunction) aEvaFunction).getFD() == null) return;
		Generate_Code_For_Method gcfm = new Generate_Code_For_Method(this, LOG);
		gcfm.generateCodeForMethod(aEvaFunction, aFileGen);
	}

	public void generateIdent(@NotNull IdentTableEntry identTableEntry) {
		assert identTableEntry.isResolved();
		assert _fileGen != null;

		EvaNode x = identTableEntry.resolvedType();

		final GenerateResult     gr          = getFileGen(null, null).gr();
		final GenerateResultSink resultSink1 = getFileGen(null, null).resultSink();
		final WorkList           wl          = getFileGen(null, null).wl();

		if (x instanceof EvaClass) {
			generate_class((EvaClass) x, gr, resultSink1);
		} else if (x instanceof EvaFunction) {
			wl.addJob(new WlGenerateFunctionC((EvaFunction) x, gr, wl, this, resultSink1));
		} else {
			LOG.err(x.toString());
			throw new NotImplementedException();
		}
	}

	@NotNull
	public String getTypeName(EvaNode aNode) {
		if (aNode instanceof EvaClass)
			return getTypeName((EvaClass) aNode);
		if (aNode instanceof EvaNamespace)
			return getTypeName((EvaNamespace) aNode);
		throw new IllegalStateException("Must be class or namespace.");
	}

	String getTypeName(@NotNull EvaClass aEvaClass) {
		return GetTypeName.forGenClass(aEvaClass);
	}

	String getAssignmentValue(VariableTableEntry aSelf, Instruction aInstruction, ClassInvocation aClsinv, BaseEvaFunction gf) {
		GetAssignmentValue gav = new GetAssignmentValue();
		return gav.forClassInvocation(aInstruction, aClsinv, gf, LOG);
	}

	@NotNull
	String getAssignmentValue(VariableTableEntry value_of_this, final InstructionArgument value, final BaseEvaFunction gf) {
		GetAssignmentValue gav = new GetAssignmentValue();
		if (value instanceof final FnCallArgs fca) {
			return gav.FnCallArgs(fca, gf, LOG);
		}

		if (value instanceof final ConstTableIA constTableIA) {
			return gav.ConstTableIA(constTableIA, gf);
		}

		if (value instanceof final IntegerIA integerIA) {
			return gav.IntegerIA(integerIA, gf);
		}

		if (value instanceof final IdentIA identIA) {
			return gav.IdentIA(identIA, gf);
		}

		LOG.err(String.format("783 %s %s", value.getClass().getName(), value));
		return String.valueOf(value);
	}

	String getTypeName(@NotNull EvaNamespace aEvaNamespace) {
		return GetTypeName.forGenNamespace(aEvaNamespace);
	}

	@Deprecated
	String getTypeName(final @NotNull TypeName typeName) {
		return GetTypeName.forTypeName(typeName, errSink);
	}	public @NotNull GenC_FileGen getFileGen(final GenerateResultSink aGrs, final WorkManager aWm) {
		if (_fileGen == null) {
			_fileGen = new GenC_FileGen(aGrs, new Old_GenerateResult(), aWm, new WorkList(), this);
		}

		return _fileGen;
	}

	String getTypeName(@NotNull TypeTableEntry tte) {
		return GetTypeName.forTypeTableEntry(tte);
	}	@Override
	public GenC_FileGen getGen() {
		assert _fileGen != null;
		return _fileGen;
	}

	String getRealTargetName(final BaseEvaFunction gf, final IdentIA target, final Generate_Code_For_Method.AOG aog, final String value) {
		int                state           = 0, code = -1;
		IdentTableEntry    identTableEntry = gf.getIdentTableEntry(target.getIndex());
		LinkedList<String> ls              = new LinkedList<String>();
		// TODO in Deduce set property lookupType to denote what type of lookup it is: MEMBER, LOCAL, or CLOSURE
		InstructionArgument backlink = identTableEntry.getBacklink();
		final String        text     = identTableEntry.getIdent().getText();
		if (backlink == null) {
			if (identTableEntry.getResolvedElement() instanceof final VariableStatement vs) {
				OS_Element              parent = vs.getParent().getParent();
				if (parent != gf.getFD()) {
					// we want identTableEntry.resolved which will be a EvaMember
					// which will have a container which will be either be a function,
					// statement (semantic block, loop, match, etc) or a EvaContainerNC
					int     y  = 2;
					EvaNode er = identTableEntry.externalRef;
					if (er instanceof final EvaContainerNC nc) {
						assert nc instanceof EvaNamespace;
						EvaNamespace ns = (EvaNamespace) nc;
//						if (ns.isInstance()) {}
						state = 1;
						code  = nc.getCode();
					}
				}
			}
			switch (state) {
			case 0:
				ls.add(Emit.emit("/*912*/") + "vsc->vm" + text); // TODO blindly adding "vm" might not always work, also put in loop
				break;
			case 1:
				ls.add(Emit.emit("/*845*/") + String.format("zNZ%d_instance->vm%s", code, text));
				break;
			default:
				throw new IllegalStateException("Can't be here");
			}
		} else
			ls.add(Emit.emit("/*872*/") + "vm" + text); // TODO blindly adding "vm" might not always work, also put in loop
		while (backlink != null) {
			if (backlink instanceof final IntegerIA integerIA) {
				String    realTargetName = getRealTargetName(gf, integerIA, Generate_Code_For_Method.AOG.ASSIGN);
				ls.addFirst(Emit.emit("/*892*/") + realTargetName);
				backlink = null;
			} else if (backlink instanceof final IdentIA identIA) {
				int             identIAIndex        = identIA.getIndex();
				IdentTableEntry identTableEntry1    = gf.getIdentTableEntry(identIAIndex);
				String          identTableEntryName = identTableEntry1.getIdent().getText();
				ls.addFirst(Emit.emit("/*885*/") + "vm" + identTableEntryName); // TODO blindly adding "vm" might not always be right
				backlink = identTableEntry1.getBacklink();
			} else
				throw new IllegalStateException("Invalid InstructionArgument for backlink");
		}
		final CReference reference = new CReference(_repo, ce);
		reference.getIdentIAPath(target, aog, value);
		String path = reference.build();
		LOG.info("932 " + path);
		String s = Helpers.String_join("->", ls);
		LOG.info("933 " + s);
		if (identTableEntry.getResolvedElement() instanceof ConstructorDef || identTableEntry.getResolvedElement() instanceof PropertyStatement/* || value != null*/)
			return path;
		else
			return s;
	}

	String getTypeNameForVariableEntry(@NotNull VariableTableEntry input) {
		return GetTypeName.forVTE(input);
	}

	@NotNull
	public String getTypeNameGNCForVarTableEntry(EvaContainer.@NotNull VarTableEntry o) {
		final String typeName;
		if (o.resolvedType() != null) {
			EvaNode xx = o.resolvedType();
			if (xx instanceof EvaClass) {
				typeName = getTypeName((EvaClass) xx);
			} else if (xx instanceof EvaNamespace) {
				typeName = getTypeName((EvaNamespace) xx);
			} else
				throw new NotImplementedException();
		} else {
			if (o.varType != null)
				typeName = getTypeName(o.varType);
			else
				typeName = "void*/*null*/";
		}
		return typeName;
	}

	@Deprecated
	String getTypeName(final @NotNull OS_Type ty) {
		return GetTypeName.forOSType(ty, LOG);
	}

	public GI_Repo repo() {
		return _repo;
	}

	enum GetTypeName {
		;

		@Deprecated
		static String forOSType(final @NotNull OS_Type ty, ElLog LOG) {
			if (ty == null) throw new IllegalArgumentException("ty is null");
			//
			String z;
			switch (ty.getType()) {
			case USER_CLASS:
				final ClassStatement el = ty.getClassOf();
				final String name;
				if (ty instanceof NormalTypeName)
					name = ((NormalTypeName) ty).getName();
				else
					name = el.getName();
				z = Emit.emit("/*443*/") + String.format("Z%d/*%s*/", -4 /*el._a.getCode()*/, name);//.getName();
				break;
			case FUNCTION:
				z = "<function>";
				break;
			case FUNC_EXPR: {
				z = "<function>";
				OS_FuncExprType fe = (OS_FuncExprType) ty;
				int             y  = 2;
			}
			break;
			case USER:
				final TypeName typeName = ty.getTypeName();
				LOG.err("Warning: USER TypeName in GenerateC " + typeName);
				final String s = typeName.toString();
				if (s.equals("Unit"))
					z = "void";
				else
					z = String.format("Z<Unknown_USER_Type /*%s*/>", s);
				break;
			case BUILT_IN:
				LOG.err("Warning: BUILT_IN TypeName in GenerateC");
				z = "Z" + ty.getBType().getCode();  // README should not even be here, but look at .name() for other code gen schemes
				break;
			case UNIT_TYPE:
				z = "void";
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + ty.getType());
			}
			return z;
		}

		@Deprecated
		static String forTypeName(final @NotNull TypeName typeName, final @NotNull ErrSink errSink) {
			if (typeName instanceof RegularTypeName) {
				final String name = ((RegularTypeName) typeName).getName(); // TODO convert to Z-name

				return String.format("Z<%s>/*kklkl*/", name);
//			return getTypeName(new OS_UserType(typeName));
			}
			errSink.reportError("Type is not fully deduced " + typeName);
			return String.valueOf(typeName); // TODO type is not fully deduced
		}

		static String forTypeTableEntry(@NotNull TypeTableEntry tte) {
			EvaNode res = tte.resolved();
			if (res instanceof final EvaContainerNC nc) {
				int code = nc.getCode();
				return "Z" + code;
			} else
				return "Z<-1>";
		}

		static String forVTE(@NotNull VariableTableEntry input) {
			OS_Type attached = input.type.getAttached();
			if (attached == null)
				return Emit.emit("/*390*/") + "Z__Unresolved*"; // TODO remove this ASAP
			//
			// special case
			//
			if (input.type.genType.node != null)
				return Emit.emit("/*395*/") + getTypeNameForGenClass(input.type.genType.node) + "*";
			//
			if (input.getStatus() == BaseTableEntry.Status.UNCHECKED)
				return "Error_UNCHECKED_Type";
			if (attached.getType() == OS_Type.Type.USER_CLASS) {
				return attached.getClassOf().name();
			} else if (attached.getType() == OS_Type.Type.USER) {
				TypeName typeName = attached.getTypeName();
				String   name;
				if (typeName instanceof NormalTypeName)
					name = ((NormalTypeName) typeName).getName();
				else
					name = typeName.toString();
				return String.format(Emit.emit("/*543*/") + "Z<%s>*", name);
			} else
				throw new NotImplementedException();
		}

		static String getTypeNameForGenClass(@NotNull EvaNode aGenClass) {
			String ty;
			if (aGenClass instanceof EvaClass)
				ty = forGenClass((EvaClass) aGenClass);
			else if (aGenClass instanceof EvaNamespace)
				ty = forGenNamespace((EvaNamespace) aGenClass);
			else
				ty = "Error_Unknown_GenClass";
			return ty;
		}

		static String forGenClass(@NotNull EvaClass aEvaClass) {
			String z;
			z = String.format("Z%d", aEvaClass.getCode());
			return z;
		}

		static String forGenNamespace(@NotNull EvaNamespace aEvaNamespace) {
			String z;
			z = String.format("Z%d", aEvaNamespace.getCode());
			return z;
		}
	}

	static class WlGenerateFunctionC implements WorkJob {

		public final  GenerateResultSink resultSink;
		private final GenerateFiles      generateC;
		private final BaseEvaFunction    gf;
		private final GenerateResult     gr;
		private final WorkList           wl;
		private       boolean            _isDone = false;

		public WlGenerateFunctionC(BaseEvaFunction aGf, GenerateResult aGr, WorkList aWl, GenerateC aGenerateC, final GenerateResultSink aResultSink) {
			gf         = aGf;
			gr         = aGr;
			wl         = aWl;
			generateC  = aGenerateC;
			resultSink = aResultSink;
		}

		public WlGenerateFunctionC(GenC_FileGen fileGen, BaseEvaFunction aGf) {
			gf = aGf;

			gr         = fileGen.gr();
			wl         = fileGen.wl();
			generateC  = fileGen.generateC();
			resultSink = fileGen.resultSink();
		}

		@Override
		public boolean isDone() {
			return _isDone;
		}

		@Override
		public void run(WorkManager aWorkManager) {
			if (gf instanceof EvaFunction)
				generateC.generate_function((EvaFunction) gf, gr, wl, resultSink);
			else
				generateC.generate_constructor((EvaConstructor) gf, gr, wl, resultSink);
			_isDone = true;
		}
	}

	/*static*/  class GetAssignmentValue {

		public String ConstTableIA(ConstTableIA constTableIA, BaseEvaFunction gf) {
			final ConstantTableEntry cte = gf.getConstTableEntry(constTableIA.getIndex());
//			LOG.err(("9001-3 "+cte.initialValue));
			switch (cte.initialValue.getKind()) {
			case NUMERIC:
				return const_to_string(cte.initialValue);
			case STRING_LITERAL:
				return const_to_string(cte.initialValue);
			case IDENT:
				final String text = ((IdentExpression) cte.initialValue).getText();
				if (BuiltInTypes.isBooleanText(text))
					return text;
				else
					throw new NotImplementedException();
			default:
				throw new NotImplementedException();
			}
		}

		String const_to_string(final IExpression expression) {
			final GCX_ConstantString cs = new GCX_ConstantString(GenerateC.this,
																 GetAssignmentValue.this,
																 expression);

			return cs.getText();
		}

		public String FnCallArgs(FnCallArgs fca, BaseEvaFunction gf, ElLog LOG) {
			final StringBuilder sb   = new StringBuilder();
			final Instruction   inst = fca.getExpression();
//			LOG.err("9000 "+inst.getName());
			final InstructionArgument x = inst.getArg(0);
			assert x instanceof ProcIA;
			final ProcTableEntry pte = gf.getProcTableEntry(to_int(x));
//			LOG.err("9000-2 "+pte);
			switch (inst.getName()) {
			case CALL: {
				if (pte.expression_num == null) {
//					assert false; // TODO synthetic methods
					final IdentExpression ptex = (IdentExpression) pte.expression;
					sb.append(ptex.getText());
					sb.append(Emit.emit("/*671*/") + "(");

					final List<String> sll = getAssignmentValueArgs(inst, gf, LOG);
					sb.append(Helpers.String_join(", ", sll));

					sb.append(")");
				} else {
					if (pte.expression_num instanceof IntegerIA) {
					} else {
						final IdentIA         ia2  = (IdentIA) pte.expression_num;
						final IdentTableEntry idte = ia2.getEntry();
						if (idte.getStatus() == BaseTableEntry.Status.UNCHECKED) {

							final DeduceElement3_IdentTableEntry de3_idte = (DeduceElement3_IdentTableEntry) idte.getDeduceElement3(pte.getDeduceElement3().deduceTypes2(), ia2.gf);
							de3_idte.sneakResolve();

						}

						if (idte.getStatus() == BaseTableEntry.Status.KNOWN) {
							final CReference         reference          = new CReference(_repo, ce);
							final FunctionInvocation functionInvocation = pte.getFunctionInvocation();
							if (functionInvocation == null || functionInvocation.getFunction() == ConstructorDefImpl.defaultVirtualCtor) {
								reference.getIdentIAPath(ia2, Generate_Code_For_Method.AOG.GET, null);
								final List<String> sll = getAssignmentValueArgs(inst, gf, LOG);
								reference.args(sll);
								String path = reference.build();
								sb.append(Emit.emit("/*829*/") + path);
							} else {
								final BaseEvaFunction pte_generated = functionInvocation.getEva();
								if (idte.resolvedType() == null && pte_generated != null)
									idte.resolveTypeToClass(pte_generated);
								reference.getIdentIAPath(ia2, Generate_Code_For_Method.AOG.GET, null);
								final List<String> sll = getAssignmentValueArgs(inst, gf, LOG);
								reference.args(sll);
								String path = reference.build();
								sb.append(Emit.emit("/*827*/") + path);
							}
						} else {
							ZonePath zone_path = _zone.getPath(ia2);

							System.out.println("763 " + zone_path);

							final String path = gf.getIdentIAPathNormal(ia2);
							sb.append(Emit.emit("/*828*/") + String.format("%s is UNKNOWN", path));
						}
					}
				}
				return sb.toString();
			}
			case CALLS: {
				CReference reference = null;
				if (pte.expression_num == null) {
					final int             y    = 2;
					final IdentExpression ptex = (IdentExpression) pte.expression;
					sb.append(Emit.emit("/*684*/"));
					sb.append(ptex.getText());
				} else {
					// TODO Why not expression_num?
					reference = new CReference(_repo, ce);
					final IdentIA ia2 = (IdentIA) pte.expression_num;
					reference.getIdentIAPath(ia2, Generate_Code_For_Method.AOG.GET, null);
					final List<String> sll = getAssignmentValueArgs(inst, gf, LOG);
					reference.args(sll);
					String path = reference.build();
					sb.append(Emit.emit("/*807*/") + path);

					final IExpression ptex = pte.expression;
					if (ptex instanceof IdentExpression) {
						sb.append(Emit.emit("/*803*/"));
						sb.append(((IdentExpression) ptex).getText());
					} else if (ptex instanceof ProcedureCallExpression) {
						sb.append(Emit.emit("/*806*/"));
						sb.append(ptex.getLeft()); // TODO Qualident, IdentExpression, DotExpression
					}
				}
				if (true /*reference == null*/) {
					sb.append(Emit.emit("/*810*/") + "(");
					{
						final List<String> sll = getAssignmentValueArgs(inst, gf, LOG);
						sb.append(Helpers.String_join(", ", sll));
					}
					sb.append(");");
				}
				return sb.toString();
			}
			default:
				throw new IllegalStateException("Unexpected value: " + inst.getName());
			}
		}

		@NotNull
		List<String> getAssignmentValueArgs(final @NotNull Instruction inst, final BaseEvaFunction gf, ElLog LOG) {
			final int          args_size = inst.getArgsSize();
			final List<String> sll       = new ArrayList<String>();
			for (int i = 1; i < args_size; i++) {
				final InstructionArgument ia = inst.getArg(i);
				final int                 y  = 2;
//			LOG.err("7777 " +ia);
				if (ia instanceof ConstTableIA) {
					final ConstantTableEntry constTableEntry = gf.getConstTableEntry(((ConstTableIA) ia).getIndex());
					sll.add(const_to_string(constTableEntry.initialValue));
				} else if (ia instanceof IntegerIA) {
					final VariableTableEntry variableTableEntry = gf.getVarTableEntry(((IntegerIA) ia).getIndex());
					sll.add(Emit.emit("/*853*/") + _zone.get(variableTableEntry, gf).getRealTargetName());
				} else if (ia instanceof IdentIA) {
					String          path = gf.getIdentIAPathNormal((IdentIA) ia);    // return x.y.z
					IdentTableEntry ite  = gf.getIdentTableEntry(to_int(ia));
					if (ite.getStatus() == BaseTableEntry.Status.UNKNOWN) {
						sll.add(String.format("%s is UNKNOWN", path));
					} else {
						final CReference reference = new CReference(_repo, ce);
						reference.getIdentIAPath((IdentIA) ia, Generate_Code_For_Method.AOG.GET, null);
						String path2 = reference.build();                        // return ZP105get_z(vvx.vmy)
						if (path.equals(path2)) {
							// should always fail
							//throw new AssertionError();
							LOG.err(String.format("864 should always fail but didn't %s %s", path, path2));
						}
//					assert ident != null;
//					IdentTableEntry ite = gf.getIdentTableEntry(((IdentIA) ia).getIndex());
//					sll.add(Emit.emit("/*748*/")+""+ite.getIdent().getText());
						sll.add(Emit.emit("/*748*/") + path2);
						LOG.info("743 " + path2 + " " + path);
					}
				} else if (ia instanceof ProcIA) {
					LOG.err("863 ProcIA");
					throw new NotImplementedException();
				} else {
					throw new IllegalStateException("Cant be here: Invalid InstructionArgument");
				}
			}
			return sll;
		}

		public String forClassInvocation(Instruction aInstruction, ClassInvocation aClsinv, BaseEvaFunction gf, ElLog LOG) {
			int                     y         = 2;
			InstructionArgument     _arg0     = aInstruction.getArg(0);
			@NotNull ProcTableEntry pte       = gf.getProcTableEntry(((ProcIA) _arg0).getIndex());
			final CtorReference     reference = new CtorReference();
			reference.getConstructorPath(pte.expression_num, gf);
			@NotNull List<String> x = getAssignmentValueArgs(aInstruction, gf, LOG);
			reference.args(x);
			return reference.build(aClsinv);
		}

		public String IdentIA(IdentIA identIA, BaseEvaFunction gf) {
			assert gf == identIA.gf; // yup
			final CReference reference = new CReference(_repo, ce);
			reference.getIdentIAPath(identIA, Generate_Code_For_Method.AOG.GET, null);
			return reference.build();
		}

		public String IntegerIA(IntegerIA integerIA, BaseEvaFunction gf) {
			VariableTableEntry vte = gf.getVarTableEntry(integerIA.getIndex());
			String             x   = getRealTargetName(gf, vte);
			return x;
		}
	}





	String getTypeNameForGenClass(@NotNull EvaNode aGenClass) {
		return GetTypeName.getTypeNameForGenClass(aGenClass);
	}







	@Override
	public GenerateResult resultsFromNodes(final @NotNull List<EvaNode> aNodes, final WorkManager wm, final GenerateResultSink grs) {
		@NotNull final GenC_FileGen fg = getFileGen(grs, wm);

		final GenerateResult gr2 = fg.gr();

		for (final EvaNode generatedNode : aNodes) {
//                      if (generatedNode.module() != mod) continue; // README curious

			//GI_Item x = GI_Repo.get(generatedNode);

			if (generatedNode instanceof final EvaContainerNC nc) {

				nc.generateCode(this, gr2, grs);
				final @NotNull Collection<EvaNode> gn1 = (nc.functionMap.values()).stream().map(x -> (EvaNode) x).collect(Collectors.toList());
				final GenerateResult               gr3 = this.generateCode(gn1, wm, grs);
				grs.additional(gr3);
				final @NotNull Collection<EvaNode> gn2 = (nc.classMap.values()).stream().map(x -> (EvaNode) x).collect(Collectors.toList());
				final GenerateResult               gr4 = this.generateCode(gn2, wm, grs);
				grs.additional(gr4);
			} else {
				LOG.info("2009 " + generatedNode.getClass().getName());
			}
		}

		return gr2;
	}


	//String getRealTargetName(final BaseEvaFunction gf, final IntegerIA target, final Generate_Code_For_Method.AOG aog) {
	//	final VariableTableEntry varTableEntry = gf.getVarTableEntry(target.getIndex());
	//	return getRealTargetName(gf, varTableEntry);
	//}
	//String getRealTargetName(final BaseEvaFunction gf, final IntegerIA target, final Generate_Code_For_Method.AOG aog) {
	//	final VariableTableEntry varTableEntry = gf.getVarTableEntry(target.getIndex());
	//	return getRealTargetName(gf, varTableEntry);
	//}


}

//
//
//
