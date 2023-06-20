/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_c;

import org.apache.commons.lang3.tuple.Pair;
import org.jdeferred2.DoneCallback;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.i.CompilationEnclosure;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.lang.i.IdentExpression;
import tripleo.elijah.lang.i.RegularTypeName;
import tripleo.elijah.lang.impl.ConstructorDefImpl;
import tripleo.elijah.lang.types.OS_FuncType;
import tripleo.elijah.lang.types.OS_UserClassType;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.nextgen.query.Operation2;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.deduce.post_bytecode.IDeduceElement3;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.ProcIA;
import tripleo.elijah.util.NotImplementedException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static tripleo.elijah.stages.deduce.DeduceTypes2.to_int;
import static tripleo.elijah.util.Helpers.String_join;

/**
 * Created 1/9/21 7:12 AM
 */
public class CReference {
	private final GI_Repo _repo/* = new GI_Repo()*/;
	//
	//
	public String __cheat_ret;
	private List<String> args;
	private ArrayList<CR_ReferenceItem1> items;
	private String rtext = null;

	public GI_Repo _repo() {
		return _repo;
	}

	public void addRef(final Reference aR) {
		refs.add(aR);
	}

	void addRef(final String text, final Ref type) {
		refs.add(new Reference(text, type));
	}

	//
	//
	EvaClass _cheat = null;

	void addRef(final String text, final Ref type, final String aValue) {
		refs.add(new Reference(text, type, aValue));
	}

	/**
	 * Call before you call build
	 *
	 * @param sl3
	 */
	public void args(final List<String> sl3) {
		args = sl3;
	}

	@NotNull
	public String build() {
		final BuildState st = new BuildState();

		for (final Reference ref : refs) {
			switch (ref.type) {
			case LITERAL:
			case DIRECT_MEMBER:
			case INLINE_MEMBER:
			case MEMBER:
			case LOCAL:
			case FUNCTION:
			case PROPERTY_GET:
			case PROPERTY_SET:
			case CONSTRUCTOR:
				ref.buildHelper(st);
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + ref.type);
			}
//			sl.add(text);
		}
//		return Helpers.String_join("->", sl);

		final StringBuilder sb = st.sb;

		if (st.needs_comma && args != null && args.size() > 0)
			sb.append(", ");

		if (st.open) {
			if (args != null) {
				sb.append(String_join(", ", args));
			}
			sb.append(")");
		}

