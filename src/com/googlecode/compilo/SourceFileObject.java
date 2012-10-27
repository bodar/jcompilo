package com.googlecode.compilo;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Lazy;
import com.googlecode.totallylazy.Strings;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class SourceFileObject extends SimpleJavaFileObject {
    private final Resource resource;
    private final Lazy<CharSequence> charContent;

    private SourceFileObject(final Resource resource) {
        super(URI.create(resource.name()), Kind.SOURCE);
        this.resource = resource;
        charContent = new Lazy<CharSequence>() {
            public CharSequence get() {
                return Strings.toString(resource.bytes());
            }
        };
    }

    public static SourceFileObject sourceFileObject(Resource resource) {
        return new SourceFileObject(resource);
    }

    public static Function1<Resource, JavaFileObject> sourceFileObject() {
        return new Function1<Resource, JavaFileObject>() {
            @Override
            public JavaFileObject call(final Resource pair) throws Exception {
                return sourceFileObject(pair);
            }
        };
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return charContent.value();
    }

    @Override
    public long getLastModified() {
        return resource.modified().getTime();
    }

    public Resource resource() {
        return resource;
    }
}
