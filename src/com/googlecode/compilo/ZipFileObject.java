package com.googlecode.compilo;

import com.googlecode.totallylazy.ZipEntryOutputStream;

import javax.tools.ForwardingJavaFileObject;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFileObject extends ForwardingJavaFileObject<JavaFileObject> {
    private final String filename;
    private final ZipOutputStream outputStream;

    public ZipFileObject(String className, JavaFileObject output, ZipOutputStream outputStream) {
        super(output);
        this.filename = className.replace('.', '/') + ".class";
        this.outputStream = outputStream;
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return new ZipEntryOutputStream(outputStream, filename);
    }
}