		return sb.toString();
	}

	List<Reference> refs;

	public void debugPath(final IdentIA identIA, final String aPath) {
		@NotNull final List<InstructionArgument> pl = _getIdentIAPathList(identIA);

		System.out.println("\\ 172-172-172-172-172 ---------------------------------------------");
		for (InstructionArgument instructionArgument : pl) {
			if (instructionArgument instanceof final ProcIA procIA) {
				System.out.println(procIA.getEntry().expression);
			} else if (instructionArgument instanceof final IdentIA argument) {
				System.out.println(argument.getEntry().getIdent().getText());
			} else if (instructionArgument instanceof final IntegerIA integerIA) {
				System.out.println(integerIA.getEntry().getName());
			}
		}
		System.out.println("- 172-172-172-172-172 ---------------------------------------------");
		System.out.printf("[%d][%s]%n", aPath.length(), aPath);
		System.out.println("/ 172-172-172-172-172 ---------------------------------------------");
	}

	public CReference(final GI_Repo aRepo, final CompilationEnclosure aCe) {
		_repo = aRepo;
	}

	@NotNull
	static List<InstructionArgument> _getIdentIAPathList(@NotNull InstructionArgument oo) {
		final List<InstructionArgument> s = new LinkedList<InstructionArgument>();
		while (oo != null) {
			if (oo instanceof IntegerIA) {
				s.add(0, oo);
				oo = null;
			} else if (oo instanceof IdentIA) {
				final IdentTableEntry ite1 = ((IdentIA) oo).getEntry();
				s.add(0, oo);
				oo = ite1.getBacklink();
			} else if (oo instanceof ProcIA) {
//				final ProcTableEntry prte = ((ProcIA)oo).getEntry();
				s.add(0, oo);
				oo = null;
			} else
				throw new IllegalStateException("Invalid InstructionArgument");
		}
		return s;
	}

	@NotNull
	private String getIdentIAPath__IdentIA(final @NotNull IdentIA ia2, final Generate_Code_For_Method.AOG aog, final String aValue, final List<InstructionArgument> s, final List<String> sl, final int i, final int sSize, final CR_ReferenceItem1 item, final IdentIA identIA) {
		String text;
		item.setType(InstructionArgument.t.IDENT);

		final IdentTableEntry idte = identIA.getEntry();
		final BaseEvaFunction gf   = identIA.gf;

		if (idte.__dt2 == null) {
			System.err.println("169169 ");
			//throw new AssertionError();
		}

		final Consumer<Reference> referenceConsumer = e -> {
			//refs.add(e);
			item.setReference(e);
		};
		final CRI_Ident criIdent = CRI_Ident.of(idte, gf);
		text = criIdent.getIdentIAPath(i, sSize, aog, sl, aValue, referenceConsumer, s, ia2, this, item);

		System.err.println("181181 " + text + " " + item.getText());
		//assert text != null;
		if (text == null) return "<<null 181>>";
		return text;
	}

	public String getIdentIAPath2(final @NotNull IdentIA ia2, final Generate_Code_For_Method.AOG aog, final String aValue) {
		final BaseEvaFunction           generatedFunction = ia2.gf;
		final List<InstructionArgument> s                 = _getIdentIAPathList(ia2);


		final List<String> texts = new ArrayList<>();

		for (InstructionArgument instructionArgument : s) {
			if (instructionArgument instanceof IntegerIA) {
				IntegerIA                         integerIA = (IntegerIA) instructionArgument;
				@NotNull final VariableTableEntry entry     = integerIA.getEntry();
				texts.add("vv" + entry.getName());
			} else if (instructionArgument instanceof IdentIA) {
				IdentIA                        identIA = (IdentIA) instructionArgument;
				@NotNull final IdentTableEntry entry   = identIA.getEntry();
				texts.add("vm" + entry.getIdent().getText());
			} else if (instructionArgument instanceof ProcIA) {
				ProcIA                        procIA = (ProcIA) instructionArgument;
				@NotNull final ProcTableEntry entry  = procIA.getEntry();
				texts.add("" + entry.getResolvedElement());
			}
		}

		return String_join("->", texts);
	}

	enum Connector {
		DBL_COLON, DOT, INVALID,
		POINTER, UNKNOWN
	}

	enum Ref {
		//  was:
		//	enum Ref {
		//		LOCAL, MEMBER, PROPERTY_GET, PROPERTY_SET, INLINE_MEMBER, CONSTRUCTOR, DIRECT_MEMBER, LITERAL, PROPERTY (removed), FUNCTION
		//	}


		CONSTRUCTOR {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				final String          text;
				final @NotNull String s = sb.toString();
				text    = String.format("%s(%s", ref.text, s);
				sb.open = false;
				if (!s.equals("")) sb.needs_comma = true;
				sb.appendText(text + ")", true);
			}
		},
		DIRECT_MEMBER {
			@Override
			public void buildHelper(final Reference ref, final @NotNull BuildState sb) {
				final String text;
				text = Emit.emit("/*124*/") + "vsc->vm" + ref.text;

				final StringBuilder sb1 = new StringBuilder();

				sb1.append(text);
				if (ref.value != null) {
					sb1.append(" = ");
					sb1.append(ref.value);
					sb1.append(";");
				}

				sb.appendText(sb1.toString(), false);
			}
		},
		FUNCTION {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				final String text;
				final String s = sb.toString();
				text    = String.format("%s(%s", ref.text, s);
				sb.open = true;
				if (!s.equals("")) sb.needs_comma = true;
				sb.appendText(text, true);
			}
		},
		INLINE_MEMBER {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				final String text = Emit.emit("/*219*/") + ".vm" + ref.text;
				sb.appendText(text, false);
			}
		},
		LITERAL {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				final String text = ref.text;
				sb.appendText(text, false);
			}
		},
		// https://www.baeldung.com/a-guide-to-java-enums
		LOCAL {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				final String text = "vv" + ref.text;
				sb.appendText(text, false);
			}
		},
		MEMBER {
			class Text implements GenerateC_Statement {
				private final Supplier<Boolean> sb;
				private final Supplier<String>  ss;
				private final String            text;

				public Text(String atext, Supplier<Boolean> asb, Supplier<String> ass) {
					text = atext;
					sb   = asb;
					ss   = ass;
				}

				@Override
				public String getText() {
					final StringBuilder sb1 = new StringBuilder();

					sb1.append("->vm" + text);

					if (sb.get()) {
						sb1.append(" = ");
						sb1.append(ss.get());
						sb1.append(";");
					}

					return text;
				}

				@Override
				public GCR_Rule rule() {
					return new GCR_Rule() {
						@Override
						public String text() {
							return "Ref MEMBER Text";
						}
					};
				}
			}

			@Override
			public void buildHelper(final Reference ref, final @NotNull BuildState sb) {
				final Text t = new Text(ref.text, () -> ref.value != null, () -> ref.value);

				sb.appendText(t.getText(), false);
			}
		},
		PROPERTY_GET {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				final String text;
				final String s = sb.toString();
				text    = String.format("%s%s)", ref.text, s);
				sb.open = false;
//				if (!s.equals(""))
				sb.needs_comma = false;
				sb.appendText(text, true);
			}
		},
		PROPERTY_SET {
			@Override
			public void buildHelper(final Reference ref, final BuildState sb) {
				final String text;
				final String s = sb.toString();
				text    = String.format("%s%s, %s);", ref.text, s, ref.value);
				sb.open = false;
//				if (!s.equals(""))
				sb.needs_comma = false;
				sb.appendText(text, true);
			}
		};

		public abstract void buildHelper(final Reference ref, final BuildState sb);
	}

	private final static class BuildState {
		boolean open = false, needs_comma = false;
		StringBuilder sb = new StringBuilder();

		public void appendText(final String text, final boolean erase) {
			if (erase)
				sb = new StringBuilder();

			sb.append(text);
		}

		@Override
		public String toString() {
			return sb.toString();
		}
		//ABOVE 3a
	}

	static class CR_ReferenceItem1 implements CR_ReferenceItem {
		private String                   arg;
		private Connector                connector;
		private GenerateC_Item           generateCItem;
		private InstructionArgument      instructionArgument;
		private Eventual<GenerateC_Item> previous;

		private Reference reference;

		private Operation2<EG_Statement> statement;

		private String                text;
		private InstructionArgument.t type;

		@Override
		public String getArg() {
			return arg;
		}

		@Override
		public Connector getConnector() {
			return connector;
		}

		@Override
		public GenerateC_Item getGenerateCItem() {
			return generateCItem;
		}

		@Override
		public InstructionArgument getInstructionArgument() {
			return instructionArgument;
		}

		@Override
		public Eventual<GenerateC_Item> getPrevious() {
			return previous;
		}

		@Override
		public Reference getReference() {
			return reference;
		}

		@Override
		public void setReference(Reference aReference) {
			reference = aReference;
		}

		@Override
		public String getText() {
			return text;
		}

		@Override
		public Operation2<EG_Statement> getStatement() {
			return statement;
		}

		@Override
		public void setStatement(Operation2<EG_Statement> aStatement) {
			statement = aStatement;
		}

		@Override
		public void setText(String aText) {
			text = aText;
		}

		@Override
		public void setPrevious(Eventual<GenerateC_Item> aPrevious) {
			previous = aPrevious;
		}

		@Override
		public void setInstructionArgument(InstructionArgument aInstructionArgument) {
			instructionArgument = aInstructionArgument;
		}

		@Override
		public void setGenerateCItem(GenerateC_Item aGenerateCItem) {
			generateCItem = aGenerateCItem;
		}

		@Override
		public void setConnector(Connector aConnector) {
			connector = aConnector;
		}

		@Override
		public void setArg(String aArg) {
			arg = aArg;
		}

		public InstructionArgument.t getType() {
			return type;
		}

		public void setType(final InstructionArgument.t aType) {
			type = aType;
		}
	}

	public void getIdentIAPath(final IdentIA aIa, final BaseEvaFunction aGf, final Generate_Code_For_Method.AOG aGet, final String aO) {
		getIdentIAPath(aIa, aGet, aO);
	}

	public String getIdentIAPath(final @NotNull IdentIA ia2, final Generate_Code_For_Method.AOG aog, final String aValue) {
		final BaseEvaFunction           generatedFunction = ia2.gf;
		final List<InstructionArgument> s                 = _getIdentIAPathList(ia2);
		refs = new ArrayList<Reference>(s.size());

		//
		//
		//
		//
		//
		//
		items = new ArrayList<CR_ReferenceItem1>(s.size());
		//
		//
		//
		//
		//
		//
		//

		//
		// TODO NOT LOOKING UP THINGS, IE PROPERTIES, MEMBERS
		//
		String             text = "";
		final List<String> sl   = new ArrayList<String>();
		for (int i = 0, sSize = s.size(); i < sSize; i++) {

			//
			//
			//
			//
			final CR_ReferenceItem1 item = new CR_ReferenceItem1();
			//
			//
			//
			//


			final InstructionArgument ia = s.get(i);

			item.setInstructionArgument(ia);

			if (ia instanceof IntegerIA) {
				text = getIdentIAPath__IntegerIA(generatedFunction, i, item, ia);
			} else if (ia instanceof final IdentIA identIA) {
				text = getIdentIAPath__IdentIA(ia2, aog, aValue, s, sl, i, sSize, item, identIA);
			} else if (ia instanceof ProcIA) {
				text = getIdentIAPath__ProcIA(item, (ProcIA) ia);
			} else {
				throw new NotImplementedException();
			}
			if (text != null)
				sl.add(text);


			//
			//
			//
			//
			//
			//
			items.add(item);
			//
			//
			//
			//
			//
			//
		}
		if (false) {//|| true || sl.size() < items.size()) {
			final List<String> itms = items.stream()
					.map(itm -> {
						var bs = new BuildState();
						itm.getReference().type.buildHelper(itm.getReference(), bs);
						return bs.sb.toString();
					})
					.collect(Collectors.toList());
			System.err.println("219219 " + items);
			rtext = String_join(".", itms);
		} else {
			rtext = String_join(".", sl);
		}
		return rtext;
	}

	static class Reference {
		final String text;
		final Ref    type;
		final String value;

		public Reference(final String aText, final Ref aType) {
			text  = aText;
			type  = aType;
			value = null;
		}

		public Reference(final String aText, final Ref aType, final String aValue) {
			text  = aText;
			type  = aType;
			value = aValue;
		}

		public void buildHelper(final BuildState st) {
			type.buildHelper(this, st);
		}
	}

	@NotNull
	private String getIdentIAPath__IntegerIA(final BaseEvaFunction generatedFunction, final int i, final CR_ReferenceItem1 item, final InstructionArgument ia) {
		String text;
		item.setType(InstructionArgument.t.VARIABLE);

		// should only be the first element if at all
		assert i == 0;
		final VariableTableEntry vte = generatedFunction.getVarTableEntry(to_int(ia));

		if (vte.getName().equals("a1")) {
			final GenType  gt1 = vte.genType;
			final GenType  gt2 = vte.type.genType;
			final EvaClass gc1 = (EvaClass) vte.genType.node;

			_cheat = gc1;

			// only gt1.node is not null

			//assert gc1.getCode() == 106 || gc1.getCode() == 105 || gc1.getCode() == 103;
			assert gc1.getName().equals("ConstString");

			// gt2

			assert gt2.resolvedn == null;
			assert gt2.typeName instanceof OS_UserType;
			assert gt2.nonGenericTypeName instanceof RegularTypeName;
			if (gc1.getCode() == 106) {
				assert gt2.resolved instanceof OS_FuncType; // wrong: should be usertype: EvaClass
			} else if (gc1.getCode() == 105) {
				assert gt2.resolved instanceof OS_UserClassType; // now good ?? :):)
			}
			assert ((ClassInvocation) gt2.ci).resolvePromise().isResolved();

			((ClassInvocation) gt2.ci).resolvePromise().then(gc -> { // wrong: should be ConstString
				if (gc1.getCode() == 106) {
					assert gc.getCode() == 102;
					assert gc.getKlass().getName().equals("Arguments");
				} else if (gc1.getCode() == 105) {
					assert gc.getCode() == 105;
					assert gc.getKlass().getName().equals("ConstString");
				}
			});

			assert gt2.functionInvocation == null;

			final int y = 2;
		}

		text = "vv" + vte.getName();
		addRef(vte.getName(), Ref.LOCAL);

		item.setReference(new Reference(vte.getName(), Ref.LOCAL));
		return text;
	}

	private String getIdentIAPath__ProcIA(final CR_ReferenceItem1 item, final ProcIA ia) {
		String text;
		item.setType(InstructionArgument.t.PROC);

		final GI_ProcIA pia = (GI_ProcIA) _repo.itemFor(ia);
		text = pia.getIdentIAPath(p -> {
			String e = p.getLeft();
			addRef(e, p.getRight());
			item.setReference(new Reference(e, p.getRight()));
		});
		return text;
	}

	public String getIdentIAPath_Proc(final @NotNull ProcTableEntry aPrte, final Consumer<Pair<String, Ref>> addRef) {
		final String[]           text = new String[1];
		final FunctionInvocation fi   = aPrte.getFunctionInvocation();

		if (fi == null) {
			tripleo.elijah.util.Stupidity.println_err_2("7777777777777777 fi getIdentIAPath_Proc " + aPrte);

			return null;//throw new IllegalStateException();
		}


		final BaseEvaFunction generated = fi.getGenerated();
		final IDeduceElement3 de_pte    = aPrte.getDeduceElement3();

		if (generated == null)
			throw new IllegalStateException();

		if (generated instanceof EvaConstructor) {
			NotImplementedException.raise();
			generated.onGenClass(genClass -> {
				final IdentExpression constructorName = generated.getFD().getNameNode();
				final String          constructorNameText;
				if (constructorName == ConstructorDefImpl.emptyConstructorName) {
					constructorNameText = "";
				} else {
					constructorNameText = constructorName.getText();
				}
				text[0] = String.format("ZC%d%s", genClass.getCode(), constructorNameText);
				addRef.accept(Pair.of(text[0], Ref.CONSTRUCTOR));
			});
			final EvaContainerNC genClass = (EvaContainerNC) generated.getGenClass();
			if (genClass == null) {
				final int y = 2;
				//generated.setClass(genClass);
			}
		} else {
			generated.onGenClass(genClass -> {
				text[0] = String.format("Z%d%s", genClass.getCode(), generated.getFD().getNameNode().getText());
				addRef.accept(Pair.of(text[0], Ref.FUNCTION));
			});
		}

		return text[0];
	}

	class Eventual<P> {
		private final DeferredObject<P, Diagnostic, Void> prom = new DeferredObject<>();

		void resolve(final P p) {
			prom.resolve(p);
		}

		void then(final DoneCallback<? super P> cb) {
			prom.then(cb);
		}
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
