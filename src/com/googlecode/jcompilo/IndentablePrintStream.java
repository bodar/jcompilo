package com.googlecode.jcompilo;

import java.io.OutputStream;
import java.io.PrintStream;

public class IndentablePrintStream extends PrintStream {
    public IndentablePrintStream(OutputStream out) {
        super(out);
    }


}
