package com.googlecode.jcompilo;

import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Destination;

import java.io.IOException;

import static com.googlecode.totallylazy.Bytes.write;
import static com.googlecode.totallylazy.Closeables.using;

public interface Outputs {
    void put(Resource resource);

    class constructors {
        public static Outputs output(final Destination destination) {
            return resource -> {
                try {
                    using(destination.destination(resource.name(), resource.modified()), write(resource.bytes()));
                } catch (IOException e) {
                    throw new UnsupportedOperationException(e);
                }
            };
        }
    }

    class functions {
        public static Block<Resource> put(final Outputs outputs) {
            return outputs::put;
        }
    }
}
