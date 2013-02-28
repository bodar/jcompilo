package com.googlecode.jcompilo.lambda;

import com.googlecode.totallylazy.Function1;

public class String_charAt extends Function1<String,Character>  {
    private final int argument0;

    public String_charAt(final int argument0) {
        this.argument0 = argument0;
    }

    @Override
    public Character call(final String s) throws Exception {
        return s.charAt(argument0);
    }

    public static String_charAt create(final int argument0) {
        return new String_charAt(argument0);
    }
}
