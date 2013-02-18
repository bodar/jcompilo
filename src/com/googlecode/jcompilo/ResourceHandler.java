package com.googlecode.jcompilo;

import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Sequences.sequence;

public interface ResourceHandler {
    boolean matches(String name);
    Sequence<Resource> handle(Resource resource);

    class methods {
        public static Outputs decorate(final Iterable<ResourceHandler> resourceHandlers, final Outputs outputs) {
            return new Outputs() {
                @Override
                public void put(final Resource resource) {
                    sequence(resourceHandlers).
                            flatMap(ResourceHandler.functions.matchAndHandle(resource)).
                            each(functions.put(outputs));
                }
            };
        }
    }

    class functions {
        public static Mapper<ResourceHandler, Sequence<Resource>> matchAndHandle(final Resource resource) {
            return new Mapper<ResourceHandler, Sequence<Resource>>() {
                @Override
                public Sequence<Resource> call(final ResourceHandler handler) throws Exception {
                    if(handler.matches(resource.name())) return handler.handle(resource);
                    return one(resource);
                }
            };
        }

        public static Predicate<ResourceHandler> matches(final Resource resource) {
            return new Predicate<ResourceHandler>() {
                @Override
                public boolean matches(final ResourceHandler other) {
                    return other.matches(resource.name());
                }
            };
        }
    }
}