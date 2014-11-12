package com.googlecode.jcompilo.intellij;

import com.googlecode.jcompilo.CompileProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.java.CannotCreateJavaCompilerException;
import org.jetbrains.jps.builders.java.JavaCompilingTool;

import javax.tools.JavaCompiler;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JCompiloTool extends JavaCompilingTool {
    @NotNull
    @Override
    public String getId() {
        return JCompiloBackendCompiler.ID;
    }

    @NotNull
    @Override
    public String getDescription() {
        return JCompiloBackendCompiler.NAME;
    }

    @NotNull
    @Override
    public JavaCompiler createCompiler() throws CannotCreateJavaCompilerException {
        return CompileProcessor.DEFAULT_COMPILER;
    }

    @NotNull
    @Override
    public List<File> getAdditionalClasspath() {
        return new ArrayList<>();
    }
}
