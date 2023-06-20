package tripleo.vendor.mal;

import java.util.*;

public class types {
	public static MalConstant Nil   = new MalConstant("nil");
	public static MalConstant True  = new MalConstant("true");
	public static MalConstant False = new MalConstant("false");

	public static Boolean _equal_Q(final MalVal a, final MalVal b) {
		final Class ota = a.getClass();
		final Class otb = b.getClass();
		if (!((ota == otb) ||
				(a instanceof MalList && b instanceof MalList))) {
			return false;
		} else {
			if (a instanceof MalInteger) {
				return ((MalInteger) a).getValue() ==
						((MalInteger) b).getValue();
			} else if (a instanceof MalSymbol) {
				return ((MalSymbol) a).getName().equals(
						((MalSymbol) b).getName());
			} else if (a instanceof MalString) {
				return ((MalString) a).getValue().equals(
						((MalString) b).getValue());
			} else if (a instanceof MalList) {
				if (((MalList) a).size() != ((MalList) b).size()) {
					return false;
				}
				for (Integer i = 0; i < ((MalList) a).size(); i++) {
					if (!_equal_Q(((MalList) a).nth(i),
								  ((MalList) b).nth(i))) {
						return false;
					}
				}
				return true;
			} else if (a instanceof final MalHashMap mhm) {
				if (((MalHashMap) a).value.size() != ((MalHashMap) b).value.size()) {
					return false;
				}
				//HashMap<String,MalVal> hm = (HashMap<String,MalVal>)a.value;
				final HashMap<String, MalVal> hm = (HashMap<String, MalVal>) mhm.value;
				for (final String k : hm.keySet()) {
					if (!_equal_Q(((MalVal) ((MalHashMap) a).value.get(k)),
								  ((MalVal) ((MalHashMap) b).value.get(k)))) {
						return false;
					}
				}
				return true;
			} else {
				return a == b;
			}
		}
	}

	//
	// General functions
	//

	public interface ILambda {
		MalVal apply(MalList args) throws MalThrowable;
	}

	//
	// Exceptions/Errors
	//
	public static class MalThrowable extends Exception {
		public MalThrowable() {
		}

		public MalThrowable(final String msg) {
			super(msg);
		}
	}

	public static class MalError extends MalThrowable {
		public MalError(final String msg) {
			super(msg);
		}
	}

	public static class MalContinue extends MalThrowable {
	}

	// Thrown by throw function
	public static class MalException extends MalThrowable {
		MalVal value;

		public MalException(final MalVal value) {
			this.value = value;
		}

		public MalException(final String value) {
			this.value = new MalString(value);
		}

		public MalVal getValue() {
			return value;
		}
	}

	//
	// Mal boxed types
	//
	abstract public static class MalVal {
		static MalVal meta = Nil;

		abstract public MalVal copy() throws MalThrowable;

		// Default is just to call regular toString()
		public String toString(final Boolean print_readably) {
			return this.toString();
		}

		public MalVal getMeta() {
			return meta;
		}

		public void setMeta(final MalVal m) {
			meta = m;
		}

		public Boolean list_Q() {
			return false;
		}
	}

	public static class MalConstant extends MalVal {
		String value;

		public MalConstant(final String name) {
			value = name;
		}

		public MalConstant copy() throws MalThrowable {
			return this;
		}

		public String toString() {
			return value;
		}
	}

	public static class MalInteger extends MalVal {
		Integer value;

		public MalInteger(final Integer v) {
			value = v;
		}

		public MalInteger copy() throws MalThrowable {
			return this;
		}

		@Override
		public String toString() {
			return value.toString();
		}

		public MalInteger add(final MalInteger other) {
			return new MalInteger(value + other.getValue());
		}

		public Integer getValue() {
			return value;
		}

		public MalInteger subtract(final MalInteger other) {
			return new MalInteger(value - other.getValue());
		}

		public MalInteger multiply(final MalInteger other) {
			return new MalInteger(value * other.getValue());
		}

		public MalInteger divide(final MalInteger other) {
			return new MalInteger(value / other.getValue());
		}

		public MalConstant lt(final MalInteger other) {
			return (value < other.getValue()) ? True : False;
		}

		public MalConstant lte(final MalInteger other) {
			return (value <= other.getValue()) ? True : False;
		}

		public MalConstant gt(final MalInteger other) {
			return (value > other.getValue()) ? True : False;
		}

		public MalConstant gte(final MalInteger other) {
			return (value >= other.getValue()) ? True : False;
		}
	}

	public static class MalSymbol extends MalVal {
		String value;

		public MalSymbol(final String v) {
			value = v;
		}

		public MalSymbol(final MalString v) {
			value = v.getValue();
		}

		public MalSymbol copy() throws MalThrowable {
			return this;
		}

