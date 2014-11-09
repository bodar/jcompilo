package com.googlecode.jcompilo;

import java.io.File;

public interface Build extends Locations, Identifiers {
    boolean build() throws Exception;
    boolean clean() throws Exception;
    boolean compile() throws Exception;
    boolean test() throws Exception;
    boolean Package() throws Exception;

    Iterable<CompileOption> compileOptions();
    Iterable<File> dependencies();
}
