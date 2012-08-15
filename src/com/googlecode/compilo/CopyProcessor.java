package com.googlecode.compilo;

import com.googlecode.totallylazy.Destination;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Source;

import static java.lang.String.format;

public class CopyProcessor implements Processor {
    private final Predicate<? super String> predicate;

    private CopyProcessor(Predicate<? super String> predicate) {
        this.predicate = predicate;
    }

    public static CopyProcessor copy(final Predicate<? super String> predicate) {
        return new CopyProcessor(predicate);
    }

    @Override
    public String call(Source source, Destination destination) throws Exception {
        int size = Source.methods.copy(source, destination);
        return format("     [copy] Copying %s files%n", size);
    }

    @Override
    public boolean matches(String other) {
        return predicate.matches(other);
    }
}
