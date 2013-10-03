package com.googlecode.jcompilo;

import com.googlecode.totallylazy.Bytes;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Uri;

import java.io.File;
import java.util.Date;

import static com.googlecode.jcompilo.MoveToTL.classFilename;
import static com.googlecode.totallylazy.Bytes.bytes;

public interface Resource {
    Uri uri();

    String name();

    Date modified();

    byte[] bytes();

    class constructors {
        public static Resource resource(final Uri uri, final String name, final Date modified, final byte[] bytes) {
            return new AResource(uri, name, modified, bytes);
        }

        public static Resource resource(final String name, final Date modified, final byte[] bytes) {
            return resource(Uri.uri(name), name, modified, bytes);
        }

        public static Resource resource(Class<?> aClass) {
            String name = classFilename(aClass.getName());
            String fileName = new File(name).getName();
            return resource(Uri.uri(aClass.getResource(fileName)), name, new Date(), Bytes.bytes(aClass.getResourceAsStream(fileName)));
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
        private final Uri uri;
        private final String name;
        private final Date modified;
        private final byte[] bytes;

        public AResource(Uri uri, String name, Date modified, byte[] bytes) {
            this.uri = uri;
            this.name = name;
            this.modified = modified;
            this.bytes = bytes;
        }

        @Override
        public Uri uri() {
            return uri;
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
