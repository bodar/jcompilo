package com.googlecode.compilo;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

public class ZipFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    private final ZipOutputStream outputStream;

    public ZipFileManager(final JavaFileManager fileManager, final ZipOutputStream zipOutputStream) throws FileNotFoundException {
        super(fileManager);
        outputStream = zipOutputStream;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        return new ZipFileObject(className, super.getJavaFileForOutput(location, className, kind, sibling), outputStream);
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }
}
