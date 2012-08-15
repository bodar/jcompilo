package com.googlecode.compilo;

import java.io.File;

import static com.googlecode.compilo.Compiler.compiler;
import static com.googlecode.totallylazy.Files.delete;
import static com.googlecode.totallylazy.Files.recursiveFiles;
import static com.googlecode.totallylazy.Sequences.sequence;
import static java.lang.String.format;

public interface Build extends Locations, Identifiers {
    Build build() throws Exception;
    Build clean() throws Exception;
    Build compile() throws Exception;
    Build test() throws Exception;
    Build Package() throws Exception;

    Iterable<CompileOption> compileOptions();
    Iterable<File> dependencies();

}
