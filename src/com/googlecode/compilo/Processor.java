package com.googlecode.compilo;

import com.googlecode.totallylazy.Destination;

public interface Processor {
    Boolean call(Inputs source, Destination destination) throws Exception;

    boolean matches(String s);
}
