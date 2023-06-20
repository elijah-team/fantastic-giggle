package tripleo.elijah.nextgen.expansion;

import junit.framework.TestCase;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.Compilation.CompilationAlways;
import tripleo.elijah.comp.i.ErrSink;
import tripleo.elijah.comp.internal.CompilationImpl;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.*;
import tripleo.elijah.nextgen.model.SM_ClassBody;
import tripleo.elijah.nextgen.model.SM_ClassDeclaration;
import tripleo.elijah.nextgen.model.SM_ClassInheritance;
import tripleo.elijah.nextgen.model.SM_ClassSubtype;
import tripleo.elijah.nextgen.model.SM_Name;
import tripleo.elijah.stages.gen_generic.GenerateFiles;
import tripleo.elijah.stages.gen_generic.OutputFileFactory;
import tripleo.elijah.stages.gen_generic.OutputFileFactoryParams;
import tripleo.elijah.test_help.Boilerplate;

import java.util.List;

import static tripleo.elijah.util.Helpers.List_of;

public class SX_NodeTest extends TestCase {

	public void testFullText() {
		//final StdErrSink      errSink       = new StdErrSink();
		//final IO              io            = new IO();
		final CompilationImpl comp;//          = new CompilationImpl(errSink, io);
		//final AccessBus       ab            = new AccessBus(comp, comp.pa());
		//final PipelineLogic   pipelineLogic = new PipelineLogic(comp._access());


		final Boilerplate b = new Boilerplate();
		b.get();
		comp = (CompilationImpl) b.comp;


		final ErrSink errSink = comp.getErrSink();
		//final IO              io            = new IO();
		//final AccessBus       ab            = new AccessBus(comp, comp.pa());
		final PipelineLogic   pipelineLogic = b.pipelineLogic();




		final OS_Module mod = comp.moduleBuilder()
				.withFileName("filename.elijah")
				.addToCompilation()
				.build();
		final OutputFileFactoryParams p    = new OutputFileFactoryParams(mod, comp.getCompilationEnclosure());
		final GenerateFiles           fgen = OutputFileFactory.create(CompilationAlways.defaultPrelude(), p);

		final SM_ClassDeclaration node = new SM_ClassDeclaration() {
			@Override
			public SM_Name name() {
				return new SM_Name() {
					@Override
					public String getText() {
						return "Main";
					}
				};
			}

			@Override
			public SM_ClassSubtype subType() {
				return SM_ClassSubtype.NORMAL;
			}

			@Override
			public SM_ClassInheritance inheritance() {
				return new SM_ClassInheritance() {
					@Override
					public List<SM_Name> names() {
						return List_of(new SM_Name() {
							@Override
							public String getText() {
								return "Arguments";
							}
						});
					}
				};
			}

			@Override
			public SM_ClassBody classBody() {
				return null;
			}
		};

		fgen.forNode(node);
	}
}