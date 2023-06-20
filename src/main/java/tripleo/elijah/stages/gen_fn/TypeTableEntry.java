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
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.i.GenericTypeName;
import tripleo.elijah.lang.i.IExpression;
import tripleo.elijah.lang.i.OS_Type;
import tripleo.elijah.lang.i.TypeName;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.DeduceTypes2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 9/12/20 10:26 PM
 */
public class TypeTableEntry {
	public final IExpression     expression;
	public final GenType         genType = new GenType();
	@NotNull
	public final Type            lifetime;
	@Nullable
	public final TableEntryIV tableEntry;
	final        int             index;
	private final List<OnSetAttached> osacbs = new ArrayList<OnSetAttached>();
	private      BaseEvaFunction __gf;
	private      DeduceTypes2    _dt2;
	@Nullable
	private      OS_Type         attached;

	public void _fix_table(final DeduceTypes2 aDeduceTypes2, final @NotNull BaseEvaFunction aEvaFunction) {
		_dt2 = aDeduceTypes2;
		__gf = aEvaFunction;
	}

	public void addSetAttached(OnSetAttached osa) {
		osacbs.add(osa);
	}

	public TypeTableEntry(final int index,
						  @NotNull final Type lifetime,
						  @Nullable final OS_Type aAttached,
						  final IExpression expression,
						  @Nullable final TableEntryIV aTableEntryIV) {
		this.index    = index;
		this.lifetime = lifetime;
		if (aAttached == null || (aAttached.getType() == OS_Type.Type.USER && aAttached.getTypeName() == null)) {
			attached = null;
			// do nothing with genType
		} else {
			attached = aAttached;
			_settingAttached(aAttached);
		}
		this.expression = expression;
		this.tableEntry = aTableEntryIV;
	}

	public void genTypeCI(ClassInvocation aClsinv) {
		genType.ci = aClsinv;
	}

	private void _settingAttached(@NotNull OS_Type aAttached) {
		switch (aAttached.getType()) {
		case USER:
			if (genType.typeName != null) {
				final TypeName typeName = aAttached.getTypeName();
				if (!(typeName instanceof GenericTypeName))
					genType.nonGenericTypeName = typeName;
			} else
				genType.typeName = aAttached/*.getTypeName()*/;
			break;
		case USER_CLASS:
//			ClassStatement c = attached.getClassOf();
			genType.resolved = aAttached; // c
			break;
		case UNIT_TYPE:
			genType.resolved = aAttached;
		case BUILT_IN:
			if (genType.typeName != null)
				genType.resolved = aAttached;
			else
				genType.typeName = aAttached;
			break;
		case FUNCTION:
			assert genType.resolved == null || genType.resolved == aAttached || /*HACK*/ aAttached.getType() == OS_Type.Type.FUNCTION;
			genType.resolved = aAttached;
			break;
		case FUNC_EXPR:
			assert genType.resolved == null || genType.resolved == aAttached;// || /*HACK*/ aAttached.getType() == OS_Type.Type.FUNCTION;
			genType.resolved = aAttached;
			break;
		default:
//			throw new NotImplementedException();
			tripleo.elijah.util.Stupidity.println_err_2("73 " + aAttached);
			break;
		}
	}

	public OS_Type getAttached() {
		return attached;
	}

	public void setAttached(GenType aGenType) {
		genType.copy(aGenType);

		setAttached(genType.resolved);
	}

	public boolean isResolved() {
		return genType.node != null;
	}

	public int getIndex() {
		return index;
	}

	@Override
	@NotNull
	public String toString() {
		return "TypeTableEntry{" +
				"index=" + index +
				", lifetime=" + lifetime +
				", attached=" + attached +
				", expression=" + expression +
				'}';
	}

	public void resolve(EvaNode aResolved) {
		genType.node = aResolved;
	}

	public EvaNode resolved() {
		return genType.node;
	}

	public enum Type {
		SPECIFIED, TRANSIENT
	}

	public void setAttached(OS_Type aAttached) {
		attached = aAttached;
		if (aAttached != null) {
			_settingAttached(aAttached);

			for (OnSetAttached cb : osacbs) {
				cb.onSetAttached(this);
			}
		}
	}

	public interface OnSetAttached {
		void onSetAttached(TypeTableEntry aTypeTableEntry);
	}

}

//
//
//
