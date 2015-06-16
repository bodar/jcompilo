package com.googlecode.jcompilo;

import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Sequence;

import java.io.File;
import java.net.URL;

public class FileUrls {
    public static URL[] asUrls(Sequence<File> jars) {
        return urls(jars).toArray(URL.class);
    }

    public static Sequence<URL> urls(Sequence<File> jars) {
        return jars.map(asUrl());
    }

    public static Function<File, URL> asUrl() {
        return new Function<File, URL>() {
            @Override
            public URL call(File file) throws Exception {
                return file.toURI().toURL();
            }
        };
    }
}
