/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Operation;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.*;
import tripleo.elijah.lang.types.OS_BuiltinType;
import tripleo.elijah.lang.types.OS_GenericTypeNameType;
import tripleo.elijah.lang.types.OS_UnknownType;
import tripleo.elijah.lang.types.OS_UserClassType;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.stages.deduce.*;
import tripleo.elijah.stages.gen_generic.CodeGenerator;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.pipeline_impl.GenerateResultSink;
import tripleo.elijah.stages.post_deduce.IPostDeduce;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.world.impl.DefaultLivingClass;

import java.util.*;

/**
 * Created 10/29/20 4:26 AM
 */
public class EvaClass extends EvaContainerNC implements GNCoded {
	public        DefaultLivingClass                  _living;
	private final ClassStatement                      klass;
	private final OS_Module                           module;
	public        ClassInvocation                     ci;
	public        Map<ConstructorDef, EvaConstructor> constructors                      = new HashMap<ConstructorDef, EvaConstructor>();
	private       boolean                             resolve_var_table_entries_already = false;

	public EvaClass(ClassStatement aClassStatement, OS_Module aModule) {
		klass  = aClassStatement;
		module = aModule;
	}

	public void addAccessNotation(AccessNotation an) {
		throw new NotImplementedException();
	}

	public void addConstructor(ConstructorDef aConstructorDef, @NotNull EvaConstructor aGeneratedFunction) {
		constructors.put(aConstructorDef, aGeneratedFunction);
	}

	@Override
	public void analyzeNode(@NotNull IPostDeduce aPostDeduce) {
		aPostDeduce.analyze_class(this);
	}

	public void createCtor0() {
		// TODO implement me
		FunctionDef fd = new FunctionDefImpl(klass, klass.getContext());
		fd.setName(Helpers.string_to_ident("<ctor$0>"));
		Scope3 scope3 = new Scope3Impl(fd);
		fd.scope(scope3);
		for (VarTableEntry varTableEntry : varTable) {
			if (varTableEntry.initialValue != IExpression.UNASSIGNED) {
				IExpression left  = varTableEntry.nameToken;
				IExpression right = varTableEntry.initialValue;

				IExpression e = ExpressionBuilder.build(left, ExpressionKind.ASSIGNMENT, right);
				scope3.add(new StatementWrapperImpl(e, fd.getContext(), fd));
			} else {
				if (getPragma("auto_construct")) {
					scope3.add(new ConstructStatementImpl(fd, fd.getContext(), varTableEntry.nameToken, null, null));
				}
			}
		}
	}

	public void fixupUserClasses(final DeduceTypes2 aDeduceTypes2, final Context aContext) {
		for (VarTableEntry varTableEntry : varTable) {
			varTableEntry.updatePotentialTypesCB = new VarTableEntry.UpdatePotentialTypesCB() {
				@Override
				public Operation<Boolean> call(final @NotNull EvaContainer aEvaContainer) {
					Operation<List<GenType>> potentialTypes00 = getPotentialTypes();

					assert potentialTypes00.mode() == Mode.SUCCESS;

					List<GenType> potentialTypes = getPotentialTypes().success();
					//

					//
					// HACK TIME
					//
					if (potentialTypes.size() == 2) {
						final ClassStatement resolvedClass1 = potentialTypes.get(0).resolved.getClassOf();
						final ClassStatement resolvedClass2 = potentialTypes.get(1).resolved.getClassOf();
						final OS_Module      prelude;
						if (potentialTypes.get(1).resolved instanceof OS_BuiltinType && potentialTypes.get(0).resolved instanceof OS_UserClassType) {
							OS_BuiltinType resolved = (OS_BuiltinType) potentialTypes.get(1).resolved;

							try {
								@NotNull final GenType rt = ResolveType.resolve_type(resolvedClass1.getContext().module(), resolved, resolvedClass1.getContext(), aDeduceTypes2._LOG(), aDeduceTypes2);
								int                    y  = 2;

								potentialTypes = Helpers.List_of(rt);
							} catch (ResolveError aE) {
								return Operation.failure(aE);
							}
						} else if (potentialTypes.get(0).resolved instanceof OS_BuiltinType && potentialTypes.get(1).resolved instanceof OS_UserClassType) {
							OS_BuiltinType resolved = (OS_BuiltinType) potentialTypes.get(0).resolved;

							try {
								@NotNull final GenType rt = aDeduceTypes2.resolve_type(resolved, resolvedClass2.getContext());
								int                    y  = 2;

								potentialTypes = Helpers.List_of(rt);
							} catch (ResolveError aE) {
								return Operation.failure(aE);
							}
						} else {

							prelude = resolvedClass1.getContext().module().prelude();

							// TODO might not work when we split up prelude
							//  Thats why I was testing for package name before
							if (resolvedClass1.getContext().module() == prelude
									&& resolvedClass2.getContext().module() == prelude) {
								// Favor String over ConstString
								if (resolvedClass1.name().equals("ConstString") && resolvedClass2.name().equals("String")) {
									potentialTypes.remove(0);
								} else if (resolvedClass2.name().equals("ConstString") && resolvedClass1.name().equals("String")) {
									potentialTypes.remove(1);
								}
							}
						}
					}

					if (potentialTypes.size() == 1) {
						if (ci.genericPart().hasGenericPart()) {
							final OS_Type t = varTableEntry.varType;
							if (t.getType() == OS_Type.Type.USER) {
								try {
									final @NotNull GenType genType = aDeduceTypes2.resolve_type(t, t.getTypeName().getContext());
									if (genType.resolved instanceof OS_GenericTypeNameType) {
										final ClassInvocation xxci = ((EvaClass) aEvaContainer).ci;

										var v = xxci.genericPart().valueForKey(t.getTypeName());
										if (v != null) {
											varTableEntry.varType = v;
										}

									}
								} catch (ResolveError aResolveError) {
									aResolveError.printStackTrace();
									//assert false;
									return Operation.failure(aResolveError);
								}
							}
						}
					}
					return Operation.success(true);
				}

				@NotNull
				public Operation<List<GenType>> getPotentialTypes() {
					List<GenType> potentialTypes = new ArrayList<>();
					for (TypeTableEntry potentialType : varTableEntry.potentialTypes) {
						int                    y = 2;
						final @NotNull GenType genType;
						try {
							if (potentialType.genType.typeName == null) {
								final OS_Type attached = potentialType.getAttached();
								if (attached == null) continue;

								genType = aDeduceTypes2.resolve_type(attached, aContext);
								if (genType.resolved == null && genType.typeName.getType() == OS_Type.Type.USER_CLASS) {
									genType.resolved = genType.typeName;
									genType.typeName = null;
								}
							} else {
								if (potentialType.genType.resolved == null && potentialType.genType.resolvedn == null) {
									final OS_Type attached = potentialType.genType.typeName;

									genType = aDeduceTypes2.resolve_type(attached, aContext);
								} else
									genType = potentialType.genType;
							}
							if (genType.typeName != null) {
								final TypeName typeName = genType.typeName.getTypeName();
								if (typeName instanceof NormalTypeName) {
									final TypeNameList genericPart = ((NormalTypeName) typeName).getGenericPart();
									if (genericPart != null && genericPart.size() > 0) {
										genType.nonGenericTypeName = typeName;
									}
								}
							}
							genType.genCIForGenType2(aDeduceTypes2);
							potentialTypes.add(genType);
						} catch (ResolveError aResolveError) {
							aResolveError.printStackTrace();
							return Operation.failure(aResolveError);
						}
					}
					//
					Set<GenType> set = new HashSet<>(potentialTypes);
//					final Set<GenType> s = Collections.unmodifiableSet(set);
					return Operation.success(new ArrayList<>(set));
				}
			};
			/*=======================================*/
			/*=======================================*/
			/*=======================================*/
			/*=======================================*/
			/*=======================================*/
			/*=======================================*/
			/*=======================================*/
			/*=======================================*/
			/*=======================================*/
			/*=======================================*/
			/*=======================================*/
			/*=======================================*/
			/*=======================================*/
			/*=======================================*/
			if (!varTableEntry.updatePotentialTypesCBPromise.isResolved()) {
				varTableEntry.updatePotentialTypesCBPromise.resolve(varTableEntry.updatePotentialTypesCB);
			}
		}
	}

