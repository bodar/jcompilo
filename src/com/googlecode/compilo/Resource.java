package com.googlecode.compilo;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;

public interface Resource {
    String name();

    byte[] bytes();

    class constructors {
        public static Resource resource(final String name, final byte[] bytes) {
            return new Resource() {
                @Override
                public String name() {
                    return name;
                }

                @Override
                public byte[] bytes() {
                    return bytes;
                }
            };
        }
    }

    class functions {
        public static Function1<Pair<String, byte[]>, Resource> resource = new Function1<Pair<String, byte[]>, Resource>() {
            @Override
            public Resource call(Pair<String, byte[]> pair) throws Exception {
                return constructors.resource(pair.first(), pair.second());
            }
        };

    }
}
