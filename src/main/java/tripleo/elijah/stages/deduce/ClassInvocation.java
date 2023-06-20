/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import tripleo.elijah.lang.i.ClassStatement;
import tripleo.elijah.lang.i.OS_Type;
import tripleo.elijah.lang.i.TypeName;
import tripleo.elijah.lang.types.OS_UnknownType;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaContainer;

/**
 * Created 3/5/21 3:51 AM
 */
public class ClassInvocation implements IInvocation {
	public final @Nullable Map<TypeName, OS_Type> genericPart;
	private final @NotNull ClassStatement         cls;
	private final          String                 constructorName;
	private final DeferredObject<EvaClass, Void, Void> resolvePromise = new DeferredObject<EvaClass, Void, Void>();
	CI_GenericPart genericPart_ = new CI_GenericPart();

	public CI_GenericPart genericPart() {
		return genericPart_;
	}

	public ClassInvocation(@NotNull ClassStatement aClassStatement, String aConstructorName) {
		cls = aClassStatement;
		final @NotNull List<TypeName> genericPart1 = aClassStatement.getGenericPart();
		if (genericPart1.size() > 0) {
			genericPart = new HashMap<TypeName, OS_Type>(genericPart1.size());
			for (TypeName typeName : genericPart1) {
				genericPart().put(typeName, new OS_UnknownType(null));
			}
		} else {
			genericPart = null;
		}
		constructorName = aConstructorName;
	}

	public String getConstructorName() {
		return constructorName;
	}

	public @NotNull ClassStatement getKlass() {
		return cls;
	}

	public @NotNull Promise<EvaClass, Void, Void> resolvePromise() {
		return resolvePromise.promise();
	}

	public @NotNull DeferredObject<EvaClass, Void, Void> resolveDeferred() {
		return resolvePromise;
	}

	public class CI_GenericPart {

		public OS_Type get(final TypeName aTypeName) {
			return genericPart.get(aTypeName);
		}

		public Map<TypeName, OS_Type> getMap() {
			return genericPart;
		}

		public boolean hasGenericPart() {
			return genericPart == null;
		}

		public void put(final TypeName aTypeName, final OS_Type aType) {
			genericPart.put(aTypeName, aType);
		}

		public void record(final TypeName aKey, final EvaContainer.@NotNull VarTableEntry aVarTableEntry) {
			genericPart.put(aKey, aVarTableEntry.varType);
		}

		public OS_Type valueForKey(final TypeName tn) {
			OS_Type realType = null;
			for (final Map.Entry<TypeName, OS_Type> entry : this.entrySet()) {
				if (entry.getKey().equals(tn)) {
					realType = entry.getValue();
					break;
				}
			}
			return realType;
		}

		public Iterable<? extends Map.Entry<TypeName, OS_Type>> entrySet() {
			return genericPart.entrySet();
		}
	}

	public void set(int aIndex, TypeName aTypeName, @NotNull OS_Type aType) {
		assert aType.getType() == OS_Type.Type.USER_CLASS;
		genericPart().put(aTypeName, aType);
	}

	@Override
	public void setForFunctionInvocation(@NotNull FunctionInvocation aFunctionInvocation) {
		aFunctionInvocation.setClassInvocation(this);
	}
}

//
//
//
