package com.googlecode.compilo;

import com.googlecode.totallylazy.Sets;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import static javax.tools.JavaFileObject.Kind.CLASS;

public class OutputsManager extends ForwardingJavaFileManager<JavaFileManager> {
    private final Outputs outputs;

    public OutputsManager(final JavaFileManager fileManager, final Outputs outputs) throws FileNotFoundException {
        super(fileManager);
        this.outputs = outputs;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        return new OutputsObject(className, super.getJavaFileForOutput(location, className, kind, sibling), outputs);
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        return super.list(location, packageName, Sets.set(CLASS), recurse);
    }
}
