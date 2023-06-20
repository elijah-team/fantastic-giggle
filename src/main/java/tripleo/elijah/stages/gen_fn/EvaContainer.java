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
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.Operation;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_VarTableEntry;
import tripleo.elijah.util.Maybe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created 2/28/21 3:23 AM
 */
public interface EvaContainer extends EvaNode {
	OS_Element getElement();

	@NotNull Maybe<VarTableEntry> getVariable(String aVarName);

	class VarTableEntry {
		public final  IExpression                         initialValue;
		public final  IdentExpression                     nameToken;
		public final VariableStatement vs;
		private final DeferredObject<OS_Type, Void, Void> _resolve_varType_Promise = new DeferredObject<>();
		private final OS_Element                          parent;
		public        List<ConnectionPair>                connectionPairs          = new ArrayList<>();
		public        List<TypeTableEntry>                potentialTypes           = new ArrayList<TypeTableEntry>();
		public        TypeName                            typeName;
		public DeferredObject<UpdatePotentialTypesCB, Void, Void> updatePotentialTypesCBPromise = new DeferredObject<>();
		public OS_Type varType;
		UpdatePotentialTypesCB updatePotentialTypesCB;
		private       EvaNode                             _resolvedType;

		public void connect(final VariableTableEntry aVte, final EvaConstructor aConstructor) {
			connectionPairs.add(new ConnectionPair(aVte, aConstructor));
		}

		public DeduceElement3_VarTableEntry getDeduceElement3() {
			return new DeduceElement3_VarTableEntry(this);
		}

		public VarTableEntry(final VariableStatement aVs,
							 final @NotNull IdentExpression aNameToken,
							 final IExpression aInitialValue,
							 final @NotNull TypeName aTypeName,
							 final @NotNull OS_Element aElement) {
			vs           = aVs;
			nameToken    = aNameToken;
			initialValue = aInitialValue;
			typeName     = aTypeName;
			varType      = new OS_UserType(typeName);
			parent       = aElement;
		}

		public void addPotentialTypes(@NotNull Collection<TypeTableEntry> aPotentialTypes) {
			potentialTypes.addAll(aPotentialTypes);
		}

		public @NotNull OS_Element getParent() {
			return parent;
		}

		public boolean isResolved() {
			return this._resolve_varType_Promise.isResolved();
		}

		public @Nullable EvaNode resolvedType() {
			return _resolvedType;
		}

		public interface UpdatePotentialTypesCB {
			Operation<Boolean> call(final @NotNull EvaContainer aEvaContainer);
		}

		public void resolve(@NotNull EvaNode aResolvedType) {
			tripleo.elijah.util.Stupidity.println_out_2(String.format("** [GeneratedContainer 56] resolving VarTableEntry %s to %s", nameToken, aResolvedType.identityString()));
			_resolvedType = aResolvedType;
		}

		public void resolve_varType(final OS_Type aOS_type) {
			_resolve_varType_Promise.resolve(aOS_type);
			varType = aOS_type;
		}

		public void resolve_varType_cb(final DoneCallback<OS_Type> aCallback) {
			_resolve_varType_Promise.then(aCallback);
		}

		public static class ConnectionPair {
			public final VariableTableEntry vte;
			final        EvaConstructor     constructor;

			@Contract(pure = true)
			public ConnectionPair(final VariableTableEntry aVte, final EvaConstructor aConstructor) {
				vte         = aVte;
				constructor = aConstructor;
			}
		}

		public void updatePotentialTypes(final @NotNull EvaContainer aEvaContainer) {
//			assert aGeneratedContainer == GeneratedContainer.this;
			updatePotentialTypesCBPromise.then(new DoneCallback<UpdatePotentialTypesCB>() {
				@Override
				public void onDone(final UpdatePotentialTypesCB result) {
					Operation<Boolean> s;

					s = result.call(aEvaContainer);

					assert s.mode() == Mode.SUCCESS;
				}
			});
		}

		public VariableStatement vs() {
			return vs;
		}
	}
}

//
//
//