	@Override
	public void generateCode(@NotNull CodeGenerator aCodeGenerator, GenerateResult aGr, final GenerateResultSink aResultSink) {
		aCodeGenerator.generate_class(this, aGr, aResultSink);
	}

	@Override
	public OS_Element getElement() {
		return getKlass();
	}

	public ClassStatement getKlass() {
		return this.klass;
	}

	@NotNull
	public String getName() {
		StringBuilder sb = new StringBuilder();
		sb.append(klass.getName());
		if (ci.genericPart().hasGenericPart()) {
			final Map<TypeName, OS_Type> map = ci.genericPart().getMap();

			if (map != null) {
				sb.append("[");
				final String joined = getNameHelper(map);
				sb.append(joined);
				sb.append("]");
			}
		}
		return sb.toString();
	}

	@NotNull
	private String getNameHelper(Map<TypeName, OS_Type> aGenericPart) {
		List<String> ls = new ArrayList<String>();
		for (Map.Entry<TypeName, OS_Type> entry : aGenericPart.entrySet()) { // TODO Is this guaranteed to be in order?
			final OS_Type value = entry.getValue(); // This can be another ClassInvocation using GenType
			final String  name;

			if (value instanceof OS_UnknownType) {
				name = "?";
			} else {
				name = value.getClassOf().getName();
			}
			ls.add(name); // TODO Could be nested generics
		}
		return Helpers.String_join(", ", ls);
	}

	@NotNull
	public String getNumberedName() {
		return getKlass().getName() + "_" + getCode();
	}

	private boolean getPragma(String auto_construct) { // TODO this should be part of ContextImpl
		return false;
	}

	@Override
	public Role getRole() {
		return Role.CLASS;
	}

	@Override
	public String identityString() {
		return String.valueOf(klass);
	}

	@Override
	public OS_Module module() {
		return module;
	}

	public boolean isGeneric() {
		return klass.getGenericPart().size() > 0;
	}

	public boolean resolve_var_table_entries(DeducePhase aDeducePhase) {
		boolean Result = false;

		if (resolve_var_table_entries_already) return true;

		for (VarTableEntry varTableEntry : varTable) {
			varTableEntry.getDeduceElement3().resolve_var_table_entries(aDeducePhase, ci);
		}

		resolve_var_table_entries_already = true; // TODO is this right?
		return Result;
	}

	@Override
	public String toString() {
		return "EvaClass{" +
				"klass=" + klass +
				", module=" + module +
				", code=" + getCode() +
				", ci=" + ci +
				'}';
	}
}

//
//
//
