package com.googlecode.jcompilo;

public class JCompiloException extends IllegalStateException {
    public JCompiloException() {
    }

    public JCompiloException(Throwable cause) {
        super(cause);
    }

    public JCompiloException(String message, Throwable cause) {
        super(message, cause);
    }

    public JCompiloException(String s) {
        super(s);
    }
}
