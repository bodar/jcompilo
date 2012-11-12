package com.googlecode.jcompilo;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class PrefixPrintStream extends PrintStream {
    private final PrefixOutputStream prefixStream;


    private PrefixPrintStream(final PrefixOutputStream out) {
        super(out);
        prefixStream = out;
    }

    public static PrefixPrintStream prefixPrintStream(OutputStream out){
        return new PrefixPrintStream(new PrefixOutputStream(out));
    }

    public PrefixPrintStream prefix(String name){
        this.prefixStream.prefix = name;
        return this;
    }

    public PrefixPrintStream clearPrefix() {
        this.prefixStream.prefix = "";
        return this;
    }

    private static class PrefixOutputStream extends FilterOutputStream {
        private String prefix = "";
        private boolean wroteNewLine = true;

        public PrefixOutputStream(OutputStream output) {
            super(output);
        }

        @Override
        public void write(int b) throws IOException {
            if(wroteNewLine) out.write(prefix.getBytes("UTF-8"));
            out.write(b);
            wroteNewLine = (b == '\n');
        }
    }
}