		public String getName() {
			return value;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public static class MalString extends MalVal {
		String value;

		public MalString(final String v) {
			value = v;
		}

		public MalString copy() throws MalThrowable {
			return this;
		}

		public String toString(final Boolean print_readably) {
			if (value.length() > 0 && value.charAt(0) == '\u029e') {
				return ":" + value.substring(1);
			} else if (print_readably) {
				return "\"" + printer.escapeString(value) + "\"";
			} else {
				return value;
			}
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return "\"" + value + "\"";
		}
	}

	public static class MalList extends MalVal {
		String start = "(", end = ")";
		List value;

		public MalList(final List val) {
			value = val;
		}

		public MalList(final MalVal... mvs) {
			value = new ArrayList<MalVal>();
			conj_BANG(mvs);
		}

		public MalList conj_BANG(final MalVal... mvs) {
			Collections.addAll(value, mvs);
			return this;
		}

		public MalList copy() throws MalThrowable {
			final MalList new_ml = new MalList();
			new_ml.value.addAll(value);
			meta = meta;
			return new_ml;
		}

		public String toString(final Boolean print_readably) {
			return start + printer.join(value, " ", print_readably) + end;
		}

		public Boolean list_Q() {
			return true;
		}

		@Override
		public String toString() {
			return start + printer.join(value, " ", true) + end;
		}

		public List getList() {
			return value;
		}

		public MalVal nth(final Integer idx) {
			return (MalVal) value.get(idx);
		}

		public MalList rest() {
			if (size() > 0) {
				return new MalList(value.subList(1, value.size()));
			} else {
				return new MalList();
			}
		}

		public Integer size() {
			return value.size();
		}

		public MalList slice(final Integer start) {
			return slice(start, value.size());
		}

		public MalList slice(final Integer start, final Integer end) {
			return new MalList(value.subList(start, end));
		}
	}

	public static class MalVector extends MalList {
		// Same implementation except for instantiation methods
		public MalVector(final List val) {
			value = val;
			start = "[";
			end   = "]";
		}

		public MalVector(final MalVal... mvs) {
			super(mvs);
			start = "[";
			end   = "]";
		}

		public MalVector copy() throws MalThrowable {
			final MalVector new_mv = new MalVector();
			new_mv.value.addAll(value);
			meta = meta;
			return new_mv;
		}

		public Boolean list_Q() {
			return false;
		}

		public MalVector slice(final Integer start, final Integer end) {
			return new MalVector(value.subList(start, end));
		}
	}

	public static class MalHashMap extends MalVal {
		Map value;

		public MalHashMap(final Map val) {
			value = val;
		}

		public MalHashMap(final MalList lst) {
			value = new HashMap<String, MalVal>();
			assoc_BANG(lst);
		}

		public MalHashMap assoc_BANG(final MalList lst) {
			for (Integer i = 0; i < lst.value.size(); i += 2) {
				value.put(((MalString) lst.nth(i)).getValue(),
						  lst.nth(i + 1));
			}
			return this;
		}

		public MalHashMap(final MalVal... mvs) {
			value = new HashMap<String, MalVal>();
			assoc_BANG(mvs);
		}

		public MalHashMap assoc_BANG(final MalVal... mvs) {
			for (Integer i = 0; i < mvs.length; i += 2) {
				value.put(((MalSymbol) mvs[i]).getName(),
						  mvs[i + 1]);
			}
			return this;
		}

		public MalHashMap copy() throws MalThrowable {
			final Map<String, MalVal> shallowCopy = new HashMap<String, MalVal>();
			shallowCopy.putAll(value);
			final MalHashMap new_hm = new MalHashMap(shallowCopy);
			meta = meta;
			return new_hm;
		}

		public String toString(final Boolean print_readably) {
			return "{" + printer.join(value, " ", print_readably) + "}";
		}

		@Override
		public String toString() {
			return "{" + printer.join(value, " ", true) + "}";
		}

		public Set _entries() {
			return value.entrySet();
		}

		public MalHashMap dissoc_BANG(final MalList lst) {
			for (Integer i = 0; i < lst.value.size(); i++) {
				value.remove(((MalString) lst.nth(i)).getValue());
			}
			return this;
		}

		public Integer size() {
			return value.size();
		}
	}

	public static class MalAtom extends MalVal {
		MalVal value;

		public MalAtom(final MalVal value) {
			this.value = value;
		}

		public MalAtom copy() throws MalThrowable {
			return new MalAtom(value);
		}

		public String toString(final Boolean print_readably) {
			return "(atom " + printer._pr_str(value, print_readably) + ")";
		}

		@Override
		public String toString() {
			return "(atom " + printer._pr_str(value, true) + ")";
		}
	}

	public static abstract class MalFunction extends MalVal
			implements ILambda, java.lang.Cloneable {
		public MalVal                     ast    = null;
		public tripleo.vendor.mal.env.Env env    = null;
		public MalList                    params = null;
		public Boolean macro  = false;

		public MalFunction() {
		}

		public MalFunction(final MalVal ast, final tripleo.vendor.mal.env.Env env, final MalList params) {
			this.ast    = ast;
			this.env    = env;
			this.params = params;
		}

		public MalFunction copy() throws MalThrowable {
			try {
				// WARNING: clone() is broken:
				//   http://www.artima.com/intv/bloch13.html
				// However, this doesn't work:
				//   MalFunction new_mf = this.getClass().newInstance();
				// So for now it's clone.
				final MalFunction new_mf = (MalFunction) this.clone();
				new_mf.ast    = ast;
				new_mf.env    = env;
				new_mf.params = params;
				new_mf.macro  = macro;
				return new_mf;
			} catch (final Throwable t) {
				// not much we can do
				t.printStackTrace();
				throw new MalError("Could not copy MalFunction: " + this);
			}
		}

		public MalVal getAst() {
			return ast;
		}

		public tripleo.vendor.mal.env.Env getEnv() {
			return env;
		}

		public MalList getParams() {
			return params;
		}

		public tripleo.vendor.mal.env.Env genEnv(final MalList args) {
			return new env.Env(env, params, args);
		}

		public Boolean isMacro() {
			return macro;
		}

		public void setMacro() {
			macro = true;
		}
	}
}
