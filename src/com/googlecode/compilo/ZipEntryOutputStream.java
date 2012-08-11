package com.googlecode.compilo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipEntryOutputStream extends OutputStream {
    private final ZipOutputStream outputStream;

    public ZipEntryOutputStream(final ZipOutputStream outputStream, String filename) throws IOException {
        this.outputStream = outputStream;
        this.outputStream.putNextEntry(new ZipEntry(filename));
    }

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
}
