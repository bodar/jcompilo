package com.googlecode.compilo;

import com.googlecode.totallylazy.Value;

import static com.googlecode.totallylazy.Sequences.sequence;

public abstract class CompileOption implements Value<Iterable<String>> {
    public static final CompileOption None = compileOption();
    public static final CompileOption Debug = compileOption("-g");
    public static final CompileOption UncheckedWarnings = compileOption("-Xlint:unchecked");
    public static final CompileOption WarningAsErrors = compileOption("-Werror");
    public static CompileOption Target(int version) { return compileOption("-target", String.valueOf(version)); }
    public static CompileOption Source(int version) { return compileOption("-source", String.valueOf(version)); }

    public static CompileOption compileOption(final String... options){
        return new CompileOption() {
            @Override
            public Iterable<String> value() {
                return sequence(options);
            }
        };
    }

}
