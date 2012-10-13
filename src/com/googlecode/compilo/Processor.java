package com.googlecode.compilo;

public interface Processor {
    boolean process(Inputs inputs, Outputs outputs) throws Exception;

    boolean matches(String filename);
}
