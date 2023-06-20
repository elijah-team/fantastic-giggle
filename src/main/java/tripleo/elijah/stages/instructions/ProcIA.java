/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.instructions;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;

/**
 * Created 1/12/21 4:22 AM
 */
public class ProcIA implements InstructionArgument {
	public final  BaseEvaFunction gf;
	private final int             index;

	public ProcIA(int i, BaseEvaFunction generatedFunction) {
		this.index = i;
		this.gf    = generatedFunction;
	}

	public @NotNull ProcTableEntry getEntry() {
		return gf.getProcTableEntry(index);
	}

	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return "ProcIA{" +
				"index=" + index + ", " +
				"func=" + gf.getProcTableEntry(index) +
				'}';
	}
}

//
//
//
