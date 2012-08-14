package com.googlecode.compilo;

import com.googlecode.totallylazy.Destination;

import javax.tools.ForwardingJavaFileObject;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.OutputStream;

public class ZipFileObject extends ForwardingJavaFileObject<JavaFileObject> {
    private final String filename;
    private final Destination outputStream;

    public ZipFileObject(String className, JavaFileObject output, Destination outputStream) {
        super(output);
        this.filename = className.replace('.', '/') + ".class";
        this.outputStream = outputStream;
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return outputStream.destination(filename);
    }
}