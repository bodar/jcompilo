package com.googlecode.jcompilo.intellij;

import com.googlecode.totallylazy.Sets;
import com.intellij.compiler.impl.javaCompiler.BackendCompiler;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class JCompiloBackendCompiler implements BackendCompiler {
    private static final Set<FileType> JAVA = Sets.<FileType>set(StdFileTypes.JAVA);
    public static final String ID = "jcompilo";
    public static final String NAME = "JCompilo";

    @NotNull
    public String getId() {
        return ID;
    }

    @NotNull
    public String getPresentableName() {
        return NAME;
    }

    @NotNull
    public Configurable createConfigurable() {
        return new JCompiloConfigurable();
    }

    @NotNull
    public Set<FileType> getCompilableFileTypes() {
        return JAVA;
    }


}