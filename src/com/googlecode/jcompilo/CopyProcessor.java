package com.googlecode.jcompilo;

import com.googlecode.totallylazy.Predicate;

import static com.googlecode.totallylazy.Closeables.using;

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
    public boolean process(Inputs inputs, Outputs outputs) throws Exception {
        env.out().prefix("     [copy] ");
        env.out().printf("Copying %s files%n", inputs.size());
        Inputs.methods.copy(inputs, outputs);
        env.out().clearPrefix();
        return true;
    }

    @Override
    public boolean matches(String other) {
        return predicate.matches(other);
    }
}
