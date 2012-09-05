package com.googlecode.compilo;

import com.googlecode.totallylazy.Source;

import static com.googlecode.compilo.Resource.constructors.resource;
import static com.googlecode.totallylazy.Sequences.one;

public interface Inputs extends Iterable<Resource> {
    int size();

    void copyTo(Outputs outputs);

    class constructors {
        public static Inputs inputs(final Source source) {
            return MemoryStore.memoryStore(source);
        }

        public static Inputs inputs(final Iterable<? extends Resource> resources) {
            return MemoryStore.memoryStore(resources);
        }

        public static Inputs inputs(final String name, byte[] bytes) {
            return inputs(one(resource(name, bytes)));
        }
    }

    class methods {
        public static void copy(Iterable<? extends Resource> inputs, Outputs outputs) {
            for (Resource resource : inputs) outputs.put(resource);
        }

    }
}
