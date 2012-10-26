package com.googlecode.compilo;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;

import java.util.Date;

public interface Resource {
    String name();

    Date modified();

    byte[] bytes();

    class constructors {
        public static Resource resource(final String name, final Date modified, final byte[] bytes) {
            return new Resource() {
                @Override
                public String name() {
                    return name;
                }

                @Override
                public Date modified() {
                    return modified;
                }

                @Override
                public byte[] bytes() {
                    return bytes;
                }
            };
        }
    }

    class functions {
        public static Function1<Resource, byte[]> bytes() {
            return new Function1<Resource, byte[]>() {
                @Override
                public byte[] call(Resource resource) throws Exception {
                    return resource.bytes();
                }
            };
        }
    }
}
