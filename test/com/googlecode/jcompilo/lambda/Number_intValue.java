package com.googlecode.jcompilo.lambda;

import com.googlecode.totallylazy.Function1;

public class Number_intValue extends Function1<Number, Integer> {
    @Override
    public Integer call(final Number n) throws Exception {
        return n.intValue();
    }
}