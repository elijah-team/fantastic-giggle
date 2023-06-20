/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tripleo.elijah.nextgen.outputtree;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author olu
 */
public class EOT_OutputTree {
	private final List<EOT_OutputFile> list = new ArrayList<>();

	public void _putSeq(final String aKey, final Path aPath, final @NotNull EG_Statement aStatement) {
		//05/18 System.err.printf("[_putSeq] %s %s %s%n", aKey, aPath, aStatement.getText());
	}

	public void add(final @NotNull EOT_OutputFile aOff) {
		//05/18 System.err.printf("[add] %s %s%n", aOff.getFilename(), aOff.getStatementSequence().getText());

		list.add(aOff);
	}

	public void addAll(final List<EOT_OutputFile> aLeof) {
		list.addAll(aLeof);
	}

	public List<EOT_OutputFile> getList() {
		return list;
	}

	public void set(final List<EOT_OutputFile> aLeof) {
		list.addAll(aLeof);
	}
}
