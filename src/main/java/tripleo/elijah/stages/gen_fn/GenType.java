/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.i.ErrSink;
import tripleo.elijah.contexts.ClassContext;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.BaseFunctionDef;
import tripleo.elijah.lang.types.OS_BuiltinType;
import tripleo.elijah.lang.types.OS_FuncExprType;
import tripleo.elijah.lang.types.OS_FuncType;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.nextgen.query.Operation2;
import tripleo.elijah.stages.deduce.*;
import tripleo.elijah.stages.deduce.post_bytecode.setup_GenType_Action;
import tripleo.elijah.stages.deduce.post_bytecode.setup_GenType_Action_Arena;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Created 5/31/21 1:32 PM
 */
public class GenType {
	public IInvocation        ci;
	public FunctionInvocation functionInvocation;
	public static GenType makeFromOSType(final OS_Type aVt, final ClassInvocation.CI_GenericPart aGenericPart, final DeduceTypes2 dt2, final DeducePhase phase, final ElLog aLOG, final ErrSink errSink) {
		return makeGenTypeFromOSType(aVt, aGenericPart, aLOG, errSink, dt2, phase);
	}
	public EvaNode node;
	public TypeName nonGenericTypeName;
	public OS_Type resolved;
	public NamespaceStatement resolvedn;
	public OS_Type typeName; // TODO or just TypeName ??

	@Contract(pure = true)
	public GenType() {
	}

	public GenType(@NotNull ClassStatement aClassStatement) {
		resolved = aClassStatement.getOS_Type();
	}

	@Contract(pure = true)
	public GenType(NamespaceStatement aNamespaceStatement) {
		resolvedn = /*new OS_Type*/(aNamespaceStatement);
	}

	public GenType(final OS_Type aAttached,
				   final OS_Type aOS_type,
				   final boolean aB,
				   final TypeName aTypeName,
				   final DeduceTypes2 deduceTypes2,
				   final ErrSink errSink,
				   final DeducePhase phase) {
		typeName = aAttached;
		resolved = aOS_type;
		if (aB) {
			ci = genCI(aTypeName, deduceTypes2, errSink, phase);
		}
	}

