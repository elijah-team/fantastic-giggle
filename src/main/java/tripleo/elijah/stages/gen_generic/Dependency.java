/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_generic;

import tripleo.elijah.lang.i.OS_Element;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.gen_fn.AbstractDependencyTracker;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaContainerNC;
import tripleo.elijah.stages.gen_fn.GenType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created 9/13/21 4:00 AM
 */
public class Dependency {
	public final Set<Dependency> deps = new HashSet<>();
	public final IDependencyReferent referent;
	public       DependencyRef   dref;
	public       OS_Element          resolved;

	public Dependency(IDependencyReferent aReferent) {
		referent = aReferent;
	}

	public Set<Dependency> getNotedDeps() {
		return deps;
	}

	public DependencyRef getRef() {
		return dref;
	}

	public String jsonString() {
		final String sb = "{\".class\": \"Dependency\", " + "referent: " + referent + ", " +
				"dref: " + (dref != null ? dref.jsonString() + ", " : "null, ") +
				"deps: " + deps + ", " +
				"resolved: " + resolved +/*+", "*/
				"}";
		return sb;
	}

	public void noteDependencies(AbstractDependencyTracker aDependencyTracker,
								 List<FunctionInvocation> aDependentFunctions,
								 List<GenType> aDependentTypes) {
		for (FunctionInvocation dependentFunction : aDependentFunctions) {
			final BaseEvaFunction generatedFunction = dependentFunction.getGenerated();
			if (generatedFunction != null)
				deps.add(generatedFunction.getDependency());
			else
				tripleo.elijah.util.Stupidity.println_err_2("52 false FunctionInvocation " + dependentFunction);
		}
		for (GenType dependentType : aDependentTypes) {
			final EvaContainerNC node = (EvaContainerNC) dependentType.node;
			if (node != null)
				deps.add(node.getDependency());
			else {
				tripleo.elijah.util.Stupidity.println_err_2("46 node is null " + (dependentType.resolved != null ? dependentType.resolved : dependentType.resolvedn));
				final Dependency d = new Dependency(null);
				d.resolved = dependentType.resolved != null ? dependentType.resolved.getClassOf() : dependentType.resolvedn;
				deps.add(d);
			}
		}
	}

	public void setRef(DependencyRef aDref) {
		dref = aDref;
	}
}

//
//
//
