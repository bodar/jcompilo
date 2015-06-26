package com.googlecode.jcompilo;

import com.googlecode.totallylazy.functions.Function1;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Sequences.sequence;

public interface ResourceHandler {
    boolean matches(String name);
    Sequence<Resource> handle(Resource resource);

    class methods {
        public static Outputs decorate(final Iterable<ResourceHandler> resourceHandlers, final Outputs outputs) {
            return resource -> sequence(resourceHandlers).
                    flatMap(functions.matchAndHandle(resource)).
                    each(Outputs.functions.put(outputs));
        }
    }

    class functions {
        public static Function1<ResourceHandler, Sequence<Resource>> matchAndHandle(final Resource resource) {
            return handler -> {
                if(handler.matches(resource.name())) return handler.handle(resource);
                return one(resource);
            };
        }

        public static Predicate<ResourceHandler> matches(final Resource resource) {
            return other -> other.matches(resource.name());
        }
    }
}