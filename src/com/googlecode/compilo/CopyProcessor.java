package com.googlecode.compilo;

import com.googlecode.totallylazy.Destination;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Source;

public class CopyProcessor implements Processor {
    private final Predicate<? super String> predicate;

    private CopyProcessor(Predicate<? super String> predicate) {
        this.predicate = predicate;
    }

    public static CopyProcessor copy(final Predicate<? super String> predicate) {
        return new CopyProcessor(predicate);
    }

    @Override
    public Integer call(Source source, Destination destination) throws Exception {
        return Source.methods.copy(source, destination);
    }

    @Override
    public boolean matches(String other) {
        return predicate.matches(other);
    }

    @Override
    public String name() {
        return "Copied";
    }
}
