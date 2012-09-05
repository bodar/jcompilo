package com.googlecode.compilo;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.FileNotFoundException;
import java.io.IOException;

public class OutputsManager extends ForwardingJavaFileManager<JavaFileManager> {
    private final Outputs outputs;

    public OutputsManager(final JavaFileManager fileManager, final Outputs outputs) throws FileNotFoundException {
        super(fileManager);
        this.outputs = outputs;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        return new ZipFileObject(className, super.getJavaFileForOutput(location, className, kind, sibling), outputs);
    }
}
