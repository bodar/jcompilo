package com.googlecode.compilo;

import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Strings;

import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class SourceFileObject extends SimpleJavaFileObject {
    private final Pair<String, InputStream> pair;

    public SourceFileObject(Pair<String, InputStream> pair) {
        super(URI.create(pair.first()), Kind.SOURCE);
        this.pair = pair;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return pair.second();
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return Strings.toString(openInputStream());
    }
}