	private static @Nullable GenType makeGenTypeFromOSType(final @NotNull OS_Type aType,
														   final ClassInvocation.CI_GenericPart aGenericPart,
														   final ElLog aLOG,
														   final ErrSink errSink, final DeduceTypes2 dt2, final DeducePhase phase) {
		GenType gt = new GenType();
		gt.typeName = aType;
		if (aType.getType() == OS_Type.Type.USER) {
			final TypeName tn1 = aType.getTypeName();
			if (tn1.isNull()) return null; // TODO Unknown, needs to resolve somewhere

			assert tn1 instanceof NormalTypeName;
			final NormalTypeName       tn  = (NormalTypeName) tn1;
			final LookupResultList     lrl = tn.getContext().lookup(tn.getName());
			final @Nullable OS_Element el  = lrl.chooseBest(null);

			DeduceTypes2.ProcessElement.processElement(el, new DeduceTypes2.IElementProcessor() {
				private void __hasElement__typeNameElement(final ClassContext.@NotNull OS_TypeNameElement typeNameElement) {
					assert aGenericPart != null;

					final OS_Type x = aGenericPart.get(typeNameElement.getTypeName());

					switch (x.getType()) {
					case USER_CLASS:
						final @Nullable ClassStatement classStatement1 = x.getClassOf(); // always a ClassStatement

						assert classStatement1 != null;

						// TODO test next 4 (3) lines are copies of above
						gt.resolved = classStatement1.getOS_Type();
						break;
					case USER:
						final NormalTypeName tn2 = (NormalTypeName) x.getTypeName();
						final LookupResultList lrl2 = tn.getContext().lookup(tn2.getName());
						final @Nullable OS_Element el2 = lrl2.chooseBest(null);

						// TODO test next 4 lines are copies of above
						if (el2 instanceof final ClassStatement classStatement2) {
							gt.resolved = classStatement2.getOS_Type();
						} else
							throw new NotImplementedException();
						break;
					}
				}

				@Override
				public void elementIsNull() {
					NotImplementedException.raise();
				}

				private void gotResolved(final @NotNull GenType gt) {
					if (gt.resolved.getClassOf().getGenericPart().size() != 0) {
						//throw new AssertionError();
						aLOG.info("149 non-generic type " + tn1);
					}
					gt.genCI(null, dt2, errSink, phase); // TODO aGenericPart
					assert gt.ci != null;
					genNodeForGenType2(gt);
				}

				@Override
				public void hasElement(final OS_Element el) {
					final Operation2<OS_Element> best1 = preprocess(el);
					if (best1.mode() == Mode.FAILURE) {
						aLOG.err("152 Can't resolve Alias statement " + el);
						errSink.reportDiagnostic(best1.failure());
						return;
					}

					final OS_Element best = best1.success();

					switch (DecideElObjectType.getElObjectType(best)) {
					case CLASS:
						final ClassStatement classStatement = (ClassStatement) best;
						gt.resolved = classStatement.getOS_Type();
						break;
					case TYPE_NAME_ELEMENT:
						final ClassContext.OS_TypeNameElement typeNameElement = (ClassContext.OS_TypeNameElement) best;
						__hasElement__typeNameElement(typeNameElement);
						break;
					default:
						aLOG.err("143 " + el);
						throw new NotImplementedException();
					}

					if (gt.resolved != null)
						gotResolved(gt);
					else {
						int y=2; //05/22
					}
				}

				private Operation2< @NotNull OS_Element> preprocess(final OS_Element el) {
					@Nullable OS_Element best = el;
					try {
						while (best instanceof AliasStatement) {
							best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, dt2);
						}
						assert best != null;
						return Operation2.success(best);
					} catch (ResolveError aResolveError) {
						return Operation2.failure(aResolveError);
					}
				}
			});
		} else
			throw new AssertionError("Not a USER Type");
		return gt;
	}

	public ClassInvocation genCI(final TypeName aGenericTypeName,
								 final DeduceTypes2 deduceTypes2,
								 final ErrSink errSink,
								 final DeducePhase phase) {
		SetGenCI              sgci = new SetGenCI();
		final ClassInvocation ci   = sgci.call(this, aGenericTypeName, deduceTypes2, errSink, phase);
		final ClassInvocation ci1  = ci;
		final ClassInvocation ci11 = ci1;
		return ci11;
	}

	/**
	 * Sets the node for a GenType, invocation must already be set
	 *
	 * @param aGenType the GenType to modify.
	 */
	public static void genNodeForGenType2(final @NotNull GenType aGenType) {
//		assert aGenType.nonGenericTypeName != null;

		final IInvocation invocation = aGenType.ci;

		if (invocation instanceof final NamespaceInvocation namespaceInvocation) {
			namespaceInvocation.resolveDeferred().then(new DoneCallback<EvaNamespace>() {
				@Override
				public void onDone(final EvaNamespace result) {
					aGenType.node = result;
				}
			});
		} else if (invocation instanceof final ClassInvocation classInvocation) {
			classInvocation.resolvePromise().then(new DoneCallback<EvaClass>() {
				@Override
				public void onDone(final EvaClass result) {
					aGenType.node = result;
				}
			});
		} else
			throw new IllegalStateException("invalid invocation");
	}

	//@ensures Result.ci != null
	//@ensures Result.resolved != null
	public static @NotNull GenType of(final NamespaceStatement aNamespaceStatement, final Supplier<NamespaceInvocation> aNamespaceInvocationSupplier) {
		final GenType genType = new GenType(aNamespaceStatement);

		final NamespaceInvocation nsi = aNamespaceInvocationSupplier.get();

		genType.ci = nsi;

		return genType;
	}

	public String asString() {
		final String sb = "GenType{" + "resolvedn=" + resolvedn +
				", typeName=" + typeName +
				", nonGenericTypeName=" + nonGenericTypeName +
				", resolved=" + resolved +
				", ci=" + ci +
				", node=" + node +
				", functionInvocation=" + functionInvocation +
				'}';
		return sb;
	}

	public void copy(GenType aGenType) {
		if (resolvedn == null) resolvedn = aGenType.resolvedn;
		if (typeName == null) typeName = aGenType.typeName;
		if (nonGenericTypeName == null) nonGenericTypeName = aGenType.nonGenericTypeName;
		if (resolved == null) resolved = aGenType.resolved;
		if (ci == null) ci = aGenType.ci;
		if (node == null) node = aGenType.node;
	}

	@Contract(value = "null -> false", pure = true)
	@Override
	public boolean equals(final Object aO) {
		if (this == aO) return true;
		if (aO == null || getClass() != aO.getClass()) return false;

		final GenType genType = (GenType) aO;

		if (!Objects.equals(resolvedn, genType.resolvedn)) return false;
		if (!Objects.equals(typeName, genType.typeName)) return false;
		if (!Objects.equals(nonGenericTypeName, genType.nonGenericTypeName))
			return false;
		if (!Objects.equals(resolved, genType.resolved)) return false;
		if (!Objects.equals(ci, genType.ci)) return false;
		if (!Objects.equals(node, genType.node)) return false;
		return Objects.equals(functionInvocation, genType.functionInvocation);
	}

	@Override
	public int hashCode() {
		int result = resolvedn != null ? resolvedn.hashCode() : 0;
		result = 31 * result + (typeName != null ? typeName.hashCode() : 0);
		result = 31 * result + (nonGenericTypeName != null ? nonGenericTypeName.hashCode() : 0);
		result = 31 * result + (resolved != null ? resolved.hashCode() : 0);
		result = 31 * result + (ci != null ? ci.hashCode() : 0);
		result = 31 * result + (node != null ? node.hashCode() : 0);
		result = 31 * result + (functionInvocation != null ? functionInvocation.hashCode() : 0);
		return result;
	}

	public void genCIForGenType2(final DeduceTypes2 deduceTypes2) {
		final List<setup_GenType_Action> list  = new ArrayList<>();
		final setup_GenType_Action_Arena arena = new setup_GenType_Action_Arena();

		genCI(nonGenericTypeName, deduceTypes2, deduceTypes2._errSink(), deduceTypes2.phase);
		final IInvocation invocation = ci;
		if (invocation instanceof final NamespaceInvocation namespaceInvocation) {
			namespaceInvocation.resolveDeferred().then(new DoneCallback<EvaNamespace>() {
				@Override
				public void onDone(final EvaNamespace result) {
					node = result;
				}
			});
		} else if (invocation instanceof final ClassInvocation classInvocation) {
			classInvocation.resolvePromise().then(new DoneCallback<EvaClass>() {
				@Override
				public void onDone(final EvaClass result) {
					node = result;
				}
			});
		} else
			throw new IllegalStateException("invalid invocation");

		for (setup_GenType_Action action : list) {
			action.run(this, arena);
		}
	}

	/**
	 * Sets the invocation ({@code genType#ci}) and the node for a GenType
	 *
	 * @param aDeduceTypes2
	 */
	public void genCIForGenType2__(final DeduceTypes2 aDeduceTypes2) {
		genCI(nonGenericTypeName, aDeduceTypes2, aDeduceTypes2._errSink(), aDeduceTypes2.phase);
		final IInvocation invocation = ci;
		if (invocation instanceof final NamespaceInvocation namespaceInvocation) {
			namespaceInvocation.resolveDeferred().then(new DoneCallback<EvaNamespace>() {
				@Override
				public void onDone(final EvaNamespace result) {
					node = result;
				}
			});
		} else if (invocation instanceof final ClassInvocation classInvocation) {
			classInvocation.resolvePromise().then(new DoneCallback<EvaClass>() {
				@Override
				public void onDone(final EvaClass result) {
					node = result;
				}
			});
		} else {
			if (resolved instanceof final OS_FuncExprType funcExprType) {
				final @NotNull GenerateFunctions genf = aDeduceTypes2.getGenerateFunctions(funcExprType.getElement().getContext().module());
				final FunctionInvocation fi = aDeduceTypes2._phase().newFunctionInvocation((BaseFunctionDef) funcExprType.getElement(),
																						   null,
																						   null);
				WlGenerateFunction gen = new WlGenerateFunction(genf, fi, aDeduceTypes2._phase().codeRegistrar);
				gen.run(null);
				node = gen.getResult();
			} else if (resolved instanceof final OS_FuncType funcType) {
				int y = 2;
			} else if (resolved instanceof OS_BuiltinType) {
				// passthrough
			} else
				throw new IllegalStateException("invalid invocation");
		}
	}

	public boolean isNull() {
		if (resolvedn != null) return false;
		if (typeName != null) return false;
		if (nonGenericTypeName != null) return false;
		if (resolved != null) return false;
		if (ci != null) return false;
		return node == null;
	}

	public void set(@NotNull OS_Type aType) {
		switch (aType.getType()) {
		case USER:
			typeName = aType;
			break;
		case USER_CLASS:
			resolved = aType;
		default:
			tripleo.elijah.util.Stupidity.println_err_2("48 Unknown in set: " + aType);
		}
	}

	static class SetGenCI {

		public ClassInvocation call(@NotNull GenType genType, TypeName aGenericTypeName, final DeduceTypes2 deduceTypes2, final ErrSink errSink, final DeducePhase phase) {
			if (genType.nonGenericTypeName != null) {
				return nonGenericTypeName(genType, deduceTypes2, errSink, phase);
			}
			if (genType.resolved != null) {
				final OS_Type.Type resolvedType = genType.resolved.getType();

				switch (resolvedType) {
				case USER_CLASS:
					return resolvedUserClass(genType, aGenericTypeName, phase, deduceTypes2, errSink);
				case FUNCTION:
					return resolvedFunction(genType, aGenericTypeName, deduceTypes2, errSink, phase);
				case FUNC_EXPR:
					// TODO what to do here?
					NotImplementedException.raise();
					break;
				}
			}
			return null;
		}

		private @NotNull ClassInvocation nonGenericTypeName(final @NotNull GenType genType, final DeduceTypes2 deduceTypes2, final ErrSink errSink, final DeducePhase phase) {
			@NotNull NormalTypeName aTyn1           = (NormalTypeName) genType.nonGenericTypeName;
			@Nullable String        constructorName = null; // FIXME this comes from nowhere

			switch (genType.resolved.getType()) {
			case GENERIC_TYPENAME:
				// TODO seems to not be necessary
				assert false;
				throw new NotImplementedException();
			case USER_CLASS:
				ClassStatement best = genType.resolved.getClassOf();
				//
				ClassInvocation clsinv2 = DeduceTypes2.ClassInvocationMake.withGenericPart(best, constructorName, aTyn1, deduceTypes2, errSink);
				clsinv2 = phase.registerClassInvocation(clsinv2);
				genType.ci = clsinv2;
				return clsinv2;
			default:
				throw new IllegalStateException("Unexpected value: " + genType.resolved.getType());
			}
		}

		private @NotNull ClassInvocation resolvedUserClass(final @NotNull GenType genType, final TypeName aGenericTypeName, final DeducePhase phase, final DeduceTypes2 deduceTypes2, final ErrSink errSink) {
			ClassStatement   best            = genType.resolved.getClassOf();
			@Nullable String constructorName = null; // TODO what to do about this, nothing I guess

			@NotNull List<TypeName>   gp = best.getGenericPart();
			@Nullable ClassInvocation clsinv;
			if (genType.ci == null) {
				clsinv = DeduceTypes2.ClassInvocationMake.withGenericPart(best, constructorName, (NormalTypeName) aGenericTypeName, deduceTypes2, errSink);
				if (clsinv == null) return null;
				clsinv     = phase.registerClassInvocation(clsinv);
				genType.ci = clsinv;
			} else
				clsinv = (ClassInvocation) genType.ci;
			return clsinv;
		}

		private @NotNull ClassInvocation resolvedFunction(final @NotNull GenType genType, final TypeName aGenericTypeName, final DeduceTypes2 deduceTypes2, final ErrSink errSink, final DeducePhase phase) {
			// TODO what to do here?
			OS_Element       ele             = genType.resolved.getElement();
			ClassStatement   best            = (ClassStatement) ele.getParent();//genType.resolved.getClassOf();
			@Nullable String constructorName = null; // TODO what to do about this, nothing I guess

			@NotNull List<TypeName>   gp = best.getGenericPart();
			@Nullable ClassInvocation clsinv;
			if (genType.ci == null) {
				clsinv = DeduceTypes2.ClassInvocationMake.withGenericPart(best, constructorName, (NormalTypeName) aGenericTypeName, deduceTypes2, errSink);
				if (clsinv == null) return null;
				clsinv     = phase.registerClassInvocation(clsinv);
				genType.ci = clsinv;
			} else
				clsinv = (ClassInvocation) genType.ci;
			return clsinv;
		}
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
