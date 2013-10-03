package com.googlecode.jcompilo.intellij;

import com.googlecode.jcompilo.Resource;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

public class ResourceModified extends LogicalPredicate<Resource> {
    private final File destinationDirectory;

    public ResourceModified(final File destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
    }

    static LogicalPredicate<Resource> modifiedMatches(final File destinationDirectory) {
        return new ResourceModified(destinationDirectory);
    }

    @Override
    public boolean matches(Resource other) {
        String path = other.name();
        File destination = new File(destinationDirectory, map(path));
        return destination.exists() && other.modified().getTime() == destination.lastModified();
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