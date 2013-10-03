package com.googlecode.jcompilo.intellij;

import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

public class VirtualFileModified extends LogicalPredicate<VirtualFile> {
    private final File destinationDirectory;

    public VirtualFileModified(final File destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
    }

    static LogicalPredicate<VirtualFile> modifiedMatches(final File destinationDirectory) {
        return new VirtualFileModified(destinationDirectory);
    }

    @Override
    public boolean matches(VirtualFile other) {
        File source = JCompiloBackendCompiler.file(other);
        String path = JCompiloBackendCompiler.relativePathV(other);
        File destination = new File(destinationDirectory, map(path));
        return destination.exists() && source.lastModified() == destination.lastModified();
    }

    private String map(String path) {
        return directory(path) + name(path);
    }

    private String name(String path) {
        return new File(path).getName().replace(".java", ".class");
    }

    private String directory(String file) {
        String p = new File(file).getParent();
        return p == null ? "" : p.replace(".", "/") + "/";
    }
}