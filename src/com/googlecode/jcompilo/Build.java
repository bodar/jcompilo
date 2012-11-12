package com.googlecode.jcompilo;

import java.io.File;

public interface Build extends Locations, Identifiers {
    Build build() throws Exception;
    Build clean() throws Exception;
    Build compile() throws Exception;
    Build test() throws Exception;
    Build Package() throws Exception;

    Iterable<CompileOption> compileOptions();
    Iterable<File> dependencies();
}
