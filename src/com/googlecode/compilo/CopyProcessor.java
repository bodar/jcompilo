package com.googlecode.compilo;

import com.googlecode.totallylazy.Destination;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Source;

import static java.lang.String.format;

public class CopyProcessor implements Processor {
    private final Environment env;
    private final Predicate<? super String> predicate;

    private CopyProcessor(Environment env, Predicate<? super String> predicate) {
        this.env = env;
        this.predicate = predicate;
    }

    public static CopyProcessor copy(Environment env, final Predicate<? super String> predicate) {
        return new CopyProcessor(env, predicate);
    }

    @Override
    public Boolean call(Source source, Destination destination) throws Exception {
        env.out().prefix("     [copy] ");
        env.out().printf("Copying %s files%n", source.sources().size());
        Source.methods.copy(source, destination);
        env.out().clearPrefix();
        return true;
    }

    @Override
    public boolean matches(String other) {
        return predicate.matches(other);
    }
}
