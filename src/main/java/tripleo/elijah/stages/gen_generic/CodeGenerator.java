/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_generic;

import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_generic.pipeline_impl.GenerateResultSink;

/**
 * Created 4/26/21 11:22 PM
 */
public interface CodeGenerator {
	void generate_class(EvaClass aEvaClass, GenerateResult aGenerateResult, final GenerateResultSink aResultSink);

	void generate_namespace(EvaNamespace aGeneratedNamespace, GenerateResult aGenerateResult, final GenerateResultSink aResultSink);
}

//
//
//