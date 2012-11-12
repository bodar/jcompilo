package com.googlecode.jcompilo;

import com.googlecode.totallylazy.Function1;

import java.util.Date;

public interface Resource {
    String name();

    Date modified();

    byte[] bytes();

    class constructors {
        public static Resource resource(final String name, final Date modified, final byte[] bytes) {
            return new AResource(name, modified, bytes);
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

    static class AResource implements Resource {
        private final String name;
        private final Date modified;
        private final byte[] bytes;

        public AResource(String name, Date modified, byte[] bytes) {
            this.name = name;
            this.modified = modified;
            this.bytes = bytes;
        }

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
    }

}
