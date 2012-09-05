package com.googlecode.compilo;

import javax.tools.ForwardingJavaFileObject;
import javax.tools.JavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ZipFileObject extends ForwardingJavaFileObject<JavaFileObject> {
    private final String filename;
    private final Outputs outputs;

    public ZipFileObject(String className, JavaFileObject output, Outputs outputs) {
        super(output);
        this.filename = className.replace('.', '/') + ".class";
        this.outputs = outputs;
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return new ByteArrayOutputStream(){
            @Override
            public void close() throws IOException {
                outputs.put(Resource.constructors.resource(filename, toByteArray()));
            }
        };
    }
}