package com.googlecode.compilo;

import com.googlecode.totallylazy.Destination;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ZipFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    private final Destination destination;

    public ZipFileManager(final JavaFileManager fileManager, final Destination destination) throws FileNotFoundException {
        super(fileManager);
        this.destination = destination;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        return new ZipFileObject(className, super.getJavaFileForOutput(location, className, kind, sibling), destination);
    }
}
