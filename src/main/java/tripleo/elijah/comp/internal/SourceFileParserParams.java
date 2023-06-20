package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.IO;
import tripleo.elijah.comp.i.ErrSink;

import java.io.File;

public record SourceFileParserParams(@NotNull File f, String file_name, ErrSink errSink, IO io, Compilation c) {
}
