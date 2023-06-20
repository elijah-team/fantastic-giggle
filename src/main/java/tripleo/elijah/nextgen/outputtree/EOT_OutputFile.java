package tripleo.elijah.nextgen.outputtree;

import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.*;
import tripleo.elijah.nextgen.inputtree.EIT_Input;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleInput;
import tripleo.elijah.nextgen.outputstatement.EG_CompoundStatement;
import tripleo.elijah.nextgen.outputstatement.EG_Naming;
import tripleo.elijah.nextgen.outputstatement.EG_SequenceStatement;
import tripleo.elijah.nextgen.outputstatement.EG_SingleStatement;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.nextgen.outputstatement.EX_Explanation;
import tripleo.elijah.stages.gen_generic.Old_GenerateResultItem;
import tripleo.elijah.stages.write_stage.pipeline_impl.WPIS_WriteInputs;
import tripleo.util.buffer.Buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static tripleo.elijah.util.Helpers.List_of;

public class EOT_OutputFile {
	public static @NotNull EOT_OutputFile bufferSetToOutputFile(final String aFilename,
																final @NotNull Collection<Buffer> aBuffers,
																final Compilation comp,
																final OS_Module aModule) {
		var ccon = comp.con();

		final List<EG_Statement> statementStream = aBuffers.stream()
				.map(buffer ->
							 new EG_SingleStatement(
									 buffer.getText(),
									 EX_Explanation.withMessage("bufferSetToOutputFile >> singleStatement"))
					).collect(Collectors.toList());
		final EG_SequenceStatement seq = new EG_SequenceStatement(new EG_Naming("yyy"), statementStream);

		final List<EIT_Input> inputs = List_of(ccon.createModuleInput(aModule));
		final EOT_OutputFile  eof    = new EOT_OutputFile(comp, inputs, aFilename, EOT_OutputType.SOURCES, seq);
		return eof;
	}

	private final String          _filename;
	private final List<EIT_Input> _inputs = new ArrayList<>();
	private final EG_Statement    _sequence; // TODO List<?> ??
	private final EOT_OutputType  _type;
	private final Compilation c;
	public List<Triple<String, WPIS_WriteInputs.XSRC, String>> x;

	public EOT_OutputFile(final @NotNull Compilation c,
						  final @NotNull List<EIT_Input> inputs,
						  final @NotNull String filename,
						  final @NotNull EOT_OutputType type,
						  final @NotNull EG_Statement sequence) {
		this.c    = c;
		_filename = filename;
		_type     = type;
		_sequence = sequence;
		_inputs.addAll(inputs);
	}

	public static @NotNull EOT_OutputFile grToOutputFile(final Compilation aC,
														 final @NotNull Old_GenerateResultItem ab) {
		final List<EIT_Input> inputs = List_of(new EIT_ModuleInput(ab.node.module(), aC));

		final EG_SingleStatement beginning = new EG_SingleStatement("", EX_Explanation.withMessage("grToOutputFile >> beginning"));
		final EG_SingleStatement middle    = new EG_SingleStatement(ab.buffer.getText(), EX_Explanation.withMessage("grToOutputFile >> middle"));
		final EG_SingleStatement ending    = new EG_SingleStatement("", EX_Explanation.withMessage("grToOutputFile >> ending"));

		final EX_Explanation explanation = EX_Explanation.withMessage("grToOutputFile >> statement -- " + "generate-result-item");

		final EG_CompoundStatement seq = new EG_CompoundStatement(beginning, ending, middle, false, explanation);

		final EOT_OutputFile eof = new EOT_OutputFile(aC, inputs, ab.output, EOT_OutputType.SOURCES, seq);
		return eof;
	}

	public String getFilename() {
		return _filename;
	}

	public List<EIT_Input> getInputs() {
		return _inputs;
	}

	public EG_Statement getStatementSequence() {
		return _sequence;
	}

	public EOT_OutputType getType() {
		return _type;
	}

	// rules/constraints whatever
}
