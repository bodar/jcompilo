package com.googlecode.compilo;

import com.googlecode.totallylazy.Destination;

import java.io.IOException;

import static com.googlecode.totallylazy.Bytes.write;
import static com.googlecode.totallylazy.Closeables.using;

public interface Outputs {
    void put(Resource resource);

    class constructors {
        public static Outputs output(final Destination destination) {
            return new Outputs() {
                @Override
                public void put(Resource resource) {
                    try {
                        using(destination.destination(resource.name()), write(resource.bytes()));
                    } catch (IOException e) {
                        throw new UnsupportedOperationException(e);
                    }
                }
            };
        }
    }
}
