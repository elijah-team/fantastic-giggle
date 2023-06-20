package tripleo.elijah.comp.nextgen;

import java.io.File;

public class CP_SubFile {
	private final CP_OutputPath parent;
	private final String        file;

	public CP_SubFile(final CP_OutputPath aCPOutputPath, final String aFile) {
		parent = aCPOutputPath;
		file   = aFile;
	}

	public File toFile() {
		return new File(file);
	}
}
