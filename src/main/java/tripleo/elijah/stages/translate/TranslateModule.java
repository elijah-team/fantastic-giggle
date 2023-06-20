//package tripleo.elijah.stages.translate;
//
//import tripleo.elijah.lang.i.*;
//import tripleo.elijah.lang.impl.AliasStatementImpl;
//import tripleo.elijah.lang.impl.VariableSequenceImpl;
//import tripleo.elijah.lang.impl.WithStatementImpl;
//import tripleo.elijah.lang2.BuiltInTypes;
//import tripleo.elijah.util.TabbedOutputStream;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
///**
// * Created 9/3/20 12:38 PM
// */
//public class TranslateModule {
//	private String getTypeString(final OS_Type type) {
//		switch (type.getType()) {
//		case BUILT_IN:
//			final BuiltInTypes bt = type.getBType();
//			return bt.name();
////				if (type.resolve())
////					return type.getClassOf().getName();
////				else
////					return "Unknown";
//		case USER:
//			return type.getTypeName().toString();
//		case USER_CLASS:
//			return type.getClassOf().getName();
//		case FUNCTION:
//			return "Function<>";
//		default:
//			throw new IllegalStateException("cant be here");
//		}
////		return type.toString();
//	}
//
//	private final OS_Module module;
//
//	public TranslateModule(final OS_Module module_) {
//		module = module_;
//	}
//
//	public void translate() {
////		TabbedOutputStream w = null;
//		//			w = getFile();
//
//		for (final ModuleItem item : module.getItems()) {
//			try {
//				if (item instanceof ClassStatement) {
//					put_class_statement((ClassStatement) item);
//				} else if (item instanceof NamespaceStatement) {
//					put_namespace_statement((NamespaceStatement) item);
//				} else
//					tripleo.elijah.util.Stupidity.println_out_2("8000 " + item);
//			} catch (final IOException e) {
//				module.getCompilation().getErrSink().exception(e);
//			}
//		}
//	}
//
//	private void put_class_statement(final ClassStatement item) throws IOException {
//		final String             cls_name = "C" + item.name();
//		final TabbedOutputStream w        = stream_for(item.getPackageName().getName(), cls_name);
//		try {
//			{
//				final String packageName = item.getPackageName().getName();
//				if (!packageName.equals("")) {
//					final String pkg_decl = "package " + packageName + ";";
//					w.put_string_ln(pkg_decl);
//					w.put_string_ln("");
//				}
//			}
//			w.put_string_ln("class " + cls_name + " {");
//			w.incr_tabs();
//			put_class_statement_internal(item, w);
//			w.dec_tabs();
//			w.put_string_ln("}");
//		} finally {
//			w.close();
//		}
//	}
//
//	private void put_class_statement_internal(final ClassStatement classStatement, final TabbedOutputStream w) throws IOException {
//		for (final ClassItem item : classStatement.getItems()) {
//			if (item instanceof FunctionDef) {
//				w.put_string("public void " + ((FunctionDef) item).name() + "(");
//				put_formal_arg_list(((FunctionDef) item).fal(), w);
//				w.put_string_ln(") {");
//				w.incr_tabs();
//				put_function_def((FunctionDef) item, w);
//				w.dec_tabs();
//				w.put_string_ln("}");
//			} else
//				tripleo.elijah.util.Stupidity.println_out_2("8001 " + item);
//		}
//	}
//
//	private void put_formal_arg_list(final FormalArgList fal, final TabbedOutputStream w) throws IOException {
//		for (final FormalArgListItem fali : fal.falis()) {
//			w.put_string(fali.typeName().toString());
//			w.put_string(" ");
//			w.put_string(fali.name());
//			w.put_string(",");
//		}
//	}
//
//	private void put_function_def(final FunctionDef functionDef, final TabbedOutputStream w) throws IOException {
//		for (final FunctionItem item : functionDef.getItems()) {
//			tripleo.elijah.util.Stupidity.println_out_2("8003 " + item);
//			if (item instanceof AliasStatementImpl) {
//
//			} else if (item instanceof CaseConditional) {
//
//			} else if (item instanceof ClassStatement) {
//
//			} else if (item instanceof StatementWrapper) {
//
//			} else if (item instanceof IfConditional) {
//
//			} else if (item instanceof Loop) {
//
//			} else if (item instanceof MatchConditional) {
//
//			} else if (item instanceof NamespaceStatement) {
//
//			} else if (item instanceof VariableSequenceImpl) {
//				for (final VariableStatement vs : ((VariableSequenceImpl) item).items()) {
//					tripleo.elijah.util.Stupidity.println_out_2("8004 " + vs);
//					final OS_Type type  = vs.initialValue().getType();
//					final String  stype = type == null ? "Unknown" : getTypeString(type);
//					tripleo.elijah.util.Stupidity.println_out_2("8004-1 " + type);
//					w.put_string_ln(String.format("%s %s;", stype, vs.getName()));
//				}
//			} else if (item instanceof WithStatementImpl) {
//
//			} else if (item instanceof SyntacticBlock) {
//
//			} else {
//				throw new IllegalStateException("cant be here");
//			}
//
//		}
//	}
//
//	private void put_namespace_statement(final NamespaceStatement item) throws IOException {
//		final StringBuilder ns_name_sb = new StringBuilder("NS_");
//		switch (item.getKind()) {
//		case NAMED:
//			ns_name_sb.append(item.name());
//			break;
//		case PRIVATE:
//			ns_name_sb.append("__private__");
//			break;
//		case MODULE:
//			ns_name_sb.append("__module__");
//			// TODO get a number for this, even if its %n_%n
//			break;
//		case PACKAGE:
//			ns_name_sb.append("__package__");
//			break;
//		}
//		final String             ns_name = ns_name_sb.toString();
//		final TabbedOutputStream w       = stream_for(item.getPackageName().getName(), ns_name);
//		try {
//			{
//				final String packageName = item.getPackageName().getName();
//				if (!packageName.equals("")) {
//					final String pkg_decl = "package " + packageName + ";";
//					w.put_string_ln(pkg_decl);
//					w.put_string_ln("");
//				}
//			}
//			w.put_string_ln("class " + ns_name + " {");
//			w.incr_tabs();
//			put_namespace_statement_internal(item, w);
//			w.dec_tabs();
//			w.put_string_ln("}");
//		} finally {
//			w.close();
//		}
//	}
//
//	private static TabbedOutputStream stream_for(final String packageName, final String name) throws FileNotFoundException {
//		String packageDirName = packageName.replace('.', '/');
//		if (packageDirName.equals(""))
//			packageDirName = ".";
//		final File dir = new File("output", packageDirName);
//		dir.mkdirs();
//		final File               file = new File(dir, name + ".java");
//		final FileOutputStream   os   = new FileOutputStream(file);
//		final TabbedOutputStream R    = new TabbedOutputStream(os);
//		return R;
//	}
//
//	private void put_namespace_statement_internal(final NamespaceStatement namespaceStatement, final TabbedOutputStream w) {
//		for (final ClassItem item : namespaceStatement.getItems()) {
//			tripleo.elijah.util.Stupidity.println_out_2("8002 " + item);
//		}
//	}
//
//	/*private TabbedOutputStream getFile() {
//		final String fn = module.getFileName();
//		final String fn2 = fn.substring(0, fn.lastIndexOf('.'));
//		final String fn3 = fn2 + ".java";
//		final Path p = Path.of("output", fn3);
//		p.getParent().toFile().mkdirs();
//		tripleo.elijah.util.Stupidity.println_err_2("PATH "+p.toString());
//		TabbedOutputStream w = null;
//		try {
//			w = new TabbedOutputStream(Files.newOutputStream(p));
//			return w;
//		} catch (final IOException e) {
//			module.getCompilation().eee.exception(e);
//		}
//		return null;
//	}*/
//}
