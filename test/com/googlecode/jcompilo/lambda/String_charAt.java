package com.googlecode.jcompilo.lambda;

import com.googlecode.totallylazy.Function1;

public class String_charAt extends Function1<String,Character>  {
    private final int argument1;

    public String_charAt(final int argument1) {
        this.argument1 = argument1;
    }

    @Override
    public Character call(final String s) throws Exception {
        return s.charAt(argument1);
    }
}
