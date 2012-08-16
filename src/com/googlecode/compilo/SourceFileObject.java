package com.googlecode.compilo;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Lazy;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Strings;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class SourceFileObject extends SimpleJavaFileObject {
    private final Pair<String, InputStream> pair;
    private final Lazy<CharSequence> charContent;

    private SourceFileObject(final Pair<String, InputStream> pair) {
        super(URI.create(pair.first()), Kind.SOURCE);
        this.pair = pair;
        charContent = new Lazy<CharSequence>() {
            public CharSequence get() {
                return Strings.toString(pair.second());
            }
        };
    }

    public static SourceFileObject sourceFileObject(Pair<String, InputStream> pair) {
        return new SourceFileObject(pair);
    }

    public static Function1<Pair<String, InputStream>, JavaFileObject> sourceFileObject() {
        return new Function1<Pair<String, InputStream>, JavaFileObject>() {
            @Override
            public JavaFileObject call(final Pair<String, InputStream> pair) throws Exception {
                return sourceFileObject(pair);
            }
        };
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return pair.second();
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return charContent.value();
    }
}
