package com.googlecode.compilo;

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
        outputStream.putNextEntry(new ZipEntry(filename));
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                outputStream.write(b);
            }

            @Override
            public void write(byte[] b) throws IOException {
                outputStream.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                outputStream.write(b, off, len);
            }

            @Override
            public void close() throws IOException {
                outputStream.closeEntry();
            }
        };
    }
}