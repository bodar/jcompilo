package com.googlecode.compilo;

import com.googlecode.totallylazy.Sources;

import java.util.Date;

import static com.googlecode.compilo.Resource.constructors.resource;
import static com.googlecode.totallylazy.Sequences.one;

public interface Inputs extends Iterable<Resource> {
    boolean isEmpty();

    int size();

    class constructors {
        public static Inputs inputs(final Sources source) {
            return MemoryStore.memoryStore(source);
        }

        public static Inputs inputs(final Iterable<? extends Resource> resources) {
            return MemoryStore.memoryStore(resources);
        }

        public static Inputs inputs(final String name, final Date modified, byte[] bytes) {
            return inputs(one(resource(name, modified, bytes)));
        }
    }

    class methods {
        public static void copy(Iterable<? extends Resource> inputs, Outputs outputs) {
            for (Resource resource : inputs) outputs.put(resource);
        }

    }
}
