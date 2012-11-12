package com.googlecode.jcompilo.junit;

import com.googlecode.totallylazy.StringPrintStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class LocalPrintStream extends PrintStream {
    public static final LocalPrintStream localPrintStream = new LocalPrintStream();

    public static String reset() {
        return threadLocal.reset();
    }

    private LocalPrintStream() {
        super(new LocalOutputStream());
    }

    private static class LocalOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
            threadLocal.get().write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            threadLocal.get().write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            threadLocal.get().write(b, off, len);
        }
    }

    private static final PrintStreamThreadLocal threadLocal = new PrintStreamThreadLocal();

    private static class PrintStreamThreadLocal extends ThreadLocal<PrintStream> {
        @Override
        protected PrintStream initialValue() {
            return new StringPrintStream();
        }

        public String reset() {
            String result = get().toString();
            set(initialValue());
            return result;
        }
    }
}