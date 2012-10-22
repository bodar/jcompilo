package com.googlecode.compilo;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Value;

import static com.googlecode.totallylazy.Callables.toString;
import static com.googlecode.totallylazy.Sequences.sequence;

public abstract class CompileOption implements Value<Iterable<String>> {
    public static final CompileOption None = compileOption();
    public static final CompileOption Debug = compileOption("-g");
    public static final CompileOption UncheckedWarnings = compileOption("-Xlint:unchecked");
    public static final CompileOption WarningAsErrors = compileOption("-Werror");
    public static CompileOption Target(int version) { return compileOption("-target", version); }
    public static CompileOption Source(int version) { return compileOption("-source", version); }
    public static CompileOption Implicit(Implicit implicit) { return compileOption("-implicit:" + implicit.toString().toLowerCase()); }

    public static CompileOption compileOption(final Object... options){
        return compileOption(sequence(options));
    }

    public static CompileOption compileOption(final Iterable<Object> options){
        return new CompileOption() {
            @Override
            public Iterable<String> value() {
                return sequence(options).map(toString);
            }
        };
    }

    @Override
    public int hashCode() {
        return values().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CompileOption && sequence(((CompileOption) obj).value()).equals(values());
    }

    @Override
    public String toString() {
        return values().toString(" ");
    }

    private Sequence<String> values() {
        return sequence(value());
    }

    public static enum Implicit {
        None,
        Class
    }

}
