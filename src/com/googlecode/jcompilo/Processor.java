package com.googlecode.jcompilo;

public interface Processor {
    boolean process(Inputs inputs, Outputs outputs) throws Exception;

    boolean matches(String filename);
}
