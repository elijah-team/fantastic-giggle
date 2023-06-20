package tripleo.vendor.mal;

import tripleo.vendor.mal.types.*;

import java.util.HashMap;

public class env {
	public static class Env {
		Env                     outer = null;
		HashMap<String, MalVal> data  = new HashMap<String, MalVal>();

		public Env(final Env outer) {
			this.outer = outer;
		}

		public Env(final Env outer, final MalList binds, final MalList exprs) {
			this.outer = outer;
			for (Integer i = 0; i < binds.size(); i++) {
				final String sym = ((MalSymbol) binds.nth(i)).getName();
				if (sym.equals("&")) {
					data.put(((MalSymbol) binds.nth(i + 1)).getName(),
							 exprs.slice(i));
					break;
				} else {
					data.put(sym, exprs.nth(i));
				}
			}
		}

		public Env find(final MalSymbol key) {
			if (data.containsKey(key.getName())) {
				return this;
			} else if (outer != null) {
				return outer.find(key);
			} else {
				return null;
			}
		}

		public MalVal get(final MalSymbol key) throws MalThrowable {
			final Env e = find(key);
			if (e == null) {
				throw new MalException(
						"'" + key.getName() + "' not found");
			} else {
				return e.data.get(key.getName());
			}
		}

		public Env set(final MalSymbol key, final MalVal value) {
			data.put(key.getName(), value);
			return this;
		}
	}
}
