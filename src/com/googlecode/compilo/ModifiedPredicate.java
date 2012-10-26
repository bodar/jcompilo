package com.googlecode.compilo;

import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.predicates.LogicalPredicate;

import java.io.File;

public class ModifiedPredicate extends LogicalPredicate<File> {
    private final File sourceDirectory;
    private final File destinationDirectory;

    private ModifiedPredicate(File sourceDirectory, File destinationDirectory) {
        this.sourceDirectory = sourceDirectory;
        this.destinationDirectory = destinationDirectory;
    }

    public static LogicalPredicate<File> modifiedMatches(final File sourceDirectory, final File destinationDirectory) {
        return new ModifiedPredicate(sourceDirectory, destinationDirectory);
    }

    @Override
    public boolean matches(File source) {
        String path = Files.relativePath(sourceDirectory, source);
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